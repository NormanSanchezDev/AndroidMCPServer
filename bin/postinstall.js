#!/usr/bin/env node

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

const jarDir = path.join(__dirname, '..', 'jars');

if (!fs.existsSync(jarDir)) {
  fs.mkdirSync(jarDir, { recursive: true });
}

const jarFiles = fs.readdirSync(jarDir).filter(f => f.endsWith('.jar'));
if (jarFiles.length > 0) {
  console.log('JAR already installed.');
  process.exit(0);
}

console.log('android-corporate-mcp installed successfully.');
console.log('Configure your MCP client to use: npx android-corporate-mcp');
