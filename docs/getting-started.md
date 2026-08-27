# Getting Started

This walks you from zero to a working tool call. It assumes no prior knowledge of the project.

## Prerequisites

| Requirement                          | Why                                                                                                                                              |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| Java 17+ (JDK or JRE)                | The server itself runs on the JVM. Built and tested against JDK 22.                                                                              |
| An MCP-capable client                | Claude Code, Claude Desktop, Codex CLI, VS Code (Copilot), Cursor, or any client that speaks MCP over stdio.                                     |
| An Android/Gradle project to inspect | The server has nothing to do without a target repository. It does **not** need to be able to build that project for the read-only tools to work. |

Node.js is only required if you install via npm (recommended). If you build the JAR yourself and run it with `java -jar`, Node is not needed.

## 1. Install

**Via npm** (published package: `android-corporate-mcp`):

```bash
npm install -g android-corporate-mcp
```

This installs a CLI named `android-corporate-mcp` that locates the bundled JAR and runs it with `java -jar`. See [`../package.json`](../package.json) and [`../bin/android-corporate-mcp.js`](../bin/android-corporate-mcp.js) for the real implementation.

**From source** (if you're building against a local checkout):

```bash
git clone <this-repo>
cd AndroidCorporateMCP
./gradlew installDist
```

produces a launcher at `build/install/AndroidMasterMCP/bin/AndroidMasterMCP`.

## 2. Configure your MCP client

Every client needs the same two things: a command to run, and (for `java -jar` installs) a path to the JAR. See [compatibility.md](compatibility.md) for the full client matrix and [configuration.md](configuration.md) for copy-pasteable snippets. Minimal Claude Code example (`.mcp.json`):

```json
{
  "mcpServers": {
    "android-mcp": {
      "command": "android-corporate-mcp"
    }
  }
}
```

(assumes global npm install; use an absolute path to the JAR if you built from source instead.)

## 3. Open an Android repository

Every tool call takes a `projectRoot` argument (an absolute path to the project you want inspected). The server itself doesn't need to be launched "inside" that project — you pass the path per call. Pick any Gradle/Android checkout you have locally.

## 4. Verify the connection

Restart your MCP client after editing its config. Confirm the server started by asking it to list its tools, or use the reference inspector:

```bash
npx @modelcontextprotocol/inspector android-corporate-mcp
```

This opens a browser UI listing all 25 registered tools (see [tools.md](tools.md) for the full reference) and lets you call them directly.

## 5. Call your first tool

`project.inspect` is the natural starting point — it's read-only and doesn't require Gradle to succeed:

```json
{
  "tool": "project.inspect",
  "arguments": {
    "projectRoot": "/absolute/path/to/your/android-project"
  }
}
```

A working project returns `"status": "success"` with the discovered modules, their type (`application`/`library`/`unknown`), and evidence pointing at the exact build files that justified each classification. See [tools.md](tools.md#projectinspect) for the full contract.

## What's next

- [architecture.md](architecture.md) — how the server is built and what happens on a tool call, end to end.
- [tools.md](tools.md) — the complete, evidence-based reference for all 25 tools.
- [security.md](security.md) — what the server reads, executes, and never sends anywhere.
- [troubleshooting.md](troubleshooting.md) — diagnosable fixes for the failure modes that actually occur in this codebase.
