# Tools Reference

This is a complete inventory of the 25 MCP tools registered in `src/main/kotlin/dev/normansanchez/androidmcp/server/Main.kt`, generated from the actual tool implementations under `src/main/kotlin/dev/normansanchez/androidmcp/tools/`. Nothing here is aspirational — if a tool isn't listed, it isn't registered. Planned-but-unimplemented ideas are listed separately at the bottom under [Planned / Not Yet Available](#planned--not-yet-available).

Every tool accepts a `projectRoot` argument (absolute path to the target Android/Gradle project) unless noted, and returns a single JSON object as its result text. Every response includes a `"status"` field; see each tool's **Failure modes** for the non-`"success"` values it can return.

## Side-effect legend

- **read-only** — filesystem reads and in-process parsing only. No subprocess is started.
- **executes Gradle** — spawns `./gradlew <task>` via `ProcessBuilder` (list-args, no shell). May write build outputs (compiled classes, reports, caches) as a side effect of Gradle itself, not of the tool's own code.

No tool in this server writes source files or deletes anything directly. `gradle.run` is the exception worth reading carefully — see its entry below.

---

## Project & Kotlin

### project.inspect

**Purpose.** Entry point for "is this even an Android project, and what's in it." Finds the Gradle settings file, walks up to 3 directory levels for `build.gradle(.kts)` files, and classifies each as `application`/`library`/`unknown` by checking for the Android Gradle Plugin ID (Kotlin DSL, Groovy DSL, and version-catalog `alias(...)` forms are all recognized).

**Input**

| Field         | Type   | Required                     | Description                       |
|---------------|--------|------------------------------|-----------------------------------|
| `projectRoot` | string | no (empty string if omitted) | Absolute path to the project root |

**Output.** `status`, `projectRoot`, `gradleProject` (bool), `settingsFile` (relative path or `null`), `androidProject` (bool — true if any module is `application`/`library`), `modules[]` (`name`, `path`, `type`, `buildFile`, `manifest`), `evidence[]` (one entry per detected AGP plugin and manifest, each pointing at the file that justified it).

**Example**

```json
{
  "status": "success",
  "androidProject": true,
  "modules": [
    { "name": "app", "path": "app", "type": "application", "buildFile": "app/build.gradle.kts", "manifest": "app/src/main/AndroidManifest.xml" }
  ]
}
```

**Evidence.** Direct filesystem read of `settings.gradle(.kts)` and every `build.gradle(.kts)` up to depth 3; text-matches AGP plugin declarations.

**Side effects.** read-only.

**Failure modes.** `invalid_project` if the path doesn't exist or isn't a directory.

---

### manifest.inspect

**Purpose.** Full structural parse of one module's `AndroidManifest.xml` — package, application class, permissions, activities (with intent filters), services, receivers, providers.

**Input**

| Field         | Type   | Required             | Description                       |
|---------------|--------|----------------------|-----------------------------------|
| `projectRoot` | string | no                   | Absolute path to the project root |
| `module`      | string | no (default `"app"`) | Module path relative to root      |

**Output.** `status`, `manifest` (relative path), `package`, `applicationClass`, `permissions[]`, `activities[]` (`name`, `exported`, `intentFilters[]` with `actions[]`/`categories[]`), `services[]`, `receivers[]`, `providers[]` (each with `name`, `exported`).

**Evidence.** `javax.xml.parsers.DocumentBuilderFactory` (namespace-aware) parse of `<module>/src/main/AndroidManifest.xml`. Reads the `android:` namespace attributes directly off the DOM — no regex.

**Side effects.** read-only.

**Failure modes.** `not_found` if the manifest doesn't exist at that path.

---

### entry_points.find

**Purpose.** Derives the app's actual entry surface from manifest evidence: launcher activities (`MAIN`/`LAUNCHER` or `LEANBACK_LAUNCHER` intent filters), deep links (`VIEW`/`BROWSABLE` filters, with scheme/host and `autoVerify`), and a count of exported components.

**Input**

| Field         | Type   | Required             | Description                       |
|---------------|--------|----------------------|-----------------------------------|
| `projectRoot` | string | no                   | Absolute path to the project root |
| `module`      | string | no (default `"app"`) | Module path relative to root      |

**Output.** `status`, `manifest`, `launchers[]` (`component`, `kind`), `deepLinks[]` (`component`, `schemes[]`, `hosts[]`, `autoVerify`), `exportedComponentCount`.

**Evidence.** Same manifest DOM parse as `manifest.inspect`, filtered to launcher/deep-link intent-filter patterns.

**Side effects.** read-only.

**Failure modes.** `not_available` if no manifest is found for the module.

---

### symbol.find

**Purpose.** Finds Kotlin declarations by name — classes, interfaces, enums, objects, functions, properties, typealiases — across the project's Kotlin source sets, using the Kotlin compiler's real parser (PSI), not text search.

**Input**

| Field          | Type    | Required                          | Description                                                                        |
|----------------|---------|-----------------------------------|------------------------------------------------------------------------------------|
| `projectRoot`  | string  | no                                | Absolute path to the project root                                                  |
| `query`        | string  | no (empty fails validation below) | Symbol name to search for (substring match by default)                             |
| `kind`         | string  | no                                | One of `class`, `interface`, `enum`, `object`, `function`, `property`, `typealias` |
| `exactMatch`   | boolean | no (default `false`)              | Exact name match instead of substring                                              |
| `includeTests` | boolean | no (default `false`)              | Include `src/test`/`src/androidTest` source sets                                   |
| `maxResults`   | integer | no (default `50`)                 | Cap on returned matches                                                            |

**Output.** `status`, `scannedFileCount`, `matchCount`, `matches[]` (`name`, `kind`, `fqName`, `container`, `file`, `line`), `matchesTruncated` (bool).

**Evidence.** PSI parse of every `.kt` file in the resolved source sets (`KotlinSourceScanner` + `SymbolExtractor`).

**Side effects.** read-only.

**Failure modes.** `invalid_project`; `invalid_request` if `query` is blank; `invalid_kind` if `kind` isn't one of the seven recognized values (response includes the valid list).

---

### symbol.references

**Purpose.** Identifier-level occurrence search for one symbol name — separates the declaration site(s) from every other place the identifier appears.

**Input**

| Field          | Type    | Required                        | Description                       |
|----------------|---------|---------------------------------|-----------------------------------|
| `projectRoot`  | string  | no                              | Absolute path to the project root |
| `symbolName`   | string  | no (must be a valid identifier) | Identifier to locate              |
| `includeTests` | boolean | no (default `false`)            | Include test source sets          |
| `maxResults`   | integer | no (default `200`)              | Cap on returned references        |

**Output.** `status`, `scannedFileCount`, `declarationCount`, `referenceCount` (total found, independent of truncation), `declarations[]` (`file`, `line`, `container`), `references[]` (`file`, `line`, capped to `maxResults`), `referencesTruncated`, and a `limitation` string that's part of the actual response: *"Identifier-level analysis within the module source sets; occurrences with the same name are reported even when they refer to different types."*

**Evidence.** PSI-based declaration extraction plus a whole-word regex (`\bidentifier\b`) scan of file contents for every other occurrence.

**Side effects.** read-only.

**Failure modes.** `invalid_project`; `invalid_request` if `symbolName` isn't a plain identifier (`^[A-Za-z_][A-Za-z0-9_]*$`).

---

### module.graph

**Purpose.** Static (no build execution) module dependency graph, parsed from `settings.gradle(.kts)` `include(...)` statements and each module's `project(...)`/`project(path = ...)` references in its build file.

**Input**

| Field         | Type   | Required | Description                       |
|---------------|--------|----------|-----------------------------------|
| `projectRoot` | string | no       | Absolute path to the project root |

**Output.** `status`, `rootProjectName`, `moduleCount`, `modules[]` (list of module paths), `edges[]` (`from`, `to`, `configuration` — e.g. `implementation`/`api`).

**Evidence.** Text parse of `settings.gradle(.kts)` and every included module's build file (`ModuleGraphParser`). A module whose build file can't be parsed contributes no edges but isn't dropped from `modules`.

**Side effects.** read-only.

**Failure modes.** `not_available` if no `settings.gradle(.kts)` exists at the root.

---

### resources.inspect

**Purpose.** Enumerates a module's `res/` tree — folder names, per-folder file counts, DPI-qualifier buckets, and (optionally) the actual names declared inside `values*/` XML files (strings, colors, dimens, etc., grouped by XML tag).

**Input**

| Field          | Type    | Required             | Description                       |
|----------------|---------|----------------------|-----------------------------------|
| `projectRoot`  | string  | no                   | Absolute path to the project root |
| `module`       | string  | no (default `"app"`) | Module path relative to root      |
| `includeNames` | boolean | no (default `true`)  | Include value-resource names      |

**Output.** `status`, `resDirectory`, `folderCount`, `fileCount`, `folders[]` (`name`, `type`, `qualifier`, `dpiBucket` if applicable, `fileCount`), and — when `includeNames` is true and any `values*` folder exists — `valueItems[]` (`tag`, `count`, `names[]` capped at 300 per tag, `namesTruncated`).

**Evidence.** Directory listing of `<module>/src/main/res`; XML parse of each `values*/*.xml` file for name collection.

**Side effects.** read-only.

**Failure modes.** `not_available` if the module has no `src/main/res` directory.

---

## Gradle Execution

### gradle.tasks

**Purpose.** The real task list for a project or module, from Gradle itself (not inferred).

**Input**

| Field            | Type    | Required           | Description                                  |
|------------------|---------|--------------------|----------------------------------------------|
| `projectRoot`    | string  | no                 | Absolute path to the project root            |
| `module`         | string  | no                 | Optional module to scope tasks (e.g. `:app`) |
| `timeoutSeconds` | integer | no (default `300`) | Execution timeout                            |

**Output.** `status`, `command` (the exact command line run), `durationMs`, `taskCount`, `tasks[]` (`name`, `group`, `description`).

**Evidence.** Runs `./gradlew [:<module>:]tasks --all` and parses stdout (`GradleTasksParser`).

**Side effects.** executes Gradle.

**Failure modes.** `gradle_not_available` (no `gradlew`); `timeout`; `gradle_error` (non-zero exit, includes `stderr` and `exitCode`).

---

### gradle.run

**Purpose.** Runs Gradle tasks the caller names, with output and exit code captured. This is the most powerful tool in the server and the one most worth reading carefully — see the caveat below.

**Input**

| Field            | Type     | Required                         | Description                                              |
|------------------|----------|----------------------------------|----------------------------------------------------------|
| `projectRoot`    | string   | no                               | Absolute path to the project root                        |
| `tasks`          | string[] | no (empty list fails validation) | Task names to execute                                    |
| `flags`          | string[] | no                               | Flags to pass; must all be in the fixed allow-list below |
| `timeoutSeconds` | integer  | no (default `600`)               | Execution timeout                                        |

**Output.** `status` (`"executed"` or `"timeout"`), `command`, `exitCode` (or `null`), `success` (bool: exit code 0 and not timed out), `durationMs`, `stdout`/`stderr` (each capped at 40,000 characters), `stdoutTruncated`.

> **Caveat: the MCP tool description calls this "allow-listed Gradle tasks," but the actual validation (`GradleCommandValidator.validateTaskName`) is a *syntax* regex — `^[a-zA-Z][a-zA-Z0-9]*(?::[a-zA-Z][a-zA-Z0-9]*)*$` — not a curated list of specific safe task names.** Any syntactically valid Gradle task path passes, including `clean`, `publish`, `assembleRelease`, or a custom task defined in the target project. Only the **flags** are a true fixed allow-list: `--parallel`, `--build-cache`, `--configuration-cache`, `--continue`, `--no-daemon`, `--info`, `--quiet`, `--console=plain`. In practice, calling `gradle.run` grants the same blast radius as a developer running `./gradlew <task>` by hand in that project. See [security.md](security.md).

**Evidence.** N/A (this tool executes rather than observes).

**Side effects.** executes Gradle — and, depending on the task named, can compile, assemble, publish, or run any other Gradle-defined behavior in the target project.

**Failure modes.** `invalid_project`; `invalid_request` (no tasks given); `invalid_task` (fails the syntax regex); `invalid_flag` (not in the allow-list; response includes `allowedFlags`); `gradle_not_available`.

---

### tests.run

**Purpose.** Aggregates existing JUnit XML reports (`build/test-results/{testDebugUnitTest,testReleaseUnitTest,test}`) into pass/fail/skip counts and failure details. Can optionally run the tests first if no reports exist yet.

**Input**

| Field            | Type    | Required             | Description                                                                 |
|------------------|---------|----------------------|-----------------------------------------------------------------------------|
| `projectRoot`    | string  | no                   | Absolute path to the project root                                           |
| `module`         | string  | no                   | Optional module scope                                                       |
| `task`           | string  | no                   | Gradle task to use when `trigger=true` (default `test` or `:<module>:test`) |
| `trigger`        | boolean | no (default `false`) | Execute the Gradle test task first if no reports are found                  |
| `timeoutSeconds` | integer | no (default `600`)   | Timeout, used only when triggering                                          |

**Output.** `status`, `reportCount`, `totalTests`, `passed`, `failed`, `skipped`, `allPassed`, `suites[]` (`name`, `tests`, `failures`, `errors`, `skipped`, `timeSeconds`, and `failuresDetail[]` — `classname`, `test`, `message`, `type` — for any suite with failures).

**Evidence.** Parses existing `TEST-*.xml` files (`JunitXmlParser`) under the standard Gradle test-report directories. Does not itself decide whether tests "should" pass.

**Side effects.** read-only unless `trigger: true` and no reports exist yet, in which case it executes Gradle (`<module>:test` or the given `task`).

**Failure modes.** `invalid_project`; `invalid_module`; `not_available` (no reports found, `trigger` was false or found nothing new — response includes `searchedPaths` and a hint); `gradle_not_available` / `invalid_task` / `timeout` / `gradle_error` when triggering fails.

---

### lint.run

**Purpose.** Same pattern as `tests.run`, for Android Lint: reads the most recently modified `lint-results*.xml` under `<module>/build/reports`, aggregates by severity, optionally triggers the lint task first.

**Input**

| Field            | Type    | Required             | Description                                                          |
|------------------|---------|----------------------|----------------------------------------------------------------------|
| `projectRoot`    | string  | no                   | Absolute path to the project root                                    |
| `module`         | string  | no                   | Optional module scope                                                |
| `task`           | string  | no                   | Gradle task when `trigger=true` (default `lint` or `:<module>:lint`) |
| `trigger`        | boolean | no (default `false`) | Execute the lint task first if no report is found                    |
| `timeoutSeconds` | integer | no (default `900`)   | Timeout, used only when triggering                                   |

**Output.** `status`, `report` (relative path), `lintVersion`, `issueCount`, `fatal`, `errors`, `warnings`, `informational`, `allClean`, `issues[]` (`id`, `severity`, `message`, `category`, `file`, `line`; capped at 200), `issuesTruncated`.

**Evidence.** Parses the newest matching `lint-results*.xml` (`LintXmlParser`). Note the tool deliberately treats a non-zero Lint exit code as still-valid evidence when triggering (Lint exits non-zero on `abortOnError`, but the report itself is complete) — only a timeout short-circuits with an error before the report is read.

**Side effects.** read-only unless `trigger: true` and no report exists yet.

**Failure modes.** `invalid_project`; `invalid_module`; `not_available` (no report / unreadable / not valid lint XML); `gradle_not_available` / `invalid_task` / `timeout` when triggering fails.

---

### dependencies.inspect

**Purpose.** Runs and parses `./gradlew [:<module>:]dependencies` into structured configurations, direct dependency coordinates, requested-vs-resolved version conflicts, and project-dependency edges.

**Input**

| Field            | Type    | Required           | Description                                                                                              |
|------------------|---------|--------------------|----------------------------------------------------------------------------------------------------------|
| `projectRoot`    | string  | no                 | Absolute path to the project root                                                                        |
| `module`         | string  | no                 | Optional module (root project if omitted)                                                                |
| `configuration`  | string  | no                 | Limit to one configuration (e.g. `releaseRuntimeClasspath`); validated against `^[a-zA-Z][a-zA-Z0-9_]*$` |
| `timeoutSeconds` | integer | no (default `300`) | Execution timeout                                                                                        |

**Output.** `status`, `module`, `command`, `configurations[]` — each with `name`, `description`, `dependencyCount`, `conflicts` (count omitted-by-conflict entries), and `directDependencies[]` (`coordinate`, `requestedVersion`, `omittedByConflict`, `repeatedSubtreeOmitted`).

**Evidence.** Real `./gradlew dependencies` output, parsed by `GradleDependenciesTreeParser` (handles version-conflict arrows `req -> resolved`, `(*)` repeated-subtree markers, `(c)` constraint lines which are dropped, and `(n)` unresolved markers).

**Side effects.** executes Gradle.

**Failure modes.** `invalid_project`; `invalid_module`; `invalid_configuration`; `gradle_not_available`; `timeout`; `gradle_error`.

---

## Gradle Intelligence

### gradle.config

**Purpose.** Static configuration snapshot for one module: applied plugins, SDK versions, Compose status, build types, product flavors, Java/Kotlin/AGP versions — without running Gradle.

**Input**

| Field         | Type   | Required                                               | Description                       |
|---------------|--------|--------------------------------------------------------|-----------------------------------|
| `projectRoot` | string | no                                                     | Absolute path to the project root |
| `module`      | string | no (defaults to `app` if it exists, else project root) | Module path relative to root      |

**Output.** `status`, `module`, `plugins[]`, `compileSdk`, `minSdk`, `targetSdk`, `composeEnabled`, `buildTypes[]`, `productFlavors[]`, `javaVersion`, `namespace`, `kotlinVersion`, `androidGradlePluginVersion` (any of the scalar fields may be `null` if not declared or not detectable from the build file text).

**Evidence.** `GradlePropertiesParser` reads and pattern-matches the module's build file plus root-level `gradle.properties`/version references.

**Side effects.** read-only.

**Failure modes.** `invalid_project`; `module_not_found`.

---

### gradle.versionCatalog

**Purpose.** Parses `gradle/libs.versions.toml` into its declared versions, libraries, and plugins.

**Input**

| Field         | Type   | Required | Description                       |
|---------------|--------|----------|-----------------------------------|
| `projectRoot` | string | no       | Absolute path to the project root |

**Output.** `status`, `versions[]` (`name`, `value`), `libraries[]` (`alias`, `group`, `name`, `version`, `versionRef`), `plugins[]` (`id`, `version`, `versionRef`).

**Evidence.** TOML parse via `VersionCatalogParser`.

**Side effects.** read-only.

**Failure modes.** `not_available` if `gradle/libs.versions.toml` doesn't exist.

---

### gradle.conventionPlugins

**Purpose.** Discovers convention plugins defined in `build-logic/` or `buildSrc/` and which modules apply each one.

**Input**

| Field         | Type   | Required | Description                       |
|---------------|--------|----------|-----------------------------------|
| `projectRoot` | string | no       | Absolute path to the project root |

**Output.** `status`, `pluginCount`, `plugins[]` (`id`, `className`, `file`, `appliedBy[]`).

**Evidence.** `ConventionPluginScanner` scan of `build-logic/`/`buildSrc/` plugin declarations plus a cross-reference against every module's build file for `id("...")` usage.

**Side effects.** read-only.

**Failure modes.** `not_available` if no convention plugins are found in either directory.

---

## Architecture & Symbols

### architecture.detect

**Purpose.** Pattern-based detection of DI framework, Compose usage, ViewModel pattern, and reactive stream types, from Kotlin source — with each finding backed by the specific evidence line that triggered it.

**Input**

| Field         | Type   | Required | Description                       |
|---------------|--------|----------|-----------------------------------|
| `projectRoot` | string | no       | Absolute path to the project root |

**Output.** `status`, `filesScanned`, `diFramework` (or `null`), `usesCompose` (bool), `viewModelPattern` (or `null`), `reactiveTypes[]`, `evidenceCount`, `evidence[]` (`category`, `name`, `evidence`, `file`, `line` — capped at 50 total across all categories).

**Evidence.** `PatternDetector` scanning non-test Kotlin source (`KotlinSourceScanner`, `includeTests = false`) for known markers (annotations, imports, base-class names).

**Side effects.** read-only.

**Failure modes.** `invalid_project`; `not_available` if no Kotlin source files are found.

---

### tests.discover

**Purpose.** Finds test files that plausibly test a given production class, by filename convention (`<Class>Test.kt`, `<Class>Tests.kt`, `<Class>Spec.kt`, case-insensitive variants), classified as `unit`/`instrumented`/`unknown` by source-set path, with a note on whether the file actually references the class name.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |
| `className` | string | **yes** | Production class name to find tests for |

**Output.** `status`, `className`, `testCount`, `tests[]` (`file`, `type`, `referencesProduction`, `lineCount`).

**Evidence.** Filename-pattern walk (depth 8) plus a plain substring check of file content for the class name.

**Side effects.** read-only.

**Failure modes.** `invalid_project`; `invalid_request` if `className` isn't a valid identifier.

---

### symbol.hierarchy

**Purpose.** Class/interface hierarchy (supertypes and subtypes) for a named class, built across the whole non-test codebase.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |
| `className` | string | **yes** | Class or interface name |

**Output.** `status`, `className`, `totalClasses` (total classes indexed, for context), `hierarchy` (a recursive node: `name`, `kind`, `supertypes[]`, `file`, `line`, `children[]`).

**Evidence.** `HierarchyBuilder` over PSI-extracted declarations from `KotlinSourceScanner` (non-test source only).

**Side effects.** read-only.

**Failure modes.** `invalid_project`; `not_found` if the class name isn't present in the indexed declarations; `error` if tree construction fails after the name was found.

---

## Build Validation

### build.validate

**Purpose.** Compiles one module (`:<module>:compileDebugKotlin`) and returns structured warnings/errors extracted from the compiler's stderr, rather than raw log text.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |
| `module` | string | no (default `"app"`) | Module to compile |
| `timeoutSeconds` | integer | no (default `600`) | Execution timeout |

**Output.** `status`, `command`, `exitCode`, `durationMs`, `success` (exit code 0), `warningCount`, `errorCount`, `warnings[]`, `errors[]` (each capped at 20 lines, deduplicated).

**Evidence.** Runs the module's `compileDebugKotlin` task; `warnings`/`errors` are lines from stderr containing `warning:`/`w:` or `error:`/`e:` respectively — a heuristic, not a structured compiler-diagnostics API.

**Side effects.** executes Gradle (always — there's no read-only fast path for this tool, unlike `tests.run`/`lint.run`).

**Failure modes.** `invalid_project`; `gradle_not_available`; `invalid_request` (module name fails the task-name regex); `timeout`.

---

### staticAnalysis.run

**Purpose.** Runs one or more configured static analysis tools and reports exit status per tool. Does **not** parse or aggregate the tools' actual findings — it reports whether each tool run succeeded and how long it took, not what it found.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |
| `module` | string | no | Optional module scope |
| `tools` | string[] | no (default `["detekt", "ktlint"]`) | Any of `detekt`, `ktlint`, `kover` |
| `timeoutSeconds` | integer | no (default `600`) | Execution timeout per tool |

**Output.** `status`, `tools[]` (`name`, `exitCode`, `durationMs`, `success`, `timedOut`, `outputLength` — the combined stdout+stderr character count, not the content itself).

**Evidence.** Maps `detekt`→`detekt`, `ktlint`→`ktlintCheck`, `kover`→`koverHtmlReport` and executes each as a separate Gradle invocation. A requested tool with a task name that fails the syntax validator is silently skipped rather than erroring the whole call.

**Side effects.** executes Gradle (one process per requested tool; these tasks only run if the target project actually applies the corresponding Gradle plugin — if it doesn't, Gradle itself will fail with a non-zero exit for that tool).

**Failure modes.** `invalid_project`; `gradle_not_available`. Per-tool failures surface as a non-zero `exitCode`/`success: false` entry rather than aborting the whole response.

---

## Android Deep Inspection

### navigation.graph

**Purpose.** Inspects both XML `NavHost` graphs and Compose Navigation routes in one call.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |

**Output.** `status`, `hasXmlNavigation`, `hasComposeNavigation`, `xmlGraphs[]` (`file`, `startDestination`, `destinationCount`, `destinations[]` with `id`, `label`, `isStartDestination`, `deepLinks[]`), `composeRoutes[]` (`route`, `file`, `line`).

**Evidence.** `NavXmlParser` for any `.xml` file whose path contains `navigation` and whose content looks like a nav graph; `ComposeNavDetector` scanning non-test Kotlin source for `NavHost`/route patterns.

**Side effects.** read-only.

**Failure modes.** None beyond `invalid_project` — a project with no navigation graphs still returns `"status": "success"` with empty arrays and both `has*` flags `false`.

---

### resource.references

**Purpose.** Given a resource identifier like `string.app_name`, finds every place it's referenced as `R.string.app_name` in Kotlin/Java or `@string/app_name` in XML.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |
| `resourceName` | string | **yes** | Resource identifier, e.g. `string.app_name` |

**Output.** `status`, `resourceName`, `totalReferences`, `kotlinReferences[]` (`file`, `line`, `context`), `xmlReferences[]` (`file`, `line`, `context`).

**Evidence.** Regex scan (`R\.<type>\.<name>` and `@<type>/<name>`) across all `.kt`/`.java` and `.xml` files (depth 8) — a literal string-pattern search, not a resolved-symbol lookup.

**Side effects.** read-only.

**Failure modes.** `invalid_project` only — an unknown resource name simply returns zero references, not an error.

---

### manifest.merge

**Purpose.** Detects merge conflicts across module manifests by comparing the same component's attributes (`exported`, `permission`, `enabled`, `directBootAware`) as declared in each module that defines it.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |

**Output.** `status`, `manifestCount`, `manifests[]` (`module`, `file`), `conflictCount`, `conflicts[]` (`type`, `component`, `attribute`, `values[]` — `module`/`value` pairs showing the disagreement).

**Evidence.** Parses every `AndroidManifest.xml` under `src/main` (depth 6) and cross-references attribute values per component name across modules. This is a static approximation of Android's real manifest merger, not a call into AGP's actual merge tool — it flags where two modules *declare different values* for the same component; it does not reproduce Android's merge-priority rules.

**Side effects.** read-only.

**Failure modes.** `invalid_project`; `not_available` if no manifests are found.

---

## Security & Compliance

### proguard.inspect

**Purpose.** Inventories ProGuard/R8 configuration: parsed rules from `.pro`/`proguard*`/`consumer-rules*` files, plus minification/resource-shrinking flags and file references pulled from build scripts.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |

**Output.** `status`, `fileCount`, `files[]` (`file`, `ruleCount`, `rules[]` — `type`, `pattern`, `line`). Rule `type` values include `keep_class`, `keepclassmembers`, `keepnames`, `dontwarn`, `assumenosideeffects`, and about a dozen other real ProGuard directive categories, plus synthetic `minification_enabled`/`resource_shrinking_enabled`/`proguard_file_ref`/`consumer_proguard_file_ref` entries derived from build-file flags rather than rule files.

**Evidence.** Line-by-line regex classification of rule files (depth 8) plus text search of build files for `isMinifyEnabled`/`minifyEnabled`, `isShrinkResources`/`shrinkResources`, and `proguardFiles(...)`/`consumerProguardFiles(...)` calls.

**Side effects.** read-only.

**Failure modes.** `not_available` if no ProGuard/R8 evidence is found anywhere in the project.

---

### security.audit

**Purpose.** Flags common Android manifest/config security issues: components exported without an intent-filter, `allowBackup`, cleartext traffic, and patterns that look like hardcoded secrets in `gradle.properties`/`local.properties` or source files.

**Input**

| Field | Type | Required | Description |
|---|---|---|---|
| `projectRoot` | string | no | Absolute path to the project root |

**Output.** `status`, `totalIssues`, `criticalCount`, `warningCount`, `infoCount`, `issues[]` (`severity`, `category`, `message`, `file`, `line`).

**Evidence.** Three independent scans: (1) manifest text scan for `android:exported="true"` — expanded to look at the enclosing `<activity|service|receiver|provider>` block for a nested `<intent-filter>`, not just the single line, since intent-filters are child elements; (2) `gradle.properties`/`local.properties` key names matched against `password`/`token`/`secret`/`key`/`api`; (3) regex scan of `.kt`/`.java`/`.xml` files for `api_key=`/`secret=`/`password=`/`token=` string-literal assignments and AWS-style key patterns.

**Side effects.** read-only. Findings are returned in the MCP response only — nothing is sent anywhere else. See [security.md](security.md).

**Failure modes.** `invalid_project` only.

---

## Planned / Not Yet Available

Nothing found during this inspection indicates in-repo evidence of additional tools planned for a specific future release (no roadmap file, no feature-flagged/disabled tool registrations in `Main.kt`). If you're looking for a capability not listed above, it does not currently exist in this codebase — open an issue rather than assuming it's coming.
