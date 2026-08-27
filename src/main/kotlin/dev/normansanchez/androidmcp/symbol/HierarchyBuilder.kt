package dev.normansanchez.androidmcp.symbol

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

data class HierarchyEntry(
    val name: String,
    val kind: String,
    val supertypes: List<String>,
    val file: String,
    val line: Int
)

object HierarchyBuilder {

    fun build(sourceFiles: List<ScannedKotlinFile>): Map<String, HierarchyEntry> {
        val entries = mutableMapOf<String, HierarchyEntry>()

        for (file in sourceFiles) {
            file.ktFile.accept(object : KtTreeVisitorVoid() {
                override fun visitClass(klass: KtClass) {
                    val name = klass.name ?: return@visitClass
                    val kind = when {
                        klass.isInterface() -> "interface"
                        klass.isEnum() -> "enum"
                        else -> "class"
                    }
                    val supertypes = klass.superTypeListEntries.mapNotNull { entry ->
                        entry.typeReference?.text?.substringBefore("<")?.trim()
                    }

                    val line = KotlinPsiEngine.lineOf(file.content, klass.textRange.startOffset)

                    entries[name] = HierarchyEntry(
                        name = name,
                        kind = kind,
                        supertypes = supertypes,
                        file = file.relativePath,
                        line = line
                    )
                    super.visitClass(klass)
                }
            })
        }

        return entries
    }

    fun buildHierarchyTree(
        entries: Map<String, HierarchyEntry>,
        targetName: String,
        depth: Int = 0,
        maxDepth: Int = 5
    ): HierarchyNode? {
        if (depth > maxDepth) return null

        val entry = entries[targetName] ?: return null

        val children = entries.values
            .filter { it.supertypes.contains(targetName) }
            .mapNotNull { child ->
                buildHierarchyTree(entries, child.name, depth + 1, maxDepth)
            }

        return HierarchyNode(
            name = entry.name,
            kind = entry.kind,
            supertypes = entry.supertypes,
            file = entry.file,
            line = entry.line,
            children = children
        )
    }
}

data class HierarchyNode(
    val name: String,
    val kind: String,
    val supertypes: List<String>,
    val file: String,
    val line: Int,
    val children: List<HierarchyNode>
)
