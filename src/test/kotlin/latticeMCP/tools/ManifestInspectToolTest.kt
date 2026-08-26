package latticeMCP.tools

import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManifestInspectToolTest {

    private fun fixtureRoot(): String {
        val temp = Files.createTempDirectory("manifest")
        return latticeMCP.fixtures.FixtureProjects.sampleAndroidProject(temp).absolutePathString()
    }

    @Test
    fun `parses permissions application class and components`() {
        val root = fixtureRoot()

        val json = ManifestInspectTool.execute(root, "app")

        assertEquals("success", json["status"]!!.jsonPrimitive.content)
        assertEquals(".CorporateApplication", json["applicationClass"]!!.jsonPrimitive.content)

        val permissions = json["permissions"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertEquals(listOf("android.permission.INTERNET", "android.permission.CAMERA"), permissions)

        val activities = json["activities"]!!.jsonArray.map { it.jsonObject }
        assertEquals(1, activities.size)
        val mainActivity = activities.first()
        assertEquals(".MainActivity", mainActivity["name"]!!.jsonPrimitive.content)
        assertEquals(true, mainActivity["exported"]!!.jsonPrimitive.boolean)

        val intentFilters = mainActivity["intentFilters"]!!.jsonArray.map { it.jsonObject }
        assertEquals(2, intentFilters.size)

        val launcherFilter = intentFilters[0]
        assertEquals(
            listOf("android.intent.action.MAIN"),
            launcherFilter["actions"]!!.jsonArray.map { it.jsonPrimitive.content }
        )
        assertEquals(
            listOf("android.intent.category.LAUNCHER"),
            launcherFilter["categories"]!!.jsonArray.map { it.jsonPrimitive.content }
        )

        val deepLinkFilter = intentFilters[1]
        assertTrue("android.intent.action.VIEW" in deepLinkFilter["actions"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertTrue("android.intent.category.BROWSABLE" in deepLinkFilter["categories"]!!.jsonArray.map { it.jsonPrimitive.content })

        assertEquals(".sync.SyncService", json["services"]!!.jsonArray.first().jsonObject["name"]!!.jsonPrimitive.content)
        assertEquals(".boot.BootReceiver", json["receivers"]!!.jsonArray.first().jsonObject["name"]!!.jsonPrimitive.content)
        assertEquals(
            "androidx.core.content.FileProvider",
            json["providers"]!!.jsonArray.first().jsonObject["name"]!!.jsonPrimitive.content
        )
    }

    @Test
    fun `returns not_found when manifest is missing`() {
        val root = fixtureRoot()

        val json = ManifestInspectTool.execute(root, "does-not-exist")

        assertEquals("not_found", json["status"]!!.jsonPrimitive.content)
    }

    @Test
    fun `parses library manifest without permissions`() {
        val root = fixtureRoot()

        val json = ManifestInspectTool.execute(root, "feature-login")

        assertEquals("success", json["status"]!!.jsonPrimitive.content)
        assertEquals(0, json["permissions"]!!.jsonArray.size)
        assertEquals(
            "com.corporate.feature.login.LoginActivity",
            json["activities"]!!.jsonArray.first().jsonObject["name"]!!.jsonPrimitive.content
        )
    }
}
