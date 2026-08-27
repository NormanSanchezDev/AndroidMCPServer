package dev.normansanchez.androidmcp.server

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonArray

import dev.normansanchez.androidmcp.tools.DependenciesInspectTool
import dev.normansanchez.androidmcp.tools.EntryPointsFindTool
import dev.normansanchez.androidmcp.tools.GradleRunTool
import dev.normansanchez.androidmcp.tools.GradleTasksTool
import dev.normansanchez.androidmcp.tools.LintRunTool
import dev.normansanchez.androidmcp.tools.ManifestInspectTool
import dev.normansanchez.androidmcp.tools.ModuleGraphTool
import dev.normansanchez.androidmcp.tools.ProjectInspectTool
import dev.normansanchez.androidmcp.tools.ResourcesInspectTool
import dev.normansanchez.androidmcp.tools.SymbolFindTool
import dev.normansanchez.androidmcp.tools.SymbolReferencesTool
import dev.normansanchez.androidmcp.tools.TestsRunTool
import dev.normansanchez.androidmcp.tools.GradleConfigTool
import dev.normansanchez.androidmcp.tools.VersionCatalogTool
import dev.normansanchez.androidmcp.tools.ConventionPluginsTool
import dev.normansanchez.androidmcp.tools.ArchitectureDetectTool
import dev.normansanchez.androidmcp.tools.TestsDiscoverTool
import dev.normansanchez.androidmcp.tools.SymbolHierarchyTool
import dev.normansanchez.androidmcp.tools.BuildValidateTool
import dev.normansanchez.androidmcp.tools.StaticAnalysisTool
import dev.normansanchez.androidmcp.tools.NavigationGraphTool
import dev.normansanchez.androidmcp.tools.ResourceReferencesTool
import dev.normansanchez.androidmcp.tools.SecurityAuditTool
import dev.normansanchez.androidmcp.tools.ManifestMergeTool
import dev.normansanchez.androidmcp.tools.ProguardInspectTool
import java.io.FileDescriptor
import java.io.FileOutputStream

private val json = Json { prettyPrint = false }

private fun args(request: CallToolRequest): JsonObject? =
    request.arguments

private fun argString(arguments: JsonObject?, key: String): String? =
    (arguments?.get(key) as? JsonPrimitive)?.contentOrNull

private fun argInt(arguments: JsonObject?, key: String): Int? =
    (arguments?.get(key) as? JsonPrimitive)?.intOrNull

private fun argBool(arguments: JsonObject?, key: String, default: Boolean): Boolean =
    (arguments?.get(key) as? JsonPrimitive)?.booleanOrNull ?: default

private fun argList(arguments: JsonObject?, key: String): List<String> =
    (arguments?.get(key) as? JsonArray)
        ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        ?: emptyList()

private fun ok(result: JsonObject): CallToolResult =
    CallToolResult(content = listOf(TextContent(text = json.encodeToString(JsonObject.serializer(), result))))

private fun fail(message: String): CallToolResult =
    CallToolResult(
        content = listOf(
            TextContent(
                text = json.encodeToString(
                    JsonObject.serializer(),
                    buildJsonObject {
                        put("status", "error")
                        put("error", message)
                    }
                )
            )
        ),
        isError = true
    )

private fun Server.register(
    name: String,
    description: String,
    properties: Map<String, JsonElement>,
    required: List<String> = emptyList(),
    handler: (arguments: JsonObject?) -> JsonObject
) {
    addTool(
        name = name,
        description = description,
        inputSchema = ToolSchema(
            properties = buildJsonObject { properties.forEach { (k, v) -> put(k, v) } },
            required = required
        )
    ) { request ->
        try {
            ok(handler(args(request)))
        } catch (e: Exception) {
            fail(e.message ?: e.javaClass.simpleName)
        }
    }
}

private val strSchema get() = buildJsonObject {
    put("type", "string")
}

private fun optStr(description: String) = buildJsonObject {
    put("type", "string")
    put("description", description)
}

private fun boolSchema(description: String) = buildJsonObject {
    put("type", "boolean")
    put("description", description)
}

private fun intSchema(description: String) = buildJsonObject {
    put("type", "integer")
    put("description", description)
}

private fun listSchema(description: String) = buildJsonObject {
    put("type", "array")
    put("items", buildJsonObject { put("type", "string") })
    put("description", description)
}

fun main(): Unit = runBlocking {
    // Keep stdout strictly for the MCP JSON-RPC protocol; any JVM/library
    // logging that writes to System.out is diverted to stderr.
    val protocolStdout = FileOutputStream(FileDescriptor.out)
    System.setOut(System.err)

    val mcpServer = Server(
        serverInfo = Implementation(
            name = "lattice-android-mcp-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
            ),
        )
    )

    mcpServer.register(
        name = "project.inspect",
        description = "Deterministic inspection of a Gradle/Android project root: settings file, modules, plugin types and evidence.",
        properties = mapOf("projectRoot" to optStr("Absolute path to the project root"))
    ) { arguments ->
        ProjectInspectTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    mcpServer.register(
        name = "manifest.inspect",
        description = "Parses src/main/AndroidManifest.xml of a module into package, components, permissions and intent filters.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Module path relative to root, default 'app'")
        )
    ) { arguments ->
        ManifestInspectTool.execute(
            argString(arguments, "projectRoot").orEmpty(),
            argString(arguments, "module") ?: "app"
        )
    }

    mcpServer.register(
        name = "entry_points.find",
        description = "Derives entry points from the manifest evidence: launcher activities, deep links and exported components.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Module path relative to root, default 'app'")
        )
    ) { arguments ->
        EntryPointsFindTool.execute(
            argString(arguments, "projectRoot").orEmpty(),
            argString(arguments, "module") ?: "app"
        )
    }

    mcpServer.register(
        name = "symbol.find",
        description = "Finds Kotlin declarations (class/interface/object/function/property/typealias) via Kotlin PSI across module source roots.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "query" to optStr("Symbol name to search"),
            "kind" to optStr("Filter: class|interface|enum|object|function|property|typealias"),
            "exactMatch" to boolSchema("Exact name match instead of contains"),
            "includeTests" to boolSchema("Include test source sets"),
            "maxResults" to intSchema("Maximum number of matches returned")
        )
    ) { arguments ->
        SymbolFindTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            query = argString(arguments, "query").orEmpty(),
            kind = argString(arguments, "kind"),
            exactMatch = argBool(arguments, "exactMatch", false),
            includeTests = argBool(arguments, "includeTests", false),
            maxResults = argInt(arguments, "maxResults") ?: 50
        )
    }

    mcpServer.register(
        name = "symbol.references",
        description = "Reports identifier-level occurrences of a symbol across Kotlin sources, separating declaration from references.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "symbolName" to optStr("Identifier to locate"),
            "includeTests" to boolSchema("Include test source sets"),
            "maxResults" to intSchema("Maximum number of references returned")
        )
    ) { arguments ->
        SymbolReferencesTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            symbolName = argString(arguments, "symbolName").orEmpty(),
            includeTests = argBool(arguments, "includeTests", false),
            maxResults = argInt(arguments, "maxResults") ?: 200
        )
    }

    mcpServer.register(
        name = "gradle.tasks",
        description = "Lists Gradle tasks by executing './gradlew tasks --all' and parsing its real output.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Optional module to scope tasks (':app')"),
            "timeoutSeconds" to intSchema("Execution timeout")
        )
    ) { arguments ->
        GradleTasksTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            module = argString(arguments, "module"),
            timeoutSeconds = argInt(arguments, "timeoutSeconds")?.toLong() ?: 300L
        )
    }

    mcpServer.register(
        name = "gradle.run",
        description = "Executes allow-listed Gradle tasks through the project wrapper, capturing exit code, duration and output.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "tasks" to listSchema("Task names to execute"),
            "flags" to listSchema("Allow-listed flags (--parallel, --build-cache, ...)"),
            "timeoutSeconds" to intSchema("Execution timeout")
        )
    ) { arguments ->
        GradleRunTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            tasks = argList(arguments, "tasks"),
            flags = argList(arguments, "flags"),
            timeoutSeconds = argInt(arguments, "timeoutSeconds")?.toLong() ?: 600L
        )
    }

    mcpServer.register(
        name = "tests.run",
        description = "Aggregates JUnit XML reports under build/test-results; optionally triggers the Gradle test task first.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Optional module scope"),
            "task" to optStr("Gradle task used when trigger=true (default 'test')"),
            "trigger" to boolSchema("Execute the Gradle test task before reading reports"),
            "timeoutSeconds" to intSchema("Execution timeout when trigger=true")
        )
    ) { arguments ->
        TestsRunTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            module = argString(arguments, "module"),
            task = argString(arguments, "task"),
            trigger = argBool(arguments, "trigger", false),
            timeoutSeconds = argInt(arguments, "timeoutSeconds")?.toLong() ?: 600L
        )
    }

    mcpServer.register(
        name = "lint.run",
        description = "Reads Android Lint XML reports under build/reports and aggregates issues by severity; optionally triggers lint first.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Optional module scope"),
            "task" to optStr("Gradle task used when trigger=true (default 'lint')"),
            "trigger" to boolSchema("Execute the lint task before reading reports"),
            "timeoutSeconds" to intSchema("Execution timeout when trigger=true")
        )
    ) { arguments ->
        LintRunTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            module = argString(arguments, "module"),
            task = argString(arguments, "task"),
            trigger = argBool(arguments, "trigger", false),
            timeoutSeconds = argInt(arguments, "timeoutSeconds")?.toLong() ?: 900L
        )
    }

    mcpServer.register(
        name = "dependencies.inspect",
        description = "Runs './gradlew :module:dependencies' and parses the real tree into configurations, coordinates, conflicts and markers.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Optional module (default root project)"),
            "configuration" to optStr("Limit to one configuration, e.g. releaseRuntimeClasspath"),
            "timeoutSeconds" to intSchema("Execution timeout")
        )
    ) { arguments ->
        DependenciesInspectTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            module = argString(arguments, "module"),
            configuration = argString(arguments, "configuration"),
            timeoutSeconds = argInt(arguments, "timeoutSeconds")?.toLong() ?: 300L
        )
    }

    mcpServer.register(
        name = "module.graph",
        description = "Static module dependency graph parsed from settings.gradle(.kts) includes and project(...) references.",
        properties = mapOf("projectRoot" to optStr("Absolute path to the project root"))
    ) { arguments ->
        ModuleGraphTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    mcpServer.register(
        name = "resources.inspect",
        description = "Enumerates res/ folders, file counts, dpi buckets and value resource names from XML evidence.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Module path relative to root, default 'app'"),
            "includeNames" to boolSchema("Include value resource names (strings, colors, ...)"),
        )
    ) { arguments ->
        ResourcesInspectTool.execute(
            argString(arguments, "projectRoot").orEmpty(),
            argString(arguments, "module") ?: "app",
            argBool(arguments, "includeNames", true)
        )
    }

    // ── Phase 1: Gradle Intelligence ─────────────────────────────────────

    mcpServer.register(
        name = "gradle.config",
        description = "Inspects Gradle configuration: applied plugins, SDK versions, Compose status, build types, product flavors.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Module path relative to root, default 'app'")
        )
    ) { arguments ->
        GradleConfigTool.execute(
            argString(arguments, "projectRoot").orEmpty(),
            argString(arguments, "module")
        )
    }

    mcpServer.register(
        name = "gradle.versionCatalog",
        description = "Parses gradle/libs.versions.toml to expose declared dependencies, versions, and aliases.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root")
        )
    ) { arguments ->
        VersionCatalogTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    mcpServer.register(
        name = "gradle.conventionPlugins",
        description = "Discovers convention plugins in build-logic/ or buildSrc/ and maps which modules apply them.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root")
        )
    ) { arguments ->
        ConventionPluginsTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    // ── Phase 2: Architecture Discovery ──────────────────────────────────

    mcpServer.register(
        name = "architecture.detect",
        description = "Detects DI framework, Compose usage, ViewModel patterns, and reactive stream types from source code.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root")
        )
    ) { arguments ->
        ArchitectureDetectTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    mcpServer.register(
        name = "tests.discover",
        description = "Finds test files related to a given production class by naming convention.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "className" to optStr("Production class name to find tests for")
        ),
        required = listOf("className")
    ) { arguments ->
        TestsDiscoverTool.execute(
            argString(arguments, "projectRoot").orEmpty(),
            argString(arguments, "className").orEmpty()
        )
    }

    // ── Phase 3: Semantic Symbol Analysis ─────────────────────────────────

    mcpServer.register(
        name = "symbol.hierarchy",
        description = "Shows class/interface hierarchy: supertypes and subtypes across the codebase.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "className" to optStr("Class or interface name")
        ),
        required = listOf("className")
    ) { arguments ->
        SymbolHierarchyTool.execute(
            argString(arguments, "projectRoot").orEmpty(),
            argString(arguments, "className").orEmpty()
        )
    }

    // ── Phase 4: Build Validation ────────────────────────────────────────

    mcpServer.register(
        name = "build.validate",
        description = "Compiles a specific module and returns structured results with warnings and errors.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Module to compile (default 'app')"),
            "timeoutSeconds" to intSchema("Execution timeout")
        )
    ) { arguments ->
        BuildValidateTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            module = argString(arguments, "module"),
            timeoutSeconds = argInt(arguments, "timeoutSeconds")?.toLong() ?: 600L
        )
    }

    mcpServer.register(
        name = "staticAnalysis.run",
        description = "Executes configured static analysis tools (detekt, ktlint, kover) and aggregates results.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "module" to optStr("Optional module scope"),
            "tools" to listSchema("Tools to run: detekt, ktlint, kover"),
            "timeoutSeconds" to intSchema("Execution timeout")
        )
    ) { arguments ->
        StaticAnalysisTool.execute(
            projectRoot = argString(arguments, "projectRoot").orEmpty(),
            module = argString(arguments, "module"),
            tools = argList(arguments, "tools").ifEmpty { null },
            timeoutSeconds = argInt(arguments, "timeoutSeconds")?.toLong() ?: 600L
        )
    }

    // ── Phase 5: Android Deep Inspection ─────────────────────────────────

    mcpServer.register(
        name = "navigation.graph",
        description = "Inspects NavHost destinations from XML navigation graphs and Compose Navigation routes.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root")
        )
    ) { arguments ->
        NavigationGraphTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    mcpServer.register(
        name = "resource.references",
        description = "Given a resource name (e.g. string.app_name), finds where it is referenced in code and XML.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root"),
            "resourceName" to optStr("Resource identifier (e.g. string.app_name)")
        ),
        required = listOf("resourceName")
    ) { arguments ->
        ResourceReferencesTool.execute(
            argString(arguments, "projectRoot").orEmpty(),
            argString(arguments, "resourceName").orEmpty()
        )
    }

    // ── Phase 5: Android Deep Inspection (cont.) ────────────────────────

    mcpServer.register(
        name = "manifest.merge",
        description = "Detects merge conflicts across module manifests by comparing component attributes.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root")
        )
    ) { arguments ->
        ManifestMergeTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    // ── Phase 6: Security & Compliance (cont.) ──────────────────────────

    mcpServer.register(
        name = "proguard.inspect",
        description = "Inspects ProGuard/R8 configuration: rules, keep annotations, minification and resource shrinking status.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root")
        )
    ) { arguments ->
        ProguardInspectTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    // ── Phase 6: Security & Compliance ───────────────────────────────────

    mcpServer.register(
        name = "security.audit",
        description = "Detects common security issues: exported components, cleartext traffic, hardcoded secrets, backup settings.",
        properties = mapOf(
            "projectRoot" to optStr("Absolute path to the project root")
        )
    ) { arguments ->
        SecurityAuditTool.execute(argString(arguments, "projectRoot").orEmpty())
    }

    val transport = StdioServerTransport(
        input = System.`in`.asSource().buffered(),
        output = protocolStdout.asSink().buffered()
    )
    mcpServer.createSession(transport)
    awaitCancellation()
}
