# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a NetBeans/Ant Java project. Use these commands:

```bash
ant compile    # Compile the project
ant run        # Run the application
ant jar        # Build distribution JAR to dist/Proy_Ventas.jar
ant clean      # Clean build artifacts
```

## Database Setup

PostgreSQL database named `punto_de_venta`. Restore baseline:
```bash
createdb -U postgres punto_de_venta
pg_restore -U postgres -d punto_de_venta migrations/V1.0__baseline.tar
```

Connection settings are in `src/ConexionBD.java` (URL, database name, user, password).

## Architecture

**Entry Point:** `Interfaz.java` - Main JFrame containing login, navigation menu, and panel switching.

**UI Pattern:** Each business module is a separate JPanel class with `P` suffix:
- `VentasP` - Point-of-sale transactions
- `InventarioP` - Product catalog and stock management
- `ApartadosP` - Customer holds/reserved items with payment tracking
- `ClientesP` - Customer database and credit accounts
- `DevolucionesP` - Product returns
- `EmpleadosP` - Staff management and authentication
- `ComprasP` - Purchase orders
- `GastosP` - Business expenses
- `GananciasP` - Profit analysis
- `AdministracionP` - Admin settings

**Database Layer:** `ConexionBD.java` - Centralized database operations using JDBC.
- `inst(String sql)` - Execute INSERT/UPDATE/DELETE
- `query(String sql)` - Execute SELECT queries
- Uses PostgreSQL stored procedures for some operations

**Utilities:**
- `Excel.java` - Apache POI report generation (Balance Sheet, Income Statement, Daily Reports)
- `GeneradorCodigoBarras.java` - Barcode generation using ZXing (EAN-13, CODE-128, QR)
- `GenTicket.java` - Receipt printing
- `Hora.java` - Background thread for clock display

**Session State:** Static variables in `Interfaz` track logged-in user (`admActivo`, `userActivo`, `idVendedor`).

## Key Dependencies

All JARs are in project root:
- `postgresql-42.7.4.jar` - JDBC driver
- `poi-*.jar`, `xmlbeans-*.jar` - Apache POI for Excel
- `core-3.5.3.jar`, `javase-3.5.3.jar` - ZXing barcodes
- `JTattoo-1.6.13.jar`, `flatlaf-3.7.jar` - UI themes

## Database Migrations

New migrations go in `migrations/` as `V[version]__[description].sql`. Wrap in `BEGIN;`/`COMMIT;` transactions. See `migrations/README.md` for workflow.

## Language

Application and codebase are in Spanish. User-facing text, variable names, and database schema use Spanish terminology.
