import { obtenerCaja } from '../utils/caja';

/** Mapa unico de identificadores historicos de vista -> ruta URL. */
export const RUTAS = {
  login: '/login',
  dashboard: '/dashboard',
  productos: '/productos',
  carrito: '/carrito',
  historial: '/historial',
  comprobante: '/comprobante',
  reportes: '/reportes',
  inventario: '/inventario',
  clientes: '/clientes',
  puntoventa: '/pos',
  apertura: '/apertura',
  cierre: '/cierre',
  devoluciones: '/devoluciones',
  comprobantes: '/comprobantes',
  usuarios: '/usuarios',
  auditoriacajas: '/auditoria-cajas'
};

export function rutaInicio(sesion) {
  if (!sesion) return RUTAS.login;
  if (sesion.rol === 'ADMIN') return RUTAS.dashboard;
  if (sesion.rol === 'VENDEDOR') {
    return obtenerCaja() ? RUTAS.puntoventa : RUTAS.apertura;
  }
  return RUTAS.productos;
}
