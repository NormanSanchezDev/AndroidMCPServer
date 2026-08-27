# Android Corporate MCP Server

Model Context Protocol (MCP) server for Android/Gradle project intelligence.

## Quick Start

### Claude Desktop

Add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "java",
      "args": ["-jar", "/path/to/android-corporate-mcp-server.jar"]
    }
  }
}
```

### VS Code Copilot

Add to `.vscode/mcp.json`:

```json
{
  "servers": {
    "android-corporate": {
      "command": "java",
      "args": ["-jar", "/path/to/android-corporate-mcp-server.jar"]
    }
  }
}
```

### Cursor

Add to MCP settings:

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "java",
      "args": ["-jar", "/path/to/android-corporate-mcp-server.jar"]
    }
  }
}
```

### OpenCode

Add to `opencode.json`:

```json
{
  "mcp": {
    "servers": {
      "android-corporate": {
        "command": "java",
        "args": ["-jar", "/path/to/android-corporate-mcp-server.jar"]
      }
    }
  }
}
```

### Docker

```bash
docker build -t android-corporate-mcp .
docker run -v /path/to/android-project:/project android-corporate-mcp
```

Then configure your MCP client to connect to the container.

## Available Tools

| Tool | Description |
|------|-------------|
| `project.inspect` | Module discovery, plugin detection |
| `manifest.inspect` | Full XML parsing, components, permissions |
| `entry_points.find` | Launcher, deep links, exported |
| `symbol.find` | PSI-based declaration search |
| `symbol.references` | Identifier-level occurrence search |
| `symbol.hierarchy` | Class/interface hierarchy tree |
| `gradle.tasks` | Real `gradlew tasks --all` output |
| `gradle.run` | Allow-listed task execution |
| `gradle.config` | SDK versions, plugins, Compose status |
| `gradle.versionCatalog` | Version catalog parsing |
| `gradle.conventionPlugins` | Convention plugin discovery |
| `tests.run` | JUnit XML aggregation |
| `tests.discover` | Find tests for a class |
| `lint.run` | Lint XML aggregation |
| `dependencies.inspect` | Dependency tree parsing |
| `module.graph` | Static module dependency graph |
| `resources.inspect` | res/ enumeration |
| `resource.references` | Trace resource usage in code/XML |
| `architecture.detect` | DI, Compose, ViewModel patterns |
| `build.validate` | Compile module with structured output |
| `staticAnalysis.run` | Detekt, ktlint, Kover |
| `navigation.graph` | NavHost + Compose Navigation |
| `manifest.merge` | Detect manifest merge conflicts |
| `proguard.inspect` | R8/ProGuard rules inspection |
| `security.audit` | Exported components, secrets, cleartext |

## Building from Source

```bash
./gradlew build
./gradlew shadowJar  # Creates fat JAR in build/libs/
```

## Requirements

- JDK 22+
- Gradle wrapper (included)
- Target project should have `gradlew` wrapper for Gradle tools
