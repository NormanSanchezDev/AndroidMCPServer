# Configuration

Copy-pasteable MCP client configuration, using the real commands this project actually produces. See [compatibility.md](compatibility.md) for which of these have been directly verified versus expected-compatible.

All configurations below assume you've installed the server (`npm install -g android-corporate-mcp`, giving you the `android-corporate-mcp` command) — see [getting-started.md](getting-started.md#1-install) if not. If you built from source instead, replace `"android-corporate-mcp"` with the absolute path to `build/install/AndroidMasterMCP/bin/AndroidMasterMCP`, or use `"java"` with `"-jar", "/absolute/path/to/AndroidMasterMCP-<version>-all.jar"`.

No tool takes configuration at server-startup time — every tool call carries its own `projectRoot`, so one server instance can be pointed at any Android project per call. There's no repo-specific setting to put in these configs.

## Claude Code

Project-scoped `.mcp.json` in your repository root:

```json
{
  "mcpServers": {
    "android-mcp": {
      "command": "android-corporate-mcp"
    }
  }
}
```

Restart Claude Code. Tools register as `mcp__android-mcp__<tool_name>` (e.g. `mcp__android-mcp__project.inspect`).

## Claude Desktop

`claude_desktop_config.json` — macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`; Windows: `%APPDATA%\Claude\claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "android-mcp": {
      "command": "android-corporate-mcp"
    }
  }
}
```

## OpenAI Codex CLI

```bash
codex mcp add android-mcp -- android-corporate-mcp
```

or directly in `~/.codex/config.toml`:

```toml
[mcp_servers.android-mcp]
command = "android-corporate-mcp"
```

## GitHub Copilot (VS Code)

`.vscode/mcp.json` — note the key is `"servers"`, not `"mcpServers"`, and each entry needs an explicit `"type"`:

```json
{
  "servers": {
    "android-mcp": {
      "type": "stdio",
      "command": "android-corporate-mcp"
    }
  }
}
```

## Cursor

`.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "android-mcp": {
      "command": "android-corporate-mcp"
    }
  }
}
```

## Verifying a configuration

```bash
npx @modelcontextprotocol/inspector android-corporate-mcp
```

opens a browser UI that performs the real MCP handshake and lists all 25 tools — use it to sanity-check a `command`/`args` pair before wiring it into a client config.

## No environment-variable configuration

Nothing in `server/Main.kt` or any of the 25 tool implementations reads an environment variable at startup. There is no way to configure default project roots, timeouts, or output limits without editing source — every tunable (timeouts, result caps) is a per-call argument with a hardcoded default. See [tools.md](tools.md) for the actual default values per tool.
