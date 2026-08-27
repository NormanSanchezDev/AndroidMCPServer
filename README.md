<p style="aliQn-content: center;">  `
  <img
    src = "https://capsule-render.vercel.app/api?type=waving&amp;height=220&amp;color=0:0D1117,45:3DDC84,100:7F52FF&amp;text=Android%20MCP&amp;fontColor=FFFFFF&amp;fontSize=54&amp;fontAlignY=38&amp;desc=Deterministic%20Android%20%2B%20Kotlin%20evidence%20for%20AI%20agents&amp;descAlignY=60&amp;descSize=18"
    alt = "Android MCP banner" />
</p>

<p style="align-content: center;">
<a href="https://modelcontextprotocol.io/">
    <img src="https://img.shields.io/badge/Protocol-MCP-7F52FF?style=for-the-badge" alt="MCP" />
</a>
    <img src="https://img.shields.io/badge/Kotlin-2.4-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Android-Evidence-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
    <img src="https://img.shields.io/badge/Status-Experimental-F4B400?style=for-the-badge" alt="Status" />
</p>
<p style="align-content: center;">
    <a href="https://normansanchez.dev">
        normansanchez.dev
    </a>
    <a href="mailto:contact@normansanchez.dev">
        contact@normansanchez.dev
    </a>
</p>

------------------------------------------------------------------------
# Android MCP

A deterministic Model Context Protocol server for inspecting,
validating, and executing operations against real Android and Kotlin
repositories.

The Android MCP gives AI agents structured access to **verifiable
Android project evidence** without requiring the agent to guess
repository structure, dependencies, Gradle configuration, manifests,
source symbols, tests, or build state.

> **The MCP observes. The agent reasons.**

The server is intentionally designed as an evidence and execution
boundary rather than an autonomous Android expert.

------------------------------------------------------------------------

## Why this exists

AI coding agents understand Android and Kotlin conceptually, but they do
not automatically know the truth about a specific repository.

An agent may know what a `ViewModel`, `StateFlow`,
`CoroutineDispatcher`, or Gradle convention plugin is, but it cannot
safely assume:

-   which architecture the repository actually uses;
-   which modules exist;
-   where a feature lives;
-   which Gradle plugins are applied;
-   which dependencies and versions are present;
-   which dependency injection framework is used;
-   whether Compose is enabled;
-   which Android SDK versions are configured;
-   what permissions exist in the manifest;
-   which tests already cover a component;
-   whether a proposed change compiles;
-   whether lint, tests, or static analysis pass;
-   whether an abstraction already exists elsewhere in the repository.

The Android MCP exists to answer those questions using **observed
repository evidence**.

Instead of allowing an AI agent to infer:

``` text
"This project probably uses Hilt."
```

the MCP allows it to establish:

``` text
Hilt dependency found:
app/build.gradle.kts:47

Plugin:
com.google.dagger.hilt.android

Existing modules using @HiltViewModel:
features/home/
features/profile/
```

That distinction is fundamental.

------------------------------------------------------------------------

# Core Principle

``` text
┌──────────────────────────────────────────────┐
│                  AI Agent                    │
│                                              │
│  Understands the task                        │
│  Reasons about architecture                  │
│  Evaluates trade-offs                        │
│  Produces implementation decisions           │
└──────────────────────┬───────────────────────┘
                       │
                       │ MCP
                       ▼
┌──────────────────────────────────────────────┐
│              Android MCP Server              │
│                                              │
│  Inspects                                    │
│  Searches                                    │
│  Resolves                                    │
│  Executes                                    │
│  Validates                                   │
│  Returns evidence                            │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│           Android / Kotlin Project           │
│                                              │
│  Kotlin                                      │
│  Gradle                                      │
│  AndroidManifest                             │
│  Resources                                   │
│  Tests                                       │
│  Build tooling                               │
└──────────────────────────────────────────────┘
```

The MCP does not replace the reasoning model.

It gives the reasoning model reliable access to reality.

------------------------------------------------------------------------

# Responsibilities

The Android MCP is responsible for exposing deterministic information
and controlled operations from Android and Kotlin repositories.

Its responsibilities are divided into several capability areas.

## Project discovery

The server can inspect the physical and logical structure of an Android
repository.

Evidence may include:

-   repository root;
-   Gradle modules;
-   Android application modules;
-   Android library modules;
-   Kotlin/JVM modules;
-   source sets;
-   test source sets;
-   build files;
-   convention plugins;
-   version catalogs;
-   manifests;
-   resources;
-   generated source configuration.

Example:

``` text
project
├── app
├── core
│   ├── common
│   ├── network
│   └── database
├── feature
│   ├── home
│   └── profile
└── build-logic
```

The server reports what exists.

It does not infer what should exist.

------------------------------------------------------------------------

# Kotlin source inspection

The MCP can expose information about Kotlin source code.

Examples include:

-   classes;
-   interfaces;
-   objects;
-   functions;
-   properties;
-   annotations;
-   constructors;
-   inheritance;
-   implemented interfaces;
-   package structure;
-   imports;
-   visibility;
-   references;
-   usages;
-   related tests.

This allows an agent to ask questions such as:

``` text
Where is LoginRepository implemented?
```

or:

``` text
Which classes implement SessionStorage?
```

or:

``` text
Where is refreshToken() used?
```

The response should contain concrete repository evidence rather than a
speculative answer.

------------------------------------------------------------------------

# Android Manifest inspection

The MCP can inspect Android manifests and expose information such as:

-   permissions;
-   activities;
-   services;
-   receivers;
-   providers;
-   intent filters;
-   exported components;
-   application configuration;
-   deep links;
-   manifest placeholders.

Example evidence:

``` text
Permission:
android.permission.POST_NOTIFICATIONS

Declared in:
app/src/main/AndroidManifest.xml:12
```

------------------------------------------------------------------------

# Gradle inspection

The MCP can inspect the actual Gradle configuration of the repository.

This includes:

-   plugins;
-   Android Gradle Plugin;
-   Kotlin version;
-   compile SDK;
-   min SDK;
-   target SDK;
-   build types;
-   product flavors;
-   dependencies;
-   version catalogs;
-   convention plugins;
-   annotation processors;
-   KSP configuration;
-   Compose configuration;
-   test dependencies.

An agent should not need to guess whether a dependency exists.

It should ask the MCP.

------------------------------------------------------------------------

# Dependency inspection

The MCP can expose declared and resolved dependencies when available.

Examples:

``` text
androidx.lifecycle:lifecycle-viewmodel-ktx
com.google.dagger:hilt-android
io.insert-koin:koin-android
com.squareup.retrofit2:retrofit
com.squareup.okhttp3:okhttp
androidx.room:room-runtime
```

The server should distinguish between:

``` text
DECLARED
```

and:

``` text
RESOLVED
```

dependency information whenever possible.

------------------------------------------------------------------------

# Repository search

The MCP provides controlled repository search capabilities.

Search may include:

-   filename;
-   path;
-   symbol;
-   class;
-   interface;
-   function;
-   annotation;
-   string;
-   Gradle dependency;
-   resource;
-   manifest declaration.

Search results should include enough context for the caller to locate
and verify the result.

Example:

``` text
Query:
SessionRepository

Results:

core/auth/src/main/kotlin/.../SessionRepository.kt
core/auth/src/main/kotlin/.../DefaultSessionRepository.kt
core/auth/src/test/kotlin/.../DefaultSessionRepositoryTest.kt
```

------------------------------------------------------------------------

# Architecture evidence

The MCP may expose evidence that helps an agent understand repository
architecture.

For example:

``` text
ViewModels found: 18

Using StateFlow: 15
Using LiveData: 3

Dependency injection:
Hilt detected

Repository implementations:
core/data/

Feature modules:
feature/*
```

However, the MCP must not convert these observations into unsupported
architectural judgments.

Valid:

``` text
15 of 18 inspected ViewModels expose StateFlow.
```

Invalid:

``` text
This project follows perfect MVI architecture.
```

The second statement requires interpretation and belongs to the agent.

------------------------------------------------------------------------

# Test discovery

The MCP can discover tests related to production code.

Supported evidence may include:

-   unit tests;
-   instrumentation tests;
-   Compose UI tests;
-   integration tests;
-   test fixtures;
-   test utilities.

Example request:

``` text
Find tests related to SessionRepository.
```

Example result:

``` text
DefaultSessionRepositoryTest.kt
RefreshSessionUseCaseTest.kt
SessionStorageTest.kt
```

------------------------------------------------------------------------

# Test execution

When explicitly requested and allowed, the MCP may execute repository
test commands.

Examples:

``` text
./gradlew test
```

``` text
./gradlew :feature:login:testDebugUnitTest
```

``` text
./gradlew connectedDebugAndroidTest
```

Execution results must be returned as evidence.

The MCP must not report:

``` text
Tests passed.
```

unless the command actually completed successfully.

------------------------------------------------------------------------

# Build validation

The MCP may execute controlled Gradle validation operations.

Examples:

``` text
./gradlew assembleDebug
```

``` text
./gradlew :app:compileDebugKotlin
```

``` text
./gradlew check
```

Results should contain:

-   command;
-   exit status;
-   duration;
-   relevant output;
-   failure reason when available.

------------------------------------------------------------------------

# Static analysis

When configured by the repository, the MCP may execute tools such as:

-   Android Lint;
-   Detekt;
-   ktlint;
-   Kover;
-   custom Gradle verification tasks.

The MCP must discover what the repository supports instead of assuming
that every project uses the same tools.

------------------------------------------------------------------------

# Evidence model

Every tool should prefer structured evidence.

Conceptually:

``` json
{
  "status": "FOUND",
  "source": "repository",
  "path": "core/auth/src/main/kotlin/SessionRepository.kt",
  "symbol": "SessionRepository",
  "evidence": "...",
  "confidence": 1.0
}
```

The exact transport schema may vary by tool, but results should
distinguish between:

``` text
FOUND
NOT_FOUND
UNAVAILABLE
AMBIGUOUS
EXECUTION_FAILED
```

`NOT_FOUND` must not mean the same thing as `UNAVAILABLE`.

For example:

``` text
NOT_FOUND
```

means:

> The repository was inspected and the requested evidence was not found.

Whereas:

``` text
UNAVAILABLE
```

means:

> The server could not inspect the required source.

That distinction prevents agents from converting missing access into
false conclusions.

------------------------------------------------------------------------

# Facts, inference and unknown information

The Android MCP operates primarily in the **FACT** layer.

Example:

``` text
FACT

File:
feature/login/LoginViewModel.kt

Observation:
LoginViewModel extends ViewModel and exposes StateFlow<LoginUiState>.
```

An AI agent may subsequently produce:

``` text
INFERENCE

The feature appears to use unidirectional state management.
```

If neither the repository nor available tooling can establish something:

``` text
UNKNOWN

No evidence was available to determine this.
```

The MCP must never fabricate the missing information.

------------------------------------------------------------------------

# Security boundary

The Android MCP may operate against proprietary and confidential
repositories.

Security is therefore part of the protocol contract.

The server must follow the principle:

``` text
minimum necessary access
```

It should only inspect or execute what is required for the requested
operation.

------------------------------------------------------------------------

# Sensitive information

Tools must avoid returning or logging secrets.

Examples include:

-   passwords;
-   access tokens;
-   refresh tokens;
-   GitHub PATs;
-   API keys;
-   private keys;
-   signing credentials;
-   keystore passwords;
-   `.jks` contents;
-   `.keystore` contents;
-   authentication headers;
-   secrets from `local.properties`;
-   environment credentials.

If a file contains both useful configuration and sensitive information,
sensitive values must be redacted.

Example:

``` text
API_URL=https://example.internal
API_KEY=[REDACTED]
```

------------------------------------------------------------------------

# Binary artifacts

The MCP is not an artifact storage service.

It must not persist or return binary contents such as:

-   APK;
-   AAB;
-   JAR binaries;
-   signing keystores;
-   compiled native libraries;
-   build outputs.

Metadata about these artifacts may be returned when relevant.

Example:

``` text
APK generated:
app/build/outputs/apk/debug/app-debug.apk

size:
28.4 MB
```

The binary itself should not be transported as normal MCP evidence.

------------------------------------------------------------------------

# Repository privacy

The server must not upload repository contents to external services as a
side effect of inspection.

Repository evidence remains local unless the host application explicitly
provides another approved transport mechanism.

The MCP itself must not introduce telemetry that exposes:

-   source code;
-   repository paths;
-   customer names;
-   credentials;
-   proprietary identifiers.

------------------------------------------------------------------------

# Controlled execution

Commands exposed through MCP must not become arbitrary remote shell
execution.

Prefer explicit operations such as:

``` text
run_unit_tests
run_android_lint
compile_module
assemble_variant
```

over unrestricted:

``` text
execute_shell_command
```

When generic execution is unavoidable, commands must be validated
against an explicit policy.

------------------------------------------------------------------------

# Repository boundaries

All filesystem operations must remain inside the configured repository
root unless a specific capability explicitly requires otherwise.

Path traversal such as:

``` text
../../
```

must not allow the MCP to inspect arbitrary files on the host machine.

Symlinks must be resolved and validated before access.

------------------------------------------------------------------------

# Deterministic behavior

Given the same:

``` text
repository state
+
tool arguments
+
environment
```

a tool should return equivalent evidence.

The MCP should avoid introducing LLM-generated interpretation into
deterministic inspection tools.

For example, symbol discovery should use source analysis or repository
inspection rather than asking another language model what symbols
probably exist.

------------------------------------------------------------------------

# Error handling

Errors should be explicit and machine-readable.

Examples:

``` text
REPOSITORY_NOT_FOUND
INVALID_REPOSITORY_ROOT
GRADLE_NOT_AVAILABLE
GRADLE_EXECUTION_FAILED
ANDROID_SDK_NOT_FOUND
SYMBOL_NOT_FOUND
AMBIGUOUS_SYMBOL
PERMISSION_DENIED
TIMEOUT
UNSUPPORTED_PROJECT
```

Do not collapse unrelated failures into:

``` text
Something went wrong.
```

An agent needs enough information to decide what to do next.

------------------------------------------------------------------------

# Tool design philosophy

Every MCP tool should answer one clear question.

Good:

``` text
inspect_project
find_symbol
find_usages
inspect_manifest
inspect_gradle
find_tests
run_unit_tests
run_lint
compile_module
```

Avoid giant tools such as:

``` text
analyze_everything_and_tell_me_what_to_do
```

The latter mixes evidence acquisition with reasoning and makes results
difficult to verify.

------------------------------------------------------------------------

# Example interaction

An agent receives a ticket:

``` text
Refresh the authentication token when the current token expires.
```

Instead of immediately proposing an implementation, the agent queries
the Android MCP.

``` text
inspect_project
```

The MCP discovers:

``` text
core/auth
core/network
feature/login
```

The agent asks:

``` text
find_symbol("SessionRepository")
```

Result:

``` text
core/auth/.../SessionRepository.kt
core/auth/.../DefaultSessionRepository.kt
```

The agent asks:

``` text
find_usages("refreshToken")
```

Result:

``` text
DefaultSessionRepository.kt
AuthenticationInterceptor.kt
SessionManager.kt
```

The agent asks:

``` text
find_tests("DefaultSessionRepository")
```

Result:

``` text
DefaultSessionRepositoryTest.kt
```

Only after gathering this evidence does the Android expert determine an
implementation strategy.

The workflow becomes:

``` text
Ticket
   │
   ▼
Agent understands intent
   │
   ▼
Android MCP gathers evidence
   │
   ▼
Agent evaluates evidence
   │
   ▼
Implementation strategy
   │
   ▼
Changes
   │
   ▼
Android MCP validates
   │
   ▼
Tests / Lint / Compile
   │
   ▼
Verified result
```

------------------------------------------------------------------------

# Integration with AI agents

The MCP is designed to work with any MCP-compatible client.

Examples may include:

-   coding agents;
-   IDE assistants;
-   local AI systems;
-   orchestration platforms;
-   CI automation;
-   specialized Android agents.

The server does not assume that a specific LLM vendor is used.

The architecture is:

``` text
Client / Agent
      │
      │ MCP
      ▼
Android MCP
      │
      ▼
Android Repository
```

This keeps repository tooling independent from the reasoning provider.

------------------------------------------------------------------------

# Recommended server instructions

The MCP server should advertise instructions similar to:

``` text
Provides deterministic Android and Kotlin project evidence.

Use this server to inspect real project structure, Android manifests,
Kotlin symbols, Gradle configuration, dependencies, tests, lint results,
build state, and other verifiable Android project data.

Prefer observed repository evidence over assumptions.

Tools must return what was actually observed or executed.

Do not invent architectural, security, performance, or best-practice
conclusions without supporting repository evidence.

Distinguish between evidence that was not found and evidence that could
not be inspected.

Never expose credentials, authentication tokens, signing secrets,
private keys, or other sensitive values.

Repository contents must remain within the configured repository
boundary unless an explicitly approved capability requires otherwise.

This server provides evidence and controlled execution.
Architectural interpretation and implementation decisions belong
to the calling agent.
```

------------------------------------------------------------------------

# Non-goals

The Android MCP is **not** intended to:

-   replace an Android engineer;
-   replace an AI reasoning agent;
-   generate unsupported architectural opinions;
-   enforce one architecture across every repository;
-   assume every project uses Compose;
-   assume every project uses MVVM or MVI;
-   assume every project uses Hilt;
-   rewrite repositories automatically without authorization;
-   act as unrestricted shell access;
-   upload proprietary repositories;
-   store APKs or build artifacts;
-   expose secrets;
-   provide conclusions without evidence.

------------------------------------------------------------------------

# Design philosophy

Android repositories vary significantly.

A modern Compose application and a ten-year-old View-based application
may both be valid production systems.

Therefore:

``` text
Repository reality > generic Android convention
```

The MCP should first understand what exists.

An expert agent can then determine whether existing patterns should be
preserved, extended, migrated, or replaced.

------------------------------------------------------------------------

# Architecture

The implementation should preserve clear boundaries between protocol,
domain capabilities and infrastructure.

Conceptually:

``` text
android-mcp/
│
├── server/
│   ├── MCP configuration
│   ├── capabilities
│   └── tool registration
│
├── domain/
│   ├── project
│   ├── kotlin
│   ├── android
│   ├── gradle
│   ├── testing
│   └── evidence
│
├── tools/
│   ├── inspect_project
│   ├── find_symbol
│   ├── find_usages
│   ├── inspect_manifest
│   ├── inspect_gradle
│   ├── find_tests
│   └── validation
│
└── infrastructure/
    ├── filesystem
    ├── kotlin
    ├── gradle
    └── process
```

Protocol-specific details should not leak unnecessarily into repository
inspection logic.

------------------------------------------------------------------------

# Future capabilities

Potential future capabilities include:

-   Kotlin PSI-based semantic analysis;
-   Android Lint structured results;
-   Gradle Tooling API integration;
-   dependency graph inspection;
-   module dependency visualization;
-   Compose inspection;
-   navigation graph inspection;
-   resource reference analysis;
-   manifest merge inspection;
-   ProGuard/R8 configuration inspection;
-   test coverage evidence;
-   changed-files analysis;
-   Git diff-aware evidence;
-   architecture pattern discovery;
-   build performance evidence;
-   dependency vulnerability metadata.

These capabilities should preserve the same fundamental contract:

> **Return evidence first. Let the caller reason from it.**

------------------------------------------------------------------------

# Summary

The Android MCP provides a reliable bridge between AI reasoning and real
Android repositories.

Without repository tooling:

``` text
AI knowledge
+
assumptions
=
potentially incorrect implementation
```

With the Android MCP:

``` text
AI knowledge
+
real repository evidence
+
verified execution
=
grounded engineering decisions
```

The server exists to make one guarantee as consistently as possible:

> **If the MCP says something exists, it observed it.\
> If it says something passed, it executed it.\
> If it does not know, it says so.**

------------------------------------------------------------------------

``
<p #@!align="center">
```
Built by `<a href="https://normansanchez.dev">Norman
Sánchez`</a>`<br/>
`<a href="mailto:contact@normansanchez.dev">contact@normansanchez.dev`</a>`
</p>
```
