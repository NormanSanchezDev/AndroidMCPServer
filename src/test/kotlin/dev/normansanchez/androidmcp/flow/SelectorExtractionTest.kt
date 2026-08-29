package dev.normansanchez.androidmcp.flow

import dev.normansanchez.androidmcp.fixtures.FixtureProjects
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SelectorExtractionTest {

    @Test
    fun `extracts xml layout actions with resource id text and content description`() {
        val temp = Files.createTempDirectory("selector-xml")
        try {
            val project = FixtureProjects.xmlFragmentApp(temp)
            val layout = Files.readString(
                project.resolve("app/src/main/res/layout/fragment_cart.xml")
            )
            val strings = mapOf(
                "pay_now" to "Pay now",
                "add_to_cart" to "Add to cart"
            )

            val actions = SelectorExtractor.layoutActions(layout, strings)

            assertEquals(2, actions.size)
            val pay = actions.first { it.resourceId == "payButton" }
            assertEquals("Pay now", pay.text)
            assertEquals("onPayClicked", pay.onClickMethod)
            val add = actions.first { it.resourceId == "addToCartButton" }
            assertEquals("Add to cart", add.contentDescription)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `emits resolved and missing selector status in the ir`() {
        val temp = Files.createTempDirectory("selector-ir")
        try {
            val xmlProject = FixtureProjects.xmlFragmentApp(temp)
            val xmlIr = FlowDetector.detect(xmlProject, DetectFlowOptions(maxDepth = 20))
            val payNode = xmlIr.flows.first { it.entryNode == "SplashActivity" }
                .nodes.first { it.id == "CartFragment.payButton" }
            assertEquals(SelectorStatus.RESOLVED, payNode.selectorStatus)
            val kinds = payNode.selectors.map { it.kind }.toSet()
            assertTrue(SelectorKind.RESOURCE_ID in kinds)
            assertTrue(SelectorKind.TEXT in kinds)
            assertEquals("Pay now", payNode.selectors.first { it.kind == SelectorKind.TEXT }.value)

            val composeProject = FixtureProjects.composeApp(temp)
            val composeIr = FlowDetector.detect(composeProject, DetectFlowOptions(maxDepth = 10))
            val loginFlow = composeIr.flows.first { it.entryNode == "MainActivity" }
            val loginButtons = loginFlow.nodes.filter {
                it.type == FlowNodeType.ACTION && it.id.startsWith("LoginScreen.")
            }
            val signIn = loginButtons.first { it.name == "Sign in" }
            assertEquals(SelectorStatus.RESOLVED, signIn.selectorStatus)
            assertTrue(SelectorKind.TEST_TAG in signIn.selectors.map { it.kind })
            assertEquals("login_button", signIn.selectors.first { it.kind == SelectorKind.TEST_TAG }.value)

            assertTrue(
                loginButtons.any { it.selectorStatus == SelectorStatus.MISSING },
                "icon button without any selector must be flagged MISSING, never invented"
            )
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun `compose selector extractor finds test tag content description and text`() {
        val temp = Files.createTempDirectory("selector-compose")
        try {
            val project = FixtureProjects.composeApp(temp)
            val login = Files.readString(
                project.resolve("app/src/main/java/com/acme/auth/ui/LoginScreen.kt")
            )

            val actions = SelectorExtractor.composeActions(login)

            val signIn = actions.first { it.selectors.any { s -> s.kind == SelectorKind.TEST_TAG } }
            assertEquals("login_button", signIn.selectors.first { it.kind == SelectorKind.TEST_TAG }.value)
            assertEquals("Sign in", signIn.label)

            assertTrue(actions.any { it.selectors.isEmpty() }, "no invented selectors: missing case has empty list")
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}