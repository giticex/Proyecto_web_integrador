# TechZone - Sistema de punto de venta y facturacion

Aplicacion web para gestionar ventas y operaciones de una tienda. El proyecto
esta organizado en un frontend React y un backend Java con Spring Boot, conectado
a una base de datos MySQL.

## Modulos del sistema

### Administrador

- **Dashboard y metricas:** resumen de ventas y actividad del sistema.
- **Punto de venta:** registro y gestion de ventas.
- **Productos e inventario:** consulta y administracion del catalogo y stock.
- **Clientes:** consulta del directorio de clientes.
- **Comprobantes:** acceso a facturas y boletas.
- **Auditoria de cajas:** revision de operaciones de caja.
- **Devoluciones:** consulta y auditoria de devoluciones.
- **Reportes:** consulta de reportes de ventas.
- **Usuarios:** administracion de usuarios del sistema.

### Vendedor

- **Punto de venta:** registro de ventas y emision de comprobantes.
- **Apertura de caja:** inicio de las operaciones de caja.
- **Arqueo y cierre de caja:** registro y cierre de caja.
- **Devoluciones:** gestion de solicitudes de devolucion.
- **Consulta de comprobantes:** busqueda de comprobantes emitidos.

### Cliente

- **Catalogo:** consulta de productos disponibles.
- **Carrito:** seleccion de productos para una compra.
- **Historial:** consulta de compras realizadas.
- **Comprobantes:** consulta o visualizacion del comprobante de una compra.

## Tecnologias

| Componente | Tecnologias |
| --- | --- |
| Frontend | React, Vite, JavaScript |
| Backend | Java 24, Spring Boot, Spring Data JPA, Maven |
| Base de datos | MySQL 8 |

## Requisitos

- JDK 24.
- Apache Maven.
- Node.js y npm.
- MySQL 8 en ejecucion.

Verifica las herramientas desde una terminal:

```bash
java -version
mvn -version
node --version
npm --version
```

## Configuracion de la base de datos

1. Abre `database/script_mysql.sql` en MySQL Workbench, DBeaver u otra
   herramienta compatible y ejecuta el script completo. El script crea la base
   `proyecto` y carga datos iniciales.
2. **Advertencia:** el script elimina y vuelve a crear las tablas existentes.
   Ejecutalo solo en una base de desarrollo; volver a ejecutarlo borra los datos
   guardados en esas tablas.
3. Revisa las credenciales de MySQL en
   `backend/src/main/resources/application.properties` y ajusta
   `spring.datasource.username` y `spring.datasource.password` a tu instalacion.
   No guardes contrasenas personales en Git.

## Ejecucion local

Abre dos terminales desde la carpeta raiz del proyecto.

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

El backend usa el perfil MySQL por defecto y escucha en
`http://localhost:8080`.

### 2. Frontend

En la segunda terminal:

```bash
cd frontend
npm ci
npm run dev
```

Abre la direccion que muestre Vite, normalmente
`http://localhost:5173`. El frontend usa por defecto la API en
`http://localhost:8080/api`.

Para detener cualquiera de los procesos, pulsa `Ctrl+C` en su terminal.

## Pruebas

Las pruebas automatizadas del backend se ejecutan desde `backend/`:

```bash
mvn test
```

Ultima verificacion local: **52 pruebas JUnit aprobadas**, sin fallos ni errores.
La coleccion de Postman ejecuto **69 pruebas aprobadas**, sin fallos ni errores.
La captura del resultado esta en
[`docs/capturas/pruebas-api/01_postman_run_results.png`](./docs/capturas/pruebas-api/01_postman_run_results.png).

## API principal

| Metodo | Ruta | Uso |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Inicio de sesion |
| `GET` | `/api/productos` | Listar productos |
| `POST` | `/api/productos` | Crear producto |
| `PUT` | `/api/productos/{id}` | Actualizar producto |
| `DELETE` | `/api/productos/{id}` | Eliminar producto |
| `GET` | `/api/clientes` | Listar clientes |
| `POST` | `/api/clientes/registro` | Registrar cliente |
| `POST` | `/api/clientes/login` | Inicio de sesion de cliente |
| `GET`, `POST` | `/api/ventas` | Listar y registrar ventas |
| `GET` | `/api/ventas/{idVenta}` | Consultar una venta |
| `GET` | `/api/ventas/cliente/{idCliente}` | Consultar ventas de un cliente |
| `GET` | `/api/dashboard` | Obtener datos del panel |
| `GET` | `/api/catalogos/tipos-documento` | Listar tipos de documento |
| `GET` | `/api/catalogos/medios-pago` | Listar medios de pago |
| `GET`, `POST` | `/api/vendedores` | Listar y registrar vendedores |

La coleccion importable de Postman esta en
[`postman/TechZone-API.postman_collection.json`](./postman/TechZone-API.postman_collection.json).

## Estructura del repositorio

```text
Proyecto_web_integrador/
|-- README.md
|-- database/
|   |-- script_mysql.sql                 Estructura y datos iniciales de MySQL
|   |-- migration_add_rol_usuario.sql    Migracion de roles de usuario
|   `-- sp_registrar_devolucion.sql      Procedimiento de devoluciones
|-- backend/
|   |-- pom.xml                          Dependencias y configuracion Maven
|   |-- README.md                        Instrucciones del backend
|   `-- src/main/
|       |-- java/pe/edu/utp/techzone/
|       |   |-- bean/                    Beans para vistas JSF
|       |   |-- config/                  Configuracion, filtros y CORS
|       |   |-- controller/              Controladores REST y servlets
|       |   |-- dto/                     Objetos de transferencia de datos
|       |   |-- entity/                  Entidades JPA
|       |   |-- exception/               Excepciones y manejo de errores
|       |   |-- repository/              Acceso a datos
|       |   `-- service/                 Logica del sistema
|       |-- resources/
|       |   |-- application*.properties  Configuracion por perfil
|       |   |-- schema.sql               Esquema para H2
|       |   |-- data.sql                 Datos iniciales para H2
|       |   `-- script_mysql.sql         Script de referencia para MySQL
|       `-- webapp/
|           |-- WEB-INF/views/           Vistas JSP
|           |-- WEB-INF/xhtml/           Vistas JSF
|           `-- jsf/                     Paginas JSF
|-- docs/
|   `-- capturas/
|       `-- pruebas-api/
|           `-- 01_postman_run_results.png Resultados del Collection Runner
|-- postman/
|   `-- TechZone-API.postman_collection.json Coleccion exportada de la API
`-- frontend/
    |-- package.json                     Dependencias y comandos npm
    |-- package-lock.json                Versiones instaladas de npm
    |-- public/                          Archivos publicos
    `-- src/
        |-- components/                  Componentes reutilizables
        |-- context/                     Estado global y sesion
        |-- pages/
        |   |-- Admin/                   Paginas de administracion
        |   |-- Vendedor/                Paginas de caja y ventas
        |   `-- ...                      Catalogo, carrito, reportes, etc.
        |-- services/                    Comunicacion con la API
        |-- utils/                       Utilidades compartidas
        |-- App.jsx                      Navegacion principal
        `-- main.jsx                     Punto de entrada de React
```

Para informacion adicional sobre la configuracion del backend, consulta
[`backend/README.md`](./backend/README.md).
