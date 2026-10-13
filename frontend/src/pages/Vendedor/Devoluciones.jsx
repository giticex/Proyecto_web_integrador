import { useEffect, useState } from 'react';
import { Check, FileWarning, Printer, RotateCcw, Search, X } from 'lucide-react';
import { ventaService } from '../../services/api';
import { registrarNotaCredito } from '../../utils/caja';

const soles = (valor) => `S/ ${Number(valor || 0).toFixed(2)}`;

const normalizarComprobante = (valor) =>
  String(valor || '')
    .replace(/\s+/g, '')
    .toUpperCase();

const etiquetaComprobante = (venta) =>
  venta?.numeroComprobante || venta?.numeroDocumento || venta?.idVenta || venta?.id || '—';

// Búsqueda flexible: comprobante completo/incluido, ID de venta o correlativo (dígitos).
const buscarVenta = (listaVentas, criterio) => {
  if (!criterio.trim()) return null;
  const busquedaLimpia = criterio.replace(/\s+/g, '').toUpperCase();
  const soloDigitos = criterio.replace(/\D/g, '');

  return (
    (listaVentas || []).find((v) => {
      const compOficial = normalizarComprobante(v.numeroComprobante || v.numeroDocumento);
      const docCliente = String(v.numeroDocumentoCliente || v.numeroDocumento || '').replace(/\s+/g, '');
      const idStr = String(v.idVenta || v.id || '');

      if (compOficial && compOficial === busquedaLimpia) return true;
      if (compOficial && compOficial.includes(busquedaLimpia)) return true;
      if (idStr.toUpperCase() === busquedaLimpia) return true;
      if (soloDigitos && (idStr === soloDigitos || docCliente === soloDigitos)) return true;

      return false;
    }) || null
  );
};

const MOTIVOS = [
  'Producto defectuoso',
  'Producto equivocado',
  'Error de cobro',
  'Cliente arrepentido',
  'Otro'
];

export default function Devoluciones() {
  const [termino, setTermino] = useState('');
  const [ventas, setVentas] = useState(null);
  const [ventaSel, setVentaSel] = useState(null);
  const [seleccion, setSeleccion] = useState([]);
  const [mensaje, setMensaje] = useState('');
  const [nota, setNota] = useState(null);

  useEffect(() => {
    ventaService
      .listar()
      .then(setVentas)
      .catch(() => {});
  }, []);

  async function buscar(e) {
    e.preventDefault();
    setMensaje('');
    setVentaSel(null);
    setNota(null);
    if (!termino.trim()) {
      setMensaje('Ingresa un número de comprobante (ej. B001-00000045) para buscar.');
      return;
    }
    try {
      const data = ventas || (await ventaService.listar());
      setVentas(data);
      const encontrado = buscarVenta(data, termino);
      if (!encontrado) {
        setMensaje(`No se encontró ningún comprobante con "${termino}". Prueba con uno de los disponibles:`);
        return;
      }
      seleccionarVenta(encontrado);
    } catch (error) {
      setMensaje(error.message);
    }
  }

  function seleccionarVenta(venta) {
    setVentaSel(venta);
    setNota(null);
    setMensaje('');
    setSeleccion(
      (venta.detalles || []).map(() => ({ checked: false, cantidad: 1, motivo: MOTIVOS[0] }))
    );
  }

  function alternar(indice) {
    setSeleccion((prev) =>
      prev.map((item, i) => (i === indice ? { ...item, checked: !item.checked } : item))
    );
  }

  function cambiarCantidad(indice, valor) {
    const detalle = ventaSel.detalles[indice];
    const num = Math.max(1, Math.min(detalle.cantidad, parseInt(valor, 10) || 1));
    setSeleccion((prev) => prev.map((item, i) => (i === indice ? { ...item, cantidad: num } : item)));
  }

  function cambiarMotivo(indice, valor) {
    setSeleccion((prev) => prev.map((item, i) => (i === indice ? { ...item, motivo: valor } : item)));
  }

  const totalReembolso = ventaSel
    ? ventaSel.detalles.reduce(
        (acc, detalle, i) =>
          seleccion[i]?.checked ? acc + Number(detalle.precioUnitario) * seleccion[i].cantidad : acc,
        0
      )
    : 0;

  function generarNota() {
    const items = ventaSel.detalles
      .map((detalle, i) => ({ detalle, sel: seleccion[i] }))
      .filter(({ sel }) => sel.checked)
      .map(({ detalle, sel }) => ({
        producto: detalle.producto,
        cantidad: sel.cantidad,
        precioUnitario: detalle.precioUnitario,
        subtotal: Number(detalle.precioUnitario) * sel.cantidad,
        motivo: sel.motivo
      }));

    if (items.length === 0) {
      setMensaje('Selecciona al menos un ítem para devolver.');
      return;
    }

    const notaCredito = {
      idNota: `NC001-${Date.now().toString(36).toUpperCase().slice(-6)}`,
      fecha: new Date().toISOString(),
      comprobante: ventaSel.numeroDocumento,
      cliente: ventaSel.cliente,
      total: totalReembolso,
      items
    };
    registrarNotaCredito(notaCredito);
    setNota(notaCredito);
    setMensaje('');
  }

  return (
    <section className="tzView">
      <div className="tzHero">
        <div>
          <span className="tzHeroTag">Post-venta</span>
          <h2>Devoluciones y Anulaciones</h2>
          <p>Busca un comprobante y genera la nota de crédito correspondiente.</p>
        </div>
        <RotateCcw size={42} />
      </div>

      <form className="tzSearchBar" onSubmit={buscar}>
        <Search size={20} />
        <input
          value={termino}
          onChange={(e) => setTermino(e.target.value)}
          placeholder="Buscar por comprobante (ej. B001-00000045 o F001-00000012)..."
        />
        <button type="submit" className="tzBtn tzBtnPrimary">
          Buscar
        </button>
      </form>

      {mensaje && <div className="tzCard tzError">{mensaje}</div>}

      {!ventaSel && !nota && ventas && ventas.length > 0 && (
        <div className="tzCard">
          <h3 className="tzCardTitle">
            <FileWarning size={18} /> Comprobantes disponibles para prueba
          </h3>
          <div className="tzSugerencias">
            {ventas.slice(0, 10).map((v) => (
              <button
                key={v.idVenta || v.id}
                type="button"
                className="tzSugerencia"
                onClick={() => {
                  setTermino(etiquetaComprobante(v));
                  seleccionarVenta(v);
                }}
              >
                <strong>{etiquetaComprobante(v)}</strong>
                <span>
                  {v.cliente} · {v.tipoDocumento} · {soles(v.total)}
                </span>
              </button>
            ))}
          </div>
        </div>
      )}

      {ventaSel && !nota && (
        <>
          <div className="tzCard tzVentaHead">
            <div>
              <span className="tzHeroTag">Comprobante</span>
              <h3>{etiquetaComprobante(ventaSel)}</h3>
              <p>
                {ventaSel.tipoDocumento} · {ventaSel.cliente} · {ventaSel.medioPago}
              </p>
            </div>
            <div className="tzVentaHeadTotal">
              <span>Total de la venta</span>
              <strong>{soles(ventaSel.total)}</strong>
            </div>
          </div>

          <div className="tzCard">
            <table className="tzTable">
              <thead>
                <tr>
                  <th></th>
                  <th>Producto</th>
                  <th>Cant. comprada</th>
                  <th>P. Unit</th>
                  <th>Cant. a devolver</th>
                  <th>Motivo</th>
                </tr>
              </thead>
              <tbody>
                {ventaSel.detalles.map((detalle, i) => (
                  <tr key={`${detalle.idProducto}-${i}`} className={seleccion[i]?.checked ? 'tzFilaActiva' : ''}>
                    <td>
                      <input
                        type="checkbox"
                        className="tzCheck"
                        checked={seleccion[i]?.checked || false}
                        onChange={() => alternar(i)}
                      />
                    </td>
                    <td>{detalle.producto}</td>
                    <td>{detalle.cantidad}</td>
                    <td>{soles(detalle.precioUnitario)}</td>
                    <td>
                      <input
                        type="number"
                        className="tzCantidad"
                        min="1"
                        max={detalle.cantidad}
                        value={seleccion[i]?.cantidad || 1}
                        disabled={!seleccion[i]?.checked}
                        onChange={(e) => cambiarCantidad(i, e.target.value)}
                      />
                    </td>
                    <td>
                      <select
                        className="tzSelectInline"
                        value={seleccion[i]?.motivo || MOTIVOS[0]}
                        disabled={!seleccion[i]?.checked}
                        onChange={(e) => cambiarMotivo(i, e.target.value)}
                      >
                        {MOTIVOS.map((motivo) => (
                          <option key={motivo}>{motivo}</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="tzDevolucionFooter">
            <div className="tzDevolucionTotal">
              <span>Total a reembolsar</span>
              <strong>{soles(totalReembolso)}</strong>
            </div>
            <button type="button" className="tzBtn tzBtnGreen" onClick={generarNota}>
              <FileWarning size={18} /> GENERAR NOTA DE CRÉDITO / ANULAR
            </button>
          </div>
        </>
      )}

      {nota && (
        <div className="tzReporteZ">
          <div className="tzReporteZHead">
            <strong>NOTA DE CRÉDITO {nota.idNota}</strong>
            <span>{new Date(nota.fecha).toLocaleString('es-PE')}</span>
          </div>
          <div className="tzStat"><span>Comprobante original</span><strong>{nota.comprobante}</strong></div>
          <div className="tzStat"><span>Cliente</span><strong>{nota.cliente}</strong></div>
          {nota.items.map((item, i) => (
            <div className="tzStat" key={i}>
              <span>{item.cantidad}x {item.producto} ({item.motivo})</span>
              <strong>{soles(item.subtotal)}</strong>
            </div>
          ))}
          <div className="tzDiferencia tzDifOk">
            <span>TOTAL A REEMBOLSAR</span>
            <strong>{soles(nota.total)}</strong>
          </div>
          <div className="tzBtnRow">
            <button type="button" className="tzBtn tzBtnPrimary" onClick={() => window.print()}>
              <Printer size={18} /> Imprimir Nota
            </button>
            <button type="button" className="tzBtn tzBtnGhost" onClick={() => { setNota(null); setVentaSel(null); setTermino(''); }}>
              <X size={18} /> Nueva búsqueda
            </button>
          </div>
          <p className="tzNotaHint">
            <Check size={14} /> Nota registrada localmente. El reingreso de stock se aplicará cuando exista el endpoint de devoluciones en el backend.
          </p>
        </div>
      )}
    </section>
  );
}