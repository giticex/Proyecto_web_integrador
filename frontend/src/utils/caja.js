const CLAVE_CAJA = 'tz_caja';
const CLAVE_CIERRES = 'tz_cierres';
const CLAVE_NOTAS = 'tz_notas_credito';

export function obtenerCaja() {
  try {
    const data = localStorage.getItem(CLAVE_CAJA);
    return data ? JSON.parse(data) : null;
  } catch {
    return null;
  }
}

export function abrirCaja(datos) {
  const estado = { ...datos, abiertaEn: new Date().toISOString() };
  localStorage.setItem(CLAVE_CAJA, JSON.stringify(estado));
  return estado;
}

export function cerrarCaja() {
  localStorage.removeItem(CLAVE_CAJA);
}

function agregarAlHistorial(clave, registro) {
  let lista = [];
  try {
    lista = JSON.parse(localStorage.getItem(clave)) || [];
  } catch {
    lista = [];
  }
  lista.unshift(registro);
  localStorage.setItem(clave, JSON.stringify(lista.slice(0, 50)));
}

export function registrarCierre(cierre) {
  agregarAlHistorial(CLAVE_CIERRES, cierre);
}

export function registrarNotaCredito(nota) {
  agregarAlHistorial(CLAVE_NOTAS, nota);
}

function leerHistorial(clave) {
  try {
    return JSON.parse(localStorage.getItem(clave)) || [];
  } catch {
    return [];
  }
}

export function obtenerCierres() {
  return leerHistorial(CLAVE_CIERRES);
}

export function obtenerNotasCredito() {
  return leerHistorial(CLAVE_NOTAS);
}