# Especificación: Sistema de Licencias + App Super Admin

## Resumen
Agregar un sistema de control de uso a la app cliente, junto con una herramienta separada (Super Admin) que el desarrollador usa para generar instaladores configurados y códigos de activación.

---

## Decisiones de diseño
- **Al vencer la licencia:** modo lectura (no bloqueo total — ventas y escritura deshabilitadas, consultas activas)
- **Super admin:** exe separado, no presente en la app del cliente
- **Generación del instalador:** automatización completa desde la UI (Ant → Launch4j → Inno Setup)
- **Códigos:** formato `XXXX-XXXX-XXXX-XXXX`, hash SHA-256 en BD, uso único

---

## Arquitectura general

```
[Super Admin App]  ──conecta──►  BD punto_de_venta-V1 (desarrollador)
       │
       │  genera backup.tar con:
       │    - licencia (fecha_expiracion configurada)
       │    - codigos_activacion (pre-generados, hasheados)
       │    - empleado (usuario/contraseña iniciales)
       ▼
[Inno Setup]  ──►  PuntoVentaSetup.exe
                         │
                         │  instala en máquina cliente
                         ▼
               [App Cliente]  ──conecta──►  BD punto_de_venta (cliente)
                                               - verifica licencia al login
                                               - valida código si cliente lo ingresa
```

---

## Nuevas tablas — Migración V1.13

Aplica a `punto_de_venta` y `punto_de_venta-V1`.

```sql
-- Licencia de la instalación (siempre una sola fila)
CREATE TABLE licencia (
    id               INTEGER PRIMARY KEY DEFAULT 1,
    fecha_inicio     DATE    NOT NULL DEFAULT CURRENT_DATE,
    fecha_expiracion DATE    NOT NULL,
    activo           BOOLEAN DEFAULT TRUE,
    CHECK (id = 1)
);

-- Códigos de activación pre-cargados en el instalador
CREATE TABLE codigo_activacion (
    id_codigo      SERIAL      PRIMARY KEY,
    codigo_hash    VARCHAR(64) NOT NULL UNIQUE,  -- SHA-256 del código en texto plano
    duracion_dias  INTEGER     NOT NULL,
    usado          BOOLEAN     DEFAULT FALSE,
    fecha_creacion TIMESTAMP   DEFAULT NOW()
);
```

---

## Archivos a crear

| Archivo | Descripción |
|---------|-------------|
| `migrations/V1.13__licencia.sql` | Migración con las dos tablas nuevas |
| `src/SuperAdminApp.java` | Main class del exe super admin (JFrame con 2 tabs) |
| `src/GeneradorInstaladorP.java` | Panel UI: configurar y lanzar el build del instalador |
| `src/GeneradorInstaladorLogic.java` | Lógica de automatización con ProcessBuilder |
| `src/CodigosP.java` | Panel UI: generar y listar códigos de activación |
| `src/LicenciaDialog.java` | Diálogo modal en app cliente para ingresar código |
| `installer/launch4j-superadmin.xml` | Config de Launch4j para SuperAdmin.exe |

---

## Archivos a modificar

| Archivo | Cambios |
|---------|---------|
| `src/ConexionBD.java` | + `verificarLicencia()`, `activarCodigo(String)`, `generarCodigos(int cantidad, int dias)` |
| `src/Interfaz.java` | + verificar licencia post-login, `aplicarModoLectura()`, botón "Activar licencia" |
| `scripts/limpiar_bd_instalador.sql` | + INSERT inicial en tabla `licencia` |

---

## Detalle: GeneradorInstaladorP

Mockup de la UI:
```
┌──────────────────────────────────────────────┐
│  Configuración del instalador                │
├──────────────────────────────────────────────┤
│  Licencia inicial:   [1 mes ▼]               │
│  Usuario inicial:    [admin      ]           │
│  Contraseña:         [••••••••• ]           │
│                                              │
│  Rutas de herramientas:                      │
│  Inno Setup:  [C:\...\ISCC.exe        ] [📁] │
│  pg_dump:     [C:\...\pg_dump.exe     ] [📁] │
│                                              │
│  [ ▶ Generar Setup.exe ]                     │
│  ▓▓▓▓░░░░░░  Paso 2/5: Compilando JAR...    │
└──────────────────────────────────────────────┘
```

Pasos automáticos (GeneradorInstaladorLogic):
1. Ejecutar `limpiar_bd_instalador.sql` → limpia tablas de negocio
2. Insertar empleado inicial con usuario/contraseña dados
3. Insertar fila en `licencia` con `fecha_expiracion = NOW() + N días`
4. `pg_dump -Ft punto_de_venta-V1 > installer/base_datos/backup.tar`
5. `ant jar` → `dist/Proy_Ventas.jar`
6. Launch4j CLI → `installer/PuntoVenta.exe`
7. `ISCC.exe installer/setup.iss` → `installer/output/PuntoVentaSetup.exe`
8. Abrir carpeta `installer/output/` en el Explorador

---

## Detalle: CodigosP

Mockup:
```
┌───────────────────────────────────────────────────────┐
│  Generar códigos de activación                        │
├───────────────────────────────────────────────────────┤
│  Duración: [30 días ▼]   Cantidad: [5]   [Generar]   │
├───────────────────────────────────────────────────────┤
│  Código               │ Días │ Usado │ Fecha          │
│  A1B2-C3D4-E5F6-G7H8  │  30  │  No   │ 27/04/2026    │
│  I9J0-K1L2-M3N4-O5P6  │  30  │  No   │ 27/04/2026    │
│  ...                                                  │
└───────────────────────────────────────────────────────┘
  ⚠ El código en texto plano solo se muestra aquí.
    En la BD se guarda únicamente el hash SHA-256.
```

---

## Detalle: verificación de licencia en app cliente

Flujo al hacer login exitoso en `Interfaz.java`:
```
iniciarSesion()
  └─► verificarLicencia()
        ├─ VIGENTE  → flujo normal
        └─ VENCIDA  → aplicarModoLectura()
                        ├─ Deshabilita: nueva venta, apartado, compra, gasto
                        ├─ Habilita: consultas, reportes, inventario lectura
                        └─ Muestra banner rojo: "Licencia vencida — [Activar]"
```

Al pulsar "Activar" → abre `LicenciaDialog`:
```
┌────────────────────────────────────┐
│  Activar licencia                  │
├────────────────────────────────────┤
│  Ingresa tu código de activación:  │
│  [XXXX-XXXX-XXXX-XXXX           ]  │
│                                    │
│        [Cancelar]  [Activar]       │
└────────────────────────────────────┘
```

`activarCodigo(String codigo)` en ConexionBD:
1. Calcula `SHA-256(codigo)`
2. Busca en `codigo_activacion` donde `codigo_hash = hash AND usado = false`
3. Si encontrado: `UPDATE licencia SET fecha_expiracion = fecha_expiracion + duracion_dias`; marca `usado = true`
4. Si no encontrado: error "Código inválido o ya utilizado"

---

## Seguridad de los códigos
- Texto plano **solo visible en el panel CodigosP** al momento de generación (no se persiste)
- BD del cliente solo contiene hashes SHA-256 (no reversibles)
- Un código = un solo uso (`usado = true` tras activar)
- Sin conexión a internet requerida (validación completamente local)

---

## Verificación / Testing
1. Aplicar V1.13 en ambas BDs
2. Abrir SuperAdmin.exe → generar 2 códigos de 30 días → anotar textos planos
3. Usar "Generar instalador" (usuario `admin`/`admin123`, licencia 1 mes) → verificar `PuntoVentaSetup.exe` generado
4. Instalar en máquina de prueba → verificar login con `admin`
5. Forzar `fecha_expiracion = CURRENT_DATE - 1` en BD del cliente → reiniciar app → verificar modo lectura + banner
6. Ingresar código válido → verificar que la licencia se extiende y modo lectura desaparece
7. Intentar el mismo código de nuevo → verificar error "código ya utilizado"
