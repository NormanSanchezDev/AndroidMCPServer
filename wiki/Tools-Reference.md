# Tools Reference

Complete documentation for all 27 MCP tools.

## Tool Categories

| Category | Tools |
|----------|-------|
| **Project & Manifest** | [project.inspect](#projectinspect), [manifest.inspect](#manifestinspect), [entry_points.find](#entrypointsfind), [manifest.merge](#manifestmerge) |
| **Symbol Analysis** | [symbol.find](#symbolfind), [symbol.references](#symbolreferences), [symbol.hierarchy](#symbolhierarchy) |
| **Gradle Intelligence** | [gradle.tasks](#gradletasks), [gradle.run](#gradlerun), [gradle.config](#gradleconfig), [gradle.versionCatalog](#gradleversioncatalog), [gradle.conventionPlugins](#gradleconventionplugins) |
| **Testing & Quality** | [tests.run](#testsrun), [tests.discover](#testsdiscover), [lint.run](#lintrun), [build.validate](#buildvalidate), [staticAnalysis.run](#staticanalysisrun) |
| **Dependencies & Modules** | [dependencies.inspect](#dependenciesinspect), [module.graph](#modulegraph) |
| **Android Resources** | [resources.inspect](#resourcesinspect), [resource.references](#resourcereferences) |
| **Architecture & Navigation** | [architecture.detect](#architecturedetect), [navigation.graph](#navigationgraph) |
| **Security & Compliance** | [security.audit](#securityaudit), [proguard.inspect](#proguardinspect) |

---

## Project & Manifest

### `project.inspect`

**Purpose:** Deterministic inspection of a Gradle/Android project root — settings file, modules, plugin types, and evidence.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "projectRoot": "/path/to/project",
  "modules": [
    {
      "module": ":app",
      "pluginTypes": ["application", "android"],
      "evidence": "..."
    }
  ]
}
```

**Example query:**
> "What modules are in this project and what plugin types do they use?"

---

### `manifest.inspect`

**Purpose:** Parses `src/main/AndroidManifest.xml` of a module into package, components, permissions, and intent filters.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Module path relative to root (default: `app`) |

**Returns:**
```json
{
  "status": "success",
  "module": "app",
  "package": "com.example.app",
  "components": ["activity", "service", "receiver", "provider"],
  "permissions": ["android.permission.INTERNET"]
}
```

**Example query:**
> "Inspect the manifest for the app module"

---

### `entry_points.find`

**Purpose:** Derives entry points from manifest evidence — launcher activities, deep links, and exported components.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Module path relative to root (default: `app`) |

**Returns:**
```json
{
  "status": "success",
  "launcherActivities": ["com.example.app.MainActivity"],
  "deepLinks": [{"activity": "...", "scheme": "app", "host": "example.com"}],
  "exportedComponents": []
}
```

**Example query:**
> "Find all entry points in this app"

---

### `manifest.merge`

**Purpose:** Detects merge conflicts across module manifests by comparing component attributes.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "manifestCount": 3,
  "conflictCount": 0,
  "conflicts": []
}
```

**Example query:**
> "Are there any manifest merge conflicts in this multi-module project?"

---

## Symbol Analysis

### `symbol.find`

**Purpose:** Finds Kotlin declarations (class/interface/object/function/property/typealias) via Kotlin PSI across module source roots.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `query` | string | ✅ | Symbol name to search |
| `kind` | string | ❌ | Filter: `class`\|`interface`\|`enum`\|`object`\|`function`\|`property`\|`typealias` |
| `exactMatch` | boolean | ❌ | Exact name match instead of contains |
| `includeTests` | boolean | ❌ | Include test source sets |
| `maxResults` | integer | ❌ | Maximum matches returned (default: 50) |

**Returns:**
```json
{
  "status": "success",
  "query": "User",
  "results": [
    {
      "name": "User",
      "kind": "class",
      "file": "data/User.kt",
      "line": 10,
      "supertypes": ["Serializable"],
      "annotations": ["@Entity"]
    }
  ]
}
```

**Example query:**
> "Find all classes named User"

---

### `symbol.references`

**Purpose:** Reports identifier-level occurrences of a symbol across Kotlin sources, separating declarations from references.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `symbolName` | string | ✅ | Identifier to locate |
| `includeTests` | boolean | ❌ | Include test source sets |
| `maxResults` | integer | ❌ | Maximum references returned (default: 200) |

**Example query:**
> "Find all references to ViewModel"

---

### `symbol.hierarchy`

**Purpose:** Builds the class/interface hierarchy tree for a symbol, showing inheritance relationships.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `symbolName` | string | ✅ | Symbol to build hierarchy for |
| `includeTests` | boolean | ❌ | Include test source sets |

**Example query:**
> "Show the class hierarchy for BaseViewModel"

---

## Gradle Intelligence

### `gradle.tasks`

**Purpose:** Lists Gradle tasks by executing `./gradlew tasks --all` and parsing its real output.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Optional module to scope tasks (e.g. `:app`) |
| `timeoutSeconds` | integer | ❌ | Execution timeout (default: 300) |

**Example query:**
> "List all available Gradle tasks"

---

### `gradle.run`

**Purpose:** Executes allow-listed Gradle tasks through the project wrapper, capturing exit code, duration, and output.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `tasks` | array | ✅ | Task names to execute |
| `flags` | array | ❌ | Allow-listed flags (`--parallel`, `--build-cache`) |
| `timeoutSeconds` | integer | ❌ | Execution timeout (default: 600) |

**Security:** Only allow-listed tasks can execute. Prevents arbitrary command execution.

**Example query:**
> "Run the test task for the app module"

---

### `gradle.config`

**Purpose:** Inspects Gradle configuration — applied plugins, Android SDK versions, Kotlin version, build types, product flavors, and Compose status.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "plugins": ["com.android.application", "org.jetbrains.kotlin.android"],
  "sdkVersions": {"compileSdk": 35, "minSdk": 24, "targetSdk": 35},
  "kotlinVersion": "2.4.0",
  "compose": true,
  "buildTypes": ["debug", "release"],
  "productFlavors": []
}
```

**Example query:**
> "What SDK versions and plugins does this project use?"

---

### `gradle.versionCatalog`

**Purpose:** Parses `gradle/libs.versions.toml` to expose declared dependencies, versions, and aliases.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "versions": {"agp": "8.5.0", "kotlin": "2.4.0"},
  "libraries": {
    "androidx-core-ktx": {"group": "androidx.core", "name": "core-ktx", "version": "1.13.1"}
  },
  "plugins": {"android-application": "com.android.application"}
}
```

**Example query:**
> "What dependencies are declared in the version catalog?"

---

### `gradle.conventionPlugins`

**Purpose:** Discovers and inspects convention plugins in `build-logic/` or `buildSrc/`.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "plugins": [
    {
      "id": "com.example.android.application",
      "file": "build-logic/src/main/kotlin/AndroidApplicationPlugin.kt"
    }
  ],
  "appliedBy": {
    ":app": ["com.example.android.application"]
  }
}
```

**Example query:**
> "What convention plugins are defined and which modules use them?"

---

## Testing & Quality

### `tests.run`

**Purpose:** Aggregates JUnit XML reports under `build/test-results`; optionally triggers the Gradle test task first.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Optional module scope |
| `task` | string | ❌ | Gradle task when `trigger=true` (default: `test`) |
| `trigger` | boolean | ❌ | Execute Gradle test before reading reports |
| `timeoutSeconds` | integer | ❌ | Execution timeout |

**Example query:**
> "Run the tests and show me the results"

---

### `tests.discover`

**Purpose:** Finds tests for a given class across test source sets.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `className` | string | ✅ | Class to find tests for |
| `includeTests` | boolean | ❌ | Include test utilities |

**Example query:**
> "Find tests for UserRepository"

---

### `lint.run`

**Purpose:** Aggregates Android Lint XML reports; optionally triggers the lint task first.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Optional module scope |
| `trigger` | boolean | ❌ | Execute lint before reading reports |

**Example query:**
> "What lint warnings does this project have?"

---

### `build.validate`

**Purpose:** Compiles a module via `gradlew` and extracts warnings/errors with structured output.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Module to compile (default: `app`) |
| `timeoutSeconds` | integer | ❌ | Execution timeout (default: 600) |

**Example query:**
> "Validate that the app module compiles successfully"

---

### `staticAnalysis.run`

**Purpose:** Runs static analysis tools — Detekt, ktlint, Kover — and returns structured results.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Optional module scope |
| `tools` | array | ❌ | Tools to run: `detekt`, `ktlint`, `kover` |

**Example query:**
> "Run Detekt and ktlint on this project"

---

## Dependencies & Modules

### `dependencies.inspect`

**Purpose:** Parses the Gradle dependency tree for a module.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Module to inspect (default: `app`) |
| `configuration` | string | ❌ | Dependency configuration (default: `debugRuntimeClasspath`) |
| `timeoutSeconds` | integer | ❌ | Execution timeout |

**Example query:**
> "What dependencies does the app module have?"

---

### `module.graph`

**Purpose:** Builds the static module dependency graph from settings/build files.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "modules": [":app", ":core-data", ":feature-login"],
  "edges": [
    {"from": ":app", "to": ":core-data"},
    {"from": ":feature-login", "to": ":core-data"}
  ]
}
```

**Example query:**
> "Show me the module dependency graph"

---

## Android Resources

### `resources.inspect`

**Purpose:** Enumerates Android resources under `res/` folders and value names.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Optional module scope (default: `app`) |

**Example query:**
> "List all resources in the app module"

---

### `resource.references`

**Purpose:** Traces resource usage in code and XML — which layouts, drawables, and strings are referenced.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `resourceName` | string | ✅ | Resource name to trace |
| `module` | string | ❌ | Optional module scope |

**Example query:**
> "Where is the layout 'activity_main' referenced?"

---

## Architecture & Navigation

### `architecture.detect`

**Purpose:** Detects architectural patterns — DI (Hilt/Koin), Compose, ViewModel/MVVM, MVI.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "patterns": {
    "hilt": {"used": true, "evidence": ["@HiltAndroidApp"]},
    "compose": {"used": true, "evidence": ["@Composable"]},
    "viewModel": {"used": true, "evidence": ["ViewModel"]}
  }
}
```

**Example query:**
> "Analyze the architecture of this project"

---

### `navigation.graph`

**Purpose:** Builds the navigation graph from NavHost (Compose) and XML navigation files.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |
| `module` | string | ❌ | Optional module scope (default: `app`) |

**Returns:**
```json
{
  "status": "success",
  "screens": ["home", "login", "profile"],
  "navigationGraph": {"start": "home", "routes": []}
}
```

**Example query:**
> "Show me the navigation graph of this app"

---

## Security & Compliance

### `security.audit`

**Purpose:** Audits the project for security issues — exported components, hardcoded secrets, cleartext traffic.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "findings": [
    {
      "severity": "high",
      "type": "exported_activity",
      "component": "MainActivity",
      "description": "Exported activity without permission"
    }
  ],
  "summary": {"high": 1, "medium": 0, "low": 0}
}
```

**Example query:**
> "Audit this project for security vulnerabilities"

---

### `proguard.inspect`

**Purpose:** Inspects ProGuard/R8 configuration — rules, keep annotations, minification and resource shrinking status.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `projectRoot` | string | ✅ | Absolute path to the project root |

**Returns:**
```json
{
  "status": "success",
  "fileCount": 1,
  "files": [
    {
      "file": "app/proguard-rules.pro",
      "ruleCount": 5,
      "rules": [
        {"type": "keep_class", "pattern": "com.example.app.**"}
      ]
    }
  ]
}
```

**Example query:**
> "What ProGuard rules are configured?"

---

## Response Format

All tools return JSON with a consistent structure:

```json
{
  "status": "success",           // success | not_available | invalid_project | error
  "error": "Error message",      // present only when status != success
  "...": "tool-specific fields"
}
```

**Common statuses:**

| Status | Meaning |
|--------|---------|
| `success` | Tool completed successfully |
| `not_available` | No matching data found (e.g., no manifests) |
| `invalid_project` | Path does not exist or is not a directory |
| `gradle_not_available` | No `gradlew` wrapper found |
| `error` | Unexpected error occurred |
