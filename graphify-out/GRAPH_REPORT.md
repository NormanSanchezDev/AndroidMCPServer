# Graph Report - AndroidCorporateMCP  (2026-08-27)

## Corpus Check
- 124 files · ~48,755 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 770 nodes · 1039 edges · 108 communities (53 shown, 55 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0f7fa3b5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- GradleCommandValidator
- JsonArray
- bash
- KotlinSourceScanner
- package.json
- androidmcp/symbol/SymbolExtractor.kt
- server/Main.kt
- .create
- GradlePropertiesParser
- .assertTrue
- ModuleGraphParser
- ProjectInspectTool
- ManifestMergeTool
- VersionCatalogParser
- Tools Reference
- androidmcp/symbol/KotlinPsiEngine.kt
- MainActivity.kt
- ConventionPluginScanner
- ArchitectureDetectTool.kt
- android-corporate-mcp.js
- EntryPointsFindTool
- ProguardInspectTool
- ManifestInspectTool
- SecurityAuditTool
- TestsRunTool
- GradleDependenciesTreeParser
- ResourceReferencesTool
- GradleTasksParser
- SymbolHierarchyToolTest
- LoginViewModel.kt
- postinstall.js
- GradleTasksParserTest
- JunitXmlParserTest
- GradleDependenciesTreeParserTest
- GradleConfigToolTest
- SymbolReferencesToolTest
- UserRepository
- gradlew
- DetektParser.kt
- KtlintParser.kt
- VersionCatalogParserTest
- LintXmlParserTest
- .sampleAndroidProject
- tools.md
- Navigation.kt
- Application
- graphify.js
- Troubleshooting
- ArchitectureDetectToolTest
- NavigationGraphToolTest
- ResourceReferencesToolTest
- .`runs audit on sample project`
- MainActivityTest
- AppCompatActivity
- KtClass
- latticeMCP
- org
- KtFile
- KtTreeVisitorVoid
- KtFile
- KtTreeVisitorVoid
- org
- kotlinx
- Element
- kotlinx
- kotlinx
- kotlinx
- kotlinx
- Element
- JsonObject
- kotlinx
- JsonObject
- kotlinx
- kotlinx
- kotlinx
- kotlinx
- JsonObject
- java
- java
- androidmcp/tools/TestsRunTool.kt
- GradleWrapperLocator
- androidmcp/lint/LintXmlParser.kt
- BuildValidateTool
- LintRunTool
- ProcessExecutor
- StaticAnalysisTool.kt
- CLAUDE.md
- Security & Privacy
- README.md
- Architecture
- Configuration
- Getting Started
- Development
- Compatibility
- Contributing
- Scope & Limitations
- ManifestInspectToolTest
- ProjectInspectToolTest
- TestsRunToolTest
- DependenciesInspectToolTest
- StaticAnalysisToolTest

## God Nodes (most connected - your core abstractions)
1. `bash` - 17 edges
2. `GradlePropertiesParser` - 15 edges
3. `Troubleshooting` - 12 edges
4. `permission` - 12 edges
5. `Security & Privacy` - 11 edges
6. `Tools Reference` - 10 edges
7. `GradleCommandValidator` - 10 edges
8. `ProcessExecutor` - 10 edges
9. `main()` - 10 edges
10. `Architecture` - 9 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (108 total, 55 thin omitted)

### Community 0 - "GradleCommandValidator"
Cohesion: 0.22
Nodes (3): GradleCommandValidator, GradleRunTool, kotlinx

### Community 1 - "JsonArray"
Cohesion: 0.13
Nodes (4): JsonArray, BuildValidateToolTest, ManifestMergeToolTest, ModuleGraphToolTest

### Community 2 - "bash"
Cohesion: 0.06
Nodes (34): git add*, git clean*, git commit*, git diff*, git log*, git push*, git reset --hard*, git show* (+26 more)

### Community 3 - "KotlinSourceScanner"
Cohesion: 0.07
Nodes (22): ComposeNavDetector, ComposeRoute, NavAction, NavArgument, NavDestination, NavGraph, NavXmlParser, HierarchyBuilder (+14 more)

### Community 4 - "package.json"
Cohesion: 0.07
Nodes (26): author, bin, android-corporate-mcp, description, engines, node, files, keywords (+18 more)

### Community 5 - "androidmcp/symbol/SymbolExtractor.kt"
Cohesion: 0.13
Nodes (14): KtNamedFunction, KtObjectDeclaration, KtParameter, KtProperty, KtTypeAlias, KtClass, KtFile, KtTreeVisitorVoid (+6 more)

### Community 6 - "server/Main.kt"
Cohesion: 0.13
Nodes (22): CallToolRequest, CallToolResult, JsonElement, argBool(), argInt(), argList(), args(), argString() (+14 more)

### Community 7 - ".create"
Cohesion: 0.08
Nodes (6): GradleFixtureProject, EntryPointsFindToolTest, GradleRunToolIntegrationTest, GradleTasksToolIntegrationTest, LintRunToolTest, ResourcesInspectToolTest

### Community 8 - "GradlePropertiesParser"
Cohesion: 0.18
Nodes (4): GradleConfig, GradlePropertiesParser, GradleConfigTool, kotlinx

### Community 9 - ".assertTrue"
Cohesion: 0.18
Nodes (3): KotlinPsiEngineTest, ConventionPluginsToolTest, ProguardInspectToolTest

### Community 10 - "ModuleGraphParser"
Cohesion: 0.14
Nodes (6): ModuleEdge, ModuleGraph, ModuleGraphParser, kotlinx, ModuleGraphTool, ModuleGraphParserTest

### Community 11 - "ProjectInspectTool"
Cohesion: 0.18
Nodes (7): AndroidModuleEvidence, AndroidModuleType, APPLICATION, LIBRARY, UNKNOWN, JsonObject, ProjectInspectTool

### Community 12 - "ManifestMergeTool"
Cohesion: 0.18
Nodes (6): Element, kotlinx, ManifestMergeTool, MergeConflict, kotlinx, ResourcesInspectTool

### Community 13 - "VersionCatalogParser"
Cohesion: 0.23
Nodes (7): CatalogLibrary, CatalogPlugin, CatalogVersion, VersionCatalog, VersionCatalogParser, kotlinx, VersionCatalogTool

### Community 14 - "Tools Reference"
Cohesion: 0.06
Nodes (35): Android Deep Inspection, architecture.detect, Architecture & Symbols, build.validate, Build Validation, dependencies.inspect, entry_points.find, gradle.config (+27 more)

### Community 15 - "androidmcp/symbol/KotlinPsiEngine.kt"
Cohesion: 0.23
Nodes (7): KotlinCoreEnvironment, PsiElement, PsiErrorElement, KotlinPsiEngine, KtTreeVisitorVoid, KtFile, KtTreeVisitorVoid

### Community 16 - "MainActivity.kt"
Cohesion: 0.29
Nodes (9): Bundle, ComponentActivity, StateFlow, ViewModel, LoginUiState, MainActivity, MainViewModel, User (+1 more)

### Community 18 - "ArchitectureDetectTool.kt"
Cohesion: 0.33
Nodes (6): ArchitectureEvidence, ArchitectureResult, PatternDetector, SourceFile, ArchitectureDetectTool, kotlinx

### Community 19 - "android-corporate-mcp.js"
Cohesion: 0.18
Nodes (9): buildDir, child, forwardedSignals, fs, jarDir, jarPath, javaCheck, path (+1 more)

### Community 20 - "EntryPointsFindTool"
Cohesion: 0.50
Nodes (3): EntryPointsFindTool, Element, kotlinx

### Community 21 - "ProguardInspectTool"
Cohesion: 0.39
Nodes (4): kotlinx, ProguardFile, ProguardInspectTool, ProguardRule

### Community 22 - "ManifestInspectTool"
Cohesion: 0.50
Nodes (3): Element, JsonObject, ManifestInspectTool

### Community 23 - "SecurityAuditTool"
Cohesion: 0.46
Nodes (3): kotlinx, SecurityAuditTool, SecurityIssue

### Community 24 - "TestsRunTool"
Cohesion: 0.46
Nodes (3): dev, kotlinx, TestsRunTool

### Community 25 - "GradleDependenciesTreeParser"
Cohesion: 0.52
Nodes (3): ConfigurationDependencies, DependencyNode, GradleDependenciesTreeParser

### Community 26 - "ResourceReferencesTool"
Cohesion: 0.48
Nodes (3): kotlinx, Ref, ResourceReferencesTool

### Community 27 - "GradleTasksParser"
Cohesion: 0.25
Nodes (4): GradleTaskEntry, GradleTasksParser, GradleTasksTool, kotlinx

### Community 29 - "LoginViewModel.kt"
Cohesion: 0.43
Nodes (5): StateFlow, ViewModel, LoginRepository, LoginState, LoginViewModel

### Community 30 - "postinstall.js"
Cohesion: 0.33
Nodes (5): { execSync }, fs, jarDir, jarFiles, path

### Community 37 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 42 - ".sampleAndroidProject"
Cohesion: 0.16
Nodes (4): FixtureProjects, SymbolFindToolTest, TestsDiscoverToolTest, VersionCatalogToolTest

### Community 44 - "Navigation.kt"
Cohesion: 0.83
Nodes (3): AppNavHost(), HomeScreen(), LoginScreen()

### Community 47 - "Troubleshooting"
Cohesion: 0.17
Nodes (12): Android SDK unavailable, Docker build fails to find the JAR, Gradle failure, Java missing or wrong version, Launcher can't locate the JAR, MCP client can't connect, Permission errors, Repository not recognized (+4 more)

### Community 86 - "androidmcp/tools/TestsRunTool.kt"
Cohesion: 0.36
Nodes (4): JunitTestCase, JunitTestSuite, JunitXmlParser, org

### Community 87 - "GradleWrapperLocator"
Cohesion: 0.29
Nodes (3): GradleWrapperLocator, DependenciesInspectTool, kotlinx

### Community 88 - "androidmcp/lint/LintXmlParser.kt"
Cohesion: 0.47
Nodes (3): LintIssue, LintReport, LintXmlParser

### Community 92 - "StaticAnalysisTool.kt"
Cohesion: 0.50
Nodes (3): kotlinx, StaticAnalysisTool, ToolResult

### Community 94 - "Security & Privacy"
Cohesion: 0.18
Nodes (11): Command execution and the one tool that matters most, Credentials and secrets, Network access, npm distribution, Repository permissions in practice, Security & Privacy, Trust boundary, What never leaves the machine (+3 more)

### Community 95 - "README.md"
Cohesion: 0.18
Nodes (10): Architecture, Compatibility, Contributing, Documentation, License, Philosophy, Quick start, Security & local-first (+2 more)

### Community 96 - "Architecture"
Cohesion: 0.22
Nodes (9): Architecture, Error propagation, Execution model for process-backed tools, How it works: one request, end to end, Lifecycle and stdio handshake, Process model, Repository access, The boundary (+1 more)

### Community 97 - "Configuration"
Cohesion: 0.25
Nodes (8): Claude Code, Claude Desktop, Configuration, Cursor, GitHub Copilot (VS Code), No environment-variable configuration, OpenAI Codex CLI, Verifying a configuration

### Community 98 - "Getting Started"
Cohesion: 0.25
Nodes (8): 1. Install, 2. Configure your MCP client, 3. Open an Android repository, 4. Verify the connection, 5. Call your first tool, Getting Started, Prerequisites, What's next

### Community 99 - "Development"
Cohesion: 0.29
Nodes (7): Adding a new MCP tool, Clone and build, Coding conventions observed in this codebase, Development, Project structure, Run locally, Tests

### Community 100 - "Compatibility"
Cohesion: 0.33
Nodes (6): Compatibility, Distribution channels, MCP clients, Platforms, Runtime requirements, Status legend

### Community 101 - "Contributing"
Cohesion: 0.33
Nodes (6): Branching model, Contributing, License, Making a change, Pull request expectations, Reporting issues

### Community 102 - "Scope & Limitations"
Cohesion: 0.40
Nodes (5): Environment limitations, In scope, Known limitations by design, Out of scope, Scope & Limitations

## Knowledge Gaps
- **173 isolated node(s):** `The problem`, `Philosophy`, `Quick start`, `What it can do`, `Compatibility` (+168 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **55 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SymbolReferencesTool` connect `KotlinSourceScanner` to `server/Main.kt`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **Why does `GradleConfigTool` connect `GradlePropertiesParser` to `server/Main.kt`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Why does `TestsRunTool` connect `TestsRunTool` to `androidmcp/tools/TestsRunTool.kt`, `server/Main.kt`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **What connects `The problem`, `Philosophy`, `Quick start` to the rest of the system?**
  _173 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `JsonArray` be split into smaller, more focused modules?**
  _Cohesion score 0.13333333333333333 - nodes in this community are weakly interconnected._
- **Should `bash` be split into smaller, more focused modules?**
  _Cohesion score 0.05714285714285714 - nodes in this community are weakly interconnected._
- **Should `KotlinSourceScanner` be split into smaller, more focused modules?**
  _Cohesion score 0.06976744186046512 - nodes in this community are weakly interconnected._