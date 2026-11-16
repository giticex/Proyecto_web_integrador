
import { apiRequest } from './api';

export const cajaService = {

  // Consultar caja abierta del usuario autenticado
  actual: () => apiRequest('/cajas/actual'),

  // Registrar apertura
  abrir: (datos) =>
    apiRequest('/cajas/abrir', {
      method: 'POST',
      body: JSON.stringify(datos)
    }),

  // Registrar cierre
  cerrar: (idCaja, datos) =>
    apiRequest(`/cajas/${idCaja}/cerrar`, {
      method: 'POST',
      body: JSON.stringify(datos)
    }),

  // Consultar historial paginado
  historial: (page = 0, size = 20) =>
    apiRequest(`/cajas/historial?page=${page}&size=${size}`)
};

