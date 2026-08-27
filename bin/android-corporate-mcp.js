#!/usr/bin/env node

const { spawnSync, spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

// MCP speaks stdio: anything printed here on stdout corrupts the protocol
// stream, so all diagnostics must go to stderr, never stdout.
function fail(message) {
  console.error(`[android-corporate-mcp] ${message}`);
  process.exit(1);
}

const javaCheck = spawnSync('java', ['-version']);
if (javaCheck.error || javaCheck.status !== 0) {
  fail(
    'Java runtime not found on PATH. Install a JDK/JRE (Java 17+) and ensure ' +
    '"java" is available, then retry.'
  );
}

const jarDir = path.join(__dirname, '..', 'jars');
const buildDir = path.join(__dirname, '..', 'build', 'libs');

const jarDirToUse = fs.existsSync(jarDir) ? jarDir : buildDir;

if (!fs.existsSync(jarDirToUse)) {
  fail('JAR file not found. Please rebuild or reinstall the package.');
}

const jarFile = fs
  .readdirSync(jarDirToUse)
  .find((f) => f.endsWith('-all.jar')) ||
  fs.readdirSync(jarDirToUse).find((f) => f.endsWith('.jar'));

if (!jarFile) {
  fail('JAR file not found. Please rebuild or reinstall the package.');
}

const jarPath = path.join(jarDirToUse, jarFile);

const child = spawn('java', ['-jar', jarPath, ...process.argv.slice(2)], {
  stdio: 'inherit',
  env: process.env
});

child.on('error', (err) => {
  fail(`Failed to launch Java process: ${err.message}`);
});

// Forward termination signals to the child so the JVM shuts down cleanly
// instead of being orphaned when the parent Node process is killed.
const forwardedSignals = ['SIGINT', 'SIGTERM', 'SIGHUP'];
forwardedSignals.forEach((signal) => {
  process.on(signal, () => {
    if (!child.killed) {
      child.kill(signal);
    }
  });
});

child.on('close', (code, signal) => {
  if (signal) {
    // Match POSIX convention: 128 + signal number, falling back to a
    // generic failure code if the signal can't be mapped.
    const signalExitCodes = { SIGINT: 130, SIGTERM: 143, SIGHUP: 129 };
    process.exit(signalExitCodes[signal] || 1);
  }
  process.exit(code === null ? 1 : code);
});
