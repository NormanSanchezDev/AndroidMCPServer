package dev.normansanchez.androidmcp.util

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProjectPathsTest {

    @Test
    fun `resolveModuleOrNull resolves within root and rejects escapes`() {
        val root = Files.createTempDirectory("module-paths")
        try {
            assertEquals(root, root.resolveModuleOrNull(null))
            assertEquals(root, root.resolveModuleOrNull(""))
            assertEquals(root.resolve("app"), root.resolveModuleOrNull(":app"))
            assertEquals(root.resolve("feature/login"), root.resolveModuleOrNull("feature/login"))
            assertNull(root.resolveModuleOrNull("../.."), "parent traversal must be rejected")
            assertNull(root.resolveModuleOrNull("./../evil"), "dot-dot inside path must be rejected")
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `resolveModuleOrNull rejects absolute paths outside root`() {
        val root = Files.createTempDirectory("module-abs")
        try {
            assertNull(root.resolveModuleOrNull("../../../etc"))
            assertNull(root.resolveModuleOrNull("/tmp/outside"))
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `isUnderExcludedDir detects generated and hidden dirs`() {
        val root = Files.createTempDirectory("excluded-dirs")
        try {
            val generated = root.resolve("app/build/intermediates/x")
            val regular = root.resolve("app/src/main/res")
            val infra = root.resolve("build-logic/settings")
            val gradleCached = root.resolve(".gradle/caches")

            assertTrue(generated.isUnderExcludedDir(root))
            assertTrue(gradleCached.isUnderExcludedDir(root))
            assertTrue(infra.isUnderExcludedDir(root, setOf("build-logic")))
            assertFalse(regular.isUnderExcludedDir(root))
        } finally {
            root.toFile().deleteRecursively()
        }
    }
}