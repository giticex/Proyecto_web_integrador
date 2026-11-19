# Backend Spring Boot

API REST de TechZone con Spring Boot 4.0.6, MySQL 8.0 y seguridad JWT.

## 1. Base de datos

Ejecutar completo en MySQL Workbench / phpMyAdmin / DBeaver:

```text
database/script_mysql.sql
```

Crea la base `proyecto`, tablas, datos de demo (usuarios `admin`, `vendedor`,
`CLI001`), procedimiento de devoluciones y la vista `historial_compras`.

## 2. Ejecutar con MySQL

```bash
mvn spring-boot:run
```

o con perfil explícito:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Variables de entorno soportadas: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`JWT_SECRET`, `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`,
`APP_CORS_ALLOWED_ORIGIN`, `PORT`.

## 3. Ejecutar con H2 (opcional)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Panel H2: <http://localhost:8080/h2-console>

## 4. Pruebas

```bash
mvn test
```

Incluye `JwtServiceTest`, `AuthServiceTest` y `PasswordServiceTest`.

## 5. Seguridad JWT

- `POST /api/auth/login` devuelve `token`, `refreshToken`, `tokenType` y
  `expiresIn`.
- `JwtAuthenticationFilter` valida el header `Authorization: Bearer <token>`.
- `SecurityFilterChain` protege `/api/**`; solo login, refresh y registro de
  clientes son públicos.
- `@PreAuthorize` aplica autorización por rol (`ADMIN`, `VENDEDOR`, `CLIENTE`).
- `POST /api/auth/logout` revoca access y refresh token (blacklist en memoria).
- `GET /api/auth/me` devuelve los datos del usuario autenticado.
- `VentaService` obtiene el vendedor desde el JWT, no desde una constante.

Filtros legados eliminados: `AccesoJsfFilter`, `VendedorApiAuthFilter` y
`VendedorAuthFilter`. Las vistas JSF conservan su propio login de sesión y
quedan fuera de la API REST.

## 6. Endpoints

```text
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
GET    /api/auth/me

GET    /api/productos
GET    /api/productos/{id}
POST   /api/productos                 (ADMIN)
PUT    /api/productos/{id}            (ADMIN)
DELETE /api/productos/{id}            (ADMIN)

POST   /api/clientes/registro         (público)
POST   /api/clientes/login            (compatibilidad)
GET    /api/clientes                  (ADMIN, VENDEDOR)
GET    /api/clientes/{id}

POST   /api/ventas                    (autenticado)
GET    /api/ventas                    (ADMIN, VENDEDOR)
GET    /api/ventas/{idVenta}
GET    /api/ventas/cliente/{idCliente}

GET    /api/dashboard                 (ADMIN)
GET    /api/vendedores                (ADMIN)
POST   /api/vendedores                (ADMIN)
GET    /api/catalogos/tipos-documento
GET    /api/catalogos/medios-pago
```

## 7. Docker

```bash
docker build -t techzone-backend .
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/proyecto?useSSL=false" \
  -e DB_USERNAME=root -e DB_PASSWORD=root \
  -e JWT_SECRET="cambia-este-secreto-de-32-caracteres" \
  techzone-backend
```

En el repositorio raíz usar `docker compose up --build` para levantar MySQL,
backend y frontend juntos.
