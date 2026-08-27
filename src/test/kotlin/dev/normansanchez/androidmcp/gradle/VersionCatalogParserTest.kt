package dev.normansanchez.androidmcp.gradle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VersionCatalogParserTest {

    @Test
    fun `parses versions and libraries`() {
        val content = """
[versions]
kotlin = "2.0.21"
agp = "8.7.3"

[libraries]
core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.15.0" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
""".trimIndent()

        val catalog = VersionCatalogParser.parseContent(content)

        assertEquals(2, catalog.versions.size)
        assertEquals("2.0.21", catalog.versions[0].value)
        assertEquals(2, catalog.libraries.size)
        assertEquals("core-ktx", catalog.libraries[0].alias)
        assertEquals("androidx.core", catalog.libraries[0].group)
        assertEquals("1.15.0", catalog.libraries[0].version)
        assertEquals(1, catalog.plugins.size)
        assertEquals("com.android.application", catalog.plugins[0].id)
    }

    @Test
    fun `resolves version refs`() {
        val content = """
[versions]
compose-bom = "2024.12.01"

[libraries]
compose-ui = { group = "androidx.compose.ui", name = "ui", version.ref = "compose-bom" }
""".trimIndent()

        val catalog = VersionCatalogParser.parseContent(content)
        assertEquals("2024.12.01", catalog.libraries[0].version)
    }
}
