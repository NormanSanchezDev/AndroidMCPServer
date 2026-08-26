package dev.normansanchez.androidmcp.symbol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinPsiEngineTest {

    @Test
    fun `parses kotlin source and extracts declarations`() {
        val source = """
            package com.example

            interface Greeter {
                fun greet(): String
            }

            class GreeterImpl(private val prefix: String) : Greeter {

                companion object {
                    const val DEFAULT_PREFIX = "Hello"
                }

                override fun greet(): String = "${'$'}prefix, world"
            }

            object GreeterFactory {
                fun create(): Greeter = GreeterImpl(GreeterImpl.DEFAULT_PREFIX)
            }
        """.trimIndent()

        val ktFile = KotlinPsiEngine.parse(source, "Greeter.kt")
        assertTrue(KotlinPsiEngine.collectParseErrors(ktFile).isEmpty())

        val symbols = SymbolExtractor.extract(source, ktFile)
        val byFqName = symbols.associateBy { it.fqName }

        assertEquals("interface", byFqName["com.example.Greeter"]?.kind)
        assertEquals("class", byFqName["com.example.GreeterImpl"]?.kind)
        assertEquals("object", byFqName["com.example.GreeterFactory"]?.kind)
        assertEquals(
            "function",
            byFqName["com.example.GreeterImpl.greet"]?.kind
        )
        assertEquals(
            "function",
            byFqName["com.example.GreeterFactory.create"]?.kind
        )
        assertEquals(
            "property",
            byFqName["com.example.GreeterImpl.prefix"]?.kind
        )

        val createSymbol = byFqName["com.example.GreeterFactory.create"]!!
        assertTrue(createSymbol.line > 1)
    }
}
