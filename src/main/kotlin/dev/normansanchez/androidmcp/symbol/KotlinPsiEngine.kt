package dev.normansanchez.androidmcp.symbol

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.K1Deprecation
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

object KotlinPsiEngine {

    @OptIn(CompilerConfiguration.Internals::class, K1Deprecation::class)
    private val environment: KotlinCoreEnvironment by lazy {
        val configuration = CompilerConfiguration()
        configuration.put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
    }

    fun parse(sourceText: String, fileName: String): KtFile {
        val psiFileFactory = PsiFileFactory.getInstance(environment.project)

        val psiFile = psiFileFactory.createFileFromText(
            fileName,
            KotlinLanguage.INSTANCE,
            sourceText
        )

        return psiFile as KtFile
    }

    fun collectParseErrors(ktFile: KtFile): List<String> {
        val errors = mutableListOf<String>()

        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitErrorElement(element: PsiErrorElement) {
                errors.add(element.errorDescription)
            }
        })

        return errors
    }

    fun declarationName(declaration: PsiElement): String? =
        (declaration as? org.jetbrains.kotlin.psi.KtNamedDeclaration)?.name

    fun lineOf(sourceText: String, offset: Int): Int =
        sourceText.substring(0, offset.coerceAtMost(sourceText.length)).count { it == '\n' } + 1
}
