# Compatibility

## Status legend

| Symbol | Meaning |
|---|---|
| ✅ Tested | Actually connected end-to-end and observed working in this project's own verification. |
| 🟡 Expected compatible | The server speaks standard MCP stdio with no client-specific code; any conforming stdio client should work, but this exact client hasn't been connected and observed. |
| 🧪 Experimental | Exists in the repo but has a known, unverified, or broken path. |
| ❌ Unsupported | Not implemented. |
| ❓ Not tested | No attempt has been made either way. |

**Important honesty note:** what was actually verified in this project is the server side of the contract — the process starts, performs the MCP `Implementation`/`ServerCapabilities` handshake objects via `io.modelcontextprotocol:kotlin-sdk-server`, keeps stdout free of anything but protocol bytes, and exits/propagates signals correctly through the npm launcher (see [security.md](security.md) and [`../bin/android-corporate-mcp.js`](../bin/android-corporate-mcp.js)). No specific client application (Claude Desktop, Cursor, etc.) was actually opened and connected to this server as part of building this documentation. Every client below is therefore marked 🟡, not ✅, unless a later contributor updates this table with real verification evidence.

## MCP clients

| Client | Status | Transport | Configuration | Notes |
|---|---|---|---|---|
| Claude Code | 🟡 Expected compatible | stdio | `.mcp.json`, `"mcpServers"` key | See [configuration.md](configuration.md#claude-code). |
| Claude Desktop | 🟡 Expected compatible | stdio | `claude_desktop_config.json`, `"mcpServers"` key | See [configuration.md](configuration.md#claude-desktop). |
| OpenAI Codex CLI | 🟡 Expected compatible | stdio | `~/.codex/config.toml`, `[mcp_servers.<name>]` | `codex mcp add` shortcut also works. |
| GitHub Copilot (VS Code) | 🟡 Expected compatible | stdio | `.vscode/mcp.json`, `"servers"` key, `"type": "stdio"` | Different key name from Claude/Cursor — see [configuration.md](configuration.md#github-copilot-vs-code). |
| Cursor | 🟡 Expected compatible | stdio | `.cursor/mcp.json`, `"mcpServers"` key | |
| OpenCode | 🟡 Expected compatible | stdio | `opencode.json`, `"mcp.servers"` key | Configuration shape carried over from `docs/MULTI_CLIENT.md`; not independently re-verified for this revision. |
| Continue | ❓ Not tested | — | — | No prior configuration for this client exists anywhere in this repository; nothing here has been verified against it. If you use Continue with this server, its MCP config format is standard and should accept the same `command`/`args` shape as the other clients — but that's an inference, not a demonstrated fact. |
| Any other stdio MCP client | 🟡 Expected compatible | stdio | client-specific | The server implements no client-specific behavior; anything that speaks MCP stdio should work the same way. |

## Distribution channels

| Channel | Status | Notes |
|---|---|---|
| npm (`android-corporate-mcp`) | 🟡 Expected compatible | Launcher (`bin/android-corporate-mcp.js`) tested directly in this session: starts the JVM, keeps stdout clean, checks for Java, forwards SIGINT/SIGTERM/SIGHUP, propagates exit codes. The npm package itself (`npm pack`, published tarball) was validated but not consumed through an actual `npm install` + client connection. |
| `./gradlew installDist` (local build) | ✅ Tested | Built and run directly in this session (`build/install/AndroidMasterMCP/bin/AndroidMasterMCP`). |
| `java -jar` (shadow JAR) | ✅ Tested | `build/libs/AndroidMasterMCP-<version>-all.jar`, built and run directly with `java -jar` in this session. |
| Docker | 🧪 Experimental — currently broken | `Dockerfile` exists at the repo root but its `COPY build/libs/android-corporate-mcp-server-*.jar app.jar` line references a JAR name that does not match the real shadow JAR output (`AndroidMasterMCP-<version>-all.jar`). As written, `docker build` will fail to find a matching file. Not fixed as part of this documentation pass — see [troubleshooting.md](troubleshooting.md#docker-build-fails-to-find-the-jar). |

## Platforms

| Platform | Status | Notes |
|---|---|---|
| macOS, Apple Silicon (arm64) | ✅ Tested | This is the platform this documentation was verified on (`Darwin`, `arm64`). |
| macOS, Intel (x86_64) | 🟡 Expected compatible | Pure JVM + Node; no native/architecture-specific code in this repo. |
| Linux x64 | 🟡 Expected compatible | Same reasoning; `gradlew` (not `gradlew.bat`) is the wrapper `GradleWrapperLocator` looks for first. |
| Linux ARM | 🟡 Expected compatible | Same reasoning. |
| Windows | 🟡 Expected compatible | `GradleWrapperLocator` explicitly falls back to `gradlew.bat` if `gradlew` isn't found/executable, and the npm `postinstall`/launcher scripts are plain Node with no POSIX-only calls. Signal handling in the launcher (`SIGINT`/`SIGTERM`/`SIGHUP` forwarding) is POSIX-signal semantics; Windows signal behavior via Node differs and hasn't been verified. |

## Runtime requirements

| Requirement | Needed for | Notes |
|---|---|---|
| Java 17+ | Running the server at all | Built and tested against JDK 22 (`kotlin { jvmToolchain(22) }` in `build.gradle.kts`). The npm launcher checks for `java` on `PATH` before attempting to start the server and fails with a clear stderr message if it's missing. |
| Node.js 18+ | The npm distribution only | `package.json` declares `"engines": { "node": ">=18" }`. Not needed if you run the JAR directly. |
| Gradle wrapper *in the target project* | The 7 process-backed tools (`gradle.tasks`, `gradle.run`, `dependencies.inspect`, `build.validate`, `staticAnalysis.run`, and `tests.run`/`lint.run` when triggering) | This server does not bundle or require its own Gradle install — it looks for `gradlew`/`gradlew.bat` inside the project you point it at. The 18 read-only tools work without it. |
| Android SDK | Only indirectly, via the target project's own Gradle build | This server never touches the Android SDK directly — no SDK path lookup, no `ANDROID_HOME` read anywhere in the codebase. If a triggered Gradle task (e.g. `compileDebugKotlin` for `build.validate`) needs the SDK, that requirement comes from the target project's own build, not from this server. |
| Git | Not required | No `git` invocation exists anywhere in the 25 tools. Only relevant if you're cloning this repository to contribute. |
