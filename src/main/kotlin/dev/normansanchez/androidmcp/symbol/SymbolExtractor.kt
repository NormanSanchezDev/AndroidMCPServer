package dev.normansanchez.androidmcp.symbol

import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtTypeAlias

data class SymbolEntry(
    val kind: String,
    val name: String,
    val fqName: String,
    val containerName: String?,
    val offset: Int,
    val line: Int,
    val nameOffset: Int,
    val supertypes: List<String> = emptyList(),
    val annotations: List<String> = emptyList(),
    val constructorParams: List<String> = emptyList(),
    val visibility: String? = null,
    val typeReference: String? = null
)

object SymbolExtractor {

    fun extract(sourceText: String, ktFile: KtFile): List<SymbolEntry> {
        val entries = mutableListOf<SymbolEntry>()
        val packageName = ktFile.packageFqName.asString()

        fun qualify(containerName: String?, name: String): String =
            listOfNotNull(packageName.takeIf { it.isNotBlank() }, containerName, name)
                .joinToString(".")

        ktFile.accept(object : KtTreeVisitorVoid() {

            override fun visitClass(klass: KtClass) {
                val name = klass.name ?: return
                val container = PsiTreeUtil.getParentOfType(
                    klass,
                    KtClass::class.java,
                    KtObjectDeclaration::class.java
                )

                val kind = when {
                    klass.isInterface() -> "interface"
                    klass.isEnum() -> "enum"
                    else -> "class"
                }

                entries.add(entry(kind, name, qualify(container?.name, name), container?.name, klass, sourceText))
                super.visitClass(klass)
            }

            override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
                val name = declaration.name ?: return
                val container = PsiTreeUtil.getParentOfType(
                    declaration,
                    KtClass::class.java,
                    KtObjectDeclaration::class.java
                )
                entries.add(
                    entry(
                        "object",
                        name,
                        qualify(container?.name, name),
                        container?.name,
                        declaration,
                        sourceText
                    )
                )
                super.visitObjectDeclaration(declaration)
            }

            override fun visitNamedFunction(function: KtNamedFunction) {
                val name = function.name ?: return
                val container = PsiTreeUtil.getParentOfType(
                    function,
                    KtClass::class.java,
                    KtObjectDeclaration::class.java
                )
                entries.add(
                    entry(
                        "function",
                        name,
                        qualify(container?.name, name),
                        container?.name,
                        function,
                        sourceText
                    )
                )
                super.visitNamedFunction(function)
            }

            override fun visitProperty(property: KtProperty) {
                val name = property.name ?: return
                val container = PsiTreeUtil.getParentOfType(
                    property,
                    KtClass::class.java,
                    KtObjectDeclaration::class.java
                )
                entries.add(
                    entry(
                        "property",
                        name,
                        qualify(container?.name, name),
                        container?.name,
                        property,
                        sourceText
                    )
                )
                super.visitProperty(property)
            }

            override fun visitParameter(parameter: KtParameter) {
                if (parameter.hasValOrVar()) {
                    val name = parameter.name ?: return
                    val container = PsiTreeUtil.getParentOfType(
                        parameter,
                        KtClass::class.java
                    )
                    entries.add(
                        entry(
                            "property",
                            name,
                            qualify(container?.name, name),
                            container?.name,
                            parameter,
                            sourceText
                        )
                    )
                }
                super.visitParameter(parameter)
            }

            override fun visitTypeAlias(typeAlias: KtTypeAlias) {
                val name = typeAlias.name ?: return
                entries.add(entry("typealias", name, qualify(null, name), null, typeAlias, sourceText))
                super.visitTypeAlias(typeAlias)
            }
        })

        return entries
    }

    private fun entry(
        kind: String,
        name: String,
        fqName: String,
        containerName: String?,
        declaration: org.jetbrains.kotlin.psi.KtNamedDeclaration,
        sourceText: String
    ): SymbolEntry {
        val offset = declaration.textRange.startOffset
        val nameOffset = declaration.nameIdentifier?.textRange?.startOffset ?: offset

        val supertypes = when (declaration) {
            is KtClass -> declaration.superTypeListEntries.mapNotNull { it.typeReference?.text?.substringBefore("<")?.trim() }
            else -> emptyList()
        }

        val annotations = declaration.annotationEntries.mapNotNull { entry ->
            entry.shortName?.asString()
        }

        val constructorParams = when (declaration) {
            is KtClass -> declaration.primaryConstructorParameters.mapNotNull { param ->
                val paramName = param.name ?: return@mapNotNull null
                val paramType = param.typeReference?.text ?: "Any"
                "$paramName: $paramType"
            }
            else -> emptyList()
        }

        val modifierText = declaration.modifierList?.text ?: ""
        val visibility = when {
            modifierText.contains("public") -> "public"
            modifierText.contains("private") -> "private"
            modifierText.contains("protected") -> "protected"
            modifierText.contains("internal") -> "internal"
            else -> null
        }

        val typeReference = when (declaration) {
            is KtProperty -> declaration.typeReference?.text
            is KtNamedFunction -> declaration.typeReference?.text
            else -> null
        }

        return SymbolEntry(
            kind = kind,
            name = name,
            fqName = fqName,
            containerName = containerName,
            offset = offset,
            line = KotlinPsiEngine.lineOf(sourceText, offset),
            nameOffset = nameOffset,
            supertypes = supertypes,
            annotations = annotations,
            constructorParams = constructorParams,
            visibility = visibility,
            typeReference = typeReference
        )
    }
}
