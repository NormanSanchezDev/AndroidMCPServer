# Troubleshooting Guide

Solutions to common issues when using Android Corporate MCP.

---

## Table of Contents

- [Server Cannot Connect](#server-cannot-connect)
- [Tools Not Appearing](#tools-not-appearing)
- [Gradle Not Found](#gradle-not-found)
- [Java Version Errors](#java-version-errors)
- [npx Issues](#npx-issues)
- [Performance Issues](#performance-issues)
- [Security Warnings](#security-warnings)

---

## Server Cannot Connect

### Symptom

The MCP server fails to connect in your AI assistant, showing a red "failed" status or connection error.

### Common Causes & Fixes

**1. Wrong command or arguments**

Verify your configuration matches the exact format in the [Configuration guide](Configuration).

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

**2. JAR path is incorrect (manual install)**

For direct JAR usage, ensure the path exists:

```bash
ls -la /path/to/android-corporate-mcp-0.1.0-all.jar
```

**3. Client not restarted**

After any configuration change, **fully quit and reopen** your AI assistant. A restart is often required to load new MCP servers.

**4. Logs show SLF4J warnings**

These warnings are normal and harmless:
```
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
```

---

## Tools Not Appearing

### Symptom

The server connects but you don't see Android tools in your assistant.

### Fix

1. Verify the server appears in your client's MCP server list
2. Confirm the status is "connected" (not "failed")
3. Restart the client
4. Try asking the assistant: *"What MCP tools do you have available?"*

### If tools still don't appear

Check that you're using a client that supports MCP tools (Claude Desktop, VS Code Copilot, Cursor, OpenCode). Some basic chat interfaces don't expose MCP tools.

---

## Gradle Not Found

### Symptom

Tools like `gradle.tasks`, `gradle.run`, `build.validate` return:

```json
{
  "status": "gradle_not_available",
  "error": "No gradlew wrapper found"
}
```

### Fix

The target project must contain a `gradlew` wrapper:

```bash
# Check if the wrapper exists
ls project-root/gradlew

# If missing, generate it in the target project
cd project-root
gradle wrapper
```

---

## Java Version Errors

### Symptom

```
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.UnsupportedClassVersionError
```

### Fix

Ensure JDK 22+ is installed and set as default:

```bash
# Check current Java version
java -version

# Set JDK 22 as default (macOS with Homebrew)
export JAVA_HOME=$(/usr/libexec/java_home -v 22)
```

### Multiple Java versions

If you have multiple JDKs, explicitly point to JDK 22 in your configuration:

```json
{
  "mcpServers": {
    "android-corporate": {
      "command": "/path/to/jdk-22/bin/java",
      "args": ["-jar", "/path/to/android-corporate-mcp-0.1.0-all.jar"]
    }
  }
}
```

---

## npx Issues

### Symptom

```
npm ERR! code ENOENT
npm ERR! syscall open
npm ERR! path /root/.npm/_cacache/...
```

### Fixes

**1. Clear npm cache:**

```bash
npm cache clean --force
```

**2. Use a specific version:**

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

**3. Preinstall globally:**

```bash
npm install -g android-corporate-mcp
```

Then use the global binary:
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

## Performance Issues

### Symptom

Gradle tools (`gradle.tasks`, `build.validate`, `tests.run`) take a long time.

### Explanation

These tools execute real Gradle commands, which can be slow on first run (downloading dependencies, configuration).

### Tips

- Use `timeoutSeconds` to cap execution time
- Prefer `lint.run` / `tests.run` without `trigger: true` to read cached reports
- Run Gradle once manually first (`./gradlew build`) to warm the cache
- Add `--build-cache` flag to `gradle.run` for incremental builds

---

## Security Warnings

### Symptom

`security.audit` reports exported components or other findings.

### What this means

The `security.audit` tool detects real or potential security issues in your project:

- **Exported activity/service/receiver** — accessible from other apps
- **Hardcoded secrets** — API keys, passwords in source code
- **Cleartext traffic** — HTTP instead of HTTPS

### Remediation

1. Review each finding in the `findings` array
2. For exported components, add `android:permission` or set `android:exported="false"`
3. Move hardcoded secrets to secure storage or `local.properties`
4. Enable HTTPS for network traffic

---

## Still Having Issues?

- Check the [GitHub Issues](https://github.com/normansanchez/AndroidCorporateMCP/issues) for known problems
- Create a new issue with:
  - Your OS and Java version
  - The client you're using (Claude Desktop, VS Code, etc.)
  - Your exact MCP configuration
  - Any error messages
