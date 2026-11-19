# Endpoints vs. Pantallas

Tabla cruzada entre los endpoints REST y las pantallas de la SPA. Todos los
endpoints requieren `Authorization: Bearer <token>` salvo los marcados como
públicos.

| # | Endpoint | Método | Roles | Pantalla(s) / componente | Acción de UI |
| --- | --- | --- | --- | --- | --- |
| 1 | `/api/auth/login` | POST | Público | `/login` (`pages/Login.jsx`) | Iniciar sesión |
| 2 | `/api/auth/register`* | — | — | `/login` (registro) | Usa `/api/clientes/registro` |
| 3 | `/api/clientes/registro` | POST | Público | `/login` | Registrar cliente |
| 4 | `/api/clientes/login` | POST | Público (compatibilidad) | — | Endpoint legado |
| 5 | `/api/auth/refresh` | POST | Público con refresh | Interceptor `services/api.js` | Renovación automática |
| 6 | `/api/auth/logout` | POST | Autenticado | `Navbar.jsx` | Cerrar sesión |
| 7 | `/api/auth/me` | GET | Autenticado | Contexto de sesión | Validar usuario |
| 8 | `/api/productos` | GET | Autenticado | `/productos`, `/pos`, `/inventario` | Catálogo / POS / stock |
| 9 | `/api/productos/{id}` | GET | Autenticado | `/productos`, `/inventario` | Detalle |
| 10 | `/api/productos` | POST | ADMIN | `/inventario` | Crear producto |
| 11 | `/api/productos/{id}` | PUT | ADMIN | `/inventario` | Editar producto |
| 12 | `/api/productos/{id}` | DELETE | ADMIN | `/inventario` | Eliminar producto |
| 13 | `/api/clientes` | GET | ADMIN, VENDEDOR | `/clientes`, `/pos` | Directorio / selector POS |
| 14 | `/api/clientes/{id}` | GET | ADMIN, VENDEDOR, propio cliente | `/historial`, `/pos` | Datos del cliente |
| 15 | `/api/ventas` | POST | Autenticado | `/carrito`, `/pos` | Cobrar venta |
| 16 | `/api/ventas` | GET | ADMIN, VENDEDOR | `/dashboard`, `/reportes`, `/comprobantes`, `/cierre` | Listados |
| 17 | `/api/ventas/{idVenta}` | GET | Autenticado | `/comprobante` | Detalle del comprobante |
| 18 | `/api/ventas/cliente/{idCliente}` | GET | ADMIN, VENDEDOR, propio cliente | `/historial`, `/comprobante` | Historial de compras |
| 19 | `/api/dashboard` | GET | ADMIN | `/dashboard`, `/reportes` | Métricas |
| 20 | `/api/catalogos/tipos-documento` | GET | Autenticado | `/carrito`, `/pos` | Selector boleta/factura |
| 21 | `/api/catalogos/medios-pago` | GET | Autenticado | `/carrito`, `/pos` | Selector medio de pago |
| 22 | `/api/vendedores` | GET | ADMIN | `/usuarios` | Listar vendedores |
| 23 | `/api/vendedores` | POST | ADMIN | `/usuarios` | Crear vendedor |

\* No existe un endpoint separado de registro de usuario administrador: el
registro público crea clientes y el ADMIN crea vendedores.

## Pantallas sin endpoint dedicado

| Pantalla | Persistencia | Observación |
| --- | --- | --- |
| `/apertura` | `localStorage` (`tz_caja`) | Apertura de caja del turno |
| `/cierre` | `localStorage` (`tz_cierres`) + ventas API | Arqueo local |
| `/devoluciones` | `localStorage` (`tz_notas_credito`) | Flujo demostrativo de notas de crédito |
| `/auditoria-cajas` | `localStorage` | Lee cierres registrados |
| `/auditoria-devoluciones` | `localStorage` | Lee notas de crédito |
| `/comprobante` | API de ventas | Impresión A4 / ticket 80 mm |

## Correspondencia de roles

| Rol | Rutas permitidas |
| --- | --- |
| ADMIN | `/dashboard`, `/pos`, `/inventario`, `/clientes`, `/reportes`, `/usuarios`, `/auditoria-cajas`, `/devoluciones`, `/comprobante`, `/comprobantes`, `/historial` |
| VENDEDOR | `/pos`, `/apertura`, `/cierre`, `/devoluciones`, `/comprobantes`, `/comprobante` |
| CLIENTE | `/productos`, `/carrito`, `/historial`, `/comprobante` |
