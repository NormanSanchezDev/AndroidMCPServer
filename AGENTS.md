# Herramientas de referencia obligatorias para este proyecto

Estas dos herramientas SON el flujo de trabajo estándar. Úsalas antes de escribir código,
buscar, leer o ejecutar cualquier comando. Las reglas de abajo replican y amplían lo de
`CLAUDE.md` al mismo nivel de autoridad.

## 1. graphify — índice de conocimiento del codebase

Existe un knowledge graph en `graphify-out/` (god nodes, comunidades, relaciones cross-file).

- Para CUALQUIER pregunta sobre el codebase corre primero `graphify query "<pregunta>"` — devuelve un subgrafo acotado, mucho menor que GRAPH_REPORT.md o grep crudo.
- `graphify path "<A>" "<B>"` para relaciones entre dos símbolos; `graphify explain "<concepto>"` para conceptos concretos; `graphify god-nodes` para los hubs arquitectónicos.
- Usa `graphify-out/wiki/index.md` (o `wiki/`) para navegación amplia, antes que navegar fuentes a ciegas.
- `graphify-out/GRAPH_REPORT.md` solo para revisión de arquitectura general cuando query/path/explain no bastan.
- Después de modificar código, ejecuta `graphify update .` para mantener el grafo al día (solo AST, sin costo de API).

## 2. RTK — filtro de salida de comandos (Rust Token Killer)

Regla de oro: **casi todos los comandos bash se ejecutan con `rtk` por delante**. Si RTK
tiene filtro lo usa; si no, pasa tal cual. No rompe nada.

```bash
rtk git status / diff / log / add / commit   # Git compacto
rtk ls / read / grep / find / diff          # Archivos y búsqueda compactos
rtk gradlew test / build / lint             # Gradle wrapper (este proyecto)
rtk err <cmd>                               # Solo errores
rtk gain                                    # Métricas de tokens ahorrados
```

En cadenas con `&&` se antepone `rtk` a cada comando. `graphify` y `codegraph_explore`
se usan tal cual (ya son compactos). El MCP `codegraph` es la alternativa preferida al
grep cuando hay índice `.codegraph/`.

## 3. Este repositorio

Es el servidor MCP **AndroidCorporateMCP**: un servidor MCP en Kotlin (transporte stdio)
que expone herramientas que devuelven EVIDENCIA determinista de un proyecto
Android/Kotlin/Gradle real (arquitectura, símbolos PSI, Gradle, lint/tests, seguridad, flujos).
Filosofía: "The MCP observes. The agent reasons." No genera código ni opiniones.
Revisa `docs/tools.md` para el contrato de cada herramienta y `BUG_REPORT.md` para bugs conocidos.