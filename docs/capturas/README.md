# Capturas de pantalla por módulo

Las capturas deben tomarse con la aplicación en ejecución (`docker compose up`
o backend + frontend locales) y guardarse en esta carpeta con los nombres
indicados. Esto permite enlazarlas desde el informe y el aula virtual.

| Archivo | Módulo | Ruta | Rol | Qué debe mostrar |
| --- | --- | --- | --- | --- |
| `01-login.png` | Autenticación | `/login` | Público | Formulario de login y registro |
| `02-dashboard-admin.png` | Dashboard | `/dashboard` | ADMIN | Tarjetas de métricas y últimas ventas |
| `03-inventario.png` | Inventario | `/inventario` | ADMIN | Tabla/gestión de productos |
| `04-pos.png` | Punto de venta | `/pos` | VENDEDOR/ADMIN | Carrito, cliente y métodos de pago |
| `05-comprobante-a4.png` | Comprobantes | `/comprobante` | Todos | Boleta/factura en formato A4 |
| `06-comprobante-ticket.png` | Comprobantes | `/comprobante` | Todos | Formato ticket 80 mm |
| `07-historial-cliente.png` | Historial | `/historial` | CLIENTE | Compras del cliente con filtros |
| `08-reportes.png` | Reportes | `/reportes` | ADMIN | Totales, IGV y productos top |
| `09-usuarios.png` | Usuarios | `/usuarios` | ADMIN | Creación/listado de vendedores |
| `10-apertura-caja.png` | Caja | `/apertura` | VENDEDOR | Fondo inicial |
| `11-cierre-caja.png` | Caja | `/cierre` | VENDEDOR | Arqueo y diferencias |
| `12-devoluciones.png` | Devoluciones | `/devoluciones` | VENDEDOR/ADMIN | Formulario o auditoría |
| `13-postman-login.png` | API | Postman | — | Login con token en respuesta |
| `14-postman-me.png` | API | Postman | — | `/auth/me` con Bearer token |
| `15-docker.png` | DevOps | Terminal | — | `docker compose ps` con 3 servicios |

## Recomendaciones

1. Usar resolución 1440x900 o 1366x768 y navegador en modo incógnito.
2. Ocultar datos personales reales; usar los datos de demostración.
3. Para Postman, difuminar el valor completo del token (mostrar solo el inicio).
4. Comprimir PNG con TinyPNG si el informe tiene límite de tamaño.

> Estas capturas son evidencia manual del criterio 7. La matriz de pruebas
> relaciona cada pantalla con sus casos en
> [`../matriz-casos-prueba.md`](../matriz-casos-prueba.md).
