# Punto de Venta (Proy_Ventas)

Sistema de Punto de Venta desarrollado en **Java** con la librería gráfica **Swing** y el entorno de desarrollo **NetBeans IDE**.

## Descripción

Esta aplicación es un sistema completo de punto de venta que permite gestionar:

- **Inventario**: Control y gestión de productos
- **Ventas**: Registro y procesamiento de ventas
- **Apartados**: Sistema de apartados para clientes
- **Clientes**: Registro y administración de clientes
- **Devoluciones**: Gestión de devoluciones de productos
- **Gastos**: Control de gastos del negocio
- **Ganancias**: Seguimiento de ganancias
- **Empleados**: Administración de empleados
- **Compras**: Registro de compras
- **Reportes**: Generación de reportes en Excel (Balance General, Estado de Resultados, Reporte Diario)

## Tecnologías Utilizadas

- **Java** (versión 23)
- **Swing** - Librería gráfica para la interfaz de usuario
- **NetBeans IDE** - Entorno de desarrollo integrado
- **PostgreSQL** - Base de datos
- **Apache POI** - Generación de archivos Excel
- **JTattoo** - Look and Feel personalizado

## Requisitos Previos

- Java JDK 23 o superior
- NetBeans IDE (recomendado)
- PostgreSQL instalado y configurado

## Instalación

### Opción 1: Usando NetBeans IDE (Recomendado)

1. Clona el repositorio:
   ```bash
   git clone https://github.com/DannG04/Punto-de-venta.git
   ```

2. Abre NetBeans IDE

3. Selecciona **File > Open Project**

4. Navega hasta la carpeta del proyecto clonado y ábrelo

5. NetBeans detectará automáticamente la configuración del proyecto

6. Configura la conexión a la base de datos PostgreSQL en el archivo `ConexionBD.java`

7. Haz clic derecho en el proyecto y selecciona **Run** para ejecutar la aplicación

### Opción 2: Usando Ant (Línea de comandos)

1. Clona el repositorio:
   ```bash
   git clone https://github.com/DannG04/Punto-de-venta.git
   cd Punto-de-venta
   ```

2. Compila el proyecto:
   ```bash
   ant compile
   ```

3. Ejecuta la aplicación:
   ```bash
   ant run
   ```

## Dependencias

El proyecto incluye las siguientes librerías (JAR incluidos en el repositorio):

- `JTattoo-1.6.13.jar` - Look and Feel
- `postgresql-42.7.4.jar` - Driver JDBC para PostgreSQL
- `poi-3.16.jar` - Apache POI (Core)
- `poi-ooxml-3.16.jar` - Apache POI (OOXML)
- `poi-ooxml-schemas-3.16.jar` - Apache POI (Schemas)
- `commons-collections4-4.1.jar` - Apache Commons Collections
- `xmlbeans-2.6.0.jar` - XMLBeans
- `Util.jar` - Utilidades adicionales

Además, el proyecto utiliza `AbsoluteLayout` de NetBeans para el diseño de formularios.

## Estructura del Proyecto

```
Punto-de-venta/
├── src/                          # Código fuente
│   ├── Interfaz.java             # Contiene la clase principal (Main)
│   ├── ConexionBD.java           # Conexión a base de datos
│   ├── VentasP.java              # Panel de ventas
│   ├── InventarioP.java          # Panel de inventario
│   ├── ClientesP.java            # Panel de clientes
│   ├── ApartadosP.java           # Panel de apartados
│   ├── DevolucionesP.java        # Panel de devoluciones
│   ├── GastosP.java              # Panel de gastos
│   ├── GananciasP.java           # Panel de ganancias
│   ├── ComprasP.java             # Panel de compras
│   ├── EmpleadosP.java           # Panel de empleados
│   ├── AdministracionP.java      # Panel de administración
│   ├── Excel.java                # Generación de reportes Excel
│   └── img/                      # Recursos de imágenes
├── nbproject/                    # Configuración de NetBeans
├── build.xml                     # Script de construcción Ant
└── *.jar                         # Librerías del proyecto
```

## Uso

1. Inicia la aplicación
2. Inicia sesión con tu usuario y contraseña
3. Navega por las diferentes secciones usando el menú lateral
4. Para realizar ventas, primero debes "Iniciar día"
5. Al finalizar, recuerda "Terminar día" para guardar el registro de ventas

## Licencia

Este proyecto es de uso privado.

---

*Desarrollado con ❤️ usando Java Swing y NetBeans IDE*
