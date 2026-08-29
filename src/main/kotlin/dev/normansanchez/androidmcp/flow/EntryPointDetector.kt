package dev.normansanchez.androidmcp.flow

import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object EntryPointDetector {

    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    private const val MAIN_ACTION = "android.intent.action.MAIN"
    private const val LAUNCHER_CATEGORY = "android.intent.category.LAUNCHER"

    fun detect(scan: AndroidProjectScan): List<FlowEntryPoint> {
        val entries = mutableListOf<FlowEntryPoint>()
        for (appModule in scan.appModules) {
            val manifest = appModule.manifest ?: continue
            if (!Files.isRegularFile(manifest)) continue
            val parsed = parseManifest(manifest, scan.root, appModule) ?: continue
            entries.addAll(parsed)
        }
        return entries
    }

    fun declaredActivities(scan: AndroidProjectScan): List<DeclaredActivity> {
        val activities = mutableListOf<DeclaredActivity>()
        for (appModule in scan.appModules) {
            val manifest = appModule.manifest ?: continue
            if (!Files.isRegularFile(manifest)) continue
            val declared = parseDeclaredActivities(manifest, scan.root, appModule) ?: continue
            activities.addAll(declared)
        }
        return activities
    }

    private fun parseDeclaredActivities(
        manifest: Path,
        root: Path,
        module: FlowModuleInfo
    ): List<DeclaredActivity>? {
        val document = try {
            val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            Files.newInputStream(manifest).use { input ->
                factory.newDocumentBuilder().parse(input)
            }
        } catch (e: Exception) {
            return null
        }
        document.documentElement.normalize()

        val manifestElement = document.documentElement
        val declaredPackage = manifestElement.getAttribute("package").takeIf { it.isNotBlank() }
        val fallbackPackage = declaredPackage ?: module.namespace ?: ""
        val relativePath = root.relativize(manifest).toString()

        val activities = mutableListOf<DeclaredActivity>()
        val application = manifestElement.getElementsByTagName("application").item(0) as? Element
            ?: return activities

        collect(application.getElementsByTagName("activity")) { element ->
            val name = element.androidAttribute("name")
            val component = resolveComponent(name, fallbackPackage) ?: return@collect
            activities.add(
                DeclaredActivity(
                    component = component,
                    simpleName = component.substringAfterLast('.'),
                    module = module.name,
                    manifestFile = relativePath
                )
            )
        }
        return activities
    }

    private fun parseManifest(
        manifest: Path,
        root: Path,
        module: FlowModuleInfo
    ): List<FlowEntryPoint>? {
        val document = try {
            val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            Files.newInputStream(manifest).use { input ->
                factory.newDocumentBuilder().parse(input)
            }
        } catch (e: Exception) {
            return null
        }
        document.documentElement.normalize()

        val manifestElement = document.documentElement
        val declaredPackage = manifestElement.getAttribute("package").takeIf { it.isNotBlank() }
        val fallbackPackage = declaredPackage ?: module.namespace ?: ""
        val relativePath = root.relativize(manifest).toString()

        val entries = mutableListOf<FlowEntryPoint>()
        val application = manifestElement.getElementsByTagName("application").item(0) as? Element
            ?: return entries

        collect(application.getElementsByTagName("activity")) { element ->
            val name = element.androidAttribute("name")
            val component = resolveComponent(name, fallbackPackage)
            val isEntry = hasLauncherFilter(element)
            if (component != null && isEntry) {
                entries.add(
                    FlowEntryPoint(
                        component = component,
                        kind = "activity",
                        module = module.name,
                        isSplash = component.contains("Splash", ignoreCase = true),
                        evidence = SourceEvidence(
                            file = relativePath,
                            symbol = component,
                            evidenceType = EvidenceType.DECLARED,
                            confidence = 1.0
                        )
                    )
                )
            }
        }

        collect(application.getElementsByTagName("activity-alias")) { element ->
            val name = element.androidAttribute("name")
            val target = element.androidAttribute("targetActivity")
            val component = resolveComponent(name, fallbackPackage)
            val targetComponent = target?.let { resolveComponent(it, fallbackPackage) }
            val isEntry = hasLauncherFilter(element)
            if (component != null && isEntry) {
                entries.add(
                    FlowEntryPoint(
                        component = component,
                        kind = "activity_alias",
                        targetComponent = targetComponent,
                        module = module.name,
                        isSplash = (targetComponent ?: component)
                            .contains("Splash", ignoreCase = true),
                        evidence = SourceEvidence(
                            file = relativePath,
                            symbol = component,
                            evidenceType = EvidenceType.DECLARED,
                            confidence = 1.0
                        )
                    )
                )
            }
        }
        return entries
    }

    private fun collect(
        nodeList: org.w3c.dom.NodeList,
        action: (Element) -> Unit
    ) {
        for (index in 0 until nodeList.length) {
            (nodeList.item(index) as? Element)?.let { action(it) }
        }
    }

    private fun hasLauncherFilter(element: Element): Boolean {
        val filters = element.getElementsByTagName("intent-filter")
        var main = false
        var launcher = false
        for (index in 0 until filters.length) {
            val filter = filters.item(index) as? Element ?: continue
            collect(filter.getElementsByTagName("action")) {
                if (it.androidAttribute("name") == MAIN_ACTION) main = true
            }
            collect(filter.getElementsByTagName("category")) {
                if (it.androidAttribute("name") == LAUNCHER_CATEGORY) launcher = true
            }
            if (main && launcher) return true
        }
        return false
    }

    private fun resolveComponent(name: String, packageName: String): String? {
        if (name.isBlank()) return null
        return when {
            name.startsWith(".") -> packageName + name
            name.contains(".") -> name
            packageName.isNotBlank() -> "$packageName.$name"
            else -> null
        }
    }

    private fun Element.androidAttribute(name: String): String =
        getAttributeNS(ANDROID_NS, name)
}

data class DeclaredActivity(
    val component: String,
    val simpleName: String,
    val module: String,
    val manifestFile: String
)