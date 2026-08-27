#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

const jarDir = path.join(__dirname, '..', 'jars');
const buildDir = path.join(__dirname, '..', 'build', 'libs');

let jarDirToUse = jarDir;
if (!fs.existsSync(jarDir)) {
  jarDirToUse = buildDir;
}

if (!fs.existsSync(jarDirToUse)) {
  console.error('Error: JAR file not found. Please rebuild or reinstall the package.');
  process.exit(1);
}

const jarFile = fs.readdirSync(jarDirToUse).find(f => f.endsWith('-all.jar') || f.endsWith('.jar'));

if (!jarFile) {
  console.error('Error: JAR file not found. Please rebuild or reinstall the package.');
  process.exit(1);
}

const jarPath = path.join(jarDirToUse, jarFile);

const java = spawn('java', ['-jar', jarPath, ...process.argv.slice(2)], {
  stdio: 'inherit',
  env: process.env
});

java.on('close', (code) => {
  process.exit(code || 0);
});
