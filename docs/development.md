# Development

Building, testing, and extending the server itself. For using the server as a client, see [getting-started.md](getting-started.md).

## Clone and build

```bash
git clone https://github.com/normansanchez/AndroidCorporateMCP.git
cd AndroidCorporateMCP
./gradlew build
```

Requires Java 17+ (built and tested against JDK 22 — `kotlin { jvmToolchain(22) }` in `build.gradle.kts`).

## Run locally

```bash
./gradlew installDist
./build/install/AndroidMasterMCP/bin/AndroidMasterMCP
```

or build the standalone shadow JAR and run it directly:

```bash
./gradlew shadowJar
java -jar build/libs/AndroidMasterMCP-0.1.0-all.jar
```

Both start the same stdio server. There's no HTTP mode — connect a real MCP client (see [configuration.md](configuration.md)) or `npx @modelcontextprotocol/inspector <path-to-binary>` to interact with it.

## Tests

```bash
./gradlew test
```

Uses JUnit 5 (`tasks.test { useJUnitPlatform() }`). Tests live under `src/test/kotlin/dev/normansanchez/androidmcp/`, one file per tool/parser (e.g. `tools/EntryPointsFindToolTest.kt`), plus fixture-backed tests under `fixtures/` (`sample-android-project`, `gradle`, `gradle-deps`, `junit`, `lint`) that most tool tests build against via `FixtureProjects.sampleAndroidProject(tempDir)`.

## Project structure

```
src/main/kotlin/dev/normansanchez/androidmcp/
  server/Main.kt        # entry point: builds Server, registers all 25 tools, starts stdio transport
  client/Main.kt         # dev-only test harness (not wired into application.mainClass)
  tools/                 # one file per MCP tool
  gradle/                # GradleWrapperLocator, GradleCommandValidator
  process/               # ProcessExecutor
  psi/                   # KotlinPsiEngine, KotlinSourceScanner, SymbolExtractor, HierarchyBuilder
src/test/kotlin/dev/normansanchez/androidmcp/
  tools/                 # one test file per tool
  fixtures/              # FixtureProjects + fixture project trees under src/test/resources/fixtures/
```

## Adding a new MCP tool

1. Create `src/main/kotlin/dev/normansanchez/androidmcp/tools/<Name>Tool.kt` following the existing pattern: an `object` with a `suspend fun execute(...)` (or `fun execute(...)` for the purely read-only tools) that returns a `JsonObject` with a `status` field. Model failure cases as distinct `status` values (`invalid_project`, `not_found`, `not_available`, etc.), not thrown exceptions — see [tools.md](tools.md) for the existing vocabulary of status values and [architecture.md#error-propagation](architecture.md#error-propagation) for why.
2. Register it in `server/Main.kt`, in the phase-grouped block matching what your tool does (the file's own comments mark the phase boundaries — project inspection, Gradle/build, symbols, etc.):
   ```kotlin
   mcpServer.addTool(
       name = "your.tool_name",
       description = "...",
       inputSchema = Tool.Input(
           properties = buildJsonObject { /* ... */ },
           required = listOf(/* ... */)
       )
   ) { request ->
       val result = YourTool.execute(/* args from request.arguments */)
       CallToolResult(content = listOf(TextContent(result.toString())))
   }
   ```
   The exact `register`/`addTool` call shape and the shared try/catch wrapper that turns unexpected exceptions into `isError: true` responses are already in `Main.kt` — copy the pattern from a neighboring tool of the same kind (read-only vs. process-backed) rather than writing it from scratch.
3. Write `src/test/kotlin/dev/normansanchez/androidmcp/tools/<Name>ToolTest.kt`. For anything touching manifest/resources/Gradle structure, build a fixture project with `FixtureProjects.sampleAndroidProject(tempDir)` (or add a new fixture under `src/test/resources/fixtures/` if the scenario needs something the existing sample project doesn't have) and assert on the returned `JsonObject`'s fields directly — see `EntryPointsFindToolTest.kt` for the shape (`json["field"]!!.jsonPrimitive.content` / `.jsonArray` / `.jsonObject`).
4. Run `./gradlew test` and confirm your new test passes alongside the existing ~24 tool/parser test files.
5. Add the tool to the inventory in [tools.md](tools.md) — Purpose, Input table, Output, Evidence, Side effects, Failure modes — following the format of an existing entry.

## Coding conventions observed in this codebase

- Every tool returns a `JsonObject` (via `kotlinx.serialization.json.buildJsonObject`), never throws for an expected failure condition.
- All subprocess execution goes through `ProcessExecutor` (`ProcessBuilder(command: List<String>)`, list-args, no shell) — never build a command as a shell string.
- Gradle task names are only validated syntactically (`GradleCommandValidator`), and flags are checked against a real fixed allow-list (`Set<String>`) — keep that distinction in mind (and in any related doc) if you touch this file.
- `System.setOut(System.err)` in `server/Main.kt`'s `main()` must run before any library code has a chance to log to stdout — don't reorder startup code above that line, and don't add `println`/stdout writes anywhere in tool code.
