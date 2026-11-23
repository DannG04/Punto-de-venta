# CI/CD con dev y master (producción) — diseño

Fecha: 2026-11-23 · Estado: acordado en conversación, pendiente de aprobar este documento

## Intención

Ordenar el flujo: `dev` integra el trabajo y `master` es producción. Desde la
actualización automática, **producción = lo que reciben las tiendas**: un release
publicado llega a todas en menos de 6 h. Se quiere que nada llegue a `dev` o
`master` sin pruebas en verde y que **el desarrollador decida cuándo publicar**
(no cada merge).

## Metas

- Pruebas automáticas en cada PR hacia `dev` y hacia `master`.
- Ramas protegidas: solo por PR, con pruebas en verde; a `master` solo desde `dev`.
- Botón **"Publicar versión"** en GitHub Actions que crea el release que consumen las tiendas.
- El mismo procedimiento de publicación sirve desde la PC del desarrollador.

## No metas

- Publicar automáticamente en cada merge a `master` (descartado: opción A).
- Compilar `PuntoVentaSetup.exe` en CI (requiere JRE e instalador de PostgreSQL
  fuera del repo, ~400 MB); se sigue generando localmente.
- Entornos de staging, despliegue a servidores, firma de código.

## Anti-metas

- Que un merge por error llegue a las tiendas.
- Que el botón publique una versión menor o igual a la vigente, o desde una rama que no sea `master`.
- Que las reglas dejen al desarrollador sin poder publicar (p. ej. el script intentando hacer push a `master` protegida).

## Contexto

- Estado al 2026-11-23: solo existen `dev` y `master` (se borraron 25 ramas).
  `origin/master` está 112 commits atrás de `dev` y no tiene nada que `dev` no tenga.
  Rama por defecto del repo: `master`. Repo público. Sin `.github/`.
- Pruebas existentes:
  - `test/correr_pruebas.sh` (Java, sin BD; bash).
  - `test/actualizacion/probar_bat_auto.ps1 -PgBin <bin>` (PostgreSQL en 127.0.0.1:5432, `postgres`/`root`).
  - `test/actualizacion/probar_publicar.ps1 -Jdk <bin>`.
- Runners `windows-latest` de GitHub: traen Git Bash, Apache Ant y PostgreSQL
  (servicio detenido; usuario `postgres`, contraseña `root`; variable `PGBIN`).
- `installer/update/publicar_version.ps1` hoy: valida contra `src/version.properties`,
  escribe la versión, compila (ant del PATH → ant de NetBeans → JDK), empaqueta con
  SHA-256 y **hace commit + tag + push** y `gh release create`.
- La app compara su `version.properties` (dentro del JAR) contra `releases/latest`.

## Enfoques considerados (disparador de producción)

| | Enfoque | A favor | En contra |
|---|---|---|---|
| A | Cada merge a `master` publica un release | Simple | Cualquier merge llega a las tiendas; aún falta probar UAC en PC real |
| **B (elegido)** | `master` solo prueba; se publica con un botón manual (workflow_dispatch) con versión y notas | Control de cuándo y qué agrupar; sin publicaciones accidentales | Un paso manual |

## Diseño

### 1. Workflow `pruebas` (`.github/workflows/pruebas.yml`)

- Disparadores: `pull_request` hacia `dev` y `master`; `push` a `dev` y `master`.
- `runs-on: windows-latest`, Java 23 (Temurin, `actions/setup-java`).
- Pasos: Java (`bash test/correr_pruebas.sh`), arrancar el servicio de PostgreSQL
  del runner y correr `probar_bat_auto.ps1 -PgBin $env:PGBIN`, y `probar_publicar.ps1 -Jdk`.
- Job extra `origen-master` (solo en PR hacia `master`): falla si la rama de origen no es `dev`.
- Nombres de jobs estables: se usan como checks requeridos (`pruebas`, `origen-master`).

### 2. Protección de ramas (configurada con `gh api`)

- `dev` y `master`: requieren PR, check `pruebas` en verde, sin force-push ni borrado,
  **aplican también a administradores** (nadie hace push directo).
- `master` además requiere el check `origen-master`.
- No se exigen aprobaciones de revisores (un solo desarrollador).
- Rama por defecto del repo → `dev` (los PR nuevos apuntan ahí por defecto).

### 3. Publicar (`.github/workflows/publicar.yml` + ajuste al script)

- `workflow_dispatch` con entradas `version` (obligatoria) y `notas` (una línea).
  Si existe `notas/v<version>.md` en `master`, se usa ese archivo como notas largas.
- Solo corre si `github.ref == refs/heads/master`; reutiliza el job de pruebas y publica solo si pasa.
- `permissions: contents: write`; usa `GITHUB_TOKEN` para `gh`.
- Cambios a `publicar_version.ps1` (aplican también a la publicación local):
  - **Nunca hace commit ni push.** Escribe la versión en `src/version.properties` e
    `installer/setup.iss` solo en la copia de trabajo para compilar, y al final las
    restaura (`git checkout --`) incluso si falla.
  - La "versión actual" sale del último release (`gh release view --json tagName`);
    si no hay releases, de `src/version.properties`.
  - Crea tag y release en un paso: `gh release create v<v> --target <sha de HEAD>`.
  - Exige estar en `master`, sin cambios, e igual a `origin/master`.
  - Si se pasa `-Jdk`, compila con el JDK directo aunque haya `ant` (en CI no se
    depende del `build.xml` de NetBeans).
  - `-SinPublicar` sigue existiendo para pruebas (ahí no se exige estar en `master`).

### 4. Puesta en marcha

1. PR con `.github/` y el script ajustado hacia `dev` (aún sin protección) → merge.
2. Activar protección de ramas y cambiar la rama por defecto a `dev`.
3. PR `dev` → `master` (pone producción al día: hoy 112 commits atrás).
4. Primer release con el botón, cuando el desarrollador lo decida (tras probar el UAC en una PC real).

## Lo que queda a criterio del constructor

- Caché de Java/ant en los workflows, timeouts y nombres de pasos.
- Cómo arrancar el servicio PostgreSQL del runner (`Start-Service` / `pg_ctl`).
- Mensajes exactos de error del script.

## Decisiones tomadas por el diseñador (revisables)

- Versión vigente = último release (no un archivo con commit) — imprescindible con `master` protegida.
- Notas: una línea en el botón, o archivo `notas/v<version>.md` para notas largas.
- Sin revisores obligatorios; las reglas aplican también al administrador.
- Rama por defecto → `dev`.
- PR `dev` → `master` con merge commit (no squash), para que `master` conserve la
  historia de `dev` y el siguiente PR solo traiga lo nuevo.
- Instalador completo fuera de CI.
