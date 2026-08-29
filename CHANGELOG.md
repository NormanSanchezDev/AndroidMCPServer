# Changelog

Todos los cambios relevantes de este proyecto (a nivel de negocio/feature, no diff técnico línea a línea) se documentan aquí.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y este proyecto usa [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Cambiado

- **Bump de versión 0.1.0 → 0.1.1**: el CI bloqueaba el merge a `release` porque `android-corporate-mcp@0.1.0` ya estaba publicado en npm (`package.json` y `build.gradle.kts` sincronizados).

- **`graphify-out/` ya no se versiona en Git**: era la causa raíz de los conflictos recurrentes en cada PR — el driver de merge local (`merge=graphify`/`merge=ours`) resolvía bien en la máquina donde se configuró, pero GitHub calcula la mergeabilidad de un PR en su propio servidor y no tiene acceso a esa configuración local, así que seguía marcando conflicto de todas formas. Se destrackeó (`git rm --cached`), se ignora por completo en `.gitignore`, y se eliminó `.gitattributes` (ya sin propósito). El grafo se sigue generando y manteniendo igual de local: `graphify update .` una vez tras clonar, y los hooks `post-checkout`/`post-commit`/`post-merge` ya existentes lo resincronizan solos. Ningún PR volverá a mostrar conflicto por esta carpeta.

### Corregido
- **PR de release (#8) bloqueado por conflictos y build roto en `release`**. Diagnóstico completo, causas reales:
  1. `release` nunca tenía el fix de `shadowJar` ni el fixture restaurado → por eso el log de build seguía mostrando el mismo warning y los mismos 2 tests fallando.
  2. Ambas ramas (`develop`/`release`) divergieron modificando `graphify-out/*` independientemente → GitHub calcula mergeabilidad server-side y no lee el `.git/config` local (`merge.graphify.driver`, `merge.ours.driver` solo existen en la máquina donde se configuraron) → por eso seguía marcando conflicto pese a la estrategia de merge configurada.
  3. Al resolver el merge localmente (donde sí aplican los drivers), el merge automático (`ort`) limpió `graphify-out/*` bien, pero dañó silenciosamente 2 archivos sin marcar conflicto: `CLAUDE.md` (perdió bloque RTK, 137 líneas) y el fixture `settings.gradle.kts` (quedó vacío) — reintrodujo contenido viejo de `release` anterior a esas correcciones. Detectado y restaurado antes de pushear.
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
