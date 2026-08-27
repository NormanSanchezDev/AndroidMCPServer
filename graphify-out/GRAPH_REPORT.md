# Graph Report - .  (2026-08-27)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 636 nodes · 875 edges · 86 communities (39 shown, 47 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `583a9283`
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
- TestsDiscoverTool
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

## Communities (86 total, 47 thin omitted)

### Community 0 - "GradleCommandValidator"
Cohesion: 0.05
Nodes (24): GradleCommandValidator, GradleTaskEntry, GradleTasksParser, GradleWrapperLocator, JunitTestCase, JunitTestSuite, JunitXmlParser, org (+16 more)

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
Cohesion: 0.18
Nodes (19): CallToolRequest, CallToolResult, JsonElement, argBool(), argInt(), argList(), args(), argString() (+11 more)

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

### Community 27 - "TestsDiscoverTool"
Cohesion: 0.38
Nodes (3): kotlinx, TestMatch, TestsDiscoverTool

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

## Knowledge Gaps
- **70 isolated node(s):** `$schema`, `model`, `small_model`, `auto`, `prune` (+65 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SymbolReferencesTool` connect `KotlinSourceScanner` to `server/Main.kt`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `GradleConfigTool` connect `GradlePropertiesParser` to `server/Main.kt`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `TestsRunTool` connect `TestsRunTool` to `GradleCommandValidator`, `server/Main.kt`?**
  _High betweenness centrality (0.035) - this node is a cross-community bridge._
- **What connects `$schema`, `model`, `small_model` to the rest of the system?**
  _70 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `GradleCommandValidator` be split into smaller, more focused modules?**
  _Cohesion score 0.05117845117845118 - nodes in this community are weakly interconnected._
- **Should `JsonArray` be split into smaller, more focused modules?**
  _Cohesion score 0.05641025641025641 - nodes in this community are weakly interconnected._
- **Should `bash` be split into smaller, more focused modules?**
  _Cohesion score 0.05714285714285714 - nodes in this community are weakly interconnected._