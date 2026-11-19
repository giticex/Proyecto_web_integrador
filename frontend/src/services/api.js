const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const CLAVE_SESION = 'sesion';
const TIMEOUT_POR_DEFECTO = 15000;
const EVENTO_SESION = 'tz:sesion-actualizada';

/**
 * Error HTTP enriquecido: conserva el status para que la UI pueda distinguir
 * 401 (sesion), 403 (permisos) y 409 (conflicto de negocio).
 */
export class ApiError extends Error {
  constructor(mensaje, status, data) {
    super(mensaje);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

function leerSesion() {
  try {
    const guardado = localStorage.getItem(CLAVE_SESION);
    return guardado ? JSON.parse(guardado) : null;
  } catch {
    return null;
  }
}

function tokenActual() {
  return leerSesion()?.token || null;
}

function refreshActual() {
  return leerSesion()?.refreshToken || null;
}

function actualizarTokens(token, refreshToken) {
  const sesion = leerSesion();
  if (!sesion) return;
  const actualizada = { ...sesion, token, refreshToken };
  localStorage.setItem(CLAVE_SESION, JSON.stringify(actualizada));
  window.dispatchEvent(new CustomEvent(EVENTO_SESION, { detail: actualizada }));
}

export function limpiarSesionLocal() {
  localStorage.removeItem(CLAVE_SESION);
  window.dispatchEvent(new CustomEvent(EVENTO_SESION, { detail: null }));
}

async function fetchConTimeout(url, config, timeout) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeout);
  try {
    return await fetch(url, { ...config, signal: controller.signal });
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new ApiError('La peticion excedio el tiempo de espera', 408, null);
    }
    throw new ApiError('No se pudo conectar con el servidor', 0, null);
  } finally {
    clearTimeout(timer);
  }
}

async function leerRespuesta(response) {
  const texto = await response.text();
  if (!texto) return null;
  try {
    return JSON.parse(texto);
  } catch {
    return texto;
  }
}

function mensajeDeError(response, data) {
  if (data && typeof data === 'object') {
    if (data.mensaje) return data.mensaje;
    if (data.message) return data.message;
  }
  if (response.status === 401) return 'Tu sesion expiro. Inicia sesion nuevamente.';
  if (response.status === 403) return 'No tienes permisos para realizar esta accion.';
  if (response.status === 409) return 'La operacion entra en conflicto con datos existentes.';
  if (response.status >= 500) return 'Error interno del servidor.';
  return 'Ocurrio un error en la peticion';
}

let refrescoEnCurso = null;

async function renovarToken() {
  if (refrescoEnCurso) return refrescoEnCurso;

  const refreshToken = refreshActual();
  if (!refreshToken) {
    throw new ApiError('No hay refresh token disponible', 401, null);
  }

  refrescoEnCurso = (async () => {
    const response = await fetchConTimeout(
      `${API_URL}/auth/refresh`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      },
      TIMEOUT_POR_DEFECTO
    );
    const data = await leerRespuesta(response);
    if (!response.ok) {
      throw new ApiError(mensajeDeError(response, data), response.status, data);
    }
    actualizarTokens(data.token, data.refreshToken);
    return data;
  })();

  try {
    return await refrescoEnCurso;
  } finally {
    refrescoEnCurso = null;
  }
}

/**
 * Wrapper HTTP centralizado:
 * - Agrega Authorization: Bearer <token>.
 * - Timeout con AbortController.
 * - Renueva el token automaticamente una vez ante un 401 y reintenta.
 * - Lanza ApiError con status 401/403/409/5xx.
 */
export async function request(endpoint, options = {}) {
  const {
    method = 'GET',
    body,
    headers = {},
    auth = true,
    timeout = TIMEOUT_POR_DEFECTO,
    reintentar = true
  } = options;

  const esRutaAuth = endpoint.startsWith('/auth/');
  const config = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers
    }
  };

  if (body !== undefined) {
    config.body = typeof body === 'string' ? body : JSON.stringify(body);
  }

  if (auth && !esRutaAuth) {
    const token = tokenActual();
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetchConTimeout(`${API_URL}${endpoint}`, config, timeout);
  const data = await leerRespuesta(response);

  if (response.status === 401 && auth && !esRutaAuth && reintentar && refreshActual()) {
    try {
      await renovarToken();
      return request(endpoint, { ...options, reintentar: false });
    } catch {
      limpiarSesionLocal();
      throw new ApiError('Tu sesion expiro. Inicia sesion nuevamente.', 401, data);
    }
  }

  if (!response.ok) {
    if (response.status === 401) limpiarSesionLocal();
    throw new ApiError(mensajeDeError(response, data), response.status, data);
  }

  return data;
}

export const authService = {
  login: (credenciales) =>
    request('/auth/login', { method: 'POST', body: credenciales, auth: false, reintentar: false }),
  me: () => request('/auth/me'),
  logout: () =>
    request('/auth/logout', {
      method: 'POST',
      body: { refreshToken: refreshActual() },
      reintentar: false
    })
};

export const productoService = {
  listar: () => request('/productos'),
  crear: (producto) => request('/productos', { method: 'POST', body: producto }),
  actualizar: (id, producto) => request(`/productos/${id}`, { method: 'PUT', body: producto }),
  eliminar: (id) => request(`/productos/${id}`, { method: 'DELETE' })
};

export const clienteService = {
  registrar: (cliente) =>
    request('/clientes/registro', { method: 'POST', body: cliente, auth: false }),
  login: (credenciales) =>
    request('/clientes/login', { method: 'POST', body: credenciales, auth: false }),
  listar: () => request('/clientes')
};

export const ventaService = {
  listar: () => request('/ventas'),
  registrar: (venta) => request('/ventas', { method: 'POST', body: venta }),
  buscar: (idVenta) => request(`/ventas/${idVenta}`),
  historialCliente: (idCliente) => request(`/ventas/cliente/${idCliente}`)
};

export const dashboardService = {
  obtener: () => request('/dashboard')
};

export const catalogoService = {
  tiposDocumento: () => request('/catalogos/tipos-documento'),
  mediosPago: () => request('/catalogos/medios-pago')
};

export const vendedorService = {
  listar: () => request('/vendedores'),
  crear: (vendedor) => request('/vendedores', { method: 'POST', body: vendedor })
};

export { API_URL, EVENTO_SESION };
