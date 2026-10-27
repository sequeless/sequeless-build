# Provenance

sequeless-build was started fresh on 2026-10-27, with no imported history, as set out in
https://github.com/sequeless/sequeless-prototype/issues/23.

Its build conventions were merged from three repos:

| Source | SHA | Paths | Move |
|---|---|---|---|
| sequeless/sequeless-filter | 14c7a5dfb5e6d3e3c19bbc500d40afaeecde3e1b | pom.xml (build, properties); .github/workflows/ci.yml, codeql.yml; renovate.json; sonar-project.properties; .editorconfig; .gitattributes; .gitignore; CODEOWNERS; LICENSE | Copied and generalised into sequeless-build-parent/pom.xml, the reusable workflows and default.json. LICENSE, CODEOWNERS, .editorconfig, .gitattributes and .gitignore are copied unchanged. |
| sequeless/sequeless-ontology | ace531320c135b40ece72336382078aacc882ba5 | .github/workflows/ci.yml | The -Dsonar.organization argument only |
| sequeless/sequeless-prototype | f1451a77dea33bf6d8c207fa2deced54bc8d7b21 | pom.xml (properties, pluginManagement, enforcer and failsafe executions); .github/workflows/ci.yml | Plugin and action versions, and the Java 25 build |

The source files were copied, not moved. Each source repo removes its own copy when it adopts this
parent: filter in its spec 6, ontology in its release-please migration, and the prototype in spec 3.

Design record: https://github.com/sequeless/sequeless-prototype/issues/17
Spec: docs/spec-01.md
