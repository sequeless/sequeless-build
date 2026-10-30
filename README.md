# sequeless-build

Build conventions for the Sequeless repos: a parent POM with no Spring, reusable GitHub workflows,
a Renovate preset, and the BOM (for hosts only).

- Design: [#17 Build baseline across repos](https://github.com/sequeless/sequeless-prototype/issues/17)
- Publishing: [#18 Where to publish artifacts](https://github.com/sequeless/sequeless-prototype/issues/18)
  (superseded for now by its interim note: GitHub Packages at `maven.pkg.github.com/sequeless/<repo>`,
  group `org.sequeless`; Maven Central is deferred)
- Plan: [#23 Order of the extraction plan](https://github.com/sequeless/sequeless-prototype/issues/23)
- Provenance: [PROVENANCE.md](PROVENANCE.md). Spec: [docs/spec-01.md](docs/spec-01.md).

## Modules

| Module | Artifact | Released |
|---|---|---|
| `sequeless-build-parent` | `org.sequeless:sequeless-build-parent` | yes |
| `sequeless-bom` | `org.sequeless:sequeless-bom` | no, until stage 1 |
| `bom-contracts` | test only | never deployed |
| (root) | aggregator | never |

## Decisions recorded here

- **Artifact ID.** The parent is `sequeless-build-parent`, not `sequeless-parent`, because the
  prototype's root POM already uses `sequeless-parent`.
- **Targeted deploy.** The release deploys only the released modules, in two steps: `install -am`
  to build upstreams locally, then `deploy -pl <released>`. A single `-am deploy` would also deploy
  upstreams that are SNAPSHOTs or already published (corrects #17 §4).
- **Class files.** Plugins that read class files (dependency analysis, JaCoCo) must be new enough
  for the highest `maven.compiler.release` a consumer uses. Apps build at 25, and dependency-plugin
  3.8.1 and JaCoCo 0.8.12 cannot read Java 25 class files, so both are now 3.10.0 and 0.8.14.

## Consuming

Add the GitHub Packages repository to your root POM. A token is needed even for public packages.

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/sequeless/*</url>
  </repository>
</repositories>
```

Use server ID `github` in `~/.m2/settings.xml`.
