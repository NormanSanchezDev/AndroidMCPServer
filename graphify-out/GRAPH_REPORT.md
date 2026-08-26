# Graph Report - /Users/normansanchez/AI/projects/concept-test/AndroidCorporateMCP  (2026-08-26)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 343 nodes · 476 edges · 31 communities (25 shown, 6 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS · INFERRED: 1 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- .sampleAndroidProject
- GradleCommandValidator
- KotlinPsiEngine
- .create
- SymbolExtractor.kt
- server/Main.kt
- permission
- bash
- ModuleGraphParser
- ProjectInspectTool
- UserRepository
- LintRunTool
- EntryPointsFindTool
- TestsRunTool
- ManifestInspectTool
- GradleDependenciesTreeParser
- ResourcesInspectTool
- GradleTasksParserTest
- JunitXmlParserTest
- GradleDependenciesTreeParserTest
- gradlew
- LintXmlParserTest
- graphify.js
- KotlinPsiEngineTest

## God Nodes (most connected - your core abstractions)
1. `bash` - 17 edges
2. `permission` - 12 edges
3. `main()` - 10 edges
4. `ProjectInspectTool` - 8 edges
5. `GradleCommandValidator` - 8 edges
6. `ProcessExecutor` - 8 edges
7. `KtTreeVisitorVoid` - 8 edges
8. `UserRepository` - 8 edges
9. `KotlinPsiEngine` - 7 edges
10. `EntryPointsFindTool` - 7 edges

## Surprising Connections (you probably didn't know these)
- `CorporateApplication` --references--> `UserRepository`  [EXTRACTED]
  src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/app/CorporateApplication.kt → src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/data/UserRepository.kt
- `MainActivity` --references--> `UserRepository`  [EXTRACTED]
  src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/app/MainActivity.kt → src/test/resources/fixtures/sample-android-project/app/src/main/java/com/corporate/data/UserRepository.kt

## Import Cycles
- None detected.

## Communities (31 total, 6 thin omitted)

### Community 0 - ".sampleAndroidProject"
Cohesion: 0.06
Nodes (11): JsonArray, FixtureProjects, java, LintRunToolTest, ManifestInspectToolTest, ModuleGraphToolTest, ProjectInspectToolTest, SymbolFindToolTest (+3 more)

### Community 1 - "GradleCommandValidator"
Cohesion: 0.07
Nodes (16): GradleCommandValidator, GradleTaskEntry, GradleTasksParser, GradleWrapperLocator, JunitTestCase, JunitTestSuite, JunitXmlParser, org (+8 more)

### Community 2 - "KotlinPsiEngine"
Cohesion: 0.10
Nodes (13): KotlinCoreEnvironment, PsiElement, PsiErrorElement, KotlinPsiEngine, KtTreeVisitorVoid, KtFile, KtTreeVisitorVoid, KotlinSourceScanner (+5 more)

### Community 3 - ".create"
Cohesion: 0.09
Nodes (5): GradleFixtureProject, EntryPointsFindToolTest, GradleRunToolIntegrationTest, GradleTasksToolIntegrationTest, ResourcesInspectToolTest

### Community 4 - "SymbolExtractor.kt"
Cohesion: 0.17
Nodes (12): KtClass, KtNamedFunction, KtObjectDeclaration, KtParameter, KtProperty, KtTypeAlias, KtFile, KtTreeVisitorVoid (+4 more)

### Community 5 - "server/Main.kt"
Cohesion: 0.25
Nodes (17): CallToolRequest, CallToolResult, JsonElement, argBool(), argInt(), argList(), args(), argString() (+9 more)

### Community 6 - "permission"
Cohesion: 0.11
Nodes (17): compaction, auto, prune, model, permission, doom_loop, edit, external_directory (+9 more)

### Community 7 - "bash"
Cohesion: 0.12
Nodes (17): git add*, git clean*, git commit*, git diff*, git log*, git push*, git reset --hard*, git show* (+9 more)

### Community 8 - "ModuleGraphParser"
Cohesion: 0.14
Nodes (6): ModuleEdge, ModuleGraph, ModuleGraphParser, kotlinx, ModuleGraphTool, ModuleGraphParserTest

### Community 9 - "ProjectInspectTool"
Cohesion: 0.18
Nodes (7): AndroidModuleEvidence, AndroidModuleType, APPLICATION, LIBRARY, UNKNOWN, JsonObject, ProjectInspectTool

### Community 10 - "UserRepository"
Cohesion: 0.23
Nodes (7): AppCompatActivity, Application, Bundle, CorporateApplication, MainActivity, User, UserRepository

### Community 11 - "LintRunTool"
Cohesion: 0.23
Nodes (5): LintIssue, LintReport, LintXmlParser, kotlinx, LintRunTool

### Community 12 - "EntryPointsFindTool"
Cohesion: 0.50
Nodes (3): EntryPointsFindTool, Element, kotlinx

### Community 13 - "TestsRunTool"
Cohesion: 0.46
Nodes (3): latticeMCP, kotlinx, TestsRunTool

### Community 14 - "ManifestInspectTool"
Cohesion: 0.50
Nodes (3): Element, JsonObject, ManifestInspectTool

### Community 15 - "GradleDependenciesTreeParser"
Cohesion: 0.52
Nodes (3): ConfigurationDependencies, DependencyNode, GradleDependenciesTreeParser

### Community 16 - "ResourcesInspectTool"
Cohesion: 0.40
Nodes (3): Element, kotlinx, ResourcesInspectTool

### Community 20 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **35 isolated node(s):** `APPLICATION`, `LIBRARY`, `UNKNOWN`, `$schema`, `model` (+30 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ModuleGraphTool` connect `ModuleGraphParser` to `server/Main.kt`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **Why does `TestsRunTool` connect `TestsRunTool` to `GradleCommandValidator`, `server/Main.kt`?**
  _High betweenness centrality (0.050) - this node is a cross-community bridge._
- **Why does `LintRunTool` connect `LintRunTool` to `server/Main.kt`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **What connects `APPLICATION`, `LIBRARY`, `UNKNOWN` to the rest of the system?**
  _35 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.sampleAndroidProject` be split into smaller, more focused modules?**
  _Cohesion score 0.06312292358803986 - nodes in this community are weakly interconnected._
- **Should `GradleCommandValidator` be split into smaller, more focused modules?**
  _Cohesion score 0.07057057057057058 - nodes in this community are weakly interconnected._
- **Should `KotlinPsiEngine` be split into smaller, more focused modules?**
  _Cohesion score 0.09686609686609686 - nodes in this community are weakly interconnected._