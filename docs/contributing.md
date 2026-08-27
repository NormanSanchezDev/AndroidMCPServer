# Contributing

## Branching model

Verified directly against this repository's branches and CI workflows — not assumed:

- Real branches: `develop`, `main`, `master`, `prod`, `release`. There is no `contrib`/`contribution` branch in this repository.
- Open pull requests against `develop`.
- `.github/workflows/ci.yml` ("MCP Release CI") triggers when a PR is merged into `release` from `develop` specifically (`github.event.pull_request.head.ref == 'develop'`). It builds, runs `./gradlew clean build`, builds the shadow JAR, extracts the version from `build.gradle.kts`, prepares and validates the npm package, and publishes to npm if that version isn't already published.
- Separately, `.github/workflows/release.yml` ("Release") triggers on every push to `prod` and *also* builds the shadow JAR, bumps `package.json`'s version, and runs `npm publish` — plus creates a GitHub Release with the JAR attached. This is a second, independent path to `npm publish` that doesn't go through `ci.yml`'s "already published" guard.

**Practical note for contributors:** these two workflows overlap in what they publish, and it's not obvious from the workflow files alone how `release` and `prod` are meant to stay in sync during normal use. If your change touches either workflow file, be aware you may be affecting two separate publish paths, not one.

## Making a change

1. Fork or branch from `develop`.
2. Make your change. If it touches server/tool code, see [development.md](development.md) for the build/test/register-a-tool workflow.
3. Run `./gradlew test` and confirm the full suite passes.
4. Open a PR against `develop`.

## Pull request expectations

- Keep changes scoped — this codebase's own convention (see [development.md#coding-conventions-observed-in-this-codebase](development.md#coding-conventions-observed-in-this-codebase)) is small, single-purpose tool objects with a consistent `status`-field contract; match that shape rather than introducing a new pattern for one tool.
- If you add or change a tool's input/output contract, update [tools.md](tools.md) in the same PR — this repository's documentation is meant to describe the code exactly as it exists, and a stale `tools.md` is treated as a real inaccuracy, not a nitpick.
- If you fix a real gap found during the writing of this documentation (the `serverInfo.name`/`client/Main.kt` "lattice" naming leftover, the `serverInfo.version` hardcoded independent of `build.gradle.kts`, or the `Dockerfile`'s JAR-name glob mismatch — see [troubleshooting.md](troubleshooting.md) and [security.md](security.md)), call it out explicitly in your PR description so reviewers know it's an intentional fix, not a refactor.

## License

No `LICENSE` file currently exists in this repository, even though `package.json` declares `"license": "MIT"`. Until that's resolved, treat the license status as unresolved rather than assuming MIT terms apply — this is a real, verified gap, not a formality.

## Reporting issues

Use the repository's GitHub Issues. Include the exact tool name and arguments used, the `status` field returned (or the raw `isError` response), and your platform (see [compatibility.md](compatibility.md) for what's actually been verified per platform).
