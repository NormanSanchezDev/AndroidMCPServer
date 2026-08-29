package dev.normansanchez.androidmcp.tools

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DetectAndroidFlowToolTest {

    @Test
    fun `executes on xml fixture and returns full ir`() {
        val temp = Files.createTempDirectory("tool-xml")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)

            val json = DetectAndroidFlowTool.execute(project.absolutePathString(), null, null, null, null, false)

            assertEquals("success", json["status"]!!.jsonPrimitive.content)
            val ir = json["flowIR"]!!.jsonObject
            val application = ir["application"]!!.jsonObject
            assertEquals("com.acme.shop", application["package_name"]!!.jsonPrimitive.content)
            val entry = application["entry_points"]!!.jsonArray.first().jsonObject
            assertEquals("com.acme.shop.ui.SplashActivity", entry["component"]!!.jsonPrimitive.content)

            val flows = ir["flows"]!!.jsonArray
            assertTrue(flows.size >= 1, "at least one flow expected")
            val firstFlow = flows.first().jsonObject
            assertEquals("SplashActivity", firstFlow["entry_node"]!!.jsonPrimitive.content)
            assertTrue(firstFlow["nodes"]!!.jsonArray.isNotEmpty())
            assertTrue(firstFlow["edges"]!!.jsonArray.isNotEmpty())

            assertFalse("mermaid" in json, "mermaid omitted when not requested")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `emits mermaid derived from the ir when requested`() {
        val temp = Files.createTempDirectory("tool-mermaid")
        try {
            val project = FixtureProjects.composeApp(temp)

            val json = DetectAndroidFlowTool.execute(project.absolutePathString(), null, null, null, null, true)

            val mermaid = json["mermaid"]!!.jsonPrimitive.content
            assertTrue(mermaid.startsWith("flowchart TD"), "mermaid is derived from the IR")
            assertTrue(("-->" in mermaid) || ("--|" in mermaid), "mermaid contains arrows")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `context hints affect warnings and seed entry points`() {
        val temp = Files.createTempDirectory("tool-hints")
        try {
            val project = FixtureProjects.composeApp(temp)
            val context = buildJsonObject {
                put("known_entry_points", kotlinx.serialization.json.buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive("MainActivity"))
                    add(kotlinx.serialization.json.JsonPrimitive("DoesNotExist"))
                })
                put("known_features", kotlinx.serialization.json.buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive("checkout"))
                })
            }

            val json = DetectAndroidFlowTool.execute(project.absolutePathString(), "app", "auto", 5, context, false)

            val ir = json["flowIR"]!!.jsonObject
            val warnings = ir["warnings"]!!.jsonArray.map { it.jsonPrimitive.content }
            assertTrue(warnings.any { it.contains("DoesNotExist") }, "unresolved hint is warned, not silently accepted")
            assertTrue(warnings.any { it.contains("scope 'app'") }, "unknown scope reported")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `empty directory reports empty flows with a warning`() {
        val temp = Files.createTempDirectory("tool-empty")
        try {
            val json = DetectAndroidFlowTool.execute(temp.absolutePathString(), null, null, null, null, false)

            val ir = json["flowIR"]!!.jsonObject
            assertTrue(ir["flows"]!!.jsonArray.isEmpty())
            assertTrue(ir["warnings"]!!.jsonArray.isNotEmpty())
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}