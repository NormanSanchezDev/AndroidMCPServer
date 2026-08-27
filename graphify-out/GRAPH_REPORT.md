# Graph Report - AndroidCorporateMCP  (2026-08-27)

## Corpus Check
- 114 files · ~39,540 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 638 nodes · 876 edges · 94 communities (43 shown, 51 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `94fc5086`
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
- .sampleAndroidProject
- ModuleGraphParser
- ProjectInspectTool
- ManifestMergeTool
- VersionCatalogParser
- HierarchyBuilder.kt
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
- FixtureProjects
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
- TestsDiscoverToolTest
- VersionCatalogToolTest
- Navigation.kt
- Application
- graphify.js
- KotlinPsiEngineTest
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

## God Nodes (most connected - your core abstractions)
1. `bash` - 17 edges
2. `GradlePropertiesParser` - 15 edges
3. `permission` - 12 edges
4. `GradleCommandValidator` - 10 edges
5. `ProcessExecutor` - 10 edges
6. `main()` - 10 edges
7. `KotlinSourceScanner` - 9 edges
8. `ManifestMergeTool` - 9 edges
9. `ProjectInspectTool` - 9 edges
10. `keywords` - 8 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (94 total, 51 thin omitted)

### Community 0 - "GradleCommandValidator"
Cohesion: 0.22
Nodes (3): GradleCommandValidator, GradleRunTool, kotlinx

### Community 1 - "JsonArray"
Cohesion: 0.06
Nodes (10): JsonArray, BuildValidateToolTest, DependenciesInspectToolTest, LintRunToolTest, ManifestInspectToolTest, ManifestMergeToolTest, ModuleGraphToolTest, ProjectInspectToolTest (+2 more)

### Community 2 - "bash"
Cohesion: 0.06
Nodes (34): git add*, git clean*, git commit*, git diff*, git log*, git push*, git reset --hard*, git show* (+26 more)

### Community 3 - "KotlinSourceScanner"
Cohesion: 0.10
Nodes (14): ComposeNavDetector, ComposeRoute, NavAction, NavArgument, NavDestination, NavGraph, NavXmlParser, KotlinSourceScanner (+6 more)

### Community 4 - "package.json"
Cohesion: 0.07
Nodes (26): author, bin, android-corporate-mcp, description, engines, node, files, keywords (+18 more)

### Community 5 - "androidmcp/symbol/SymbolExtractor.kt"
Cohesion: 0.13
Nodes (14): KtNamedFunction, KtObjectDeclaration, KtParameter, KtProperty, KtTypeAlias, KtClass, KtFile, KtTreeVisitorVoid (+6 more)

### Community 6 - "server/Main.kt"
Cohesion: 0.16
Nodes (20): CallToolRequest, CallToolResult, JsonElement, argBool(), argInt(), argList(), args(), argString() (+12 more)

### Community 7 - ".create"
Cohesion: 0.10
Nodes (5): GradleFixtureProject, EntryPointsFindToolTest, GradleRunToolIntegrationTest, GradleTasksToolIntegrationTest, ResourcesInspectToolTest

### Community 8 - "GradlePropertiesParser"
Cohesion: 0.18
Nodes (4): GradleConfig, GradlePropertiesParser, GradleConfigTool, kotlinx

### Community 9 - ".sampleAndroidProject"
Cohesion: 0.20
Nodes (3): ConventionPluginsToolTest, ProguardInspectToolTest, SymbolFindToolTest

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

### Community 14 - "HierarchyBuilder.kt"
Cohesion: 0.23
Nodes (8): HierarchyBuilder, KtTreeVisitorVoid, HierarchyEntry, HierarchyNode, KtClass, KtTreeVisitorVoid, kotlinx, SymbolHierarchyTool

### Community 15 - "androidmcp/symbol/KotlinPsiEngine.kt"
Cohesion: 0.23
Nodes (7): KotlinCoreEnvironment, PsiElement, PsiErrorElement, KotlinPsiEngine, KtTreeVisitorVoid, KtFile, KtTreeVisitorVoid

### Community 16 - "MainActivity.kt"
Cohesion: 0.29
Nodes (9): Bundle, ComponentActivity, StateFlow, ViewModel, LoginUiState, MainActivity, MainViewModel, User (+1 more)

### Community 17 - "ConventionPluginScanner"
Cohesion: 0.27
Nodes (4): ConventionPlugin, ConventionPluginScanner, ConventionPluginsTool, kotlinx

### Community 18 - "ArchitectureDetectTool.kt"
Cohesion: 0.33
Nodes (6): ArchitectureEvidence, ArchitectureResult, PatternDetector, SourceFile, ArchitectureDetectTool, kotlinx

### Community 19 - "android-corporate-mcp.js"
Cohesion: 0.22
Nodes (8): buildDir, fs, jarDir, jarFile, jarPath, java, path, { spawn }

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

### Community 44 - "Navigation.kt"
Cohesion: 0.83
Nodes (3): AppNavHost(), HomeScreen(), LoginScreen()

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

## Knowledge Gaps
- **71 isolated node(s):** `graphify`, `$schema`, `model`, `small_model`, `auto` (+66 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SymbolReferencesTool` connect `KotlinSourceScanner` to `server/Main.kt`?**
  _High betweenness centrality (0.060) - this node is a cross-community bridge._
- **Why does `GradleConfigTool` connect `GradlePropertiesParser` to `server/Main.kt`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `TestsRunTool` connect `TestsRunTool` to `androidmcp/tools/TestsRunTool.kt`, `server/Main.kt`?**
  _High betweenness centrality (0.035) - this node is a cross-community bridge._
- **What connects `graphify`, `$schema`, `model` to the rest of the system?**
  _71 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `JsonArray` be split into smaller, more focused modules?**
  _Cohesion score 0.05641025641025641 - nodes in this community are weakly interconnected._
- **Should `bash` be split into smaller, more focused modules?**
  _Cohesion score 0.05714285714285714 - nodes in this community are weakly interconnected._
- **Should `KotlinSourceScanner` be split into smaller, more focused modules?**
  _Cohesion score 0.09788359788359788 - nodes in this community are weakly interconnected._