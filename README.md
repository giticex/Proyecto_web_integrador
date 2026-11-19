# TechZone - Sistema de Facturación Web

Sistema de punto de venta y facturación electrónica para una tienda de tecnología.
Incluye una SPA en React, una API REST en Spring Boot con seguridad JWT, base de
datos MySQL y despliegue contenerizado con Docker.

## 1. Stack tecnológico

| Capa | Tecnología |
| --- | --- |
| Backend | Java 24, Spring Boot 4.0.6, Spring Security, Spring Data JPA |
| Seguridad | JWT (jjwt 0.12.6), BCrypt, `@PreAuthorize` por rol, blacklist de tokens |
| Frontend | React 19, Vite 8, React Router DOM, Lucide Icons |
| Base de datos | MySQL 8.0 (H2 opcional para pruebas) |
| Pruebas | JUnit 5, Mockito, Spring Boot Test |
| DevOps | Docker multi-stage, Docker Compose, Nginx |
| Documentación | Markdown + colección Postman |

## 2. Arquitectura

```text
Proyecto_web_integrador/
├── backend/                  API REST Spring Boot + vistas JSF legadas
│   ├── src/main/java/...     controller / service / repository / entity / security
│   ├── src/test/java/...     pruebas JUnit + Mockito
│   └── Dockerfile            build Maven + runtime JRE 24
├── frontend/                 SPA React + Vite
│   ├── src/routes/           ProtectedRoute y mapa de rutas
│   ├── src/services/api.js   interceptor HTTP centralizado
│   ├── nginx.conf            SPA fallback + proxy /api
│   └── Dockerfile            build Node + runtime Nginx
├── database/                 script MySQL y procedimientos
├── docs/                     pruebas, endpoints/pantallas, despliegue, Postman
├── docker-compose.yml        MySQL + backend + frontend
└── README.md
```

Flujo de autenticación:

1. `POST /api/auth/login` valida credenciales (empleado o cliente) y devuelve
   `token` (access, 1 h) y `refreshToken` (7 días).
2. El frontend guarda la sesión y el interceptor agrega
   `Authorization: Bearer <token>` a cada petición.
3. `JwtAuthenticationFilter` valida firma, expiración y blacklist, y publica el
   principal con authorities `ROLE_ADMIN`, `ROLE_VENDEDOR` o `ROLE_CLIENTE`.
4. `@PreAuthorize` restringe cada endpoint según el rol.
5. Ante un 401 el interceptor llama a `POST /api/auth/refresh` una vez y
   reintenta la petición original.
6. `POST /api/auth/logout` revoca access y refresh tokens en la blacklist.

## 3. Roles y credenciales de demostración

| Rol | Usuario | Contraseña | Pantalla inicial |
| --- | --- | --- | --- |
| ADMIN | `admin` | `123456` | `/dashboard` |
| VENDEDOR | `vendedor` | `123456` | `/pos` (o `/apertura` si no hay caja) |
| CLIENTE | `CLI001` | `123456` | `/productos` |

> El usuario `vendedor` se crea al ejecutar `database/script_mysql.sql`. En una
> base ya existente usar la pantalla **Gestión de Usuarios** (ADMIN) o el
> endpoint `POST /api/vendedores`.

## 4. Endpoints principales

| Método | Endpoint | Roles | Descripción |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Público | Login y emisión de tokens |
| POST | `/api/auth/refresh` | Público (refresh válido) | Renueva access token |
| POST | `/api/auth/logout` | Autenticado | Revoca tokens |
| GET | `/api/auth/me` | Autenticado | Datos del usuario del token |
| GET | `/api/productos` | Autenticado | Lista productos |
| POST/PUT/DELETE | `/api/productos/**` | ADMIN | Gestión de productos |
| GET | `/api/clientes` | ADMIN, VENDEDOR | Lista clientes |
| POST | `/api/clientes/registro` | Público | Registro de cliente |
| POST | `/api/ventas` | Autenticado | Registra venta con usuario del JWT |
| GET | `/api/ventas` | ADMIN, VENDEDOR | Lista ventas |
| GET | `/api/ventas/{id}` | Autenticado | Detalle de venta |
| GET | `/api/ventas/cliente/{id}` | ADMIN, VENDEDOR o el propio cliente | Historial |
| GET | `/api/dashboard` | ADMIN | Métricas |
| GET | `/api/vendedores` | ADMIN | Lista vendedores |
| POST | `/api/vendedores` | ADMIN | Crea vendedor |
| GET | `/api/catalogos/*` | Autenticado | Tipos de documento y medios de pago |

La tabla cruzada completa está en
[`docs/endpoints-vs-pantallas.md`](docs/endpoints-vs-pantallas.md).

## 5. Rutas del frontend (React Router)

| Ruta | Módulo | Rol |
| --- | --- | --- |
| `/login` | Inicio de sesión / registro | Público |
| `/dashboard` | Dashboard y métricas | ADMIN |
| `/pos` | Punto de venta | ADMIN, VENDEDOR |
| `/inventario` | Gestión de productos | ADMIN |
| `/clientes` | Directorio de clientes | ADMIN |
| `/reportes` | Reportes SUNAT | ADMIN |
| `/usuarios` | Gestión de usuarios | ADMIN |
| `/auditoria-cajas` | Auditoría de cajas | ADMIN |
| `/apertura` | Apertura de caja | VENDEDOR |
| `/cierre` | Arqueo y cierre | VENDEDOR |
| `/comprobantes` | Consultar comprobantes | ADMIN, VENDEDOR |
| `/devoluciones` | Devoluciones / auditoría | ADMIN, VENDEDOR |
| `/productos` | Catálogo | CLIENTE |
| `/carrito` | Carrito de compras | CLIENTE |
| `/historial` | Historial de compras | CLIENTE, ADMIN |
| `/comprobante` | Boleta / factura | Todos |

`ProtectedRoute` redirige a `/login` sin sesión y al inicio del rol cuando no
hay permisos.

## 6. Ejecución local

### 6.1 Base de datos

Ejecutar `database/script_mysql.sql` completo en MySQL Workbench, DBeaver o
phpMyAdmin. Crea la base `proyecto`, tablas, datos de demo y la vista
`historial_compras`.

### 6.2 Backend

```bash
cd backend
mvn spring-boot:run
```

Variables de entorno opcionales:

```text
DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, APP_CORS_ALLOWED_ORIGIN, PORT
```

API: `http://localhost:8080/api`

### 6.3 Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

SPA: `http://localhost:5173`

## 7. Ejecución con Docker

```bash
cp .env.example .env
docker compose up --build
```

| Servicio | URL |
| --- | --- |
| Frontend (Nginx) | http://localhost:8081 |
| Backend | http://localhost:8080/api |
| MySQL | localhost:3308 (mapeado al 3306 interno) |

El frontend usa `VITE_API_URL=/api` y Nginx hace proxy hacia el backend. El
script SQL se monta como inicialización de MySQL (`docker-entrypoint-initdb.d`).

## 8. Pruebas

```bash
cd backend
mvn test
```

Pruebas incluidas:

- `JwtServiceTest`: emisión, validación, tipo de token y revocación.
- `TokenBlacklistServiceTest` (incluida en JwtServiceTest): expiración de entradas.
- `AuthServiceTest`: login de empleado y credenciales inválidas con Mockito.
- `PasswordServiceTest`: BCrypt y compatibilidad con contraseñas planas.

Matriz manual de casos de prueba:
[`docs/matriz-casos-prueba.md`](docs/matriz-casos-prueba.md).

## 9. Despliegue

El proyecto incluye `render.yaml`, `vercel.json` y Dockerfiles listos para
Railway/Render. La guía paso a paso está en
[`docs/despliegue.md`](docs/despliegue.md).

> Las URLs públicas deben configurarse al conectar la cuenta del proveedor; no
> se incluyen credenciales en el repositorio.

## 10. Documentación

- [Matriz de casos de prueba](docs/matriz-casos-prueba.md)
- [Endpoints vs pantallas](docs/endpoints-vs-pantallas.md)
- [Guía de despliegue](docs/despliegue.md)
- [Capturas por módulo](docs/capturas/README.md)
- [Colección Postman](docs/postman/TechZone-API.postman_collection.json)
- [Entorno Postman](docs/postman/TechZone-Local.postman_environment.json)

## 11. Seguridad implementada (criterio 6.4)

- Filtro JWT propio (`JwtAuthenticationFilter`) sin filtros duplicados legados.
- `SecurityFilterChain` stateless con rutas públicas mínimas.
- Autorización declarativa con `@PreAuthorize` por rol.
- Logout con blacklist en memoria y rotación de refresh tokens.
- Contraseñas BCrypt con migración transparente desde texto plano.
- CORS restringido por variable de entorno.
- Manejo JSON uniforme de 401 y 403.
