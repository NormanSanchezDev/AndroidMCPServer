# Android Corporate MCP — Implementation Plan

## Current State

**~45% complete.** Core tools work, PSI parsing is real, tests pass. Major gaps in
Gradle intelligence, architecture discovery, and build-tooling integration.

### What works today

| Tool | Status | Notes |
|------|--------|-------|
| `project.inspect` | Working | Module discovery, plugin detection |
| `manifest.inspect` | Working | Full XML parsing, components, permissions |
| `entry_points.find` | Working | Launcher, deep links, exported |
| `symbol.find` | Working | PSI-based declaration search |
| `symbol.references` | Working | Identifier-level occurrence search |
| `gradle.tasks` | Working | Real `gradlew tasks --all` output |
| `gradle.run` | Working | Allow-listed task execution |
| `tests.run` | Working | JUnit XML aggregation + optional trigger |
| `lint.run` | Working | Lint XML aggregation + optional trigger |
| `dependencies.inspect` | Working | Dependency tree parsing |
| `module.graph` | Working | Settings/build file static graph |
| `resources.inspect` | Working | res/ folder enumeration + value names |

---

## Phase 1 — Gradle Intelligence (Priority: HIGH)

The biggest gap. Agents cannot reason about a project without knowing its
actual Gradle configuration.

### 1.1 `gradle.config` — inspect Gradle configuration

**Purpose:** Expose applied plugins, Android SDK versions, Kotlin version,
compile/min/target SDK, build types, product flavors, compose status.

**Implementation:**
- Execute `./gradlew properties --console=plain` or parse `build.gradle.kts`
- Parse `android { compileSdk, minSdk, targetSdk }`
- Detect applied plugins from `plugins { }` block
- Detect Compose: `buildFeatures { compose = true }`
- Detect build types and product flavors
- Return structured JSON evidence

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/GradleConfigTool.kt`

**Parser needed:**
- `src/main/kotlin/dev/normansanchez/androidmcp/gradle/GradlePropertiesParser.kt`

### 1.2 `gradle.versionCatalog` — inspect version catalog

**Purpose:** Parse `gradle/libs.versions.toml` to expose declared
dependencies, versions, and aliases.

**Implementation:**
- Read `gradle/libs.versions.toml`
- Parse `[versions]`, `[libraries]`, `[plugins]` sections
- Return structured catalog with coordinates and version constraints

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/VersionCatalogTool.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/gradle/VersionCatalogParser.kt`

### 1.3 `gradle.conventionPlugins` — detect convention plugins

**Purpose:** Discover and inspect convention plugins in `build-logic/` or
`buildSrc/`.

**Implementation:**
- Walk `build-logic/src/main/kotlin/**/*.kt` and `buildSrc/src/main/kotlin/**/*.kt`
- Extract plugin IDs from `@AutoService(Plugin::class)` or `fun Plugin`
- Map which modules apply which convention plugins

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/ConventionPluginsTool.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/gradle/ConventionPluginScanner.kt`

---

## Phase 2 — Architecture Discovery (Priority: HIGH)

Agents need to understand DI, architecture patterns, and framework usage
without guessing.

### 2.1 `architecture.detect` — detect DI and architecture patterns

**Purpose:** Determine which DI framework is used, whether Compose is
present, common base classes, and reactive stream types.

**Implementation:**
- Scan Kotlin sources for annotations: `@HiltAndroidApp`, `@HiltViewModel`,
  `@Inject`, `import org.koin`, `import org.koin.dsl.module`
- Detect Compose: `import androidx.compose`
- Detect architecture patterns from base classes: `ViewModel`,
  `MviViewModel`, `MoleculeViewModel`
- Detect reactive types: `StateFlow`, `LiveData`, `MutableState`
- Return evidence, never conclusions

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/ArchitectureDetectTool.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/architecture/PatternDetector.kt`

### 2.2 `tests.discover` — find tests related to production code

**Purpose:** Given a class name, find related test files by naming convention
and import analysis.

**Implementation:**
- For a given symbol, search for `*Test.kt`, `*Tests.kt`, `*Spec.kt`
- Check test file imports for references to the production class
- Return test file paths, locations, and whether they are unit/instrumented

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/TestsDiscoverTool.kt`

---

## Phase 3 — Semantic Symbol Analysis (Priority: MEDIUM)

Upgrade from name-level to type-level understanding.

### 3.1 Enhance `symbol.find` with type information

**Purpose:** Return full type signatures, annotations, supertypes, and
constructor parameters.

**Implementation:**
- Extend `SymbolExtractor` to capture:
  - Supertype list (`listOf("ViewModel", "StateFlow<LoginUiState>")`)
  - Annotations (`@HiltViewModel`, `@Inject constructor`)
  - Constructor parameters
  - Visibility modifier
- This is incremental — same PSI visitor, more data collected

**Files to modify:**
- `src/main/kotlin/dev/normansanchez/androidmcp/symbol/SymbolExtractor.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/symbol/KotlinPsiEngine.kt`

### 3.2 `symbol.hierarchy` — class/interface hierarchy

**Purpose:** Given a class name, show its supertypes and subtypes across the
codebase.

**Implementation:**
- Parse all KtClass declarations, collect supertype references
- Build a map: class → supertypes, supertype → subtypes
- Return hierarchy tree rooted at the requested symbol

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/SymbolHierarchyTool.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/symbol/HierarchyBuilder.kt`

---

## Phase 4 — Build Validation & Execution (Priority: MEDIUM)

Safer, more granular build operations.

### 4.1 `build.validate` — compile a specific module

**Purpose:** Execute `./gradlew :module:compileDebugKotlin` and return
structured results.

**Implementation:**
- Wrap `gradle.run` with a focus on compilation tasks
- Parse compiler warnings from stderr
- Return exit code, duration, warning count, error details

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/BuildValidateTool.kt`

### 4.2 `staticAnalysis.run` — Detekt, ktlint, Kover

**Purpose:** Execute configured static analysis tools and aggregate results.

**Implementation:**
- Detect which tools are configured (check plugins, Gradle tasks)
- Execute: `detekt`, `ktlintCheck`, `koverHtmlReport`
- Parse outputs into structured evidence
- Return issues by severity with file:line references

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/StaticAnalysisTool.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/staticanalysis/DetektParser.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/staticanalysis/KtlintParser.kt`

---

## Phase 5 — Android Deep Inspection (Priority: MEDIUM)

### 5.1 `manifest.merge` — inspect manifest merge conflicts

**Purpose:** Detect merge conflicts across module manifests using
`./gradlew :app:processDebugMainManifest --dry-run` or merged manifest
inspection.

**Implementation:**
- Read merged manifest from `build/intermediates/merged_manifest/`
- Compare with source manifests
- Report conflicts and overrides

### 5.2 `navigation.graph` — inspect NavHost destinations

**Purpose:** Parse navigation XML graphs or Compose navigation routes.

**Implementation:**
- Find `navigation/*.xml` files, parse `<fragment>`, `<action>`, `<deepLink>`
- Detect Compose Navigation: `NavHost`, `composable("route")`
- Return destination tree with deep links

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/NavigationGraphTool.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/navigation/NavXmlParser.kt`
- `src/main/kotlin/dev/normansanchez/androidmcp/navigation/ComposeNavDetector.kt`

### 5.3 `resource.references` — trace resource usage

**Purpose:** Given a resource name (e.g. `R.string.login_title`), find where
it is referenced in code and XML.

**Implementation:**
- Regex search for `R.type.name` in Kotlin/Java sources
- Search for `@type/name` in XML layouts
- Return file:line evidence for each reference

**Files to create:**
- `src/main/kotlin/dev/normansanchez/androidmcp/tools/ResourceReferencesTool.kt`

---

## Phase 6 — Security & Compliance (Priority: LOW)

### 6.1 `security.audit` — basic security checks

**Purpose:** Detect common security issues in Android projects.

**Implementation:**
- Check for `android:exported="true"` without intent filters
- Detect `allowBackup="true"` in manifest
- Check for `usesCleartextTraffic="true"`
- Detect hardcoded secrets in `BuildConfig` or source
- Check ProGuard/R8 is enabled for release

### 6.2 `proguard.inspect` — inspect R8/ProGuard configuration

**Purpose:** Parse ProGuard rules and report keep annotations, warnings.

---

## Phase 7 — Integration & Packaging (Priority: LOW)

### 7.1 Docker support

- Create `Dockerfile` for containerized deployment
- Package as fat JAR with `shadow` plugin
- Support `docker run` with volume mount to target repo

### 7.2 CI/CD integration

- GitHub Action for automated MCP validation
- Test against popular open-source Android projects
- Publish to MCP registry

### 7.3 Multi-client documentation

- Claude Desktop configuration example
- VS Code Copilot configuration
- OpenCode configuration
- Cursor configuration

---

## File Structure After Phase 1-3

```
src/main/kotlin/dev/normansanchez/androidmcp/
├── server/Main.kt
├── client/Main.kt
├── model/
│   ├── AndroidModuleEvidence.kt
│   ├── AndroidModuleType.kt
│   └── SymbolEntry.kt                 (move from symbol/)
├── process/
│   └── ProcessExecutor.kt
├── gradle/
│   ├── GradleCommandValidator.kt
│   ├── GradleTasksParser.kt
│   ├── GradleWrapperLocator.kt
│   ├── GradlePropertiesParser.kt      (NEW - 1.1)
│   ├── VersionCatalogParser.kt        (NEW - 1.2)
│   └── ConventionPluginScanner.kt     (NEW - 1.3)
├── symbol/
│   ├── KotlinPsiEngine.kt
│   ├── KotlinSourceScanner.kt
│   ├── SymbolExtractor.kt             (enhanced - 3.1)
│   └── HierarchyBuilder.kt            (NEW - 3.2)
├── architecture/                       (NEW - 2.1)
│   └── PatternDetector.kt
├── dependencies/
│   └── GradleDependenciesTreeParser.kt
├── graph/
│   └── ModuleGraphParser.kt
├── junit/
│   └── JunitXmlParser.kt
├── lint/
│   └── LintXmlParser.kt
├── navigation/                         (NEW - 5.2)
│   ├── NavXmlParser.kt
│   └── ComposeNavDetector.kt
├── staticanalysis/                     (NEW - 4.2)
│   ├── DetektParser.kt
│   └── KtlintParser.kt
└── tools/
    ├── ProjectInspectTool.kt
    ├── ManifestInspectTool.kt
    ├── EntryPointsFindTool.kt
    ├── SymbolFindTool.kt              (enhanced - 3.1)
    ├── SymbolReferencesTool.kt
    ├── SymbolHierarchyTool.kt         (NEW - 3.2)
    ├── GradleTasksTool.kt
    ├── GradleRunTool.kt
    ├── GradleConfigTool.kt            (NEW - 1.1)
    ├── VersionCatalogTool.kt          (NEW - 1.2)
    ├── ConventionPluginsTool.kt       (NEW - 1.3)
    ├── TestsRunTool.kt
    ├── TestsDiscoverTool.kt           (NEW - 2.2)
    ├── LintRunTool.kt
    ├── DependenciesInspectTool.kt
    ├── ModuleGraphTool.kt
    ├── ResourcesInspectTool.kt
    ├── ResourceReferencesTool.kt      (NEW - 5.3)
    ├── ArchitectureDetectTool.kt      (NEW - 2.1)
    ├── BuildValidateTool.kt           (NEW - 4.1)
    ├── StaticAnalysisTool.kt          (NEW - 4.2)
    ├── NavigationGraphTool.kt         (NEW - 5.2)
    └── SecurityAuditTool.kt           (NEW - 6.1)
```

---

## Execution Order

| Phase | Effort | Dependency | Estimated Time |
|-------|--------|------------|----------------|
| 1.1 Gradle Config | Medium | None | 2-3 hours |
| 1.2 Version Catalog | Low | None | 1-2 hours |
| 1.3 Convention Plugins | Medium | None | 2-3 hours |
| 2.1 Architecture Detect | Medium | Phase 3.1 | 3-4 hours |
| 2.2 Tests Discover | Low | None | 1-2 hours |
| 3.1 Symbol Enhancement | Medium | None | 3-4 hours |
| 3.2 Symbol Hierarchy | Medium | Phase 3.1 | 2-3 hours |
| 4.1 Build Validate | Low | None | 1-2 hours |
| 4.2 Static Analysis | Medium | None | 3-4 hours |
| 5.1 Manifest Merge | Low | None | 2-3 hours |
| 5.2 Navigation Graph | Medium | None | 3-4 hours |
| 5.3 Resource References | Low | None | 1-2 hours |
| 6.1 Security Audit | Low | None | 2-3 hours |
| 6.2 ProGuard Inspect | Low | None | 1-2 hours |
| 7.1 Docker | Low | All phases | 2-3 hours |
| 7.2 CI/CD | Low | 7.1 | 2-3 hours |
| 7.3 Multi-client docs | Low | 7.1 | 1-2 hours |

---

## Testing Strategy

Every new tool must include:

1. **Unit test** with fixture data (no Gradle execution)
2. **Integration test** against `src/test/resources/fixtures/sample-android-project/`
3. **Input validation** — invalid paths, missing files, empty results
4. **Security checks** — no path traversal, no secret exposure

Test naming convention:
```
src/test/kotlin/dev/normansanchez/androidmcp/tools/{ToolName}ToolTest.kt
src/test/kotlin/dev/normansanchez/androidmcp/{domain}/{ParserName}Test.kt
```

---

## Versioning

After Phase 1-3 complete: release `0.2.0` with 15+ tools.
After Phase 4-5 complete: release `0.3.0` with 20+ tools.
After Phase 6-7 complete: release `1.0.0` production-ready.
