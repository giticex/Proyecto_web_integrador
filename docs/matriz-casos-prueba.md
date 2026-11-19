# Matriz de casos de prueba - TechZone

Leyenda de estado: `Automatizado`, `Manual`, `Pendiente`.
Fecha de referencia: 2026-11.

## 1. Autenticación y seguridad

| ID | Módulo | Caso de prueba | Precondición | Pasos | Resultado esperado | Tipo | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AUTH-01 | Login | Login ADMIN correcto | BD inicializada | POST `/api/auth/login` con `admin` / `123456` | 200, `token`, `refreshToken`, `rol=ADMIN` | API | Automatizado (Postman) |
| AUTH-02 | Login | Login VENDEDOR correcto | Usuario `vendedor` creado | POST login `vendedor` / `123456` | 200, `rol=VENDEDOR` | API | Automatizado (Postman) |
| AUTH-03 | Login | Login CLIENTE correcto | Cliente `CLI001` existente | POST login `CLI001` / `123456` | 200, `rol=CLIENTE` | API | Automatizado (Postman) |
| AUTH-04 | Login | Contraseña incorrecta | Usuario existente | POST login con password errónea | 400, mensaje "Credenciales incorrectas" | API | Manual |
| AUTH-05 | Seguridad | Acceso sin token | API arriba | GET `/api/productos` sin header | 401 JSON | API | Automatizado |
| AUTH-06 | Seguridad | Token inválido | API arriba | GET con `Bearer abc` | 401 JSON | API | Manual |
| AUTH-07 | Seguridad | Token expirado | Token con `exp` vencido | GET `/api/productos` | 401; frontend intenta refresh | API/UI | Manual |
| AUTH-08 | Seguridad | Rol insuficiente | Token VENDEDOR | GET `/api/dashboard` | 403 JSON | API | Manual |
| AUTH-09 | Seguridad | Logout revoca access | Token válido | POST `/api/auth/logout`; reutilizar token | Logout 200; siguiente petición 401 | API | Automatizado (Postman) |
| AUTH-10 | Seguridad | Rotación refresh | Refresh válido | POST `/api/auth/refresh` dos veces con el mismo token | Primero 200; segundo 400 | API | Manual |
| AUTH-11 | Seguridad | `/auth/me` | Token ADMIN | GET `/api/auth/me` | 200 con usuario y rol | API | Automatizado (Postman) |
| AUTH-12 | Unidad | Firma y claims JWT | — | `mvn test` | `JwtServiceTest` en verde | JUnit | Automatizado |
| AUTH-13 | Unidad | Revocación de token | — | `mvn test` | `revocarInvalidaElToken` en verde | JUnit | Automatizado |
| AUTH-14 | Unidad | Password BCrypt | — | `mvn test` | `PasswordServiceTest` en verde | JUnit | Automatizado |

## 2. Ventas y comprobantes

| ID | Módulo | Caso | Precondición | Pasos | Resultado esperado | Tipo | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| VEN-01 | POS | Registrar venta como vendedor | Token VENDEDOR, stock disponible | Completar POS y cobrar | Venta creada; `persona` = vendedor del token | UI/API | Manual |
| VEN-02 | POS | Venta como cliente | Token CLIENTE, carrito con items | Confirmar compra | Venta con persona del cliente y comprobante generado | UI/API | Manual |
| VEN-03 | POS | Stock insuficiente | Producto con stock 1 | Intentar vender 2 | Error de negocio; no descuenta stock | API | Manual |
| VEN-04 | Comprobante | Serie por tipo | Venta boleta/factura | Revisar `numeroDocumento` | `B001-########` o `F001-########` | API | Manual |
| VEN-05 | Comprobante | Reimpresión | Venta existente | Abrir `/comprobante` desde historial | Se muestra boleta/factura seleccionada | UI | Manual |
| VEN-06 | Historial | Filtrar por fecha | Ventas cargadas | Usar filtros desde/hasta | Solo ventas dentro del rango | UI | Manual |
| VEN-07 | Seguridad | Usuario de venta real | Token `vendedor` | Registrar venta y consultar detalle | `persona` corresponde a `vendedor`, no a `admin` | API | Manual |

## 3. Productos e inventario

| ID | Módulo | Caso | Precondición | Pasos | Resultado esperado | Tipo | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| PRO-01 | Productos | Listar catálogo | Token válido | GET `/api/productos` | 200 con lista | API | Automatizado |
| PRO-02 | Productos | Crear producto | Token ADMIN | POST producto válido | 200 con producto creado | API | Automatizado (Postman) |
| PRO-03 | Productos | Crear producto sin token | Sin sesión | POST producto | 401 | API | Manual |
| PRO-04 | Productos | Crear como vendedor | Token VENDEDOR | POST producto | 403 | API | Manual |
| PRO-05 | Productos | Actualizar stock | Token ADMIN | PUT con nuevo stock | Stock actualizado | UI/API | Manual |
| PRO-06 | Productos | Eliminar producto | Token ADMIN | DELETE producto sin ventas | 200 y producto ausente | API | Manual |
| PRO-07 | Productos | Validación de campos | Token ADMIN | POST con precio negativo | 400 "Datos incompletos o inválidos" | API | Manual |
| PRO-08 | Inventario | Alerta de stock bajo | Producto con stock 0 | Abrir `/inventario` | Alerta visible | UI | Manual |

## 4. Clientes y usuarios

| ID | Módulo | Caso | Precondición | Pasos | Resultado esperado | Tipo | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CLI-01 | Clientes | Registro público | Sin sesión | POST `/api/clientes/registro` | Cliente creado y login posible | API | Automatizado (Postman) |
| CLI-02 | Clientes | Registro duplicado | Cliente ya existe | Repetir registro | 400 "El cliente ya existe" | API | Manual |
| CLI-03 | Clientes | Listar clientes | Token ADMIN/VENDEDOR | GET `/api/clientes` | 200 con lista | API | Manual |
| CLI-04 | Clientes | Cliente no lista | Token CLIENTE | GET `/api/clientes` | 403 | API | Manual |
| USU-01 | Usuarios | Crear vendedor | Token ADMIN | POST `/api/vendedores` | Vendedor creado con rol VENDEDOR | API | Automatizado (Postman) |
| USU-02 | Usuarios | Crear vendedor duplicado | Usuario existente | Repetir POST | 400 usuario ya registrado | API | Manual |
| USU-03 | Usuarios | Vendedor no crea usuarios | Token VENDEDOR | POST `/api/vendedores` | 403 | API | Manual |

## 5. Dashboard, reportes y caja

| ID | Módulo | Caso | Precondición | Pasos | Resultado esperado | Tipo | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DASH-01 | Dashboard | Ver métricas | Token ADMIN | GET `/api/dashboard` | 200 con totales | API/UI | Automatizado |
| DASH-02 | Dashboard | Vendedor bloqueado | Token VENDEDOR | GET `/api/dashboard` | 403 | API | Manual |
| REP-01 | Reportes | Totales y productos top | Ventas en BD | Abrir `/reportes` | Tarjetas y ranking coherentes | UI | Manual |
| REP-02 | Reportes | Imprimir | Navegador | Botón Imprimir | Vista de impresión sin sidebar | UI | Manual |
| CAJ-01 | Caja | Apertura | Token VENDEDOR | Registrar fondo inicial | Caja abierta en localStorage, redirige a `/pos` | UI | Manual |
| CAJ-02 | Caja | Cierre y arqueo | Caja abierta y ventas | Registrar conteo físico | Reporte de diferencias y cierre | UI | Manual |
| DEV-01 | Devoluciones | Registrar devolución | Venta existente | Abrir `/devoluciones` | Nota de crédito registrada | UI | Manual |
| DEV-02 | Devoluciones | Auditoría admin | Token ADMIN | Abrir `/devoluciones` | Se muestra auditoría, no formulario vendedor | UI | Manual |

## 6. Navegación y frontend

| ID | Módulo | Caso | Precondición | Pasos | Resultado esperado | Tipo | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| UI-01 | Router | Ruta protegida sin sesión | localStorage limpio | Abrir `/dashboard` | Redirige a `/login` | UI | Manual |
| UI-02 | Router | Ruta de otro rol | Sesión CLIENTE | Abrir `/dashboard` | Redirige a `/productos` | UI | Manual |
| UI-03 | Router | Deep link SPA | Docker/Nginx | Abrir `/inventario` directo con sesión ADMIN | Carga la SPA (fallback Nginx) | UI | Manual |
| UI-04 | HTTP | Inyección Bearer | Sesión activa | Abrir DevTools > Network | Todas las llamadas llevan `Authorization` | UI | Manual |
| UI-05 | HTTP | Refresh automático | Access token vencido, refresh válido | Navegar a `/inventario` | Se renueva y reintenta sin cerrar sesión | UI | Manual |
| UI-06 | HTTP | Timeout | Backend detenido | Realizar petición | Mensaje "La peticion excedio el tiempo de espera" | UI | Manual |
| UI-07 | HTTP | 403 | Token VENDEDOR | Abrir endpoint admin desde Postman | Mensaje de permisos | API | Manual |
| UI-08 | ErrorBoundary | Error de render | Provocar excepción en un componente | Abrir pantalla | Se muestra "Algo salió mal" y botón recargar | UI | Manual |
| UI-09 | Responsive | Menú móvil | Ancho < 768 px | Abrir sidebar | Overlay y cierre al navegar | UI | Manual |

## 7. Despliegue y DevOps

| ID | Módulo | Caso | Precondición | Pasos | Resultado esperado | Tipo | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DEV-OPS-01 | Docker | Build backend | Docker | `docker compose build backend` | Imagen generada sin errores | DevOps | Manual |
| DEV-OPS-02 | Docker | Build frontend | Docker | `docker compose build frontend` | Imagen Nginx generada | DevOps | Manual |
| DEV-OPS-03 | Docker | Levantar stack | `.env` configurado | `docker compose up` | MySQL healthy, backend y frontend arriba | DevOps | Manual |
| DEV-OPS-04 | Docker | Persistencia | Volumen `mysql_data` | Reiniciar compose | Datos conservados | DevOps | Manual |
| DEV-OPS-05 | CI/Test | Suite JUnit | Maven | `mvn test` | 0 fallos | JUnit | Automatizado |
| DEV-OPS-06 | Docs | Colección Postman | Postman importado | Ejecutar carpeta Auth | Login/me/refresh/logout exitosos | API | Manual |
