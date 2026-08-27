# Android Corporate MCP

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![MCP](https://img.shields.io/badge/MCP-Server-blue.svg)](https://modelcontextprotocol.io)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-purple.svg)](https://kotlinlang.org)
[![Tests](https://img.shields.io/badge/Tests-68-green.svg)](#testing)

> **Model Context Protocol server for Android/Gradle project intelligence**

A powerful MCP server that provides AI assistants with deep insights into Android projects. Analyze architecture, detect patterns, validate builds, and ensure security compliance — all through natural language.

## What is this?

Android Corporate MCP is a server that implements the [Model Context Protocol](https://modelcontextprotocol.io) to expose Android project analysis tools to AI assistants like Claude, GitHub Copilot, and Cursor.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Your AI Assistant                        │
│                   (Claude, VS Code Copilot, Cursor)             │
└───────────────────────────┬─────────────────────────────────────┘
                            │ MCP Protocol (stdio)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Android Corporate MCP                         │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │   Gradle    │  │  Architecture│  │   Security  │            │
│  │ Intelligence│  │  Discovery   │  │   Audit     │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │   Symbol    │  │   Build     │  │ Navigation  │            │
│  │  Analysis   │  │ Validation  │  │   Graph     │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Your Android Project                       │
│                   (Gradle, Kotlin, Java, XML)                   │
└─────────────────────────────────────────────────────────────────┘
```

## Features

### 🎯 27 Analysis Tools

| Category | Tools | Description |
|----------|-------|-------------|
| **Gradle Intelligence** | `gradle.config`, `gradle.versionCatalog`, `gradle.conventionPlugins` | Deep Gradle configuration analysis |
| **Architecture Discovery** | `architecture.detect`, `tests.discover` | Detect DI, Compose, ViewModel patterns |
| **Symbol Analysis** | `symbol.find`, `symbol.references`, `symbol.hierarchy` | PSI-based code analysis |
| **Build Validation** | `build.validate`, `staticAnalysis.run` | Compile and lint checks |
| **Android Inspection** | `manifest.merge`, `navigation.graph`, `resource.references` | Manifest and navigation analysis |
| **Security & Compliance** | `security.audit`, `proguard.inspect` | Vulnerability detection |

### 🚀 Key Capabilities

- **Real Gradle Execution** — Runs actual Gradle tasks, not just static analysis
- **PSI-Based Analysis** — Uses Kotlin compiler for accurate symbol resolution
- **Cross-Module Detection** — Analyzes relationships between modules
- **Security Auditing** — Detects exported components, hardcoded secrets, cleartext traffic
- **Pattern Detection** — Identifies Hilt, Koin, Compose, MVVM, MVI patterns

## Quick Start

### Installation

```bash
# Using npx (recommended)
npx android-corporate-mcp

# Or install globally
npm install -g android-corporate-mcp
```

### Configuration

Add to your AI assistant's MCP configuration:

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

### Usage

Ask your AI assistant:

> "Analyze the architecture of this Android project"
> 
> "What dependencies does the app module have?"
> 
> "Are there any security vulnerabilities in the manifest?"
> 
> "Show me the navigation graph"

## Documentation

- **[Installation Guide](Installation)** — Detailed setup instructions
- **[Configuration](Configuration)** — Client-specific configuration
- **[Tools Reference](Tools-Reference)** — Complete tool documentation
- **[Examples](Examples)** — Usage examples and patterns
- **[Troubleshooting](Troubleshooting)** — Common issues and solutions
- **[API Reference](API-Reference)** — Technical API documentation

## Requirements

- **JDK 22+** — Required for Kotlin compilation
- **Node.js 18+** — Required for npm distribution
- **Gradle Wrapper** — Target project should have `gradlew`

## Architecture

```
android-corporate-mcp/
├── src/main/kotlin/
│   ├── server/           # MCP server setup
│   ├── tools/            # 25 analysis tools
│   ├── gradle/           # Gradle parsers
│   ├── architecture/     # Pattern detection
│   ├── symbol/           # PSI analysis
│   ├── navigation/       # Navigation graph
│   ├── staticanalysis/   # Lint/analysis
│   └── process/          # Process execution
├── src/test/             # 68 tests
├── bin/                  # npm wrapper
└── .github/workflows/    # CI/CD
```

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "dev.normansanchez.androidmcp.tools.SecurityAuditToolTest"
```

**68 tests** covering all 27 tools with unit and integration tests.

## Contributing

Contributions are welcome! Please see [Contributing Guide](Contributing) for details.

## License

MIT License — see [LICENSE](LICENSE) for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/normansanchez/AndroidCorporateMCP/issues)
- **Documentation**: [Wiki](https://github.com/normansanchez/AndroidCorporateMCP/wiki)
- **MCP Protocol**: [modelcontextprotocol.io](https://modelcontextprotocol.io)

---

**Built with ❤️ for the Android community**
