import { apiRequest } from './api';

export const devolucionService = {

    // Consultar cantidades devueltas por detalle de venta.
resumenPorVenta: (idVenta) =>
  apiRequest(
    `/devoluciones/venta/${encodeURIComponent(idVenta)}/resumen`
  ),

  // Registrar devolución
  registrar: (datos) =>
    apiRequest('/devoluciones', {
      method: 'POST',
      body: JSON.stringify(datos)
    }),

  // Consultar historial paginado
  listar: (page = 0, size = 20) =>
    apiRequest(`/devoluciones?page=${page}&size=${size}`),

  // Consultar devolución específica
  buscar: (id) =>
    apiRequest(`/devoluciones/${id}`),

  // Consultar devoluciones de una venta
  porVenta: (idVenta, page = 0, size = 20) =>
    apiRequest(
      `/devoluciones/venta/${encodeURIComponent(idVenta)}?page=${page}&size=${size}`
    )
};
