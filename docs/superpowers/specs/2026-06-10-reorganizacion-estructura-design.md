# Reorganización de la estructura del proyecto

**Fecha:** 2026-06-10
**Rama:** `refactor/estructura`
**Alcance elegido:** Limpieza de repositorio + organización de `src/` en paquetes Java por capas.
**Fuera de alcance:** Partir las clases "Dios" (`ConexionBD`, `ComprasP`, etc.) — anotado para una sesión futura.

## Problema

El proyecto está desordenado en dos planos:

1. **Raíz del repositorio:** ~15 `.jar` sueltos, archivos basura versionados o sueltos
   (`nul`, `Back1`, `backup_1.0.tar`, `logo.png` de 3 MB, `impresion*.txt`), y carpetas de
   reportes generados (`BalancesGenerales/`, `Estados de resultados/`, `Reportes de ventas/`)
   que siguen trackeadas en git aunque ya están en `.gitignore` (se commitearon antes de
   ignorarlas).
2. **Código fuente (`src/`):** las 40+ clases viven en el **paquete por defecto** (ningún
   `.java` declara `package`), en una sola carpeta plana junto a `icons/` e `img/`.

## Hallazgos que hacen seguro el refactor

- Los iconos se cargan por **classpath absoluto** (`SvgIcon.load("/icons/...")`), no por ruta
  relativa al paquete. Mover las clases a paquetes **no rompe** la carga de recursos, siempre
  que `icons/` e `img/` queden en la raíz del classpath (`src/icons`, `src/img`).
- `db.properties` y `config.properties` se leen con `new File(...)` desde el directorio de
  ejecución → no les afecta el cambio de paquetes.
- Ningún `.form` referencia clases del proyecto como componentes personalizados (todos son
  `javax`/`java` estándar) → mover los paneles no rompe ningún `.form`.
- Acoplamiento: `ConexionBD` la usan 22 archivos y `SvgIcon` 17 — son el centro de gravedad y
  la razón de la estrategia de imports por wildcard (ver Fase 2, paso 7).

## Estructura objetivo

```
lib/                         <- todos los .jar (hoy en la raíz)
src/
  puntoventa/
    ui/      Interfaz, VentasP, InventarioP, ComprasP, ClientesP,
             ApartadosP, DevolucionesP, EmpleadosP, GastosP, GananciasP,
             AdministracionP, CategoriasP, KardexP, ProveedoresP,
             CotizacionesP, EmpresaP, TicketDialog, CalendarioPanel  (+ sus .form)
    db/      ConexionBD
    report/  Excel, GenTicket, TicketBuilder, TicketData,
             GeneradorCodigoBarras, BalGeneral
    web/     WebInventario
    util/    SvgIcon, Hora, ConfigApp, GestorErrores, Cake, Mise
  icons/     <- se quedan en la raíz del classpath (NO se mueven)
  img/       <- idem
```

`model/` no se crea todavía (estaría vacío y git no versiona carpetas vacías). Se añade cuando
exista la primera clase de datos.

### Clasificación de archivos ambiguos

| Archivo            | Paquete  | Motivo                                            |
|--------------------|----------|---------------------------------------------------|
| `Cake.java`        | `util`   | Helpers de validación de caracteres               |
| `Mise.java`        | `util`   | Helpers de tablas (`DefaultTableModel`)           |
| `BalGeneral.java`  | `report` | Arma la hoja POI del balance general              |
| `CalendarioPanel`  | `ui`     | `JPanel` hecho a mano (sin `.form`)               |
| `CotizacionesP`    | `ui`     | `JPanel` hecho a mano (sin `.form`)               |
| `EmpresaP`         | `ui`     | `JPanel` hecho a mano (sin `.form`)               |

## Fase 1 — Limpieza segura (sin tocar lógica Java)

1. **JARs → `lib/`:** mover los ~15 `.jar` de la raíz a `lib/` y actualizar las referencias en
   `nbproject/project.properties` (`file.reference.*.jar` → `lib/...`). Toca config de build,
   no código Java.
2. **Borrar basura:** `nul`, `Back1`, `backup_1.0.tar` (ya marcados como borrados en git) y
   `logo.png` (3 MB) de la raíz — verificar antes que no se use; existe `src/img/logo.png`.
3. **Dejar de trackear reportes generados:** `git rm --cached` de `BalancesGenerales/`,
   `Estados de resultados/` y `Reportes de ventas/` (ya están en `.gitignore` pero siguen
   versionados).
4. **`impresion*.txt`:** los lee `GenTicket` en tiempo de ejecución → se dejan donde están
   (no se mueven) para no romper rutas. Solo se documentan.

## Fase 2 — Paquetes por capas

5. Crear las carpetas de paquete bajo `src/puntoventa/` y mover cada `.java` **junto con su
   `.form`** correspondiente.
6. Añadir `package puntoventa.<capa>;` al inicio de cada archivo.
7. **Estrategia de imports (clave para no romper):** en cada archivo `ui` que lo necesite,
   agregar imports por capa: `import puntoventa.db.*;`, `import puntoventa.report.*;`,
   `import puntoventa.util.*;`, `import puntoventa.web.*;`. El wildcard por capa evita olvidar
   clases sueltas y es un patrón seguro para una migración mecánica. Las clases de `db`,
   `report`, `web` y `util` que se referencien entre sí reciben el import puntual que les falte.
8. Cambiar `main.class=Interfaz` → `main.class=puntoventa.ui.Interfaz` en
   `nbproject/project.properties`.

## Riesgos y reversibilidad

- Todo el trabajo va en la rama `refactor/estructura`. Si algo no compila, `git reset` y listo.
- **Fase 1 y Fase 2 en commits separados** para poder revertir una sin la otra.
- El usuario compila (no se ejecuta `ant` desde el agente); cualquier import faltante lo caza
  el compilador y se corrige.
- Riesgo real bajo: los recursos van por classpath absoluto y los `.form` no referencian clases
  del proyecto.

## Verificación

- Tras la Fase 1: el usuario compila (`ant compile`) y la app sigue arrancando.
- Tras la Fase 2: el usuario compila; cero errores de import; la app arranca y los iconos se ven.
- Revisar que `dist`/`build` no queden con rutas viejas (limpiar con `ant clean` si hace falta).
