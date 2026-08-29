package dev.normansanchez.androidmcp.navigation

import dev.normansanchez.androidmcp.symbol.KotlinPsiEngine
import dev.normansanchez.androidmcp.symbol.ScannedKotlinFile
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposeNavDetectorRegressionTest {

    private fun scanned(content: String, name: String = "AppNavHost.kt"): ScannedKotlinFile =
        ScannedKotlinFile(
            path = Path.of(name),
            relativePath = name,
            content = content,
            ktFile = KotlinPsiEngine.parse(content, name)
        )

    @Test
    fun `detects named-argument multiline and plain composable routes`() {
        val content = """
            package com.acme

            @Composable
            fun AppNav() {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable(route = "splash") { SplashScreen() }
                    composable(
                        route = "home"
                    ) { HomeScreen() }
                    composable("bare") { BareScreen() }
                }
            }
        """.trimIndent()

        val routes = ComposeNavDetector.detect(listOf(scanned(content)))

        val byRoute = routes.associateBy { it.route }
        assertEquals(setOf("splash", "home", "bare"), routes.map { it.route }.toSet())
        assertTrue(routes.all { it.file == "AppNavHost.kt" })
        assertEquals(7, byRoute["splash"]?.line)
        assertEquals(8, byRoute["home"]?.line)
        assertEquals(11, byRoute["bare"]?.line)
        assertTrue(ComposeNavDetector.hasNavHost(listOf(scanned(content))))
    }

    @Test
    fun `detects navigate calls with and without named argument`() {
        val content = """
            fun go(navController: NavHostController) {
                navController.navigate("profile")
                navController.navigate(route = "settings")
            }
        """.trimIndent()

        val routes = ComposeNavDetector.detect(listOf(scanned(content, "Navigator.kt")))

        assertEquals(setOf("profile", "settings"), routes.map { it.route }.toSet())
        assertEquals(2, routes.size)
    }
}