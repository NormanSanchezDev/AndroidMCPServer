# Contributing Guide

Thank you for considering contributing to Android Corporate MCP!

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Architecture Overview](#architecture-overview)
- [Adding a New Tool](#adding-a-new-tool)
- [Testing](#testing)
- [Code Style](#code-style)
- [Commit Guidelines](#commit-guidelines)
- [Release Process](#release-process)

---

## Getting Started

### Prerequisites

- JDK 22+
- Git
- (Optional) An AI assistant with MCP support for manual testing

### Fork and clone

```bash
git clone https://github.com/normansanchez/AndroidCorporateMCP.git
cd AndroidCorporateMCP
```

### Build

```bash
./gradlew build
```

---

## Development Setup

### IDE

IntelliJ IDEA is recommended for Kotlin development:

1. Open the project in IntelliJ
2. Let Gradle import the project (this may take a few minutes)
3. Ensure the Kotlin plugin is installed

### Running tests

```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "dev.normansanchez.androidmcp.tools.SecurityAuditToolTest"

# Run with live Gradle (integration tests)
./gradlew test --tests "*IntegrationTest"
```

---

## Architecture Overview

```
src/main/kotlin/dev/normansanchez/androidmcp/
├── server/               # MCP server setup & tool registration (Main.kt)
├── tools/                # Analysis tools (one class per tool)
├── gradle/               # Gradle parsers & validators
├── architecture/         # Pattern detection
├── symbol/               # PSI-based symbol analysis
├── navigation/           # Navigation graph builders
├── staticanalysis/       # Lint/analysis parsers
├── dependencies/         # Dependency tree parsing
├── graph/                # Module graph
├── junit/                # JUnit XML parsing
├── lint/                 # Lint XML parsing
└── process/              # Process execution helpers
```

### Core concepts

**Tool pattern:**

```kotlin
object SomeTool {
    fun execute(...): JsonObject {
        // 1. Validate inputs
        // 2. Perform analysis
        // 3. Return structured JSON
    }
}
```

**Server registration** (in `Main.kt`):

```kotlin
mcpServer.register(
    name = "tool.name",
    description = "...",
    properties = mapOf("arg" to optStr("..."))
) { arguments ->
    SomeTool.execute(argString(arguments, "arg").orEmpty())
}
```

---

## Adding a New Tool

### 1. Create the tool class

Create `src/main/kotlin/dev/normansanchez/androidmcp/tools/SomeTool.kt`:

```kotlin
package dev.normansanchez.androidmcp.tools

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SomeTool {
    fun execute(projectRoot: String, param: String = ""): JsonObject {
        val root = java.nio.file.Path.of(projectRoot)
        if (!java.nio.file.Files.isDirectory(root)) {
            return buildJsonObject {
                put("status", "invalid_project")
            }
        }
        // ... perform analysis ...
        return buildJsonObject {
            put("status", "success")
            put("result", ...)
        }
    }
}
```

### 2. Register in the server

Add to `Main.kt`:

```kotlin
mcpServer.register(
    name = "tool.name",
    description = "Short description",
    properties = mapOf(
        "projectRoot" to optStr("Absolute path to the project root"),
        "param" to optStr("Parameter description")
    )
) { arguments ->
    SomeTool.execute(
        argString(arguments, "projectRoot").orEmpty(),
        argString(arguments, "param").orEmpty()
    )
}
```

### 3. Create a test

Create `src/test/kotlin/dev/normansanchez/androidmcp/tools/SomeToolTest.kt`:

```kotlin
package dev.normansanchez.androidmcp.tools

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SomeToolTest {

    @Test
    fun `handles invalid path`() {
        val json = SomeTool.execute("/nonexistent/path")
        assertEquals("invalid_project", json["status"]?.toString()?.removeSurrounding("\""))
    }

    @Test
    fun `analyzes fixture project`() {
        val temp = Files.createTempDirectory("some-tool")
        try {
            val project = FixtureProjects.sampleAndroidProject(temp)
            val json = SomeTool.execute(project.absolutePathString())
            assertEquals("success", json["status"]?.toString()?.removeSurrounding("\""))
        } finally {
            temp.toFile().deleteRecursively()
        }
    }
}
```

### 4. Use the fixture project for testing

`FixtureProjects.sampleAndroidProject(temp)` creates a real Android project with 3 modules, version catalog, navigation, and architecture patterns — perfect for integration tests.

---

## Testing

### Coverage requirements

Every tool must include:

1. **Unit test** with fixture data (no Gradle execution)
2. **Integration test** against `src/test/resources/fixtures/sample-android-project/`
3. **Input validation** — invalid paths, missing files, empty results
4. **Security checks** — no path traversal, no secret exposure

### Test naming convention

```
src/test/kotlin/dev/normansanchez/androidmcp/tools/{ToolName}ToolTest.kt
src/test/kotlin/dev/normansanchez/androidmcp/{domain}/{ParserName}Test.kt
```

---

## Code Style

- **Kotlin official style** — follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **No comments** unless absolutely necessary — code should be self-documenting
- **Use `buildJsonObject`** for all JSON output
- **Return structured `JsonObject`** — never plain strings
- **Validate all inputs** — return `invalid_project` for bad paths
- **Follow the tool pattern** exactly (see above)

---

## Commit Guidelines

Use clear, descriptive commit messages:

```
feat: add manifest.merge tool for detecting merge conflicts
fix: handle missing version catalog in gradle.config
test: add tests for navigation.graph tool
docs: update tools reference with new tool
```

**Prefixes:**

| Prefix | Meaning |
|--------|---------|
| `feat:` | New feature or tool |
| `fix:` | Bug fix |
| `test:` | Test changes |
| `docs:` | Documentation changes |
| `refactor:` | Code refactoring (no behavior change) |

---

## Release Process

Releases are automated via GitHub Actions. Pushing to `master` triggers the pipeline:

1. Build shadow JAR
2. Run tests
3. Publish to npm
4. Create GitHub Release

### Manual version bump

Update `version` in `build.gradle.kts`:

```kotlin
version = "0.2.0"
```

Then push to master. The pipeline reads the version from `build.gradle.kts` and publishes automatically.

---

## Questions?

Open an issue or start a discussion on GitHub.
