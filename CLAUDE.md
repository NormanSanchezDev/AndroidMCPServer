## CHANGELOG

Este proyecto mantiene `CHANGELOG.md` (formato Keep a Changelog) para dar seguimiento a features a nivel de negocio por release.

Regla fija: **antes de crear cualquier commit, actualizar la sección `[Unreleased]` de `CHANGELOG.md`** con una entrada legible a nivel de negocio (qué cambió y por qué importa, no el diff técnico). Al cortar un release, mover `[Unreleased]` a una sección `[x.y.z] - AAAA-MM-DD` nueva.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
