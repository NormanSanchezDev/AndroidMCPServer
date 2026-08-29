# Graph Report - AndroidCorporateMCP  (2026-08-29)

## Corpus Check
- 177 files · ~64,053 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1359 nodes · 1931 edges · 101 communities (80 shown, 21 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 109 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f30fb831`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Tools Reference
- JsonArray
- bash
- .parse
- package.json
- SymbolExtractor.kt
- server/Main.kt
- .create
- EntryPointDetector
- .assertTrue
- Tools Reference
- Android Corporate MCP
- ManifestMergeTool
- VersionCatalogParser
- Usage Examples
- KotlinSourceScanner
- SessionViewModel
- ConventionPluginScanner
- FlowGraphBuilder
- android-corporate-mcp.js
- EntryPointsFindTool
- Android Corporate MCP — Implementation Plan
- Troubleshooting Guide
- CartFragment.kt
- Configuration Guide
- GradleDependenciesTreeParser
- Contributing Guide
- 3. Detalle de bugs
- FixtureProjects
- LoginViewModel.kt
- postinstall.js
- GradleTasksParserTest
- JunitXmlParserTest
- GradleDependenciesTreeParserTest
- .sampleAndroidProject
- GradlePropertiesParser
- app/MainActivity.kt
- KotlinNavExtractor
- DetektParser.kt
- KtlintParser.kt
- VersionCatalogParserTest
- LintXmlParserTest
- DetectFlowOptions
- GradleCommandValidator
- Navigation.kt
- ShopApplication
- graphify.js
- XmlNavGraphParser
- SupportFragment.kt
- Troubleshooting
- Security & Privacy
- SecurityAuditToolTest
- MainActivityTest
- ArchitectureDetectTool.kt
- Architecture
- GradleTasksTool.kt
- UiFramework
- ProcessExecutor
- Getting Started
- Development
- Compatibility
- Contributing
- LintXmlParser.kt
- LintRunTool
- BuildValidateTool
- StaticAnalysisTool.kt
- gradlew
- EntryPointsFindToolTest
- ResourcesInspectToolTest
- CorporateApplication.kt
- SummaryFragment.kt
- Configuration
- SelectorExtractor
- ProjectInspectToolTest
- OrderDoneFragment.kt
- RTK Commands by Workflow
- FlowNodeType
- README.md
- ComposeNavParser
- ManifestInspectToolTest
- ManifestMergeToolTest
- Herramientas de referencia obligatorias para este proyecto
- tools.md
- Scope & Limitations
- TestsRunToolTest
- ProjectPathsTest
- CheckoutActivity.kt

## God Nodes (most connected - your core abstractions)
1. `FlowGraphBuilder` - 26 edges
2. `FlowDetector` - 20 edges
3. `bash` - 19 edges
4. `SourceEvidence` - 16 edges
5. `GradlePropertiesParser` - 16 edges
6. `FixtureProjects` - 16 edges
7. `FlowNode` - 14 edges
8. `DetectFlowOptions` - 14 edges
9. `Android Corporate MCP — Implementation Plan` - 13 edges
10. `permission` - 12 edges

## Surprising Connections (you probably didn't know these)
- `AppNavRoot()` --calls--> `HomeScreen()`  [EXTRACTED]
  src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/navigation/AppNavHost.kt → src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/ui/HomeScreen.kt
- `AppNavRoot()` --calls--> `LoginScreen()`  [EXTRACTED]
  src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/navigation/AppNavHost.kt → src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/ui/LoginScreen.kt
- `AppNavRoot()` --calls--> `SplashScreen()`  [EXTRACTED]
  src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/navigation/AppNavHost.kt → src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/ui/SplashScreen.kt
- `MainActivity` --references--> `UserRepository`  [EXTRACTED]
  src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/app/MainActivity.kt → src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/data/UserRepository.kt
- `CartFragment` --references--> `CheckoutViewModel`  [EXTRACTED]
  src/test/resources/fixtures/xml-fragment-app/app/src/main/java/com/acme/shop/cart/CartFragment.kt → src/test/resources/fixtures/xml-fragment-app/app/src/main/java/com/acme/shop/cart/CheckoutViewModel.kt

## Import Cycles
- None detected.

## Communities (101 total, 21 thin omitted)

### Community 0 - "Tools Reference"
Cohesion: 0.05
Nodes (37): Android Deep Inspection, architecture.detect, Architecture & Symbols, build.validate, Build Validation, dependencies.inspect, detect_android_flow, entry_points.find (+29 more)

### Community 1 - "JsonArray"
Cohesion: 0.08
Nodes (7): JsonArray, BuildValidateToolTest, DependenciesInspectToolTest, java, LintRunToolTest, ModuleGraphToolTest, StaticAnalysisToolTest

### Community 2 - "bash"
Cohesion: 0.05
Nodes (36): git add*, git clean*, git commit*, git diff*, git log*, git push*, git reset --hard*, git show* (+28 more)

### Community 3 - ".parse"
Cohesion: 0.36
Nodes (5): NavAction, NavArgument, NavDestination, NavGraph, NavXmlParser

### Community 4 - "package.json"
Cohesion: 0.07
Nodes (26): author, bin, android-corporate-mcp, description, engines, node, files, keywords (+18 more)

### Community 5 - "SymbolExtractor.kt"
Cohesion: 0.13
Nodes (14): KtNamedFunction, KtObjectDeclaration, KtParameter, KtProperty, KtTypeAlias, KtClass, KtFile, KtTreeVisitorVoid (+6 more)

### Community 6 - "server/Main.kt"
Cohesion: 0.09
Nodes (28): CallToolRequest, CallToolResult, JsonElement, argBool(), argInt(), argList(), args(), argString() (+20 more)

### Community 7 - ".create"
Cohesion: 0.14
Nodes (3): GradleFixtureProject, GradleRunToolIntegrationTest, GradleTasksToolIntegrationTest

### Community 8 - "EntryPointDetector"
Cohesion: 0.09
Nodes (13): FlowEntryPoint, AndroidProjectScanner, FlowModuleInfo, DeclaredActivity, EntryPointDetector, Element, org, ModuleEdge (+5 more)

### Community 9 - ".assertTrue"
Cohesion: 0.10
Nodes (6): EntryPointDetectorTest, NavXmlParserRegressionTest, KotlinPsiEngineTest, ConventionPluginsToolTest, DetectAndroidFlowToolTest, ProguardInspectToolTest

### Community 10 - "Tools Reference"
Cohesion: 0.06
Nodes (36): Android Resources, `architecture.detect`, Architecture & Navigation, `build.validate`, `dependencies.inspect`, Dependencies & Modules, `entry_points.find`, `gradle.config` (+28 more)

### Community 11 - "Android Corporate MCP"
Cohesion: 0.06
Nodes (29): 🎯 27 Analysis Tools, Android Corporate MCP, Architecture, Configuration, Contributing, Documentation, Features, Installation (+21 more)

### Community 12 - "ManifestMergeTool"
Cohesion: 0.09
Nodes (15): AndroidModuleEvidence, AndroidModuleType, APPLICATION, LIBRARY, UNKNOWN, AttrValue, kotlinx, ManifestMergeTool (+7 more)

### Community 13 - "VersionCatalogParser"
Cohesion: 0.23
Nodes (7): CatalogLibrary, CatalogPlugin, CatalogVersion, VersionCatalog, VersionCatalogParser, kotlinx, VersionCatalogTool

### Community 14 - "Usage Examples"
Cohesion: 0.06
Nodes (34): Architecture Analysis, Build & Testing, Check for merge conflicts, Check Gradle configuration, Check ProGuard rules, Check resource usage, Code Navigation, Common Workflows (+26 more)

### Community 15 - "KotlinSourceScanner"
Cohesion: 0.06
Nodes (25): KotlinCoreEnvironment, PsiElement, PsiErrorElement, ComposeNavDetector, ComposeRoute, HierarchyBuilder, KtTreeVisitorVoid, HierarchyEntry (+17 more)

### Community 16 - "SessionViewModel"
Cohesion: 0.12
Nodes (16): Bundle, ComponentActivity, MainActivity, AppNavRoot(), Authenticated, Expired, StateFlow, ViewModel (+8 more)

### Community 17 - "ConventionPluginScanner"
Cohesion: 0.27
Nodes (4): ConventionPlugin, ConventionPluginScanner, ConventionPluginsTool, kotlinx

### Community 18 - "FlowGraphBuilder"
Cohesion: 0.05
Nodes (33): AndroidFlow, AndroidFlowIr, EvidenceType, DECLARED, INFERRED, FlowAmbiguity, FlowApplication, FlowEdge (+25 more)

### Community 19 - "android-corporate-mcp.js"
Cohesion: 0.18
Nodes (9): buildDir, child, forwardedSignals, fs, jarDir, jarPath, javaCheck, path (+1 more)

### Community 20 - "EntryPointsFindTool"
Cohesion: 0.09
Nodes (16): JunitTestCase, JunitTestSuite, JunitXmlParser, org, EntryPointsFindTool, Element, kotlinx, Element (+8 more)

### Community 21 - "Android Corporate MCP — Implementation Plan"
Cohesion: 0.06
Nodes (31): 1.1 `gradle.config` — inspect Gradle configuration, 1.2 `gradle.versionCatalog` — inspect version catalog, 1.3 `gradle.conventionPlugins` — detect convention plugins, 2.1 `architecture.detect` — detect DI and architecture patterns, 2.2 `tests.discover` — find tests related to production code, 3.1 Enhance `symbol.find` with type information, 3.2 `symbol.hierarchy` — class/interface hierarchy, 4.1 `build.validate` — compile a specific module (+23 more)

### Community 22 - "Troubleshooting Guide"
Cohesion: 0.07
Nodes (28): Common Causes & Fixes, Explanation, Fix, Fix, Fix, Fixes, Gradle Not Found, If tools still don't appear (+20 more)

### Community 23 - "CartFragment.kt"
Cohesion: 0.15
Nodes (15): CartFragment, Bundle, Fragment, LayoutInflater, View, ViewGroup, CartItem, CheckoutRepository (+7 more)

### Community 24 - "Configuration Guide"
Cohesion: 0.07
Nodes (27): 1. Edit the configuration, 1. Edit the configuration, 1. Locate the configuration file, 1. Open MCP settings, 1. Open the MCP settings, 1. Register the server, 2. Add the server, 2. Add the server (+19 more)

### Community 25 - "GradleDependenciesTreeParser"
Cohesion: 0.52
Nodes (3): ConfigurationDependencies, DependencyNode, GradleDependenciesTreeParser

### Community 26 - "Contributing Guide"
Cohesion: 0.08
Nodes (24): 1. Create the tool class, 2. Register in the server, 3. Create a test, 4. Use the fixture project for testing, Adding a New Tool, Architecture Overview, Build, Code Style (+16 more)

### Community 27 - "3. Detalle de bugs"
Cohesion: 0.12
Nodes (16): 1. Resumen ejecutivo, 1b. Estado de corrección (actualización 2026-08-29), 2. Metodología, 3. Detalle de bugs, 4. Deuda técnica menor (no crítico), 5. Mapa de impacto, 6. Plan de acción propuesto, BUG-01 — `manifest.merge` pierde la atribución de conflictos (+8 more)

### Community 28 - "FixtureProjects"
Cohesion: 0.12
Nodes (4): FixtureProjects, ComposeNavigationTest, ModuleDiscoveryTest, XmlNavigationTest

### Community 29 - "LoginViewModel.kt"
Cohesion: 0.43
Nodes (5): StateFlow, ViewModel, LoginRepository, LoginState, LoginViewModel

### Community 30 - "postinstall.js"
Cohesion: 0.33
Nodes (5): { execSync }, fs, jarDir, jarFiles, path

### Community 34 - ".sampleAndroidProject"
Cohesion: 0.07
Nodes (9): ArchitectureDetectToolTest, GradleConfigToolTest, NavigationGraphToolTest, ResourceReferencesToolTest, SymbolFindToolTest, SymbolHierarchyToolTest, SymbolReferencesToolTest, TestsDiscoverToolTest (+1 more)

### Community 35 - "GradlePropertiesParser"
Cohesion: 0.18
Nodes (4): GradleConfig, GradlePropertiesParser, GradleConfigTool, kotlinx

### Community 36 - "app/MainActivity.kt"
Cohesion: 0.25
Nodes (9): Bundle, ComponentActivity, StateFlow, ViewModel, LoginUiState, MainActivity, MainViewModel, User (+1 more)

### Community 37 - "KotlinNavExtractor"
Cohesion: 0.22
Nodes (6): Decision, DecisionExtractor, DecisionOutcome, ClickHandler, KotlinNavExtractor, NavCall

### Community 42 - "DetectFlowOptions"
Cohesion: 0.11
Nodes (5): DetectFlowOptions, AmbiguousNavigationTest, DecisionExtractionTest, EvidenceClassificationTest, SelectorExtractionTest

### Community 43 - "GradleCommandValidator"
Cohesion: 0.18
Nodes (4): GradleCommandValidator, GradleWrapperLocator, DependenciesInspectTool, kotlinx

### Community 44 - "Navigation.kt"
Cohesion: 0.83
Nodes (3): AppNavHost(), HomeScreen(), LoginScreen()

### Community 45 - "ShopApplication"
Cohesion: 0.21
Nodes (9): Application, SessionStore, ShopApplication, AppCompatActivity, Bundle, LoginActivity, AppCompatActivity, Bundle (+1 more)

### Community 47 - "XmlNavGraphParser"
Cohesion: 0.24
Nodes (5): Element, NavXmlAction, NavXmlDestination, NavXmlGraph, XmlNavGraphParser

### Community 48 - "SupportFragment.kt"
Cohesion: 0.20
Nodes (9): AppCompatActivity, Bundle, MainActivity, Bundle, Fragment, LayoutInflater, View, ViewGroup (+1 more)

### Community 49 - "Troubleshooting"
Cohesion: 0.17
Nodes (12): Android SDK unavailable, Docker build fails to find the JAR, Gradle failure, Java missing or wrong version, Launcher can't locate the JAR, MCP client can't connect, Permission errors, Repository not recognized (+4 more)

### Community 50 - "Security & Privacy"
Cohesion: 0.18
Nodes (11): Command execution and the one tool that matters most, Credentials and secrets, Network access, npm distribution, Repository permissions in practice, Security & Privacy, Trust boundary, What never leaves the machine (+3 more)

### Community 54 - "ArchitectureDetectTool.kt"
Cohesion: 0.33
Nodes (6): ArchitectureEvidence, ArchitectureResult, PatternDetector, SourceFile, ArchitectureDetectTool, kotlinx

### Community 56 - "Architecture"
Cohesion: 0.22
Nodes (9): Architecture, Error propagation, Execution model for process-backed tools, How it works: one request, end to end, Lifecycle and stdio handshake, Process model, Repository access, The boundary (+1 more)

### Community 57 - "GradleTasksTool.kt"
Cohesion: 0.25
Nodes (4): GradleTaskEntry, GradleTasksParser, GradleTasksTool, kotlinx

### Community 58 - "UiFramework"
Cohesion: 0.20
Nodes (6): UiFramework, COMPOSE, MIXED, UNKNOWN, XML, UiFrameworkDetector

### Community 59 - "ProcessExecutor"
Cohesion: 0.28
Nodes (4): ProcessExecutor, ProcessResult, GradleRunTool, kotlinx

### Community 60 - "Getting Started"
Cohesion: 0.25
Nodes (8): 1. Install, 2. Configure your MCP client, 3. Open an Android repository, 4. Verify the connection, 5. Call your first tool, Getting Started, Prerequisites, What's next

### Community 61 - "Development"
Cohesion: 0.29
Nodes (7): Adding a new MCP tool, Clone and build, Coding conventions observed in this codebase, Development, Project structure, Run locally, Tests

### Community 62 - "Compatibility"
Cohesion: 0.33
Nodes (6): Compatibility, Distribution channels, MCP clients, Platforms, Runtime requirements, Status legend

### Community 63 - "Contributing"
Cohesion: 0.33
Nodes (6): Branching model, Contributing, License, Making a change, Pull request expectations, Reporting issues

### Community 64 - "LintXmlParser.kt"
Cohesion: 0.47
Nodes (3): LintIssue, LintReport, LintXmlParser

### Community 67 - "StaticAnalysisTool.kt"
Cohesion: 0.50
Nodes (3): kotlinx, StaticAnalysisTool, ToolResult

### Community 68 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 87 - "SummaryFragment.kt"
Cohesion: 0.42
Nodes (6): Bundle, Fragment, LayoutInflater, View, ViewGroup, SummaryFragment

### Community 88 - "Configuration"
Cohesion: 0.25
Nodes (8): Claude Code, Claude Desktop, Configuration, Cursor, GitHub Copilot (VS Code), No environment-variable configuration, OpenAI Codex CLI, Verifying a configuration

### Community 90 - "SelectorExtractor"
Cohesion: 0.36
Nodes (4): Selector, ComposeAction, LayoutAction, SelectorExtractor

### Community 92 - "OrderDoneFragment.kt"
Cohesion: 0.43
Nodes (6): Bundle, Fragment, LayoutInflater, View, ViewGroup, OrderDoneFragment

### Community 93 - "RTK Commands by Workflow"
Cohesion: 0.12
Nodes (15): Analysis & Debug (70-90% savings), Build & Compile (80-90% savings), Files & Search (60-75% savings), Git (59-80% savings), GitHub (26-87% savings), Golden Rule, graphify, Infrastructure (85% savings) (+7 more)

### Community 94 - "FlowNodeType"
Cohesion: 0.29
Nodes (7): FlowNodeType, ACTION, DECISION, EXTERNAL, PROCESS, SCREEN, UNKNOWN

### Community 95 - "README.md"
Cohesion: 0.18
Nodes (10): Architecture, Compatibility, Contributing, Documentation, License, Philosophy, Quick start, Security & local-first (+2 more)

### Community 96 - "ComposeNavParser"
Cohesion: 0.38
Nodes (3): ComposeNavCall, ComposeNavParser, ComposeRoute

### Community 101 - "Herramientas de referencia obligatorias para este proyecto"
Cohesion: 0.40
Nodes (4): 1. graphify — índice de conocimiento del codebase, 2. RTK — filtro de salida de comandos (Rust Token Killer), 3. Este repositorio, Herramientas de referencia obligatorias para este proyecto

### Community 103 - "Scope & Limitations"
Cohesion: 0.40
Nodes (5): Environment limitations, In scope, Known limitations by design, Out of scope, Scope & Limitations

### Community 108 - "CheckoutActivity.kt"
Cohesion: 0.60
Nodes (3): CheckoutActivity, AppCompatActivity, Bundle

## Knowledge Gaps
- **383 isolated node(s):** `$schema`, `model`, `small_model`, `auto`, `prune` (+378 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FlowDetector` connect `FlowGraphBuilder` to `DetectFlowOptions`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `DetectAndroidFlowTool` connect `FlowGraphBuilder` to `server/Main.kt`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **Why does `DetectFlowOptions` connect `DetectFlowOptions` to `FlowGraphBuilder`, `FixtureProjects`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Are the 14 inferred relationships involving `SourceEvidence` (e.g. with `.parseManifest()` and `.registerCodeClickHandlers()`) actually correct?**
  _`SourceEvidence` has 14 INFERRED edges - model-reasoned connections that need verification._
- **What connects `$schema`, `model`, `small_model` to the rest of the system?**
  _383 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Tools Reference` be split into smaller, more focused modules?**
  _Cohesion score 0.05405405405405406 - nodes in this community are weakly interconnected._
- **Should `JsonArray` be split into smaller, more focused modules?**
  _Cohesion score 0.08333333333333333 - nodes in this community are weakly interconnected._