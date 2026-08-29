package dev.normansanchez.androidmcp.flow

import java.nio.file.Path

data class DetectFlowOptions(
    val scope: String = "application",
    val entryPoint: String = "auto",
    val maxDepth: Int = 10,
    val knownFeatures: List<String> = emptyList(),
    val knownEntryPoints: List<String> = emptyList()
)

object FlowDetector {

    private val DECLARED = EvidenceType.DECLARED
    private val INFERRED = EvidenceType.INFERRED

    fun detect(root: Path, options: DetectFlowOptions): AndroidFlowIr {
        val builder = FlowGraphBuilder()
        val warnings = mutableListOf<String>()
        val ambiguities = mutableListOf<FlowAmbiguity>()

        val project = AndroidProjectScanner.scan(root)
        if (project == null) {
            return AndroidFlowIr(
                application = FlowApplication(),
                flows = emptyList(),
                ambiguities = emptyList(),
                warnings = listOf("Not an Android/Gradle project root: no modules discovered")
            )
        }

        if (options.scope != "application") {
            warnings.add("scope '${options.scope}' is not recognized; defaulting to 'application'")
        }

        val entryPoints = EntryPointDetector.detect(project)
        val declaredActivities = EntryPointDetector.declaredActivities(project)
        val strings = loadStrings(project)
        val sources = SourceScanner.scanSources(project)
        val layouts = SourceScanner.scanLayoutImages(project)
        val graphs = SourceScanner.scanNavigationXml(project)

        val screenByFile = sources.associate { ref ->
            ref.relativePath to
                (SourceScanner.screenNameOfFile(ref.content)
                    ?: ref.path.fileName.toString().removeSuffix(".kt").removeSuffix(".java"))
        }

        val layoutToScreen = HashMap<String, String>()
        sources.forEach { ref ->
            val screen = screenByFile[ref.relativePath] ?: return@forEach
            UiFrameworkDetector.layoutNames(ref.content).forEach { name ->
                layoutToScreen.putIfAbsent(name, screen)
            }
        }

        registerManifestActivities(builder, declaredActivities)
        registerXmlGraphs(builder, graphs)
        registerNavHosts(builder, layouts, graphs, layoutToScreen, project)

        val decisionCounters = HashMap<String, Int>()
        val composeActionCounters = HashMap<String, Int>()

        for (ref in sources) {
            val screen = screenByFile[ref.relativePath] ?: continue
            registerSourceScreen(builder, screen, ref, UiFrameworkDetector.detect(ref.content))
        }

        for (ref in sources) {
            val screen = screenByFile[ref.relativePath] ?: continue
            val content = ref.content
            registerComposeRoutes(builder, screen, content, ref)
            registerStartDestination(builder, screen, content, ref)
        }

        for (ref in sources) {
            val screen = screenByFile[ref.relativePath] ?: continue
            val content = ref.content

            registerSetContentCall(builder, screen, content, ref)
            registerNavCalls(builder, screen, content, ref, ambiguities)
            registerDecisions(builder, screen, content, ref, decisionCounters)
            registerComposeActions(builder, screen, content, ref, composeActionCounters)
        }

        registerLayoutActions(builder, layouts, layoutToScreen, strings, project)
        registerCodeClickHandlers(builder, sources, screenByFile)

        if (entryPoints.size > 1) {
            ambiguities.add(
                FlowAmbiguity(
                    kind = "entry_point",
                    description = "Multiple MAIN/LAUNCHER components found; each produces its own flow",
                    candidates = entryPoints.map {
                        it.targetComponent ?: it.component
                    }.distinct()
                )
            )
        }

        var seeds = entryPoints
            .map { it.targetComponent ?: it.component }
            .mapNotNull { builder.resolve(it) }
            .distinct()
            .toMutableList()

        if (options.entryPoint != null && options.entryPoint != "auto") {
            val requested = builder.resolve(options.entryPoint)
            if (requested == null) {
                warnings.add("entry_point '${options.entryPoint}' does not resolve to any screen")
            } else {
                seeds = mutableListOf(requested)
            }
        }

        options.knownEntryPoints.forEach { hint ->
            val resolved = builder.resolve(hint)
            if (resolved == null) {
                warnings.add("context known_entry_points: '$hint' did not resolve to a screen")
            } else {
                seeds.add(resolved)
            }
        }
        seeds = seeds.distinct().toMutableList()

        options.knownFeatures.forEach { feature ->
            val matched = builder.nodesSnapshot().any {
                it.name?.contains(feature, ignoreCase = true) == true ||
                    it.route?.contains(feature, ignoreCase = true) == true ||
                    it.id.contains(feature, ignoreCase = true)
            }
            if (!matched) {
                warnings.add("context known_features: '$feature' was not found in the discovered flow")
            }
        }

        var flows = builder.buildFlows(seeds, options.maxDepth)
        if (flows.isEmpty()) {
            val allNodes = builder.nodesSnapshot()
            if (allNodes.isNotEmpty()) {
                warnings.add("No launcher entry point found; emitting a single unrooted flow")
                flows = listOf(
                    AndroidFlow(
                        id = "flow.application",
                        name = "application",
                        entryNode = null,
                        nodes = allNodes,
                        edges = builder.edgesSnapshot()
                    )
                )
            }
        }

        val applicationModules = project.appModules.map { it.name }
        val packageName = project.appModules.firstNotNullOfOrNull { module ->
            loadManifestPackage(module) ?: module.namespace
        }

        return AndroidFlowIr(
            application = FlowApplication(
                packageName = packageName,
                entryPoints = entryPoints,
                applicationModules = applicationModules,
                modules = project.modules.map {
                    FlowModule(name = it.name, type = it.type, path = root.relativize(it.path).toString())
                },
                moduleDependencies = project.moduleDependencies
            ),
            flows = flows,
            ambiguities = ambiguities,
            warnings = warnings.distinct()
        )
    }

    private fun registerManifestActivities(builder: FlowGraphBuilder, activities: List<DeclaredActivity>) {
        activities.forEach { activity ->
            builder.addNode(
                FlowNode(
                    id = activity.simpleName,
                    type = FlowNodeType.SCREEN,
                    name = activity.simpleName,
                    uiFramework = UiFramework.UNKNOWN,
                    source = SourceLocation(file = activity.manifestFile, symbol = activity.component),
                    className = activity.component,
                    evidence = listOf(
                        SourceEvidence(
                            file = activity.manifestFile,
                            symbol = activity.component,
                            evidenceType = DECLARED,
                            confidence = 1.0
                        )
                    )
                )
            )
            builder.registerClassName(activity.component, activity.simpleName)
        }
    }

    private fun registerXmlGraphs(builder: FlowGraphBuilder, graphs: List<NavXmlGraph>) {
        for (graph in graphs) {
            for (destination in graph.destinations) {
                val nodeId = destinationNodeId(destination)
                builder.addNode(
                    FlowNode(
                        id = nodeId,
                        type = FlowNodeType.SCREEN,
                        name = destination.label ?: destination.resourceId,
                        uiFramework = UiFramework.XML,
                        source = SourceLocation(file = graph.file, symbol = destination.className),
                        className = destination.className,
                        startDestination = destination.isStartDestination,
                        deepLinks = destination.deepLinks,
                        evidence = listOf(
                            SourceEvidence(
                                file = graph.file,
                                symbol = destination.className ?: destination.resourceId,
                                evidenceType = DECLARED,
                                confidence = 1.0
                            )
                        )
                    )
                )
                builder.registerResourceId(destination.resourceId, nodeId)
                destination.className?.let { builder.registerClassName(it, nodeId) }
            }
            for (action in graph.actions) {
                val from = builder.resolve("@id/${action.from}")
                val to = builder.resolve("@id/${action.to}")
                if (from != null && to != null) {
                    builder.addEdge(
                        from = from,
                        to = to,
                        label = "action ${action.actionId}",
                        evidence = SourceEvidence(
                            file = graph.file,
                            symbol = action.actionId.takeIf { it.isNotBlank() },
                            evidenceType = DECLARED,
                            confidence = 1.0
                        )
                    )
                    builder.registerActionId(action.actionId, to)
                }
            }
        }
    }

    private fun registerNavHosts(
        builder: FlowGraphBuilder,
        layouts: List<Pair<Path, String>>,
        graphs: List<NavXmlGraph>,
        layoutToScreen: Map<String, String>,
        project: AndroidProjectScan
    ) {
        val graphStartByGraphId = HashMap<String, String?>()
        graphs.forEach { graph ->
            graphStartByGraphId[graph.graphId.orEmpty()] = graph.destinations.firstOrNull { it.isStartDestination }
                ?.let { builder.resolve("@id/${it.resourceId}") }
        }

        layouts.forEach { (path, content) ->
            val layoutName = path.fileName.toString().removeSuffix(".xml")
            val graphRef = UiFrameworkDetector.navHostFragmentGraph(content) ?: return@forEach
            val host = layoutToScreen[layoutName] ?: return@forEach
            val start = graphStartByGraphId[graphRef]
            if (start != null) {
                builder.addEdge(
                    from = host,
                    to = start,
                    label = "NavHost $graphRef",
                    evidence = SourceEvidence(
                        file = project.root.relativize(path).toString(),
                        symbol = graphRef,
                        evidenceType = INFERRED,
                        confidence = 0.9
                    )
                )
            }
        }
    }

    private fun registerSourceScreen(
        builder: FlowGraphBuilder,
        screen: String,
        ref: SourceFileRef,
        framework: UiFramework
    ) {
        namesForFile(ref.path, screen).forEach { builder.registerClassName(it, screen) }
        builder.addNode(
            FlowNode(
                id = screen,
                type = FlowNodeType.SCREEN,
                name = screen,
                uiFramework = framework,
                source = SourceLocation(file = ref.relativePath, symbol = screen),
                evidence = listOf(
                    SourceEvidence(
                        file = ref.relativePath,
                        symbol = screen,
                        evidenceType = INFERRED,
                        confidence = 0.7
                    )
                )
            )
        )
    }

    private fun registerComposeRoutes(
        builder: FlowGraphBuilder,
        screen: String,
        content: String,
        ref: SourceFileRef
    ) {
        for (route in ComposeNavParser.routes(content)) {
            builder.addNode(
                FlowNode(
                    id = route.route,
                    type = FlowNodeType.SCREEN,
                    name = route.route,
                    uiFramework = UiFramework.COMPOSE,
                    source = SourceLocation(file = ref.relativePath, symbol = screen, line = route.line),
                    route = route.route,
                    evidence = listOf(
                        SourceEvidence(
                            file = ref.relativePath,
                            symbol = screen,
                            line = route.line,
                            evidenceType = DECLARED,
                            confidence = 1.0
                        )
                    )
                )
            )
            builder.registerRoute(route.route, route.route)
            route.screenCall?.let { call ->
                val target = builder.resolve(call)
                if (target != null) {
                    builder.addEdge(
                        from = route.route,
                        to = target,
                        label = "composable content",
                        evidence = SourceEvidence(
                            file = ref.relativePath,
                            symbol = call,
                            evidenceType = INFERRED,
                            confidence = 0.9
                        )
                    )
                }
            }
        }
    }

    private fun registerStartDestination(
        builder: FlowGraphBuilder,
        screen: String,
        content: String,
        ref: SourceFileRef
    ) {
        if (!content.contains("NavHost")) return
        val destinations = ComposeNavParser.startDestinations(content)
        if (destinations.isEmpty()) return
        destinations.forEach { destination ->
            val target = builder.resolve(destination)
            if (target == null) return@forEach
            builder.addEdge(
                from = screen,
                to = target,
                label = "NavHost start",
                evidence = SourceEvidence(
                    file = ref.relativePath,
                    symbol = screen,
                    evidenceType = INFERRED,
                    confidence = 0.9
                )
            )
        }
    }

    private fun registerSetContentCall(
        builder: FlowGraphBuilder,
        screen: String,
        content: String,
        ref: SourceFileRef
    ) {
        Regex("""setContent\s*\{[\s\S]{0,80}?([A-Za-z_][A-Za-z0-9_]*)\(""")
            .find(content)
            ?.let { match ->
                val target = builder.resolve(match.groupValues[1]) ?: return@let
                builder.addEdge(
                    from = screen,
                    to = target,
                    label = "setContent",
                    evidence = SourceEvidence(
                        file = ref.relativePath,
                        symbol = screen,
                        evidenceType = DECLARED,
                        confidence = 1.0
                    )
                )
            }
    }

    private fun registerNavCalls(
        builder: FlowGraphBuilder,
        screen: String,
        content: String,
        ref: SourceFileRef,
        ambiguities: MutableList<FlowAmbiguity>
    ) {
        val calls = KotlinNavExtractor.intentCalls(content) +
            KotlinNavExtractor.transactionCalls(content) +
            KotlinNavExtractor.navigationCalls(content)
        for (call in calls) {
            val target = builder.resolve(call.target)
            if (target == null) {
                val externalId = externalNodeId(call.target)
                builder.addNode(
                    FlowNode(
                        id = externalId,
                        type = FlowNodeType.EXTERNAL,
                        name = externalId,
                        source = SourceLocation(file = ref.relativePath, symbol = call.target, line = call.line),
                        evidence = listOf(
                            SourceEvidence(
                                file = ref.relativePath,
                                symbol = call.target,
                                line = call.line,
                                evidenceType = DECLARED,
                                confidence = 1.0
                            )
                        )
                    )
                )
                builder.addEdge(
                    from = screen,
                    to = externalId,
                    label = call.kind,
                    evidence = SourceEvidence(
                        file = ref.relativePath,
                        symbol = call.target,
                        line = call.line,
                        evidenceType = DECLARED,
                        confidence = 1.0
                    )
                )
                ambiguities.add(
                    FlowAmbiguity(
                        kind = "navigation",
                        description = "Navigation target '${call.target}' could not be resolved to a known screen",
                        candidates = listOf(call.target)
                    )
                )
            } else {
                builder.addEdge(
                    from = screen,
                    to = target,
                    label = call.kind,
                    evidence = SourceEvidence(
                        file = ref.relativePath,
                        symbol = call.target,
                        line = call.line,
                        evidenceType = DECLARED,
                        confidence = 1.0
                    )
                )
            }
        }
    }

    private fun registerDecisions(
        builder: FlowGraphBuilder,
        screen: String,
        content: String,
        ref: SourceFileRef,
        counters: HashMap<String, Int>
    ) {
        for (decision in DecisionExtractor.decisions(content)) {
            val sequence = counters.merge(screen, 1) { current, _ -> current + 1 } ?: 1
            val decisionId = "$screen.decision_$sequence"
            builder.addNode(
                FlowNode(
                    id = decisionId,
                    type = FlowNodeType.DECISION,
                    name = decision.condition,
                    condition = decision.condition,
                    uiFramework = UiFrameworkDetector.detect(content),
                    source = SourceLocation(file = ref.relativePath, symbol = screen, line = decision.line),
                    evidence = listOf(
                        SourceEvidence(
                            file = ref.relativePath,
                            symbol = screen,
                            line = decision.line,
                            evidenceType = INFERRED,
                            confidence = 0.8
                        )
                    )
                )
            )
            builder.addEdge(
                from = screen,
                to = decisionId,
                condition = decision.condition,
                evidence = SourceEvidence(
                    file = ref.relativePath,
                    symbol = screen,
                    line = decision.line,
                    evidenceType = INFERRED,
                    confidence = 0.8
                )
            )
            decision.outcomes.forEach { outcome ->
                val target = outcome.target?.let { builder.resolve(it) }
                if (target == null) return@forEach
                builder.addEdge(
                    from = decisionId,
                    to = target,
                    condition = outcome.label,
                    evidence = SourceEvidence(
                        file = ref.relativePath,
                        symbol = outcome.label,
                        line = decision.line,
                        evidenceType = INFERRED,
                        confidence = 0.6
                    )
                )
            }
        }
    }

    private fun registerComposeActions(
        builder: FlowGraphBuilder,
        screen: String,
        content: String,
        ref: SourceFileRef,
        counters: HashMap<String, Int>
    ) {
        for (action in SelectorExtractor.composeActions(content)) {
            val sequence = counters.merge(screen, 1) { current, _ -> current + 1 } ?: 1
            val actionId = "$screen.action_$sequence"
            val selectors = action.selectors
            val name = action.label ?: selectors.firstOrNull()?.value ?: "tap"
            builder.addNode(
                FlowNode(
                    id = actionId,
                    type = FlowNodeType.ACTION,
                    name = name,
                    uiFramework = UiFramework.COMPOSE,
                    source = SourceLocation(file = ref.relativePath, symbol = screen, line = action.line),
                    selectors = selectors,
                    selectorStatus = if (selectors.isEmpty()) SelectorStatus.MISSING else SelectorStatus.RESOLVED,
                    evidence = listOf(
                        SourceEvidence(
                            file = ref.relativePath,
                            symbol = name,
                            line = action.line,
                            evidenceType = DECLARED,
                            confidence = 1.0
                        )
                    )
                )
            )
            builder.addEdge(
                from = screen,
                to = actionId,
                label = "onClick",
                evidence = SourceEvidence(
                    file = ref.relativePath,
                    symbol = screen,
                    line = action.line,
                    evidenceType = INFERRED,
                    confidence = 0.9
                )
            )
            DecisionExtractor.targetsIn(action.body).forEach { call ->
                val target = builder.resolve(call.target)
                if (target == null) return@forEach
                builder.addEdge(
                    from = actionId,
                    to = target,
                    label = call.kind,
                    evidence = SourceEvidence(
                        file = ref.relativePath,
                        symbol = call.target,
                        line = action.line,
                        evidenceType = DECLARED,
                        confidence = 1.0
                    )
                )
            }
        }
    }

    private fun registerLayoutActions(
        builder: FlowGraphBuilder,
        layouts: List<Pair<Path, String>>,
        layoutToScreen: Map<String, String>,
        strings: Map<String, String>,
        project: AndroidProjectScan
    ) {
        layouts.forEach { (path, content) ->
            val layoutName = path.fileName.toString().removeSuffix(".xml")
            val screen = layoutToScreen[layoutName] ?: return@forEach
            SelectorExtractor.layoutActions(content, strings).forEach { action ->
                val selectors = buildList {
                    add(Selector(SelectorKind.RESOURCE_ID, action.resourceId))
                    action.text?.let { add(Selector(SelectorKind.TEXT, it)) }
                    action.contentDescription?.let { add(Selector(SelectorKind.CONTENT_DESCRIPTION, it)) }
                }
                val actionId = "$screen.${action.resourceId}"
                builder.addNode(
                    FlowNode(
                        id = actionId,
                        type = FlowNodeType.ACTION,
                        name = action.text ?: action.contentDescription ?: action.resourceId,
                        uiFramework = UiFramework.XML,
                        source = SourceLocation(file = project.root.relativize(path).toString(), symbol = action.resourceId),
                        layout = layoutName,
                        selectors = selectors,
                        selectorStatus = if (selectors.isEmpty()) SelectorStatus.MISSING else SelectorStatus.RESOLVED,
                        evidence = listOf(
                            SourceEvidence(
                                file = project.root.relativize(path).toString(),
                                symbol = action.resourceId,
                                evidenceType = DECLARED,
                                confidence = 1.0
                            )
                        )
                    )
                )
                builder.addEdge(
                    from = screen,
                    to = actionId,
                    label = "onClick",
                    evidence = SourceEvidence(
                        file = project.root.relativize(path).toString(),
                        symbol = action.resourceId,
                        evidenceType = INFERRED,
                        confidence = 0.9
                    )
                )
            }
        }
    }

    private fun registerCodeClickHandlers(
        builder: FlowGraphBuilder,
        sources: List<SourceFileRef>,
        screenByFile: Map<String, String>
    ) {
        sources.forEach { ref ->
            val screen = screenByFile[ref.relativePath] ?: return@forEach
            KotlinNavExtractor.clickHandlers(ref.content).forEach { handler ->
                val actionId = "$screen.${handler.resourceId}"
                val existing = builder.node(actionId)
                val evidence = SourceEvidence(
                    file = ref.relativePath,
                    symbol = handler.resourceId,
                    line = handler.line,
                    evidenceType = DECLARED,
                    confidence = 1.0
                )
                if (existing == null) {
                    builder.addNode(
                        FlowNode(
                            id = actionId,
                            type = FlowNodeType.ACTION,
                            name = handler.resourceId,
                            uiFramework = UiFramework.XML,
                            source = SourceLocation(file = ref.relativePath, symbol = handler.resourceId, line = handler.line),
                            selectorStatus = SelectorStatus.MISSING,
                            evidence = listOf(evidence)
                        )
                    )
                    builder.addEdge(
                        from = screen,
                        to = actionId,
                        label = "setOnClickListener",
                        evidence = SourceEvidence(
                            file = ref.relativePath,
                            symbol = screen,
                            line = handler.line,
                            evidenceType = INFERRED,
                            confidence = 0.9
                        )
                    )
                } else {
                    builder.addNode(existing.copy(evidence = existing.evidence + evidence))
                }
                DecisionExtractor.targetsIn(handler.body).forEach { call ->
                    val target = builder.resolve(call.target)
                    if (target == null) return@forEach
                    builder.addEdge(
                        from = actionId,
                        to = target,
                        label = call.kind,
                        evidence = SourceEvidence(
                            file = ref.relativePath,
                            symbol = call.target,
                            line = handler.line,
                            evidenceType = DECLARED,
                            confidence = 1.0
                        )
                    )
                }
            }
        }
    }

    private fun destinationNodeId(destination: NavXmlDestination): String =
        destination.className?.let { SourceScanner.simpleName(it) } ?: destination.resourceId

    private fun externalNodeId(token: String): String = "external:$token"

    private fun loadStrings(project: AndroidProjectScan): Map<String, String> {
        val strings = HashMap<String, String>()
        project.modules.forEach { module ->
            val valuesDir = module.path.resolve("src/main/res/values")
            if (!java.nio.file.Files.isDirectory(valuesDir)) return@forEach
            java.nio.file.Files.walk(valuesDir).use { paths ->
                paths.forEach { path ->
                    if (!path.toString().endsWith("strings.xml")) return@forEach
                    try {
                        val content = java.nio.file.Files.readString(path)
                        Regex("""<string\s+name="([^"]+)">([^<]*)</string>""")
                            .findAll(content)
                            .forEach { match ->
                                strings.putIfAbsent(match.groupValues[1], match.groupValues[2])
                            }
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return strings
    }

    private fun loadManifestPackage(module: FlowModuleInfo): String? {
        val manifest = module.manifest ?: return null
        return try {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            val document = java.nio.file.Files.newInputStream(manifest).use { input ->
                factory.newDocumentBuilder().parse(input)
            }
            document.documentElement.getAttribute("package").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun namesForFile(path: java.nio.file.Path, screen: String): List<String> {
        val fileName = path.fileName.toString().removeSuffix(".kt").removeSuffix(".java")
        return if (fileName == screen) listOf(fileName) else listOf(fileName, screen)
    }
}