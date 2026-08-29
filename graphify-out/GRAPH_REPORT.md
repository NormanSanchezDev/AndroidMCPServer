# Graph Report - AndroidCorporateMCP  (2026-08-29)

## Corpus Check
- 177 files · ~64,051 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1409 nodes · 1988 edges · 142 communities (96 shown, 46 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 109 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `050d87da`
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
- Installation Guide
- ManifestMergeTool
- VersionCatalogParser
- Usage Examples
- KotlinSourceScanner
- SessionViewModel
- ConventionPluginScanner
- .detect
- android-corporate-mcp.js
- ProjectInspectTool
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
- LintRunTool.kt
- kotlinx
- BuildValidateTool
- kotlinx
- gradlew
- AndroidFlowIr.kt
- FlowGraphBuilder
- CorporateApplication.kt
- JsonObject
- kotlinx
- AndroidProjectScan
- kotlinx
- DetectAndroidFlowTool.kt
- JunitXmlParser
- ManifestInspectTool
- TestsRunTool
- Android Corporate MCP
- SummaryFragment.kt
- Configuration
- AndroidFlow
- ProguardInspectTool
- SelectorExtractor
- OrderDoneFragment.kt
- RTK Commands by Workflow
- FlowNodeType
- README.md
- ComposeNavParser
- ManifestInspectToolTest
- ManifestMergeToolTest
- StaticAnalysisTool.kt
- Herramientas de referencia obligatorias para este proyecto
- tools.md
- Scope & Limitations
- .namesForFile
- .destinationNodeId
- LintRunToolTest
- ProjectPathsTest
- CheckoutActivity.kt
- SelectorExtractionTest
- Adding a New Tool
- DependenciesInspectToolTest
- Getting Started
- Quick Start
- Development Setup
- AppCompatActivity
- KtClass
- latticeMCP
- org
- KtFile
- KtTreeVisitorVoid
- KtFile
- KtTreeVisitorVoid
- org
- Element
- kotlinx
- kotlinx
- kotlinx
- Element
- JsonObject
- kotlinx
- kotlinx
- kotlinx
- JsonObject
- java
- java

## God Nodes (most connected - your core abstractions)
1. `FlowGraphBuilder` - 26 edges
2. `FlowDetector` - 20 edges
3. `bash` - 19 edges
4. `GradlePropertiesParser` - 16 edges
5. `FixtureProjects` - 16 edges
6. `SourceEvidence` - 16 edges
7. `FlowNode` - 14 edges
8. `DetectFlowOptions` - 14 edges
9. `Android Corporate MCP — Implementation Plan` - 13 edges
10. `permission` - 12 edges

## Surprising Connections (you probably didn't know these)
- `UserRepository` --references--> `MainActivity`  [EXTRACTED]
  src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/data/UserRepository.kt → src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/app/MainActivity.kt
- `AppNavRoot()` --calls--> `HomeScreen()`  [EXTRACTED]
  src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/navigation/AppNavHost.kt → src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/ui/HomeScreen.kt
- `AppNavRoot()` --calls--> `LoginScreen()`  [EXTRACTED]
  src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/navigation/AppNavHost.kt → src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/ui/LoginScreen.kt
- `AppNavRoot()` --calls--> `SplashScreen()`  [EXTRACTED]
  src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/navigation/AppNavHost.kt → src/test/resources/fixtures/compose-app/app/src/main/java/com/acme/auth/ui/SplashScreen.kt
- `CartFragment` --references--> `CheckoutViewModel`  [EXTRACTED]
  src/test/resources/fixtures/xml-fragment-app/app/src/main/java/com/acme/shop/cart/CartFragment.kt → src/test/resources/fixtures/xml-fragment-app/app/src/main/java/com/acme/shop/cart/CheckoutViewModel.kt

## Import Cycles
- None detected.

## Communities (142 total, 46 thin omitted)

### Community 0 - "Tools Reference"
Cohesion: 0.05
Nodes (37): Android Deep Inspection, architecture.detect, Architecture & Symbols, build.validate, Build Validation, dependencies.inspect, detect_android_flow, entry_points.find (+29 more)

### Community 1 - "JsonArray"
Cohesion: 0.09
Nodes (5): JsonArray, BuildValidateToolTest, ModuleGraphToolTest, ProjectInspectToolTest, StaticAnalysisToolTest

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
Cohesion: 0.08
Nodes (29): CallToolRequest, CallToolResult, JsonElement, argBool(), argInt(), argList(), args(), argString() (+21 more)

### Community 7 - ".create"
Cohesion: 0.09
Nodes (5): GradleFixtureProject, EntryPointsFindToolTest, GradleRunToolIntegrationTest, GradleTasksToolIntegrationTest, ResourcesInspectToolTest

### Community 8 - "EntryPointDetector"
Cohesion: 0.09
Nodes (13): FlowEntryPoint, AndroidProjectScanner, FlowModuleInfo, DeclaredActivity, EntryPointDetector, Element, org, ModuleEdge (+5 more)

### Community 9 - ".assertTrue"
Cohesion: 0.10
Nodes (6): XmlNavigationTest, NavXmlParserRegressionTest, KotlinPsiEngineTest, ConventionPluginsToolTest, DetectAndroidFlowToolTest, ProguardInspectToolTest

### Community 10 - "Tools Reference"
Cohesion: 0.06
Nodes (36): Android Resources, `architecture.detect`, Architecture & Navigation, `build.validate`, `dependencies.inspect`, Dependencies & Modules, `entry_points.find`, `gradle.config` (+28 more)

### Community 11 - "Installation Guide"
Cohesion: 0.15
Nodes (13): Check the JAR version, Connect a client, Installation Guide, Installing JDK 22, Method 1: npx (Recommended), Method 2: Global npm Install, Method 3: Direct JAR Download, Method 4: Docker (+5 more)

### Community 12 - "ManifestMergeTool"
Cohesion: 0.11
Nodes (11): Element, AttrValue, kotlinx, ManifestMergeTool, MergeConflict, kotlinx, ResourcesInspectTool, kotlinx (+3 more)

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

### Community 18 - ".detect"
Cohesion: 0.21
Nodes (13): AndroidFlowIr, DeclaredActivity, FlowAmbiguity, FlowGraphBuilder, FlowModuleInfo, FlowAmbiguity, SourceEvidence, SourceLocation (+5 more)

### Community 19 - "android-corporate-mcp.js"
Cohesion: 0.14
Nodes (12): buildDir, child, forwardedSignals, fs, jarDir, jarFile, jarPath, java (+4 more)

### Community 20 - "ProjectInspectTool"
Cohesion: 0.17
Nodes (7): AndroidModuleEvidence, AndroidModuleType, APPLICATION, LIBRARY, UNKNOWN, JsonObject, ProjectInspectTool

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
Cohesion: 0.17
Nodes (12): Architecture Overview, Code Style, Commit Guidelines, Contributing Guide, Core concepts, Coverage requirements, Manual version bump, Questions? (+4 more)

### Community 27 - "3. Detalle de bugs"
Cohesion: 0.12
Nodes (16): 1. Resumen ejecutivo, 1b. Estado de corrección (actualización 2026-08-29), 2. Metodología, 3. Detalle de bugs, 4. Deuda técnica menor (no crítico), 5. Mapa de impacto, 6. Plan de acción propuesto, BUG-01 — `manifest.merge` pierde la atribución de conflictos (+8 more)

### Community 28 - "FixtureProjects"
Cohesion: 0.11
Nodes (4): FixtureProjects, ComposeNavigationTest, EntryPointDetectorTest, ModuleDiscoveryTest

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
Cohesion: 0.20
Nodes (13): Bundle, ComponentActivity, Bundle, ComponentActivity, StateFlow, ViewModel, LoginUiState, MainActivity (+5 more)

### Community 37 - "KotlinNavExtractor"
Cohesion: 0.22
Nodes (6): Decision, DecisionExtractor, DecisionOutcome, ClickHandler, KotlinNavExtractor, NavCall

### Community 42 - "DetectFlowOptions"
Cohesion: 0.15
Nodes (4): DetectFlowOptions, AmbiguousNavigationTest, DecisionExtractionTest, EvidenceClassificationTest

### Community 43 - "GradleCommandValidator"
Cohesion: 0.16
Nodes (5): GradleCommandValidator, GradleWrapperLocator, DependenciesInspectTool, kotlinx, resolveModuleOrNull()

### Community 44 - "Navigation.kt"
Cohesion: 0.83
Nodes (3): AppNavHost(), HomeScreen(), LoginScreen()

### Community 45 - "ShopApplication"
Cohesion: 0.21
Nodes (9): Application, SessionStore, ShopApplication, AppCompatActivity, Bundle, LoginActivity, AppCompatActivity, Bundle (+1 more)

### Community 47 - "XmlNavGraphParser"
Cohesion: 0.29
Nodes (4): Element, NavXmlAction, NavXmlGraph, XmlNavGraphParser

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

### Community 64 - "LintRunTool.kt"
Cohesion: 0.38
Nodes (3): LintIssue, LintReport, LintXmlParser

### Community 68 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 69 - "AndroidFlowIr.kt"
Cohesion: 0.12
Nodes (15): EvidenceType, DECLARED, INFERRED, FlowApplication, FlowModule, ModuleDependency, SelectorKind, CONTENT_DESCRIPTION (+7 more)

### Community 70 - "FlowGraphBuilder"
Cohesion: 0.16
Nodes (4): FlowEdge, FlowNode, FlowGraphBuilder, MutableEdge

### Community 71 - "CorporateApplication.kt"
Cohesion: 0.83
Nodes (3): Application, CorporateApplication, Application

### Community 76 - "AndroidProjectScan"
Cohesion: 0.23
Nodes (4): AndroidProjectScan, AndroidProjectScan, NavXmlGraph, SourceScanner

### Community 78 - "DetectAndroidFlowTool.kt"
Cohesion: 0.43
Nodes (4): AndroidFlowIr, DetectAndroidFlowTool, JsonObject, kotlinx

### Community 79 - "JunitXmlParser"
Cohesion: 0.43
Nodes (4): JunitTestCase, JunitTestSuite, JunitXmlParser, org

### Community 80 - "ManifestInspectTool"
Cohesion: 0.50
Nodes (3): Element, JsonObject, ManifestInspectTool

### Community 81 - "TestsRunTool"
Cohesion: 0.46
Nodes (3): dev, kotlinx, TestsRunTool

### Community 86 - "Android Corporate MCP"
Cohesion: 0.17
Nodes (12): 🎯 27 Analysis Tools, Android Corporate MCP, Architecture, Contributing, Documentation, Features, 🚀 Key Capabilities, License (+4 more)

### Community 87 - "SummaryFragment.kt"
Cohesion: 0.42
Nodes (6): Bundle, Fragment, LayoutInflater, View, ViewGroup, SummaryFragment

### Community 88 - "Configuration"
Cohesion: 0.25
Nodes (8): Claude Code, Claude Desktop, Configuration, Cursor, GitHub Copilot (VS Code), No environment-variable configuration, OpenAI Codex CLI, Verifying a configuration

### Community 90 - "ProguardInspectTool"
Cohesion: 0.39
Nodes (4): kotlinx, ProguardFile, ProguardInspectTool, ProguardRule

### Community 91 - "SelectorExtractor"
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

### Community 100 - "StaticAnalysisTool.kt"
Cohesion: 0.50
Nodes (3): kotlinx, StaticAnalysisTool, ToolResult

### Community 101 - "Herramientas de referencia obligatorias para este proyecto"
Cohesion: 0.40
Nodes (4): 1. graphify — índice de conocimiento del codebase, 2. RTK — filtro de salida de comandos (Rust Token Killer), 3. Este repositorio, Herramientas de referencia obligatorias para este proyecto

### Community 103 - "Scope & Limitations"
Cohesion: 0.40
Nodes (5): Environment limitations, In scope, Known limitations by design, Out of scope, Scope & Limitations

### Community 106 - "LintRunToolTest"
Cohesion: 0.18
Nodes (4): java, LintRunToolTest, java, TestsRunToolTest

### Community 108 - "CheckoutActivity.kt"
Cohesion: 0.60
Nodes (3): CheckoutActivity, AppCompatActivity, Bundle

### Community 110 - "Adding a New Tool"
Cohesion: 0.40
Nodes (5): 1. Create the tool class, 2. Register in the server, 3. Create a test, 4. Use the fixture project for testing, Adding a New Tool

### Community 118 - "Getting Started"
Cohesion: 0.50
Nodes (4): Build, Fork and clone, Getting Started, Prerequisites

### Community 119 - "Quick Start"
Cohesion: 0.50
Nodes (4): Configuration, Installation, Quick Start, Usage

### Community 120 - "Development Setup"
Cohesion: 0.67
Nodes (3): Development Setup, IDE, Running tests

## Knowledge Gaps
- **386 isolated node(s):** `graphify`, `Golden Rule`, `Build & Compile (80-90% savings)`, `Test (60-99% savings)`, `Git (59-80% savings)` (+381 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **46 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FlowDetector` connect `.detect` to `.namesForFile`, `.destinationNodeId`, `DetectFlowOptions`, `DetectAndroidFlowTool.kt`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **Why does `DetectAndroidFlowTool` connect `DetectAndroidFlowTool.kt` to `server/Main.kt`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Why does `GradlePropertiesParser` connect `GradlePropertiesParser` to `EntryPointDetector`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **What connects `graphify`, `Golden Rule`, `Build & Compile (80-90% savings)` to the rest of the system?**
  _386 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Tools Reference` be split into smaller, more focused modules?**
  _Cohesion score 0.05405405405405406 - nodes in this community are weakly interconnected._
- **Should `JsonArray` be split into smaller, more focused modules?**
  _Cohesion score 0.08695652173913043 - nodes in this community are weakly interconnected._
- **Should `bash` be split into smaller, more focused modules?**
  _Cohesion score 0.05405405405405406 - nodes in this community are weakly interconnected._