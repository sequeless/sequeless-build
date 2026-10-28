# Spec 1 — Stage 0: sequeless-build

Issue: [#29](https://github.com/sequeless/sequeless-prototype/issues/29). Part of the map
[Decompose the prototype into a CQRS repo layout](https://github.com/sequeless/sequeless-prototype/issues/11), spec 1 of 19.

This file lives in the prototype until `sequeless-build` exists. Per
[#49](https://github.com/sequeless/sequeless-prototype/issues/49), it moves into the new repo as
`docs/spec-01.md` in the first commit and is deleted here in the same change.

Decisions this spec carries out, and must not reopen:

- [Build baseline across repos (#17)](https://github.com/sequeless/sequeless-prototype/issues/17)
- [Where to publish artifacts (#18)](https://github.com/sequeless/sequeless-prototype/issues/18), as superseded by its interim note: GitHub Packages for now, Central deferred
- [Order of the extraction plan and how git history is kept (#23)](https://github.com/sequeless/sequeless-prototype/issues/23)
- Human tasks already done: group ID `org.sequeless` and GitHub Packages
  ([#48](https://github.com/sequeless/sequeless-prototype/issues/48)); public repo, created now
  ([#49](https://github.com/sequeless/sequeless-prototype/issues/49)); token scopes
  ([#50](https://github.com/sequeless/sequeless-prototype/issues/50)); Actions may open PRs and the
  Renovate app is installed ([#51](https://github.com/sequeless/sequeless-prototype/issues/51));
  org secret `PACKAGES_READ_TOKEN` holding a `read:packages` PAT
  ([#54](https://github.com/sequeless/sequeless-prototype/issues/54)).

## 1. Identity, stage and dependencies

| | |
|---|---|
| Repo | `sequeless/sequeless-build`, public, default branch `main`, Apache-2.0 (same as filter and ontology) |
| Stage | 0, in parallel with spec 2 (`sequeless-template`) |
| Waits for | nothing |
| Blocks | spec 3 (prototype adopts the parent), spec 5 (kernel), spec 6 (filter), and through them everything else |
| Depends on | no Sequeless artifact. No Spring, no Jena, no Lombok. |
| Publishes | `org.sequeless:sequeless-build-parent` (pom) and the reusable workflows (git tags). `org.sequeless:sequeless-bom` is built but **not released** until stage 1. |

What the repo owns:

1. **The parent POM**: plugin management and build conventions, with no Spring.
2. **Reusable GitHub workflows** (`on: workflow_call`): CI, CodeQL, Sonar, and release+deploy.
3. **The Renovate preset** that every Sequeless repo extends.
4. **The BOM** (for hosts only), plus a never-released `bom-contracts` module whose CI runs every
   testkit contract against it.

### Artifact ID: `sequeless-build-parent`, not `sequeless-parent`

The prototype's root reactor is already `org.sequeless:sequeless-parent:0.1.0-SNAPSHOT`. It was
never published, but reusing that name for the new parent would put two different POMs behind the
same coordinates while spec 3 is in flight (the prototype's local `~/.m2` holds the old one). The
new parent is `org.sequeless:sequeless-build-parent`. Spec 3 decides whether the prototype root
keeps its name.

## 2. Moves

Nothing moves out of the prototype, so there is no deletion commit and no reverse pointer. The
build conventions are *taken from* three repos and merged, so `PROVENANCE.md` records all three.
Re-read the SHAs at extraction time. The values below are the heads on 2026-10-27, when this spec
was written.

| Source repo | SHA (2026-10-27) | Paths | What is taken |
|---|---|---|---|
| `sequeless-filter` | `14c7a5dfb5e6d3e3c19bbc500d40afaeecde3e1b` | `pom.xml` (`<build>` and `<properties>`), `.github/workflows/ci.yml`, `.github/workflows/codeql.yml`, `renovate.json`, `sonar-project.properties`, `.editorconfig`, `.gitattributes`, `.gitignore`, `CODEOWNERS`, `LICENSE` | Spotless/palantir, JaCoCo, surefire/failsafe and enforcer config; the CI, Sonar and CodeQL job shapes; the Renovate rules |
| `sequeless-ontology` | `ace531320c135b40ece72336382078aacc882ba5` | `.github/workflows/ci.yml` | Only the `-Dsonar.organization` argument. The rest of ontology's build files match filter's. |
| `sequeless-prototype` | `f1451a77dea33bf6d8c207fa2deced54bc8d7b21` | `pom.xml` (`<properties>`, `<pluginManagement>`, the inherited enforcer and failsafe executions), `.github/workflows/ci.yml` | Newer plugin and action versions (compiler 3.16.0, surefire/failsafe 3.6.0, enforcer 3.6.3, ArchUnit 1.5.0; `checkout@v7`, `setup-java@v6`), the Java 25 build, `-ntp` |

Where the three disagree, take the **newer version** and the **prototype's** Java baseline (build on
25). Filter and ontology are on JDK 21 and `actions/*@v4`. They catch up when they adopt the parent:
filter in spec 6, ontology any time after stage 0.

**`PROVENANCE.md` content** (first commit):

```markdown
# Provenance

sequeless-build was started fresh on <DATE> (no imported history), per
https://github.com/sequeless/sequeless-prototype/issues/23.

Its build conventions were merged from:

| Source | SHA | Paths | Move |
|---|---|---|---|
| sequeless/sequeless-filter | <SHA> | pom.xml (build, properties); .github/workflows/{ci,codeql}.yml; renovate.json; sonar-project.properties; .editorconfig; .gitattributes; .gitignore; CODEOWNERS; LICENSE | copied and generalised into sequeless-build-parent/pom.xml, .github/workflows/maven-*.yml and default.json |
| sequeless/sequeless-ontology | <SHA> | .github/workflows/ci.yml | Sonar organization argument only |
| sequeless/sequeless-prototype | <SHA> | pom.xml (properties, pluginManagement, enforcer and failsafe executions); .github/workflows/ci.yml | plugin and action versions, Java 25 build |

The source files were copied, not moved: each source repo deletes its copy when it adopts this
parent (filter: spec 6; ontology: its release-please migration; prototype: spec 3).

Design: https://github.com/sequeless/sequeless-prototype/issues/17
```

## 3. Prerequisite refactors

None. No code moves, so the behaviour-before-move rule has nothing to apply to. Filter, ontology
and the prototype stay as they are until they adopt the parent in their own specs.

## 4. Package renames

None. The only Java in the repo is the test code in `bom-contracts`, under the new package
`org.sequeless.build.bom`.

## 5. Modules and their dependencies

```
sequeless-build/
├── pom.xml                       aggregator, org.sequeless:sequeless-build, never released or deployed
├── sequeless-build-parent/pom.xml   org.sequeless:sequeless-build-parent, packaging pom   [released]
├── sequeless-bom/pom.xml         org.sequeless:sequeless-bom, packaging pom             [built, not released at stage 0]
├── bom-contracts/pom.xml         org.sequeless:bom-contracts, test-only               [never released or deployed]
├── default.json                  Renovate preset
├── renovate.json                 { "extends": ["github>sequeless/sequeless-build"] }
├── release-please-config.json
├── .release-please-manifest.json
├── .github/
│   ├── version.txt               version of the "workflows" release-please package
│   └── workflows/
│       ├── maven-ci.yml          reusable
│       ├── maven-codeql.yml      reusable
│       ├── maven-sonar.yml       reusable
│       ├── maven-release.yml     reusable
│       ├── ci.yml                this repo's own CI, which calls the reusable files by local path
│       ├── codeql.yml            this repo's own CodeQL run, language `actions`
│       └── release.yml           this repo's own release, which calls maven-release.yml
├── PROVENANCE.md  README.md  LICENSE  CODEOWNERS  .editorconfig  .gitattributes  .gitignore
└── docs/spec-01.md               this file
```

The Makefile, `mvnw`, commitlint, the devcontainer and the canonical `BoundaryRulesTest` belong to
`sequeless-template` (spec 2), not here. Until the template exists, this repo builds with a plain
`mvn` from `setup-java`, as the prototype does today.

### Dependency graph

```
sequeless-build (aggregator) ── modules ──► sequeless-build-parent, sequeless-bom, bom-contracts

bom-contracts ── no parent (repeats the parent's conventions; see its pom.xml)
bom-contracts ── import ──► sequeless-bom            (in-reactor)
sequeless-bom ── no parent ─ standalone; carries its own <repositories>/<distributionManagement>
sequeless-build-parent ── no parent
```

The BOM has **no parent** and the parent **never imports the BOM** (#17 §6). If either referenced
the other, every parent release would force a BOM release and the reverse, which is the loop #17
rules out. The BOM is for hosts only. Libraries declare the versions of their Sequeless
dependencies directly.

### `sequeless-build-parent`

`packaging pom`, `org.sequeless:sequeless-build-parent:0.1.0-SNAPSHOT`, no `<parent>`.

**Properties** (consumers override them):

| Property | Default | Notes |
|---|---|---|
| `maven.compiler.release` | `21` | Libraries keep 21. Apps (the prototype host) set `25` (#17 §1). |
| `project.build.sourceEncoding` / `reporting` | `UTF-8` | |
| `sequeless.github.repository` | `${project.artifactId}` | Each repo's root sets this to its GitHub repo name. Used in the deploy URL. |
| `sequeless.enforcer.core.skip` | `true` | Core modules set `false` to ban Spring, Jena and Lombok. |
| `sequeless.dependency-analyze.skip` | `false` | Escape hatch for modules where `analyze` gives false positives, such as Spring Boot starters. |
| `jacoco.check.skip` / `jacoco.line.min` | `true` / `0.10` | Same as filter. |
| `skipITs` | `false` | |
| Plugin and ArchUnit versions | see below | One property per version, so that Renovate bumps them in one place |

**`<dependencyManagement>`**: **only** `com.tngtech.archunit:archunit-junit5` (`1.5.0`, test scope).
The parent deliberately does **not** manage JUnit, AssertJ, Mockito, Testcontainers or Lombok.
`spring-boot-dependencies` manages all of them, and a version in an inherited
`<dependencyManagement>` overrides an *imported* BOM. That would silently override Spring Boot's
pins in the prototype, which its root POM explicitly forbids today. Library repos import
`org.junit:junit-bom` in their own root (the template, spec 2, ships this).

**`<pluginManagement>`** (version and configuration):

| Plugin | Version | Configuration |
|---|---|---|
| `maven-compiler-plugin` | 3.16.0 | `<release>${maven.compiler.release}</release>`, `<parameters>true</parameters>`. No annotation processors. Lombok is configured by the modules that use it. |
| `maven-surefire-plugin` | 3.6.0 | includes `**/*Test.java` |
| `maven-failsafe-plugin` | 3.6.0 | includes `**/*IT.java`, `skipITs=${skipITs}` |
| `maven-enforcer-plugin` | 3.6.3 | see executions |
| `maven-dependency-plugin` | latest 3.x | `analyze-only` config: `failOnWarning=true`, `ignoreNonCompile=true`, `skip=${sequeless.dependency-analyze.skip}` |
| `spotless-maven-plugin` | latest 2.x | palantir-java-format, removeUnusedImports, importOrder, trimTrailingWhitespace, endWithNewline, sortPom `expandEmptyElements=false` (filter's config) |
| `jacoco-maven-plugin` | latest 0.8.x | filter's four executions (prepare unit, prepare IT, report, check) |
| `sonar-maven-plugin` | latest | version only |
| `maven-source-plugin`, `maven-javadoc-plugin` | latest 3.x | `jar-no-fork` / `jar`, used by the `release` profile |
| `maven-deploy-plugin`, `maven-install-plugin` | latest 3.x | version only |

Use the newest release of each plugin on the day of implementation. The versions in the table are
the floor taken from the sources.

**`<build><plugins>`** (inherited by every module of every consumer):

- enforcer `enforce-baseline` at `validate`: `requireJavaVersion [25,)` (the **build** JDK is always
  25, #17 §1), `requireMavenVersion [3.9,)`, `dependencyConvergence`,
  `banDuplicatePomDependencyVersions`.
- enforcer `ban-core-dependencies` at `validate`, `skip=${sequeless.enforcer.core.skip}`:
  `bannedDependencies` excluding `org.springframework*`, `org.apache.jena*` and
  `org.projectlombok:lombok`, with the message "core modules must not depend on Spring, Jena or
  Lombok" (#17 §5). It is off by default, so apps and adapters are unaffected and core modules
  opt in with one property.
- dependency `analyze-only` at `verify` (#17 §5: nothing crosses through a transitive dependency).
- failsafe `integration-test` and `verify` (the prototype's inherited execution).
- spotless `check` at `verify`.
- jacoco executions as in filter.

**`release` profile** (activated by the release workflow): attach sources and javadoc jars. **No
GPG.** Signing and the Central Portal plugin are added when the deferred Central task
([#53](https://github.com/sequeless/sequeless-prototype/issues/53)) is done. That move changes the
repository URL and adds signing. It does not change coordinates.

**Repositories (GitHub Packages, interim):**

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/sequeless/*</url>
    <releases><enabled>true</enabled></releases>
    <snapshots><enabled>true</enabled></snapshots>
  </repository>
</repositories>
<distributionManagement>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/sequeless/${sequeless.github.repository}</url>
  </repository>
  <snapshotRepository>
    <id>github</id>
    <url>https://maven.pkg.github.com/sequeless/${sequeless.github.repository}</url>
  </snapshotRepository>
</distributionManagement>
```

- The server ID is **`github`**. It is the ID #54 asked for: every `~/.m2/settings.xml` and every
  `setup-java` step uses `server-id: github`.
- `OWNER/*` is GitHub's documented form for "install packages from any repository" of an owner, so
  one entry covers every Sequeless repo.
- A token is needed even for public packages: `PACKAGES_READ_TOKEN` in CI, and the personal PAT
  locally.
- **Parent resolution:** Maven resolves a `<parent>` before it reads the parent's
  `<repositories>`. Every consumer's root POM must therefore repeat the `github` repository entry
  (the template ships it). The parent's copy still serves the consumer's modules and its
  dependencies.

### `sequeless-bom`

`packaging pom`, `org.sequeless:sequeless-bom:0.1.0-SNAPSHOT`, no parent. It carries the same
`<repositories>` and `<distributionManagement>` blocks, with `sequeless.github.repository`
hard-coded to `sequeless-build`.

At stage 0 its `<dependencyManagement>` is **empty**, because nothing has been released yet. Stage 1
specs (5, 6) add the kernel and filter artifacts at their released versions, and that is the BOM's
first release. Rule: **the BOM only ever lists released versions**. `bom-contracts` checks this
(below).

### `bom-contracts`

Test-only and never deployed (`maven.deploy.skip=true`, `maven.install.skip=true`). It has **no
parent**: a parent link would couple it to the parent's release version. Release PR #1 bumped the
parent to 0.1.0 without this module, which is what PR #4 fixed. It repeats the parent's build
conventions by hand, and it imports `sequeless-bom` and `junit-bom` in its own `<dependencyManagement>`.

- **Contract run.** As later stages release testkits (`*-testfixtures`, `*-testkit`, for example
  `query-testkit`), each one is added here as a test-scope dependency with **no version** (the BOM
  supplies it) and listed in surefire's `<dependenciesToScan>`. The contracts then all run together
  against the BOM's versions (#17 §6). At stage 0 the list is empty.
- **`BomListsOnlyReleasesTest`** (the one real test at stage 0, under `org.sequeless.build.bom`):
  it parses `../sequeless-bom/pom.xml` and fails if any managed `org.sequeless` entry has a
  `-SNAPSHOT` version or a property-based version that resolves to one. This guards #23's rule that
  `main` never references a SNAPSHOT. It passes trivially on an empty BOM, and it must have its own
  negative test: a fixture POM that contains a SNAPSHOT entry is rejected.
- Test dependencies: `junit-bom` imported in this module's own `<dependencyManagement>` (it is a
  library-style module), plus `junit-jupiter` and `assertj-core`. Parse the POM with the JDK's own
  DOM parser, not a new dependency.

## 6. Release config

release-please, manifest mode (#17 §4).

`release-please-config.json`:

```json
{
  "$schema": "https://raw.githubusercontent.com/googleapis/release-please/main/schemas/config.json",
  "bump-minor-pre-major": true,
  "include-component-in-tag": true,
  "tag-separator": "-",
  "plugins": [{ "type": "maven-workspace", "considerAllArtifacts": false }],
  "packages": {
    "sequeless-build-parent": {
      "release-type": "maven",
      "component": "sequeless-build-parent",
      "release-as": "0.1.0"
    },
    ".github": {
      "release-type": "simple",
      "component": "workflows",
      "release-as": "0.1.0"
    }
  }
}
```

`.release-please-manifest.json`: `{}` at the import commit.

- **First versions:** `sequeless-build-parent` **0.1.0** and `workflows` **0.1.0** (#23 §4: every
  never-published module starts at 0.1.0). `release-as` pins the first run. Remove it in the PR
  that follows the first release, or every later release would be 0.1.0 again.
- **Tags:** `sequeless-build-parent-v0.1.0` and `workflows-v0.1.0`, in the `<module>-vX.Y.Z` form
  from #17 §4.
- **The reactor root and `bom-contracts`** are not packages: they are never released and their
  POMs say `maven.deploy.skip=true`.
- **`sequeless-bom` is left out of the config at stage 0** ("exists but unreleased", #23). Stage 1
  adds it with `release-as: 0.1.0` once its first entries land. **BOM change in this spec: none.**
- **Snapshot PR:** the `maven` release type opens a PR that moves the module to the next
  `-SNAPSHOT` after each release (#17 §4). Confirm this on the first release, and if it does not
  happen, record why in the README.
- **Workflows are versioned too.** Consumers call `sequeless/sequeless-build/.github/workflows/maven-ci.yml@workflows-vX.Y.Z`,
  and Renovate's `github-actions` manager bumps the ref. The **Renovate preset is not versioned**:
  consumers extend `github>sequeless/sequeless-build` on the default branch, so a rule change
  reaches the whole org at once, which is the usual Renovate convention.
- **Changelog:** it starts at the import commit. Commit it as `feat: import build conventions`
  (with PROVENANCE pointing to the sources) so that release-please has a releasable commit.

## 7. CI and workflows

### Reusable workflows (`on: workflow_call`)

All of them: `ubuntu-latest`, `actions/checkout@v7`, `actions/setup-java@v6` with temurin and
`java-version` defaulting to `'25'`, `cache: maven`, `server-id: github`, Maven run as
`./mvnw` if it exists, else `mvn`, always `-B -ntp`. Action refs are pinned by digest (the
preset's `helpers:pinGitHubActionDigests`).

| File | Inputs | Secrets | What it does |
|---|---|---|---|
| `maven-ci.yml` | `java-version` (`'25'`), `maven-args` (`verify`) | `PACKAGES_READ_TOKEN` (optional) | `setup-java` with `server-username: GITHUB_ACTOR` and `server-password` from the read token, falling back to `GITHUB_TOKEN`, then `mvn -B -ntp ${maven-args}`. `permissions: contents: read, packages: read`. Concurrency group `ci-${{ github.ref }}` with cancel-in-progress. |
| `maven-codeql.yml` | `languages` (`java-kotlin`), `java-version` | `PACKAGES_READ_TOKEN` (optional) | filter's CodeQL job, generalised to a `languages` matrix. Java uses build-mode `manual` with `mvn -DskipTests -DskipITs -Dspotless.check.skip=true package`. `actions` uses build-mode `none`. `security-events: write`. |
| `maven-sonar.yml` | `java-version`, `sonar-organization` (`sequeless`) | `SONAR_TOKEN` (optional), `PACKAGES_READ_TOKEN` (optional) | filter's Sonar job with `fetch-depth: 0` and ontology's `-Dsonar.organization`. It skips with a notice when `SONAR_TOKEN` is absent. |
| `maven-release.yml` | `java-version` | `PACKAGES_READ_TOKEN` (optional), `RELEASE_PLEASE_TOKEN` (optional) | See below. `permissions: contents: write, pull-requests: write, packages: write`. |

**`maven-release.yml`, step by step:**

1. `googleapis/release-please-action@v4`, run with `RELEASE_PLEASE_TOKEN` if it is set, else
   `GITHUB_TOKEN`.
2. If `releases_created` is true, read `paths_released` and drop non-Maven paths (`.github`).
   Stop if nothing is left.
3. Check out the release commit and set up Java with `server-password: ${{ secrets.GITHUB_TOKEN }}`
   (deploy) and `server-id: github`.
4. `mvn -B -ntp -Prelease -pl <released> -am install -DskipTests` builds the released modules and
   their in-repo upstreams into the local repository.
5. `mvn -B -ntp -Prelease -pl <released> deploy -DskipTests` deploys **only** the released modules.

**Correction to #17 §4:** a single `-pl <released> -am deploy` would also deploy the upstream
modules that `-am` pulls in. Those are at a `-SNAPSHOT` version, or at a release version that has
already been published. GitHub Packages does not document whether it lets a version be published
twice, so the deploy has to be targeted, and the two-step form above does that. Tests are not
rerun: the release PR's CI already ran them.

### This repo's own workflows

- `ci.yml` (push to `main`, PRs). Each job calls a reusable workflow whose job is named `maven`, so the check names are `build / maven` and `bom-contracts / maven`:
  - `uses: ./.github/workflows/maven-ci.yml` with `secrets: inherit`. This builds all three
    modules: Spotless, enforcer, `dependency:analyze` and `BomListsOnlyReleasesTest`.
  - Calling the reusable file by local path means every PR that changes a reusable workflow also
    tests it.
  - Add a job named `bom-contracts`: `maven-ci.yml` with `maven-args: -pl bom-contracts -am verify`.
    It is required on PRs that touch `sequeless-bom/` and is the gate before each BOM release
    (#17 §6). At stage 0 it is the same as the full build, but it is named separately so that
    branch protection can require it from day one.
- `codeql.yml` (push, PRs and a weekly cron): `maven-codeql.yml` with
  `languages: '["actions","java-kotlin"]'`. In this repo `actions` is the language that matters.
- `release.yml` (push to `main`): `uses: ./.github/workflows/maven-release.yml`, `secrets: inherit`.
- No Sonar job here: there is almost no main code to analyse. The reusable file is tested by
  spec 3's adoption in the prototype.

### Release PRs and `GITHUB_TOKEN`

PRs and tags created with `GITHUB_TOKEN` **do not trigger other workflows**. Release-please's PRs
therefore get no CI run. If branch protection requires `ci`, they cannot be merged without an
admin override. Pick one option during implementation, and record the choice in the README:

- **(a)** Create a PAT or GitHub App token as org secret `RELEASE_PLEASE_TOKEN` (the open checkbox on
  [#51](https://github.com/sequeless/sequeless-prototype/issues/51)). The release workflow already
  accepts it. **Recommended:** release PRs then run CI the same way as every other PR.
- **(b)** Keep `GITHUB_TOKEN` and close and reopen each release PR by hand to trigger CI.

Deploying is unaffected either way, because it runs in the same workflow run as release-please.

### Renovate preset (`default.json`)

Start from filter's `renovate.json`, unchanged (`config:recommended`, `:dependencyDashboard`,
`:semanticCommits`, `helpers:pinGitHubActionDigests`, America/Toronto, Monday before 6am,
`chore(deps): ` prefix, `rangeStrategy: bump`, automerge for patch and minor, Maven updates
grouped). Add:

1. **Sequeless artifacts are releasable changes.** For `matchPackageNames: ["org.sequeless:**"]`,
   set `semanticCommitType: fix` and `groupName: sequeless artifacts`, with no automerge. A
   dependency bump between Sequeless modules must produce a patch release (#17 §4). A `chore`
   commit would not.
2. **The BOM is gated by its contracts.** For `matchFileNames: ["sequeless-bom/pom.xml"]`, set
   `automerge: false`. The `bom-contracts` job must pass, and a human merges the PR.
3. **Reusable workflow refs.** For `matchPackageNames: ["sequeless/sequeless-build"]` in the
   `github-actions` manager, group the updates and set `semanticCommitType: ci`.
4. **The GitHub Packages registry.** Add a `hostRules` entry for `maven.pkg.github.com`. Renovate
   needs a token to see new `org.sequeless` releases. With the hosted Renovate app, the token is set
   as an encrypted value or in the app's org-level secret settings, never in plain text. **New human
   task** (see §9): add a `read:packages` token to the Renovate app for the `sequeless` org.

`renovate.json` in this repo: `{ "extends": ["github>sequeless/sequeless-build"] }`.

### Repo settings (the agent does these with `gh`; #50 gave the scopes)

- Branch protection on `main`: require PRs, require the `build / maven` and `bom-contracts / maven`
  checks, allow linear history only.
- Actions → General: allow every repo in the `sequeless` org to call this repo's workflows (needed
  for a public repo's reusable workflows to be callable; check what the org policy already allows).
- Repo secrets come from org secrets: `PACKAGES_READ_TOKEN`, and `RELEASE_PLEASE_TOKEN` if option
  (a) is chosen.

## 8. ADRs

None. #17 recorded none ("easy to reverse"), and #23 lists no ADR for this repo. The README links
#17, #18 with its interim note, and #23 as the design record. Two choices made in this spec are
worth a line each in the README rather than an ADR, because both are easy to undo:

- the artifact ID `sequeless-build-parent`, and why;
- the targeted two-step deploy (correction to #17 §4).

## 9. Implementation order

Commit or PR boundaries:

1. Create the repo: `gh repo create sequeless/sequeless-build --public`.
2. **Import commit** `feat: import build conventions`, containing everything in §5 plus this file
   as `docs/spec-01.md` and `PROVENANCE.md` with the SHAs and the date filled in. In the same
   change, delete `docs/extraction/spec-01-sequeless-build.md` from the prototype, with a commit
   naming the new repo and the import commit's SHA.
3. Set branch protection and the Actions access setting. Check that `ci` and `codeql` are green on
   `main`.
4. Merge the first release-please PR, then check:
   - the tags `sequeless-build-parent-v0.1.0` and `workflows-v0.1.0` exist;
   - `org.sequeless:sequeless-build-parent:0.1.0` appears under the repo's Packages;
   - neither `sequeless-bom`, `bom-contracts` nor the aggregator was deployed;
   - the snapshot PR (`0.1.1-SNAPSHOT`) opened.
5. Follow-up PR: remove both `release-as` keys.
6. **Smoke test from outside:** in a scratch directory, a POM with
   `<parent>org.sequeless:sequeless-build-parent:0.1.0</parent>` and the `github` repository runs
   `mvn -B validate` using a `read:packages` token. This proves the read path that spec 3 depends
   on.
7. Tell the owner the server ID (`github`) for their `~/.m2/settings.xml`. This closes the last open
   checkbox on #54.

**New human tasks this spec raises** (open them as `wayfinder:task` issues):

- the Renovate app needs a `read:packages` token for `maven.pkg.github.com` (§7, preset item 4);
- decide on `RELEASE_PLEASE_TOKEN` (option a or b in §7). This is the open checkbox on #51.

## 10. Definition of done

The issue's checklist, read for a stage-0 repo that moves no code and releases no BOM:

| Item | For this spec |
|---|---|
| The new repo's CI is green | `ci` (build and bom-contracts) and `codeql` are green on `main` at the release commit. |
| The prototype's CI is green against released artifacts | The prototype is not changed by this spec and stays green. Consuming the released parent is spec 3. Step 6 (the smoke test) shows that spec 3 can resolve the parent. |
| The BOM's CI, which runs every testkit contract, is green | `bom-contracts` is green: `BomListsOnlyReleasesTest` passes and there are no contracts yet. |
| The release and the BOM re-release are done | `sequeless-build-parent` 0.1.0 and `workflows` 0.1.0 are released and the parent is deployed. **BOM: not released by design.** Its first release is in stage 1. |
| Cut-over: the prototype deleted the moved code, with a reverse pointer | Not a cut-over spec. The only prototype change is removing this spec file (step 2). |
