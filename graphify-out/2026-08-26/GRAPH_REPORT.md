# Graph Report - /Users/normansanchez/AI/projects/concept-test/AndroidCorporateMCP  (2026-08-23)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 81 nodes · 90 edges · 11 communities (10 shown, 1 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- bash
- ProjectInspectTool
- permission
- ManifestInspectTool
- opencode.json
- AndroidModuleType
- gradlew
- graphify.js

## God Nodes (most connected - your core abstractions)
1. `bash` - 22 edges
2. `permission` - 12 edges
3. `ProjectInspectTool` - 9 edges
4. `AndroidModuleEvidence` - 5 edges
5. `AndroidModuleType` - 5 edges
6. `ManifestInspectTool` - 5 edges
7. `compaction` - 3 edges
8. `read` - 3 edges
9. `plugin` - 2 edges
10. `$schema` - 1 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (11 total, 1 thin omitted)

### Community 0 - "bash"
Cohesion: 0.09
Nodes (22): cat ~/.config/*, cat ~/.local/*, cat ~/.ssh/*, cd ..*, find ./*, git add*, git clean*, git commit* (+14 more)

### Community 1 - "ProjectInspectTool"
Cohesion: 0.22
Nodes (4): JsonArray, AndroidModuleEvidence, JsonObject, ProjectInspectTool

### Community 2 - "permission"
Cohesion: 0.15
Nodes (13): permission, doom_loop, edit, external_directory, glob, grep, list, lsp (+5 more)

### Community 3 - "ManifestInspectTool"
Cohesion: 0.46
Nodes (3): Element, JsonObject, ManifestInspectTool

### Community 4 - "opencode.json"
Cohesion: 0.29
Nodes (6): compaction, auto, prune, plugin, $schema, .opencode/plugins/graphify.js

### Community 5 - "AndroidModuleType"
Cohesion: 0.40
Nodes (4): AndroidModuleType, APPLICATION, LIBRARY, UNKNOWN

### Community 6 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **39 isolated node(s):** `$schema`, `auto`, `prune`, `*.env`, `*.env.example` (+34 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **1 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `bash` connect `bash` to `permission`?**
  _High betweenness centrality (0.199) - this node is a cross-community bridge._
- **Why does `permission` connect `permission` to `bash`, `opencode.json`?**
  _High betweenness centrality (0.179) - this node is a cross-community bridge._
- **What connects `$schema`, `auto`, `prune` to the rest of the system?**
  _39 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `bash` be split into smaller, more focused modules?**
  _Cohesion score 0.09090909090909091 - nodes in this community are weakly interconnected._