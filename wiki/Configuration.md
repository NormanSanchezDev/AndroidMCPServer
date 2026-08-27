# Configuration Guide

Configure Android Corporate MCP for your preferred AI assistant.

---

## Table of Contents

- [Claude Desktop](#claude-desktop)
- [VS Code Copilot](#vs-code-copilot)
- [Cursor](#cursor)
- [OpenCode](#opencode)
- [Claude Code](#claude-code)
- [Zed](#zed)
- [Common Configuration Patterns](#common-configuration-patterns)

---

## Claude Desktop

### 1. Locate the configuration file

| OS | Path |
|----|------|
| macOS | `~/Library/Application Support/Claude/claude_desktop_config.json` |
| Windows | `%APPDATA%\Claude\claude_desktop_config.json` |
| Linux | `~/.config/Claude/claude_desktop_config.json` |

### 2. Add the server

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "npx",
      "args": ["android-corporate-mcp"]
    }
  }
}
```

### 3. Restart Claude Desktop

Fully quit and reopen Claude Desktop. The new tools appear in the tools list.

---

## VS Code Copilot

### 1. Open the MCP settings

Menu: `View` → `Command Palette` → `MCP: Open Configuration File`

Or create `.vscode/mcp.json` in your project:

### 2. Add the server

```json
{
  "servers": {
    "android-corporate": {
      "type": "stdio",
      "command": "npx",
      "args": ["android-corporate-mcp"]
    }
  }
}
```

### 3. Restart VS Code

Reload the window (`Cmd+Shift+P` → `Developer: Reload Window`).

---

## Cursor

### 1. Open MCP settings

Menu: `Settings` → `MCP Servers`

### 2. Add the server

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "npx",
      "args": ["android-corporate-mcp"]
    }
  }
}
```

### 3. Save and enable

The server should appear in your MCP server list with status "connected".

---

## OpenCode

### 1. Edit the configuration

Modify `opencode.json` in your project or global config at `~/.config/opencode/opencode.json`:

```json
{
  "mcp": {
    "servers": {
      "android-corporate": {
        "command": "npx",
        "args": ["android-corporate-mcp"]
      }
    }
  }
}
```

### 2. Restart opencode

Exit and restart the opencode CLI session.

---

## Claude Code

### 1. Register the server

```bash
claude mcp add android-corporate -- npx android-corporate-mcp
```

### 2. Verify

```bash
claude mcp list
```

---

## Zed

### 1. Edit the configuration

`~/.config/zed/settings.json`:

```json
{
  "context_servers": {
    "android-corporate": {
      "command": "npx",
      "args": ["android-corporate-mcp"]
    }
  }
}
```

---

## Common Configuration Patterns

### Using a specific JAR version

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/android-corporate-mcp-0.1.0-all.jar"
      ]
    }
  }
}
```

### Using npx with a specific version

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "npx",
      "args": ["android-corporate-mcp@0.1.0"]
    }
  }
}
```

### Using Docker with a mounted project

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "docker",
      "args": [
        "run", "--rm", "-i",
        "-v", "/path/to/android/project:/project",
        "normansanchez/android-corporate-mcp:latest"
      ]
    }
  }
}
```

---

## Verification

After configuration:

1. Restart your AI assistant
2. Look for the `android-corporate` server in your MCP tools list
3. Try asking: *"What tools are available for this Android project?"*

If the server fails to connect, see [Troubleshooting](Troubleshooting).
