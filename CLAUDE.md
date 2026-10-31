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
- A release with a tag but no GitHub release object is found by tag fallback. release-please logs
  "Expected 2 releases, only found 1". `sequeless-build-parent-v0.1.2` has a tag and no GitHub
  release. Its next CHANGELOG pulled in commits from before 0.1.2 (PR #26). Create the missing release
  object, then rerun Release to regenerate the CHANGELOG.

## Merging

- Merge PRs with a rebase, not a merge commit: `gh pr merge <n> --rebase`, after checks pass.
- Confirm before merging any release PR, because it publishes.

## Published versions

- `sequeless-build-parent`: 0.1.0 (tag only), 0.1.1 (first GitHub Packages version, deployed by a
  manual `workflow_dispatch` of `release.yml`, PR #11), 0.1.2 (released by hand, PR #20; the template pins it).
- `workflows`: tags `workflows-v0.1.0`, `workflows-v0.1.1` and `workflows-v0.1.2` (PR #22).
  The template pins `workflows-v0.1.1`.
