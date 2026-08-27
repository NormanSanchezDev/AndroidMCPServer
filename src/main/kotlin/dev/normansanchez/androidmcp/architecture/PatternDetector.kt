package dev.normansanchez.androidmcp.architecture

data class ArchitectureEvidence(
    val category: String,
    val name: String,
    val evidence: String,
    val file: String,
    val line: Int
)

data class ArchitectureResult(
    val diFramework: String?,
    val diEvidence: List<ArchitectureEvidence>,
    val usesCompose: Boolean,
    val composeEvidence: List<ArchitectureEvidence>,
    val viewModelPattern: String?,
    val viewModelEvidence: List<ArchitectureEvidence>,
    val reactiveTypes: List<String>,
    val reactiveEvidence: List<ArchitectureEvidence>,
    val baseClasses: List<String>,
    val baseClassEvidence: List<ArchitectureEvidence>
)

object PatternDetector {

    private val DI_ANNOTATIONS = mapOf(
        "Hilt" to listOf("@HiltAndroidApp", "@HiltViewModel", "@AndroidEntryPoint", "@HiltWorker"),
        "Koin" to listOf("import org.koin", "import org.koin.dsl.module", "koinModule", "single {", "factory {"),
        "Dagger" to listOf("@Component", "@Subcomponent", "@Module"),
        "Manual" to listOf("object.*Factory", "companion object.*fun create")
    )

    private val COMPOSE_INDICATORS = listOf(
        "import androidx.compose",
        "@Composable",
        "ComposeContent",
        "setContent {"
    )

    private val VIEWMODEL_PATTERNS = mapOf(
        "ViewModel" to listOf(": ViewModel()", "extends ViewModel", ": AndroidViewModel"),
        "MviViewModel" to listOf("MviViewModel", "MviContract", "sealed class.*Intent"),
        "MoleculeViewModel" to listOf("MoleculeViewModel", "@MoleculeViewModel")
    )

    private val REACTIVE_TYPES = listOf(
        "StateFlow", "MutableStateFlow", "MutableState",
        "LiveData", "MutableLiveData",
        "SharedFlow", "MutableSharedFlow",
        "BehaviorSubject", "PublishSubject"
    )

    fun detect(sourceFiles: List<SourceFile>): ArchitectureResult {
        val diEvidence = mutableListOf<ArchitectureEvidence>()
        val composeEvidence = mutableListOf<ArchitectureEvidence>()
        val viewModelEvidence = mutableListOf<ArchitectureEvidence>()
        val reactiveEvidence = mutableListOf<ArchitectureEvidence>()
        val baseClassEvidence = mutableListOf<ArchitectureEvidence>()

        for (file in sourceFiles) {
            val lines = file.content.lines()
            for ((index, line) in lines.withIndex()) {
                for ((framework, indicators) in DI_ANNOTATIONS) {
                    for (indicator in indicators) {
                        if (line.contains(indicator)) {
                            diEvidence.add(
                                ArchitectureEvidence("di", framework, indicator.trim(), file.path, index + 1)
                            )
                        }
                    }
                }

                for (indicator in COMPOSE_INDICATORS) {
                    if (line.contains(indicator)) {
                        composeEvidence.add(
                            ArchitectureEvidence("compose", "Compose", indicator.trim(), file.path, index + 1)
                        )
                    }
                }

                for ((pattern, indicators) in VIEWMODEL_PATTERNS) {
                    for (indicator in indicators) {
                        if (Regex(indicator).containsMatchIn(line)) {
                            viewModelEvidence.add(
                                ArchitectureEvidence("viewmodel", pattern, indicator.trim(), file.path, index + 1)
                            )
                        }
                    }
                }

                for (reactiveType in REACTIVE_TYPES) {
                    if (line.contains(reactiveType)) {
                        reactiveEvidence.add(
                            ArchitectureEvidence("reactive", reactiveType, reactiveType, file.path, index + 1)
                        )
                    }
                }
            }
        }

        val diFramework = diEvidence.groupBy { it.name }
            .maxByOrNull { it.value.size }?.key

        val usesCompose = composeEvidence.isNotEmpty()

        val viewModelPattern = viewModelEvidence.groupBy { it.name }
            .maxByOrNull { it.value.size }?.key

        val reactiveTypes = reactiveEvidence.map { it.name }.distinct()

        return ArchitectureResult(
            diFramework = diFramework,
            diEvidence = diEvidence,
            usesCompose = usesCompose,
            composeEvidence = composeEvidence,
            viewModelPattern = viewModelPattern,
            viewModelEvidence = viewModelEvidence,
            reactiveTypes = reactiveTypes,
            reactiveEvidence = reactiveEvidence,
            baseClasses = emptyList(),
            baseClassEvidence = baseClassEvidence
        )
    }
}

data class SourceFile(
    val path: String,
    val content: String
)
