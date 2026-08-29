# Troubleshooting

Only problems that actually apply to this codebase, each traced to real source. If your issue isn't here, check [limitations.md](limitations.md) — it may be a known "by design" gap rather than a bug.

## Java missing or wrong version

**Symptom:** `[android-corporate-mcp] Java runtime not found on PATH.` from the npm launcher, or the server exits immediately after `java -jar ...`.

**Cause:** `bin/android-corporate-mcp.js` runs `spawnSync('java', ['-version'])` before starting the server and exits with a clear stderr message if that fails. If you're running the JAR directly instead, a missing/old JVM produces a normal `java` launch error.

**How to diagnose:** `java -version` on the command line.

**Solution:** Install a JDK/JRE 17+ (this project is built and tested against JDK 22) and make sure `java` is on `PATH`.

## Server doesn't start

**Symptom:** The MCP client reports the server failed to launch, or it exits with no MCP handshake.

**Cause:** Most commonly a Java-availability or JAR-location problem (see the two sections above/below). Less commonly, `projectRoot` isn't the issue at startup — no tool argument is read until a `tools/call` request arrives.

**How to diagnose:** Run the exact `command`/`args` from your client config directly in a terminal and read stderr — all diagnostics go there by design (`server/Main.kt` reclaims stdout for the MCP protocol before any logging occurs).

**Solution:** Fix whatever stderr reports (Java, JAR path). If stderr is empty and the process just exits, confirm the JAR isn't corrupted by re-running `./gradlew shadowJar` or reinstalling the npm package.

## MCP client can't connect

**Symptom:** The client shows the server as unreachable or times out on the initial handshake.

**Cause:** Usually a config-shape mismatch (see [configuration.md](configuration.md) — note GitHub Copilot/VS Code uses `"servers"` with `"type": "stdio"`, not `"mcpServers"` like the others), or a `command` that isn't actually on `PATH` in the environment the client launches from (GUI apps like Claude Desktop may have a different `PATH` than your shell).

**How to diagnose:** Run `npx @modelcontextprotocol/inspector <command>` with the exact same command your client config uses — if the Inspector connects and lists 26 tools, the server itself is fine and the issue is client-side configuration.

**Solution:** Match the config shape exactly to [configuration.md](configuration.md) for your client, or use an absolute path to the `android-corporate-mcp` binary / JAR if `PATH` resolution is the problem.

## stdio polluted (garbled/invalid JSON-RPC from the client's point of view)

**Symptom:** The client reports malformed responses or the handshake fails with a parse error, despite the process starting.

**Cause:** This should not happen in the current code — `server/Main.kt`'s `main()` captures the raw stdout file descriptor for the MCP transport first, then immediately calls `System.setOut(System.err)` so any subsequent library logging (SLF4J/kotlin-logging) is structurally redirected away from stdout. If you see this, it points to either a third-party dependency writing directly to file descriptor 1 (bypassing `System.out`), or a change to `Main.kt` that reordered this setup.

**How to diagnose:** Run the server directly and pipe only stdout to a file: `android-corporate-mcp > /tmp/stdout.log 2>/tmp/stderr.log`, then send it a `tools/list` request via the Inspector and inspect `/tmp/stdout.log` for anything besides JSON-RPC frames.

**Solution:** If non-protocol bytes appear on stdout, this is a real bug — report it with the exact bytes captured; it should not require a workaround.

## Android SDK unavailable

**Symptom:** A Gradle-backed tool (`build.validate`, `gradle.run` against SDK-dependent tasks like `compileDebugKotlin`) fails with an SDK-related Gradle error.

**Cause:** This server never touches the Android SDK directly (no `ANDROID_HOME` read, no SDK path lookup anywhere in the codebase) — SDK requirements come entirely from the target project's own Gradle build.

**How to diagnose:** Run the same task by hand in the target project: `./gradlew compileDebugKotlin`. If that fails the same way outside this server, it's an SDK/environment issue in the target project, not this server.

**Solution:** Set up the Android SDK for the target project as you normally would (e.g. `local.properties` with `sdk.dir`, or `ANDROID_HOME`) — this server has no involvement in that setup.

## Gradle failure

**Symptom:** A process-backed tool returns `status: "gradle_error"` or `status: "gradle_not_available"`.

**Cause:** `gradle_not_available` means `GradleWrapperLocator` couldn't find an executable `gradlew`/`gradlew.bat` at the target project's root — there is no system-Gradle fallback. `gradle_error` means the wrapper was found and ran, but Gradle itself failed (compile error, missing dependency, etc.).

**How to diagnose:** Check that `<projectRoot>/gradlew` (or `gradlew.bat` on Windows) exists and is executable. For `gradle_error`, the tool's own output field (see [tools.md](tools.md) for the exact field name per tool) contains Gradle's real stderr/stdout, truncated per that tool's cap.

**Solution:** Confirm `projectRoot` points at the actual Gradle project root (not a subdirectory), and that `./gradlew <task>` succeeds when run by hand there first.

## Repository not recognized

**Symptom:** A tool returns `status: "invalid_project"`.

**Cause:** The path passed as `projectRoot` doesn't look like a Gradle project to the tool (missing `settings.gradle`/`settings.gradle.kts` or equivalent marker, depending on the specific tool's check).

**How to diagnose:** Confirm the exact `projectRoot` string the agent sent (absolute path expected) actually points at the Gradle project root.

**Solution:** Pass the project root directory, not a module subdirectory or an arbitrary source folder.

## Tool returns no evidence

**Symptom:** A tool returns `status: "success"` with empty arrays/objects, or `status: "not_found"` / `"not_available"`.

**Cause:** This is a normal, deterministic response, not an error — see [limitations.md](limitations.md#known-limitations-by-design). Absence of evidence means the tool's specific detection method (identifier-substring match, XML pattern, naming convention) didn't match anything — not that the feature it's looking for doesn't exist in the project.

**How to diagnose:** Check the specific tool's "Evidence" and "Failure modes" sections in [tools.md](tools.md) — each tool's detection method is documented there.

**Solution:** Try a broader/different query argument (e.g. a shorter substring for `symbol.find`), or confirm the code you're looking for is actually in a source set the tool scans.

## Permission errors

**Symptom:** A tool returns a filesystem or process-execution error tied to permissions.

**Cause:** The server runs with exactly the OS-user permissions of whoever launched it (see [security.md](security.md)) — no elevation, no sandboxing. If that user can't read a file or execute `gradlew`, neither can the server.

**How to diagnose:** Try the equivalent operation (reading the file, running `./gradlew <task>`) directly as the same OS user.

**Solution:** Fix the underlying filesystem permissions (e.g. `chmod +x gradlew`) — this is not something the server can work around.

## Launcher can't locate the JAR

**Symptom:** `[android-corporate-mcp] JAR file not found. Please rebuild or reinstall the package.`

**Cause:** `bin/android-corporate-mcp.js` looks for a `jars/` directory next to the npm package first, falling back to `build/libs/`, and within whichever directory exists, picks a file ending in `-all.jar` (falling back to any `.jar`). If neither directory exists, or exists but is empty, this error fires.

**How to diagnose:** Check whether `jars/` exists in your installed npm package location (`npm root -g`/`android-corporate-mcp` package dir) and contains a `.jar` file.

**Solution:** Reinstall the npm package (`npm install -g android-corporate-mcp`), or if running from source, run `./gradlew shadowJar` first so `build/libs/AndroidMasterMCP-<version>-all.jar` exists.

## Docker build fails to find the JAR

**Symptom:** `docker build` fails at the `COPY` step with a "no such file" error.

**Cause:** The repository's `Dockerfile` has `COPY build/libs/android-corporate-mcp-server-*.jar app.jar`, but the actual shadow JAR this project produces is named `AndroidMasterMCP-<version>-all.jar` (from `rootProject.name = "AndroidMasterMCP"` in `settings.gradle.kts`). The glob in the `Dockerfile` does not match. This is a real, currently-unfixed bug in the repository, not a usage error — see [compatibility.md](compatibility.md#distribution-channels).

**How to diagnose:** Run `./gradlew shadowJar && ls build/libs/` and compare the actual filename to the `Dockerfile`'s `COPY` glob.

**Solution (workaround until fixed upstream):** Edit the `Dockerfile`'s `COPY` line locally to match the real filename (`COPY build/libs/AndroidMasterMCP-*-all.jar app.jar`) before building, or track the fix in [contributing.md](contributing.md).
