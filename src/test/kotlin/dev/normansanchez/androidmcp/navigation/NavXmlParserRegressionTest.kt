package dev.normansanchez.androidmcp.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavXmlParserRegressionTest {

    private val navXml = """
        <?xml version="1.0" encoding="utf-8"?>
        <navigation xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:app="http://schemas.android.com/apk/res-auto"
            android:id="@+id/nav_graph"
            app:startDestination="@+id/homeFragment">

            <fragment
                android:id="@+id/homeFragment"
                android:name="com.acme.HomeFragment"
                android:label="Home">
                <argument
                    android:name="itemId"
                    app:argType="string"
                    android:defaultValue="0" />
                <action
                    android:id="@+id/action_home_to_login"
                    app:destination="@+id/loginFragment" />
            </fragment>

            <fragment
                android:id="@+id/loginFragment"
                android:name="com.acme.LoginFragment">
                <deep-link android:uri="corporate://login" />
            </fragment>

            <activity
                android:id="@+id/shopActivity"
                android:name="com.acme.ShopActivity">
                <deep-link app:uri="acme://shop/orders/done" />
            </activity>
        </navigation>
    """.trimIndent()

    @Test
    fun `parses start destination actions arguments and deep links from both namespaces`() {
        val graph = NavXmlParser.parse(navXml)

        assertEquals("homeFragment", graph.startDestination)
        assertEquals(3, graph.destinations.size)

        val home = graph.destinations.first { it.id == "homeFragment" }
        assertTrue(home.isStartDestination, "start destination marked with @+id prefix must match")
        assertEquals("Home", home.label)
        assertEquals("action_home_to_login", home.actions.first().id)
        assertEquals("loginFragment", home.actions.first().destination)
        assertEquals("itemId", home.arguments.first().name)
        assertEquals("string", home.arguments.first().type)
        assertEquals("0", home.arguments.first().defaultValue)

        val login = graph.destinations.first { it.id == "loginFragment" }
        assertTrue(login.deepLinks.contains("corporate://login"), "android:uri deep link extracted")

        val shop = graph.destinations.first { it.id == "shopActivity" }
        assertTrue(shop.deepLinks.contains("acme://shop/orders/done"), "app:uri deep link extracted")
    }

    @Test
    fun `matches start destination when only navigation uses @id prefix`() {
        val xml = """
            <navigation xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                app:startDestination="@id/cartFragment">
                <fragment android:id="@+id/cartFragment" android:name="com.acme.CartFragment" />
            </navigation>
        """.trimIndent()

        val graph = NavXmlParser.parse(xml)

        assertEquals("cartFragment", graph.startDestination)
        assertEquals(1, graph.destinations.size)
        assertTrue(graph.destinations.first().isStartDestination)
    }

    @Test
    fun `returns empty graph for malformed xml`() {
        val graph = NavXmlParser.parse("<navigation")

        assertEquals(null, graph.startDestination)
        assertEquals(0, graph.destinations.size)
    }
}