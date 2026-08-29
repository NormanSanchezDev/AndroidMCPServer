# Scope & Limitations

## In scope

What `AndroidCorporateMCP` actually does, based on its 26 registered tools:

- Discovers Gradle/Android project structure, modules, and plugin types (`project.inspect`, `module.graph`).
- Parses Android manifests, resources, navigation graphs, and ProGuard/R8 config structurally (`manifest.inspect`, `entry_points.find`, `resources.inspect`, `navigation.graph`, `manifest.merge`, `proguard.inspect`).
- Finds and cross-references Kotlin declarations using the real Kotlin compiler parser, not text search (`symbol.find`, `symbol.references`, `symbol.hierarchy`).
- Runs and parses real Gradle output — tasks, dependency trees, tests, lint, compilation, static analysis (`gradle.tasks`, `gradle.run`, `dependencies.inspect`, `tests.run`, `lint.run`, `build.validate`, `staticAnalysis.run`).
- Detects common architectural patterns (DI framework, Compose, ViewModel, reactive types) and common security misconfigurations, from pattern evidence with file/line pointers (`architecture.detect`, `security.audit`).

Every one of these returns *evidence* — a file, a line, a parsed value, a process exit code — never a generated recommendation. See [architecture.md](architecture.md) for the reasoning/evidence boundary and [tools.md](tools.md) for the complete, per-tool contract.

## Out of scope

Verified against the actual implementation, not asserted from marketing intuition:

- **Not an LLM.** There is no model, no embeddings, no generation anywhere in this codebase. Every tool is deterministic parsing or subprocess execution.
- **Does not replace the coding agent.** The server has no tool that writes or edits source files. Nothing in `src/main/kotlin/dev/normansanchez/androidmcp/tools/` calls `Files.write` or equivalent on a project file — every tool either reads, or executes a Gradle task that Gradle itself may write outputs for (build artifacts, reports), not source edits.
- **Does not store repositories.** Confirmed in [security.md](security.md) — no database, no cache, no persisted copy of anything read. Every response is computed fresh per call.
- **Does not send code to external services.** Confirmed in [security.md](security.md) — no outbound network call exists in any of the 26 tools.
- **Does not decide architecture.** `architecture.detect` reports what patterns it found (DI framework, Compose usage, etc.) with evidence; it does not recommend an architecture or flag one as wrong.
- **Does not invent evidence.** Every non-`"success"` status (`not_available`, `not_found`, `invalid_project`, etc.) is a distinct, deterministic signal for *why* no evidence was returned, rather than a fabricated result. See [architecture.md#error-propagation](architecture.md#error-propagation).

## Known limitations by design

These aren't bugs — they're consequences of how the tools are implemented, worth knowing before you rely on a result:

- **Identifier-level, not type-level, symbol matching.** `symbol.references` says this in its own response (`limitation` field): occurrences of the same name are reported "even when they refer to different types." Two unrelated classes both named `Repository` will show up in the same reference search.
- **`resource.references` is a string-pattern search**, not a resolved-reference lookup — it matches `R.<type>.<name>` / `@<type>/<name>` text patterns, so it can't follow indirection (e.g. a resource ID passed through a variable before use).
- **`manifest.merge` approximates conflicts, not Android's real merge algorithm.** It flags where two modules declare *different values* for the same component attribute; it does not reproduce AGP's actual manifest-merger priority rules (tool-vs-app manifest precedence, `tools:` merge directives, etc.).
- **`tests.discover` and `security.audit`/`proguard.inspect`'s secret detection are convention/pattern-based**, not semantic. A test file that doesn't follow the `<Class>Test.kt` naming convention won't be found; a secret assigned via a non-literal expression won't be flagged.
- **`staticAnalysis.run` reports whether each tool ran successfully, not what it found.** It does not parse detekt/ktlint/kover output into structured findings — only exit code, duration, and output length.
- **`gradle.run`'s task validation is syntactic, not a curated safe-list.** See [security.md](security.md) and [tools.md#gradlerun](tools.md#gradlerun) — this is the single most important limitation to understand before connecting this server to an agent you don't fully trust.
- **Absence of evidence is not evidence of absence, but the server can't tell you that.** If `symbol.find` returns zero matches for `"Auth"`, that means no declaration matched that substring in the scanned source sets — not that the project has no authentication code (it might be named differently, or live in a source set that wasn't scanned, e.g. `includeTests` defaults to `false`).

## Environment limitations

- No result caching between calls — a Gradle-backed tool re-runs the full Gradle invocation every time, with whatever latency that implies for large projects.
- Output is truncated for large results (400,000 characters by default for most process output, 40,000 for `gradle.run`, 200 items for lint issues, 50 items for architecture evidence, etc. — see [tools.md](tools.md) for the exact cap per tool). A truncated response is always marked as such (`*Truncated: true`), never silently cut.
- Single stdio session per process — the server was not built or tested for concurrent multi-client access to one running instance.
