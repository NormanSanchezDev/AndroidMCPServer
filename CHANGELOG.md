# Changelog

Todos los cambios relevantes de este proyecto (a nivel de negocio/feature, no diff técnico línea a línea) se documentan aquí.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y este proyecto usa [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Corregido
- **PR de release (#8) bloqueado por conflictos y build roto en `release`**: la rama `release` nunca había recibido el fix de `shadowJar` ni la restauración del fixture de tests, y además divergía de `develop` en los archivos de `graphify-out/`, bloqueando el merge en GitHub. Se hizo merge local de `origin/release` en `develop` (los merge drivers de graphify resolvieron esos archivos automáticamente) y se corrigió manualmente el daño colateral que ese merge introdujo en `CLAUDE.md` (perdió el bloque de instrucciones RTK) y en el fixture `ambiguous-entry-app/settings.gradle.kts` (quedó vacío) — ambos por reintroducir contenido antiguo de `release` que databa de antes de esas correcciones.
- **Build de CI roto por advertencia de Shadow/Gradle 9**: el plugin `com.gradleup.shadow` marcaba `KotlinModuleMetadataTransformer` con `DuplicatesStrategy.EXCLUDE`, arriesgando que se descartaran silenciosamente metadatos de módulos Kotlin duplicados. Se fijó `duplicatesStrategy = DuplicatesStrategy.INCLUDE` en la tarea `shadowJar`.
- **2 tests fallando en CI** (`AmbiguousNavigationTest`, `EntryPointDetectorTest`): el fixture `ambiguous-entry-app` había perdido su `settings.gradle.kts` en un rebase previo, causando que el escáner de proyecto Android retornara `null` (NPE) y que no se detectaran los 3 entry points esperados. Restaurado.

### Añadido
- **Estrategia de Git para `graphify-out/`**: los merges ya no generan conflictos en los artefactos del grafo de conocimiento.
  - `graph.json` usa un merge driver de unión (`merge=graphify`), ya existente.
  - El resto de archivos derivados (`graph.html`, `GRAPH_REPORT.md`, `manifest.json`, `.graphify_labels.json*`, `.graphify_analysis.json`) ahora usan `merge=ours` vía `.gitattributes` — Git nunca intenta fusionarlos línea a línea.
  - Nuevo hook `post-merge` (versionado en `scripts/git-hooks/post-merge`, instalable con una copia a `.git/hooks/`) que resincroniza el grafo automáticamente después de cada `git merge` / `git pull`.

## [0.0.3] - 2026-08-29

### Corregido
- Bump de `com.gradleup.shadow` a `9.6.1` — la versión `8.3.6` escribía `startScripts.mainClassName`, una propiedad que Gradle 9 eliminó, rompiendo la tarea `:startShadowScripts`.
- `SymbolReferencesTool`: ahora reporta el total real de referencias en lugar de limitarlo a `maxResults`, consistente con `declarationCount`/`referencesTruncated`.
- `SecurityAuditTool`: revisa el bloque completo del elemento del manifiesto en busca de `intent-filter`, en lugar de solo la línea `exported="true"` — antes marcaba como falso positivo casi cualquier componente exportado.
- `GradleDependenciesTreeParser`: eliminada condición siempre verdadera (código muerto).

### Cambiado
- Se dejó de trackear `build/`, `.gradle/` y la caché/backups derivados de graphify en Git; se ignoran `.claude/` y `.agents/` (artefactos locales de sesión, no compartibles).

## [0.0.2] - 2026-08-27

### Añadido
- Ajustes de empaquetado npm (`bin/android-corporate-mcp.js`, `.npmignore`) y documentación inicial (`docs/getting-started.md`, `docs/architecture.md`, `docs/tools.md`, `docs/security.md`, entre otros) para distribuir el servidor MCP vía npm.

## [0.0.1] - 2026-08-26 / 2026-08-27

### Añadido
- Primera versión funcional del servidor MCP (Model Context Protocol) para inteligencia de proyectos Android/Gradle: herramientas de inspección de manifiesto, árbol de dependencias Gradle, búsqueda de símbolos/referencias Kotlin y auditoría de seguridad básica.
- Integración con Kotlin SDK de MCP sobre Ktor (cliente y servidor).
- Empaquetado ejecutable vía Shadow (fat JAR).

---

## Convención de commits para este CHANGELOG

**Regla fija para todo commit generado por el asistente**: al final de cada commit, actualizar la sección `[Unreleased]` de este archivo con una entrada legible a nivel de negocio (qué cambió y por qué le importa al usuario/negocio, no el diff técnico) antes de crear el commit. Cuando se corte un release, mover `[Unreleased]` a una sección `[x.y.z] - AAAA-MM-DD` nueva.
