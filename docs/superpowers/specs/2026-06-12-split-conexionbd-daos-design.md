# Split de `ConexionBD` en DAOs por dominio

**Fecha:** 2026-06-12
**Rama:** (a crear) `refactor/conexionbd-daos`
**Alcance elegido:** Partir la clase Dios `ConexionBD` (2234 líneas, ~90 métodos) en DAOs cohesivos por dominio, manteniendo una fachada permanente para no tocar a los 22 llamadores.
**Fuera de alcance:** migrar los llamadores a usar DAOs directos (posible spec futuro); partir los paneles UI (`ComprasP`, `InventarioP`, etc.); tocar `Excel` / `WebInventario`.

---

## ⏸️ Estado para retomar (pausado el 2026-06-12)

**Dónde estamos en el flujo brainstorming → plan → implementación:**
- ✅ Diseño explorado y **aprobado por el usuario** (4 preguntas resueltas: empezar por ConexionBD; split completo incremental; variante A fachada permanente; diseño OK).
- ✅ Spec escrito y auto-revisado (este archivo).
- ⏳ **PENDIENTE: el usuario revisa este spec.** Falta su OK final, en especial: el mapeo método→DAO, los 3 juicios de frontera (`seleccionarVendedor`→Empleado, `obtenerMinMaxDescuentoTemp`→Venta, `folioYaRegistrado`→Compra), y el orden de ejecución.
- ⬜ **SIGUIENTE PASO al aprobar:** invocar la skill `superpowers:writing-plans` para generar el plan de implementación (NO empezar a codear antes).

**Contexto de git / entorno:**
- Trabajo actual sobre la rama `dev` (limpia; el PR #41 ya mergeó la reorganización en paquetes previa). El refactor anterior — limpieza de raíz + paquetes `puntoventa.{ui,db,report,web,util}` — **ya está hecho y mergeado**.
- **Antes de implementar** hay que crear la rama `refactor/conexionbd-daos` (este spec aún no está commiteado; el usuario lleva los commits).

**Preferencias del usuario (críticas para la ejecución):**
- **El usuario compila**, el agente NO ejecuta `ant compile`/`ant run`. En cada checkpoint se le pide compilar y se espera su resultado.
- **El usuario hace todos los commits**; el agente solo indica cuándo y con qué mensaje.

**Decisiones ya cerradas (no re-litigar):** fachada permanente (variante A, llamadores intactos salvo el rename de `RenglonCompra` en `ComprasP`); 16 DAOs + `BaseDAO` en `puntoventa.db`; sin pool de conexiones (YAGNI); verificación por compilación + arranque manual (no hay tests).

---

## Problema

`puntoventa.db.ConexionBD` es el centro de gravedad del proyecto: la usan 22 archivos. Concentra en una sola clase:

- Las **funciones generales** de acceso a datos (`inst(sql)`, `query(sql)`, helper `nvl`).
- **~90 métodos de dominio** que cubren TODOS los negocios: empleados, productos, ventas (+ venta_temp), compras (+ borradores), clientes, devoluciones, apartados, gastos, ganancias, proveedores, categorías, empresa, listas de precios, cotizaciones (+ coti_temp) y reportes/kardex/cortes.
- Una **clase de datos anidada** (`RenglonCompra`).

Esto la vuelve imposible de razonar como unidad, mezcla dominios sin relación y hace que cualquier cambio toque un archivo de 2234 líneas.

## Hallazgos que condicionan el diseño

- Los **22 llamadores** instancian `ConexionBD conect = new ConexionBD()` y llaman métodos sobre esa instancia.
- Hay **36 llamadas crudas** a `.inst(...)` / `.query(...)` desde la UI y reportes: los paneles arman SQL y lo ejecutan directo, no solo vía métodos de dominio. Por eso `inst`/`query` deben seguir siendo públicos en la fachada.
- `RenglonCompra` (clase anidada pública) la usa `ComprasP` como `ConexionBD.RenglonCompra`.
- No hay suite de tests. La verificación es **compilación + arranque manual**; el usuario compila (el agente no ejecuta `ant`).

## Estrategia: fachada permanente (variante A)

`ConexionBD` se conserva como **fachada delgada**. La lógica SQL de cada dominio se muda a su DAO; cada método de `ConexionBD` queda como **delegador de una línea**. Los 22 llamadores **no se tocan** (excepción única: la referencia a `RenglonCompra`, ver abajo). Riesgo casi nulo, reversible por commit.

### Arquitectura objetivo (todo en `puntoventa.db`)

```
puntoventa.db
├── BaseDAO            <- carga db.properties (constructor), provee inst(sql), query(sql),
│                          nvl(), y la obtención de conexión. Clase base de TODOS los DAOs.
├── ConexionBD         <- FACHADA: extiende BaseDAO; conserva inst/query públicos (heredados)
│                          para las 36 llamadas crudas; sus ~90 métodos de dominio pasan a ser
│                          delegadores de 1 línea hacia los DAOs.
├── RenglonCompra      <- clase de datos top-level (extraída de ConexionBD)
├── EmpleadoDAO        ProductoDAO     VentaDAO        CompraDAO
├── ClienteDAO         DevolucionDAO   ApartadoDAO     GastoDAO
├── GananciaDAO        ProveedorDAO    CategoriaDAO    EmpresaDAO
└── ListaPreciosDAO    CotizacionDAO   ReporteDAO
```

### BaseDAO

Absorbe lo común que hoy vive disperso en `ConexionBD`:

- Campos de credenciales (`url`, `nameBD`, `usuario`, `contra`) y el `DateTimeFormatter`.
- El **constructor** que lee `db.properties` con `new File("db.properties")` (idéntico al actual).
- `public boolean inst(String sql)` y `public ResultSet query(String sql)`.
- `protected static String nvl(String s)`.

Cada DAO `extends BaseDAO` y, al instanciarse, carga `db.properties` igual que hoy hace cada `new ConexionBD()` (mismo comportamiento; **sin** pool de conexiones — YAGNI).

### ConexionBD (fachada)

```java
public class ConexionBD extends BaseDAO {
    private final EmpleadoDAO empleados = new EmpleadoDAO();
    private final ProductoDAO productos = new ProductoDAO();
    // ...un campo por dominio
    public boolean insertarEmpleado(String[] c){ return empleados.insertarEmpleado(c); }
    public void insertarProducto(String[] d){ productos.insertarProducto(d); }
    // inst()/query() se heredan de BaseDAO → las 36 llamadas crudas siguen igual
}
```

## Mapeo completo método → DAO

| DAO | Métodos |
|-----|---------|
| **BaseDAO** | `inst`, `query`, `nvl`, constructor + credenciales |
| **EmpleadoDAO** | `insertarEmpleado`, `actualizarEmpleado`, `inactivarEmpleado`, `verificarUsuario`, `idEmpleado`, `seleccionarVendedor` |
| **ProductoDAO** | `insertarProducto`, `insertarProductoConCodigo`, `actualizarProducto`, `eliminarProducto`, `reactivarProducto`, `archivarProducto`, `estadoProducto`, `obtenerProductosInactivos`, `hayPocosProductos`, `resolverCodigo`, `obtenerProductoParaCompra`, `codigoBarrasDuplicado`, `crearProductoDesdeCompra`, `insertarProductoConCodigoYCategoria`, `actualizarProductoConCategoria`, `actualizarCamposCompraProducto`, `obtenerMaxDescuento` |
| **VentaDAO** | `obtenerDatosVenta`, `limpiarVentaTemp`, `insertarVentaTemp`, `eliminarVentaTemp`, `sumaVentaTemp`, `registrarVenta`, `registrarVentaConFormaPago`, `actualizarDescuentoTemp`, `actualizarDescuentoVenta`, `obtenerMinMaxDescuentoTemp` |
| **CompraDAO** | `insertarCompra`, `actualizarCompra`, `eliminarCompra`, `insertarProdCompra`, `eliminarProdCompra`, `guardarFacturaCompleta` (usa `RenglonCompra`), `guardarBorrador`, `obtenerCabeceraBorrador`, `eliminarBorrador`, `actualizarTotalesCompra`, `folioYaRegistrado` |
| **ClienteDAO** | `insertarCliente`, `actualizarCliente`, `inactivarCliente` |
| **DevolucionDAO** | `insertarDevolucion`, `eliminarDevolucion`, `insertarProdDevolucion`, `eliminarProdDevolucion` |
| **ApartadoDAO** | `obtenerDatosApartado` (usa `TipoTicket`/`TicketData`), `insertarApartado`, `actualizarApartado`, `eliminarApartado`, `revisarApartado`, `cantidadCancelarApartado`, `cancelarApartado`, `entregarApartado`, `insertarProdApartado`, `eliminarProdApartado`, `seleccionarApartado`, `seleccionarProductos` |
| **GastoDAO** | `insertarGasto`, `actualizarGasto` |
| **GananciaDAO** | `insertarOtraGanancia`, `actualizarOtraGanancia` |
| **ProveedorDAO** | `insertarProveedor`, `editarProveedor`, `obtenerDatosProveedor`, `cambiarEstatusProveedor`, `obtenerProveedores` |
| **CategoriaDAO** | `insertarCategoria`, `editarCategoria`, `cambiarEstatusCategoria`, `obtenerCategorias`, `obtenerTodasCategorias`, `buscarCategoriasPorNombre` |
| **EmpresaDAO** | `obtenerEmpresa`, `actualizarEmpresa`, `empresaInicializada` |
| **ListaPreciosDAO** | `obtenerListas`, `obtenerPrecioEnLista`, `actualizarPrecioEnLista`, `insertarPrecioEnLista`, `eliminarPrecioEnLista`, `actualizarPrecioListaTemp` |
| **CotizacionDAO** | `buscarProductosCotizacion`, `limpiarCotizacionTemp`, `insertarCotizacionTemp`, `eliminarCotizacionTemp`, `mostrarCotizacionTemp`, `sumaCotizacionTemp`, `actualizarDescuentoCotiTemp`, `guardarCotizacion`, `obtenerCotizaciones`, `obtenerDetalleCotizacion`, `obtenerCotizacionHeader`, `convertirCotizacionAVenta` (cross-call a venta), `cancelarCotizacion` |
| **ReporteDAO** | `corteDiario`, `totalesDia`, `reporte_diario` (×2), `ObtenerDato`, `buscarProductoKardex`, `kardexProducto` |

**Juicios de frontera** (documentados para evitar discusión durante la implementación):
- `seleccionarVendedor` → `EmpleadoDAO` (un vendedor es un empleado), aunque se use al imprimir tickets de apartado.
- `obtenerMinMaxDescuentoTemp` → `VentaDAO` (opera sobre `venta_temp`/carrito), aunque conceptualmente roza listas de precios.
- `folioYaRegistrado` → `CompraDAO` (valida el folio dentro del flujo de compra).

## Mecánica de la migración (por DAO)

Para cada dominio:
1. Crear `XxxDAO extends BaseDAO` en `puntoventa.db`.
2. **Mover** (cortar) los métodos del dominio desde `ConexionBD` al DAO, sin cambiar su cuerpo (siguen usando `inst`/`query`/`nvl` heredados de `BaseDAO`).
3. En `ConexionBD`: añadir el campo `private final XxxDAO xxx = new XxxDAO();` y reemplazar cada método movido por su **delegador de una línea** con la misma firma.
4. **Checkpoint:** el usuario compila (`ant clean compile`). Debe dar BUILD SUCCESSFUL.
5. Commit del dominio.

### Llamadas cruzadas entre dominios

Algunos métodos de dominio invocan lógica de otro dominio (ej. `convertirCotizacionAVenta` necesita registrar una venta). Regla: **el DAO instancia el DAO hermano que necesita** (`private final VentaDAO ventas = new VentaDAO();` dentro de `CotizacionDAO`) y llama su método. Estos casos se detectan al mover cada método (cualquier llamada a un método que ya no esté en la clase la marca el compilador) y se resuelven puntualmente.

### RenglonCompra

`RenglonCompra` se extrae como **clase top-level** `puntoventa.db.RenglonCompra`. Su firma se usa en `CompraDAO.guardarFacturaCompleta`. Hay que actualizar su única referencia externa en `ComprasP` (`ConexionBD.RenglonCompra` → `RenglonCompra`, más su `import`): es el **único** llamador que se toca, y es un cambio mecánico de nombre sin lógica.

## Orden de ejecución

`BaseDAO` primero (habilita todo lo demás), luego los dominios de menor a mayor acoplamiento para ganar confianza temprano:

1. `BaseDAO`: mover a ella el constructor + credenciales e `inst`/`query`/`nvl` (cortados de `ConexionBD`); declarar `ConexionBD extends BaseDAO` y **borrar** de `ConexionBD` su constructor y esos métodos (quedan heredados, sin duplicar ni delegar). Las 36 llamadas crudas `conect.inst/query` siguen resolviéndose por herencia.
2. `EmpleadoDAO`
3. `CategoriaDAO`
4. `ProveedorDAO`
5. `GastoDAO`
6. `GananciaDAO`
7. `EmpresaDAO`
8. `ClienteDAO`
9. `DevolucionDAO`
10. `RenglonCompra` (extracción) + `CompraDAO`
11. `ProductoDAO`
12. `ListaPreciosDAO`
13. `ApartadoDAO`
14. `VentaDAO`
15. `CotizacionDAO`
16. `ReporteDAO`

## Verificación

- **Por DAO:** compilación limpia (`ant clean compile`) → BUILD SUCCESSFUL; cero `cannot find symbol`.
- **Final:** `ant run` y humo funcional manual: login, registrar una venta, registrar una compra, crear/abonar un apartado, generar un reporte. Confirmar que la app se comporta idéntica.
- Cada DAO va en su **commit** independiente para revertir granularmente.

## Riesgos y reversibilidad

- **Riesgo bajo:** la fachada mantiene firmas idénticas; los llamadores no cambian (salvo el rename de `RenglonCompra`). Cualquier desajuste lo caza el compilador.
- **Riesgo a vigilar:** llamadas cruzadas entre dominios mal resueltas → las marca el compilador al mover el método; se corrigen instanciando el DAO hermano.
- Todo el trabajo va en `refactor/conexionbd-daos`; si algo no compila, `git reset` del último DAO.

## Estado final esperado

- `ConexionBD` reducida a una fachada de delegadores + herencia de `BaseDAO` (sin lógica SQL propia salvo lo heredado).
- 16 DAOs cohesivos, cada uno entendible y modificable en aislamiento.
- Los 22 llamadores siguen funcionando sin cambios de lógica.
- Base lista para, en un spec futuro, migrar llamadores a DAOs directos y adelgazar los paneles UI.
