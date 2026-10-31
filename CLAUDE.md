# CLAUDE.md

Notes for agents working in this repo. The spec is `docs/spec-01.md`.

## Releases (release-please)

- Release runs on every push to `main` (`.github/workflows/release.yml` calls `maven-release.yml`).
  It runs release-please, then deploys the released Maven modules.
- `sequeless-build-parent` and `workflows` (`.github/`) share one combined release PR, titled
  `chore: release main`, because `separate-pull-requests` is not set. A combined title has no
  component or version in it.
- A release is created and deployed only when a release PR is merged and its label is
  `autorelease: pending`. A PR labelled `autorelease: snapshot` merges without tagging or deploying.
  PR #23 did that on 2026-10-31.
- Before a release PR merges, read its diff: the version, `.release-please-manifest.json` and the
  CHANGELOG. Merging a parent release publishes to GitHub Packages, and that cannot be undone.
- Java snapshot logic: before each release, release-please opens a snapshot PR unless a merged commit
  for the component has a SNAPSHOT version in its title. This repo's titles never have one, so every
  run opened an empty snapshot PR (#23). `sequeless-build-parent` sets `skip-snapshot: true`
  (PR #25), so its release PR opens. The parent stays at the released version after a release.
- `release-as` is set for the parent (PR #24). It did not stop the snapshot path, so keep
  `skip-snapshot` with it.
- release-please finds the last release from GitHub release objects. A tag without one is found by
  tag fallback, and the log says "Expected 2 releases, only found 1". `sequeless-build-parent-v0.1.2`
  had a tag and no GitHub release, so one was created on 2026-10-31.
- `.release-please-manifest.json` must hold the last published version. A release PR moves it on
  merge. If that release is never published, the manifest stays ahead, release-please looks for a tag
  that does not exist, and the next CHANGELOG pulls in older commits. Fix: set the manifest back to
  the last published version (PR #29), then rerun Release.

## Merging

- Merge PRs with a rebase, not a merge commit: `gh pr merge <n> --rebase`, after checks pass.
- Confirm before merging any release PR, because it publishes.

## Published versions

- `sequeless-build-parent`: 0.1.0 (tag only), 0.1.1 (first GitHub Packages version, deployed by a
  manual `workflow_dispatch` of `release.yml`, PR #11), 0.1.2 (released by hand, PR #20; the template
  pins it), 0.1.3 (release PR #26, tag `sequeless-build-parent-v0.1.3`, resolved from GitHub Packages).
- `workflows`: tags `workflows-v0.1.0`, `workflows-v0.1.1` and `workflows-v0.1.2` (PR #22).
  The template pins `workflows-v0.1.1`.
