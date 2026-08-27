# Usage Examples

Practical examples of using Android Corporate MCP with your AI assistant.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Project Analysis](#project-analysis)
- [Architecture Analysis](#architecture-analysis)
- [Code Navigation](#code-navigation)
- [Build & Testing](#build--testing)
- [Security Analysis](#security-analysis)
- [Dependency Management](#dependency-management)
- [Common Workflows](#common-workflows)

---

## Getting Started

Once connected, ask your AI assistant to explore the project:

```
"I want to understand this Android project. Start by inspecting the project
structure and telling me what modules exist and what they do."
```

The assistant will call tools like `project.inspect`, `module.graph`, and `gradle.config` to build a mental model of your codebase.

---

## Project Analysis

### Inspect project structure

```
"Analyze the project structure. What modules exist?
What plugin types do they use?"
```

**Tools used:** `project.inspect`, `module.graph`

### Check Gradle configuration

```
"What Gradle configuration does this project use?
Report SDK versions, Kotlin version, and whether Compose is enabled."
```

**Tools used:** `gradle.config`

### View version catalog

```
"Show me the dependencies in the version catalog.
Which libraries are declared and what versions?"
```

**Tools used:** `gradle.versionCatalog`

---

## Architecture Analysis

### Detect architecture patterns

```
"Analyze the project architecture.
Is it using Hilt, Koin, or manual DI?
Is it using Compose or XML layouts?
Does it follow MVVM or MVI?"
```

**Tools used:** `architecture.detect`

### Map the navigation flow

```
"Show me the navigation graph.
What screens exist and how do they connect?"
```

**Tools used:** `navigation.graph`

### Understand the module graph

```
"Explain the module dependencies.
Which module depends on which?
Is there a clean dependency direction?"
```

**Tools used:** `module.graph`

---

## Code Navigation

### Find symbols

```
"Find all classes and interfaces related to 'Repository'
in the project. Show me their file locations."
```

**Tools used:** `symbol.find`

### Trace references

```
"Where is the class 'UserRepository' used across the codebase?"
```

**Tools used:** `symbol.references`

### Explore hierarchy

```
"Show me the class hierarchy for 'BaseViewModel'.
What are its subclasses and superclasses?"
```

**Tools used:** `symbol.hierarchy`

---

## Build & Testing

### Run tests

```
"Run the project tests and summarize:
how many pass, how many fail, and any errors."
```

**Tools used:** `tests.run` (with `trigger: true`)

### Discover tests for a class

```
"Find the tests for the 'LoginViewModel' class."
```

**Tools used:** `tests.discover`

### Run lint

```
"Run Android Lint and report the warnings
and errors found."
```

**Tools used:** `lint.run`

### Validate build

```
"Validate that the app module compiles.
Report any compilation errors or warnings."
```

**Tools used:** `build.validate`

### Static analysis

```
"Run Detekt and ktlint on the project.
Report any code style issues."
```

**Tools used:** `staticAnalysis.run`

---

## Security Analysis

### Security audit

```
"Audit this project for security issues.
Check for exported components, hardcoded secrets,
and cleartext traffic."
```

**Tools used:** `security.audit`

### Check ProGuard rules

```
"What ProGuard rules are configured?
Is minification enabled?"
```

**Tools used:** `proguard.inspect`

### Verify manifest security

```
"Check exported components in the manifest.
Are any exported without permission?
Any dangerous permissions declared?"
```

**Tools used:** `manifest.inspect`, `security.audit`

---

## Dependency Management

### Inspect dependencies

```
"What dependencies does the app module have
and what are their versions?"
```

**Tools used:** `dependencies.inspect`

### Check resource usage

```
"Where is the layout 'activity_main' referenced?"
```

**Tools used:** `resource.references`

### Check for merge conflicts

```
"Are there any manifest merge conflicts
in this multi-module project?"
```

**Tools used:** `manifest.merge`

---

## Common Workflows

### Onboarding a new developer

```
"I just joined this project. Analyze the architecture,
explain the module structure, show me the main screens,
and point out anything I should know about security."
```

**Tools used:** `project.inspect`, `architecture.detect`, `navigation.graph`, `security.audit`, `gradle.config`

### Pre-release checklist

```
"Before we release, run a full analysis:
1. Validate the build compiles
2. Run all tests
3. Run static analysis (detekt, ktlint)
4. Audit security
5. Check for manifest merge conflicts
6. Verify ProGuard rules"
```

**Tools used:** `build.validate`, `tests.run`, `staticAnalysis.run`, `security.audit`, `manifest.merge`, `proguard.inspect`

### Refactoring guidance

```
"I want to refactor the UserRepository.
Find all its usages, understand the class hierarchy,
and tell me which tests cover it."
```

**Tools used:** `symbol.find`, `symbol.references`, `symbol.hierarchy`, `tests.discover`

### Understanding a specific screen

```
"Explain the 'Login' screen:
- Find the LoginActivity and LoginViewModel
- Trace the navigation to it
- Identify any security concerns
- Find its tests"
```

**Tools used:** `symbol.find`, `navigation.graph`, `security.audit`, `tests.discover`
