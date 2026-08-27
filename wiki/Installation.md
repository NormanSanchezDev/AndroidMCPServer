# Installation Guide

This guide covers all ways to install Android Corporate MCP.

## Prerequisites

Before installing, ensure you have:

| Requirement | Version | Check Command |
|-------------|---------|---------------|
| Java (JDK) | 22+ | `java -version` |
| Node.js (for npm) | 18+ | `node --version` |
| npm | 9+ | `npm --version` |

### Installing JDK 22

**macOS (Homebrew):**
```bash
brew install --cask temurin
```

**Linux (apt):**
```bash
sudo apt update
sudo apt install openjdk-22-jdk
```

**Windows (Chocolatey):**
```powershell
choco install temurin
```

### Verify Java

```bash
java -version
# Should output something like:
# openjdk version "22.0.1" 2024-04-16
```

---

## Method 1: npx (Recommended)

No installation required — `npx` downloads and runs automatically.

```bash
npx android-corporate-mcp
```

**For MCP clients**, use this configuration:

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

---

## Method 2: Global npm Install

Installs globally for reuse across projects.

```bash
npm install -g android-corporate-mcp
```

**For MCP clients:**

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "android-corporate-mcp"
    }
  }
}
```

---

## Method 3: Direct JAR Download

For users who want to run the server directly without npm.

1. Download the JAR from [GitHub Releases](https://github.com/normansanchez/AndroidCorporateMCP/releases)
2. Place it somewhere stable, e.g. `/usr/local/lib/`
3. Run it directly:

```bash
java -jar /usr/local/lib/android-corporate-mcp-0.1.0-all.jar
```

**For MCP clients:**

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "java",
      "args": ["-jar", "/usr/local/lib/android-corporate-mcp-0.1.0-all.jar"]
    }
  }
}
```

---

## Method 4: Docker

For containerized environments or CI.

```bash
docker pull normansanchez/android-corporate-mcp:latest
```

**For MCP clients:**

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "docker",
      "args": [
        "run", "--rm", "-i",
        "-v", "/path/to/your/android-project:/project",
        "normansanchez/android-corporate-mcp:latest"
      ]
    }
  }
}
```

**Note:** Docker requires mounting your Android project directory as a volume.

---

## Method 5: Build from Source

For contributors or users who want the latest features.

```bash
# Clone the repository
git clone https://github.com/normansanchez/AndroidCorporateMCP.git
cd AndroidCorporateMCP

# Build the shadow JAR
./gradlew shadowJar

# The JAR is created at:
# build/libs/AndroidMasterMCP-0.1.0-all.jar
```

---

## Verification

After installation, verify the server works:

### Check the JAR version

```bash
java -jar /path/to/android-corporate-mcp-0.1.0-all.jar --version
```

### Connect a client

1. Open your MCP client (Claude Desktop, VS Code, Cursor)
2. Add the MCP server configuration
3. Restart the client
4. Look for "android-corporate" in available tools

If you see tools like `gradle.config`, `architecture.detect`, `security.audit`, the installation worked.

---

## What's Next?

- **[Configuration](Configuration)** — Set up the server for your specific AI assistant
- **[Tools Reference](Tools-Reference)** — Explore all 27 available tools
- **[Examples](Examples)** — See practical usage examples
