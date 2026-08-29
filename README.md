<p align="center">
  <img
    src="https://capsule-render.vercel.app/api?type=waving&height=220&color=0:0D1117,45:3DDC84,100:7F52FF&text=Android%20MCP&fontColor=FFFFFF&fontSize=54&fontAlignY=38&desc=Deterministic%20Android%20%2B%20Kotlin%20evidence%20for%20AI%20agents&descAlignY=60&descSize=18"
    alt="Android MCP banner" />
</p>

<p align="center">
  <a href="https://modelcontextprotocol.io/"><img src="https://img.shields.io/badge/Protocol-MCP-7F52FF?style=for-the-badge" alt="MCP" /></a>
  <img src="https://img.shields.io/badge/Kotlin-2.4-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Android-Evidence-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Status-Experimental-F4B400?style=for-the-badge" alt="Status" />
</p>

<p align="center">
  <a href="https://normansanchez.dev">normansanchez.dev</a> ·
  <a href="mailto:contact@normansanchez.dev">contact@normansanchez.dev</a>
</p>

---

Deterministic [Model Context Protocol](https://modelcontextprotocol.io/) server that gives AI agents **structured, verifiable evidence** about a real Android/Kotlin/Gradle repository — instead of letting them guess.

> **The MCP observes. The agent reasons.**

## The problem

An agent may know what a `ViewModel` or a Gradle convention plugin generally *is*, but it can't safely assume which architecture *this* repository actually uses, which plugins are applied, or whether a proposed change compiles — without either reading the whole repo itself or guessing. This server closes that gap: 26 tools inspect the real project (source, manifests, Gradle output, compiler-parsed symbols) and return what was actually found — never an inferred opinion.

## Philosophy

Every tool returns evidence: a file path, a line, a parsed value, a process exit code. Nothing is generated, summarized, or recommended by the server itself — that reasoning stays with the connected agent. See [docs/architecture.md](docs/architecture.md) for how that boundary is enforced end to end.

## Quick start

```bash
npm install -g android-corporate-mcp
```

Add to your MCP client config (Claude Code `.mcp.json` shown; see [docs/configuration.md](docs/configuration.md) for every supported client):

```json
{
  "mcpServers": {
    "android-mcp": {
      "command": "android-corporate-mcp"
    }
  }
}
```

Restart your client, open an Android/Gradle repository, and call a tool:

```json
// tools/call project.inspect { "projectRoot": "/absolute/path/to/your-android-project" }
{
  "status": "success",
  "modules": ["app", "core:network", "feature:home"],
  "settingsFile": "settings.gradle.kts"
}
```

Full walkthrough: [docs/getting-started.md](docs/getting-started.md).

## What it can do

26 tools across project discovery, Kotlin symbol analysis, Gradle execution, build validation, security auditing, and flow detection — full contract for each in [docs/tools.md](docs/tools.md).

| Category | Examples |
|---|---|
| Project & Kotlin | `project.inspect`, `manifest.inspect`, `symbol.find`, `symbol.references`, `module.graph` |
| Gradle execution | `gradle.tasks`, `gradle.run`, `tests.run`, `lint.run`, `dependencies.inspect` |
| Architecture & build | `architecture.detect`, `build.validate`, `staticAnalysis.run` |
| Android deep inspection | `navigation.graph`, `resource.references`, `manifest.merge` |
| Security | `proguard.inspect`, `security.audit` |

## Compatibility

Speaks standard MCP stdio — no client-specific code. Verified directly on macOS (Apple Silicon) with the local build/JAR/npm launcher; other MCP clients and platforms are expected-compatible but not yet individually confirmed. Full, honestly-graded matrix (tested vs. expected vs. untested) in [docs/compatibility.md](docs/compatibility.md).

## Architecture

```mermaid
flowchart TB
    A["AI Agent<br/>(Claude, Codex, Copilot...)"] -->|MCP stdio| B["Android MCP Server"]
    B -->|reads / executes| C[("Android + Kotlin<br/>repository")]
    C -->|evidence, JSON| B
    B -->|structured evidence| A
    A -->|reasons, decides| D["Implementation"]

    style A fill:#7F52FF,color:#fff
    style B fill:#0D1117,color:#3DDC84,stroke:#3DDC84
    style C fill:#3DDC84,color:#0D1117
    style D fill:#F4B400,color:#0D1117
```

One process, local stdio transport, no daemon, no persisted state between calls. Full lifecycle, handshake, and a real end-to-end request walkthrough: [docs/architecture.md](docs/architecture.md).

## Security & local-first

The server runs with the same filesystem and process permissions as the user who launches it, and makes no outbound network call in any of its 26 tools — everything stays on your machine, over local stdio. `gradle.run` executes Gradle tasks the caller names (validated only for syntax, not restricted to a curated safe list), which is the one tool worth restricting if you connect this server to an agent you don't fully trust. Full breakdown, with no unverifiable "100% secure" claims: [docs/security.md](docs/security.md).

## Documentation

[Getting started](docs/getting-started.md) · [Architecture](docs/architecture.md) · [Tools](docs/tools.md) · [Compatibility](docs/compatibility.md) · [Configuration](docs/configuration.md) · [Security](docs/security.md) · [Limitations](docs/limitations.md) · [Development](docs/development.md) · [Contributing](docs/contributing.md) · [Troubleshooting](docs/troubleshooting.md)

## Contributing

See [docs/contributing.md](docs/contributing.md) for the real branching model, PR expectations, and how to add a new tool ([docs/development.md](docs/development.md)).

## License

`package.json` declares MIT, but no `LICENSE` file exists in this repository yet — treat licensing as unresolved until that's added (tracked in [docs/contributing.md](docs/contributing.md)).

---

<p align="center">
  Built by <a href="https://normansanchez.dev">Norman Sánchez</a><br/>
  <a href="mailto:contact@normansanchez.dev">contact@normansanchez.dev</a>
</p>
