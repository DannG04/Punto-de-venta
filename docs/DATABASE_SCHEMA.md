# Database Schema Documentation

## Database: punto_de_venta (PostgreSQL)

This document describes the complete database schema for the Point of Sale system.

---

## Tables Overview

| Table | Description |
|-------|-------------|
| `empleado` | Employee/staff records and authentication |
| `cliente` | Customer database |
| `producto` | Product catalog and inventory |
| `venta` | Completed sales transactions |
| `venta_detalle` | Line items for each sale |
| `venta_temp` | Temporary cart items during sale |
| `apartado` | Customer holds/layaway orders |
| `apartado_detalle` | Line items for layaway orders |
| `compras` | Purchase orders from suppliers |
| `compra_producto` | Line items for purchase orders |
| `devolucion_ventas` | Product return headers |
| `devolucion_ventas_detalle` | Product return line items |
| `gastos` | Business expenses |
| `otras_ganancias` | Additional income/revenue |

---

## Custom Types (Domains/Enums)

### curp_dominio
Domain type for Mexican CURP-format IDs (used for employee and customer IDs).
- Base type: `varchar(18)`
- CHECK constraint validates full CURP format:
  - 4 letters (first vowel in position 2)
  - 6 digits (birth date YYMMDD)
  - 1 letter (gender: H/M)
  - 2 letters (state code)
  - 3 consonants
  - 1 alphanumeric + 1 digit (verification)
- Example: `XAXX111111HCCXXXX0` (generic/anonymous customer)

### id_producto_dominio
Domain type for product IDs.
- Base type: `varchar(50)`
- CHECK constraint: `^[A-Za-z0-9_-]{3,50}$` (alphanumeric with dashes/underscores, 3-50 chars)
- Auto-generated IDs use format: `P-00000000001` (via `generar_id_producto()` function)

### puestoValido (Enum)
Employee role/position type.
- `'gerente'` - Manager with full access
- `'vendedor'` - Sales staff with limited access

### estatusValido (Enum)
Active/inactive status for employees and customers.
- `'Activo'` - Active record
- `'Inactivo'` - Deactivated record

### estadoValido (Enum)
Layaway order status.
- `'Vigente'` - Active/pending
- `'Entregado'` - Delivered/completed
- `'Cancelado'` - Cancelled
- `'Anulado'` - Voided/annulled

### tipoVenta (Enum)
Sale type (pricing tier).
- `'Mayoreo'` - Wholesale price
- `'Menudeo'` - Retail price

---

## Table Definitions

### empleado (Employees)

Staff records including authentication credentials and roles.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_empleado` | curp_dominio | NOT NULL | | Primary key (CURP format) |
| `nombre` | varchar(50) | | | Employee name |
| `puesto` | puestoValido | | | Role: 'gerente' or 'vendedor' |
| `telefono` | varchar(10) | | | Phone number (exactly 10 digits) |
| `usuario` | varchar(20) | | | Login username |
| `contrasenia` | varchar(20) | | | Login password |
| `estatus` | estatusValido | | 'Activo' | Active/Inactive status |

**Primary Key:** `id_empleado`

**Check Constraints:**
- `verif_te`: Phone must be exactly 10 characters

**Triggers:**
- `trigger_verif_puesto` (INSERT/UPDATE) - Validates employee position
- `trigger_verif_tel_e` (INSERT/UPDATE) - Validates phone format

---

### cliente (Customers)

Customer database.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_cliente` | curp_dominio | NOT NULL | | Primary key (CURP format) |
| `nombre` | varchar(50) | | | Customer name |
| `telefono` | varchar(10) | | | Phone number (exactly 10 digits) |
| `estatus` | estatusValido | | 'Activo' | Active/Inactive status |

**Primary Key:** `id_cliente`

**Check Constraints:**
- `verif_tel`: Phone must be exactly 10 characters

**Triggers:**
- `trigger_verif_tel_c` (INSERT/UPDATE) - Validates phone format

---

### producto (Products)

Product catalog with inventory tracking.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_producto` | id_producto_dominio | NOT NULL | | Primary key |
| `nombre` | varchar(50) | | | Product name |
| `cantidad` | integer | | | Current inventory quantity |
| `precio_mayoreo` | numeric(10,2) | | | Wholesale price |
| `precio_menudeo` | numeric(10,2) | | | Retail price |

**Primary Key:** `id_producto`

**Triggers:**
- `trigger_validar_id_producto` (INSERT/UPDATE) - Validates product ID format

---

### venta (Sales)

Completed sales transaction headers.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_venta` | varchar(15) | NOT NULL | | Primary key (auto-generated) |
| `id_empleado` | curp_dominio | | | FK to empleado (salesperson) |
| `total_venta` | numeric(10,2) | | | Total amount |
| `fecha_venta` | date | | | Transaction date |
| `id_cliente` | curp_dominio | | 'XAXX111111HCCXXXX0' | FK to cliente (default: generic) |

**Primary Key:** `id_venta`

**Foreign Keys:**
- `id_empleado` → `empleado(id_empleado)` ON UPDATE CASCADE ON DELETE CASCADE

**Triggers:**
- `trigger_set_campos_venta` (INSERT) - Auto-generates ID and sets date

---

### venta_detalle (Sale Line Items)

Individual products sold in each transaction.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_venta` | varchar(15) | | | FK to venta |
| `id_producto` | id_producto_dominio | | | FK to producto |
| `cantidad_producto` | integer | | | Quantity sold |
| `precio_dado` | numeric(10,2) | | | Unit price at sale |
| `precio_total` | numeric(10,2) | | | Line total |
| `tipo_venta` | tipoVenta | | | Wholesale or retail |

**Foreign Keys:**
- `id_venta` → `venta(id_venta)` ON UPDATE CASCADE ON DELETE CASCADE
- `id_producto` → `producto(id_producto)` ON UPDATE CASCADE ON DELETE CASCADE

---

### venta_temp (Shopping Cart)

Temporary storage for items during an active sale session.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_producto` | id_producto_dominio | | | FK to producto |
| `nombre_p` | varchar(50) | | | Product name (cached) |
| `cantidad_prod` | integer | | | Quantity |
| `precio_dado` | numeric(10,2) | | | Unit price |
| `precio_total` | numeric(10,2) | | | Line total |
| `tipo_venta` | tipoVenta | | | Wholesale or retail |

**Foreign Keys:**
- `id_producto` → `producto(id_producto)` ON UPDATE CASCADE ON DELETE CASCADE

**Triggers:**
- `trigger_verif_exist_insert` (INSERT) - Validates product exists
- `trigger_verif_exist_update` (UPDATE) - Validates product exists

---

### apartado (Layaway/Holds)

Customer layaway orders with payment tracking.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_apartado` | varchar(15) | NOT NULL | | Primary key (auto-generated) |
| `id_empleado` | curp_dominio | | | FK to empleado |
| `id_cliente` | curp_dominio | | | FK to cliente |
| `fecha_inicio` | date | | | Creation date |
| `fecha_limite` | date | | | Due date |
| `cantidad_dada` | numeric(10,2) | | | Amount paid (down payment + payments) |
| `cantidad_faltante` | numeric(10,2) | | | Remaining balance |
| `cantidad_total` | numeric(10,2) | | | Total amount |
| `estado` | estadoValido | | | Status (Vigente/Entregado/Cancelado/Anulado) |

**Primary Key:** `id_apartado`

**Foreign Keys:**
- `id_empleado` → `empleado(id_empleado)` ON UPDATE CASCADE ON DELETE CASCADE
- `id_cliente` → `cliente(id_cliente)` ON UPDATE CASCADE ON DELETE CASCADE

**Triggers:**
- `trigger_set_campos_ap` (INSERT) - Auto-generates ID and sets dates

---

### apartado_detalle (Layaway Line Items)

Products in each layaway order.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_apartado` | varchar(15) | | | FK to apartado |
| `id_producto` | id_producto_dominio | | | FK to producto |
| `cantidad` | integer | | | Quantity |
| `precio_unitario` | numeric(10,2) | | | Unit price |
| `precio_total` | numeric(10,2) | | | Line total |
| `tipo` | tipoVenta | | | Wholesale or retail |

**Foreign Keys:**
- `id_apartado` → `apartado(id_apartado)` ON UPDATE CASCADE ON DELETE CASCADE
- `id_producto` → `producto(id_producto)` ON UPDATE CASCADE ON DELETE CASCADE

**Triggers:**
- `trigger_set_campos_apdet` (INSERT/UPDATE) - Auto-calculates totals

---

### compras (Purchase Orders)

Inventory purchases from suppliers.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_compra` | varchar(15) | NOT NULL | | Primary key (auto-generated) |
| `id_empleado` | curp_dominio | | | FK to empleado |
| `fecha_compra` | date | | | Purchase date |
| `descripcion` | varchar(100) | | | Notes/description |
| `monto` | numeric(10,2) | | | Total cost |

**Primary Key:** `id_compra`

**Foreign Keys:**
- `id_empleado` → `empleado(id_empleado)` ON UPDATE CASCADE ON DELETE CASCADE

**Triggers:**
- `trigger_set_campos_compras` (INSERT) - Auto-generates ID and sets date

---

### compra_producto (Purchase Order Line Items)

Products in each purchase order.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_compra` | varchar(15) | | | FK to compras |
| `id_producto` | id_producto_dominio | | | FK to producto |
| `cantidad` | integer | | | Quantity purchased |
| `precio_adquirido` | numeric(10,2) | | | Unit cost |
| `precio_total` | numeric(10,2) | | | Line total |

**Foreign Keys:**
- `id_compra` → `compras(id_compra)` ON UPDATE CASCADE ON DELETE CASCADE
- `id_producto` → `producto(id_producto)` ON UPDATE CASCADE ON DELETE CASCADE

---

### devolucion_ventas (Returns)

Product return transaction headers.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_devolucion` | varchar(15) | NOT NULL | | Primary key (auto-generated) |
| `id_venta` | varchar(15) | | | FK to original venta |
| `fecha_devolucion` | date | | | Return date |
| `monto` | numeric(10,2) | | | Refund amount |

**Primary Key:** `id_devolucion`

**Foreign Keys:**
- `id_venta` → `venta(id_venta)` ON UPDATE CASCADE ON DELETE CASCADE

**Triggers:**
- `trigger_set_campos_devv` (INSERT) - Auto-generates ID and sets date

---

### devolucion_ventas_detalle (Return Line Items)

Products returned in each return transaction.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_devolucion` | varchar(15) | | | FK to devolucion_ventas |
| `id_producto` | id_producto_dominio | | | FK to producto |
| `motivo` | varchar(50) | | | Reason for return |
| `cantidad` | integer | | | Quantity returned |
| `total` | numeric(10,2) | | | Line total |

**Foreign Keys:**
- `id_devolucion` → `devolucion_ventas(id_devolucion)` ON UPDATE CASCADE ON DELETE CASCADE
- `id_producto` → `producto(id_producto)` ON UPDATE CASCADE ON DELETE CASCADE

---

### gastos (Expenses)

Business operating expenses.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_gasto` | varchar(15) | | | Expense ID (auto-generated) |
| `id_empleado` | curp_dominio | | | FK to empleado |
| `fecha_gasto` | date | | | Date |
| `descripcion` | varchar(50) | | | Expense description |
| `monto` | numeric(10,2) | | | Amount |

**Foreign Keys:**
- `id_empleado` → `empleado(id_empleado)` ON UPDATE CASCADE ON DELETE CASCADE

**Triggers:**
- `trigger_set_campos_gstos` (INSERT) - Auto-generates ID and sets date

---

### otras_ganancias (Other Income)

Additional revenue not from product sales.

| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id_otras_ganancias` | varchar(15) | NOT NULL | | Primary key (auto-generated) |
| `descripcion` | varchar(100) | | | Income description |
| `monto` | numeric(10,2) | | | Amount |
| `fecha_otras_ganancias` | date | | | Date |

**Primary Key:** `id_otras_ganancias`

**Triggers:**
- `trigger_set_campos_og` (INSERT) - Auto-generates ID and sets date

---

## Entity Relationship Diagram (Conceptual)

```
empleado ─────────────┬───────────────┬───────────────┬─────────────────┐
                      │               │               │                 │
                      ▼               ▼               ▼                 ▼
                   venta          apartado        compras            gastos
                      │               │               │
                      ▼               ▼               ▼
               venta_detalle   apartado_detalle  compra_producto
                      │               │               │
                      └───────────────┴───────────────┘
                                      │
                                      ▼
                                  producto

cliente ──────────────┬───────────────┐
                      │               │
                      ▼               ▼
                   venta          apartado

venta ────────────────┐
                      │
                      ▼
             devolucion_ventas
                      │
                      ▼
         devolucion_ventas_detalle
```

---

## Triggers Summary

| Trigger | Table | Event | Function | Description |
|---------|-------|-------|----------|-------------|
| `trigger_set_campos_ap` | apartado | INSERT | `set_campos_ap()` | Auto-generates ID, sets dates and initial values |
| `trigger_set_campos_apdet` | apartado_detalle | INSERT/UPDATE | `set_campos_apdet()` | Auto-calculates subtotals and updates apartado total |
| `trigger_verif_tel_c` | cliente | INSERT/UPDATE | `verif_tel_c()` | Validates phone number format |
| `trigger_set_campos_compras` | compras | INSERT | `set_campos_compras()` | Auto-generates ID and sets date |
| `trigger_set_campos_devv` | devolucion_ventas | INSERT | `set_campos_devv()` | Auto-generates ID and sets date |
| `trigger_verif_puesto` | empleado | INSERT/UPDATE | `verif_puesto()` | Validates employee role/position |
| `trigger_verif_tel_e` | empleado | INSERT/UPDATE | `verif_tel_e()` | Validates phone number format |
| `trigger_set_campos_gstos` | gastos | INSERT | `set_campos_gstos()` | Auto-generates ID and sets date |
| `trigger_set_campos_og` | otras_ganancias | INSERT | `set_campos_og()` | Auto-generates ID and sets date |
| `trigger_validar_id_producto` | producto | INSERT/UPDATE | `validar_id_producto()` | Validates product ID format |
| `trigger_set_campos_venta` | venta | INSERT | `set_campos_venta()` | Auto-generates ID and sets date |
| `trigger_verif_exist_insert` | venta_temp | INSERT | `verif_exist_insert()` | Validates product exists before adding to cart |
| `trigger_verif_exist_update` | venta_temp | UPDATE | `verif_exist_update()` | Validates product exists on update |

---

## Stored Functions

The database includes 56 stored functions organized by business domain.

### Sales Functions (Ventas)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `realizar_venta` | `idemp curp_dominio` | varchar | Process sale for employee (no specific customer) |
| `realizar_venta` | `idemp curp_dominio, idecli curp_dominio` | varchar | Process sale with specific customer |
| `set_venta_temp` | `ideprod, cant, acumular` | void | Add/update item in temporary cart |
| `suma_venta_temp` | none | numeric | Calculate total of current cart |

**`set_venta_temp(ideprod, cant, acumular)`**
```sql
-- Adds product to temporary cart or updates quantity if acumular=true
SELECT set_venta_temp('PROD001', 2, true);
```

### Layaway Functions (Apartados)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `reg_apartado` | `idemp curp_dominio, idecli curp_dominio` | varchar | Create new layaway order, returns ID |
| `reg_apdet` | `ideapar, idepro, cant, acum` | void | Add product to layaway detail |
| `act_apartado` | `ideap, cantdd` | void | Add payment to layaway order |
| `entregar_ap` | `ideap` | void | Mark layaway as delivered/completed |
| `cancelar_ap` | `ideap` | void | Cancel layaway, return stock |
| `anular_ap` | `ideapart` | void | Void/annul layaway order |
| `cancelar_ap_cliente` | `ideap` | numeric | Cancel layaway for client, returns refund amount |
| `elim_apartado` | `ideap` | void | Delete layaway record |
| `elim_apdet` | `ideap, idepro` | void | Delete layaway detail item |
| `reg_apartado_venta` | `idapart, idemp` | void | Convert completed layaway to sale |
| `ap_vigentes` | none | void | Check/update expired layaways |
| `regresar_productos_inv` | `ideap` | void | Return layaway products to inventory |

**`act_apartado(ideap, cantdd)`**
```sql
-- Adds payment of 500.00 to layaway AP001
SELECT act_apartado('AP001', 500.00);
```

### Purchase Functions (Compras)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `reg_compra` | `idemp, des, mon` | varchar | Create purchase order, returns ID |
| `reg_compra_prod` | `idecom, idepro, prec, cant, acum` | void | Add product to purchase order |
| `elim_compra` | `idecom` | void | Delete purchase order |
| `elim_compra_prod` | `idecom, idepro` | void | Delete purchase order item |

### Returns Functions (Devoluciones)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `reg_devolucion` | `ideven, mon` | varchar | Create return record, returns ID |
| `reg_devolucion_prod` | `idedev, ideve, idepro, mot, canti` | void | Add product to return, `mot` indicates if return to inventory |
| `elim_devolucion` | `idedev` | void | Delete return record |
| `elim_devolucion_prod` | `idedev, idepro` | void | Delete return detail item |

**`reg_devolucion_prod(idedev, ideve, idepro, mot, canti)`**
```sql
-- Return 2 units, mot=true means return to inventory
SELECT reg_devolucion_prod('DEV001', 'VEN001', 'PROD001', true, 2);
```

### Inventory Functions (Inventario)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `act_productos_inventario` | `incrementar boolean, ideprod, cant` | void | Adjust stock (+/-) |
| `generar_id_producto` | none | varchar | Generate unique product ID |
| `delete_producto` | `ideprod` | void | Delete product |
| `eliminar_prod` | `idepro` | void | Remove product from system |
| `hay_pocos_prod` | none | boolean | Check if any product below min stock |

**`act_productos_inventario(incrementar, ideprod, cant)`**
```sql
-- Add 10 units to product
SELECT act_productos_inventario(true, 'PROD001', 10);
-- Remove 5 units from product
SELECT act_productos_inventario(false, 'PROD001', 5);
```

### Employee Functions (Empleados)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `reg_empleado` | `nomb, passwor, puest` | void | Register new employee |
| `ide_emp` | `neim varchar` | curp_dominio | Get employee ID by name |
| `ide_emp` | `userr, contra` | TABLE(ide, neim, pues, tel) | Login validation, returns employee info |
| `inactivar_empleado` | `idemp, inact` | void | Activate/deactivate employee |
| `verif_user` | `userr` | boolean | Check if username exists |

### Customer Functions (Clientes)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `inactivar_cliente` | `idecli curp_dominio` | void | Deactivate customer account |
| `inactivar_cliente` | `idecli curp_dominio, inactivar boolean` | void | Activate/deactivate customer |
| `anticipo_clientes` | none | numeric | Get total customer advances/deposits |

### Reporting Functions (Reportes)

| Function | Parameters | Returns | Description |
|----------|------------|---------|-------------|
| `balance_general` | `inicio date, fin date` | TABLE(caja, inventario, clientes) | Balance sheet for date range |
| `estado_resultados` | `inv_ini numeric` | TABLE(ventas, devoluciones, ...) | Income statement |
| `reporte_diario` | none | TABLE(ventas, devoluciones, ...) | Daily summary report |
| `total_caja` | `inicio date, fin date` | numeric | Cash total for date range |
| `total_inventario` | none | numeric | Current inventory value |
| `total_clientes` | none | numeric | Total customer receivables |

**`balance_general(inicio, fin)`**
```sql
-- Get balance sheet for January 2024
SELECT * FROM balance_general('2024-01-01', '2024-01-31');
-- Returns: caja | inventario | clientes
```

**`estado_resultados(inv_ini)`**
```sql
-- Income statement with initial inventory of 50000
SELECT * FROM estado_resultados(50000.00);
-- Returns: total_ventas | total_devoluciones | inventario_inicial |
--          total_compras | total_gastos | inventario_final | total_otras_ganancias
```

**`reporte_diario()`**
```sql
-- Get today's report
SELECT * FROM reporte_diario();
-- Returns: total_ventas | total_devoluciones | total_compras |
--          total_gastos | total_otras_ganancias
```

### Trigger Functions

| Function | Returns | Description |
|----------|---------|-------------|
| `set_campos_ap` | trigger | Auto-populates apartado fields |
| `set_campos_apdet` | trigger | Auto-calculates apartado_detalle fields |
| `set_campos_compras` | trigger | Auto-populates compras fields |
| `set_campos_devv` | trigger | Auto-populates devolucion_ventas fields |
| `set_campos_gstos` | trigger | Auto-populates gastos fields |
| `set_campos_og` | trigger | Auto-populates otras_ganancias fields |
| `set_campos_venta` | trigger | Auto-populates venta fields |
| `validar_id_producto` | trigger | Validates product ID format |
| `verif_exist_insert` | trigger | Validates product existence on cart insert |
| `verif_exist_update` | trigger | Validates product existence on cart update |
| `verif_puesto` | trigger | Validates employee position |
| `verif_tel_c` | trigger | Validates customer phone format |
| `verif_tel_e` | trigger | Validates employee phone format |

---

## Sequences

| Sequence | Type | Description |
|----------|------|-------------|
| `id_producto_seq` | bigint | Sequence for generating unique product IDs. Used by `generar_id_producto()` function. Starts at 0, max 99999999999. |

---

## Notes

1. **ID Generation**: Most tables use auto-generated varchar(15) IDs (e.g., 'VEN001', 'AP001', 'COM001') created by trigger functions. Products use a separate sequence (`id_producto_seq`) generating IDs like 'P-00000000001'.

2. **CURP Format**: Employee and customer IDs use Mexican CURP format (18-character alphanumeric). A generic customer ID `XAXX111111HCCXXXX0` is used for anonymous/walk-in sales.

3. **Dual Pricing**: Products have both `precio_mayoreo` (wholesale) and `precio_menudeo` (retail) prices. The `tipoVenta` enum tracks which price was used in each sale.

4. **Inventory Management**: Stock levels (`cantidad` in `producto`) are automatically adjusted when sales are completed or returns are processed.

5. **Temporary Cart**: `venta_temp` stores items during an active sale session. It has no primary key and is cleared when sale is completed.

6. **Layaway States**: Apartados progress through states: Vigente (active) → Entregado (delivered) or Cancelado/Anulado (cancelled/voided).

7. **Cascade Deletes**: All foreign keys use `ON UPDATE CASCADE ON DELETE CASCADE`, meaning related records are automatically updated or deleted.
