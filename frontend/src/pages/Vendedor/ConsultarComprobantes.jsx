import { useEffect, useState } from 'react';
import { Printer, ReceiptText, RefreshCw, Search } from 'lucide-react';
import { ventaService } from '../../services/api';
import { formatoHora } from '../../utils/format';

const soles = (valor) => `S/ ${Number(valor || 0).toFixed(2)}`;

const etiquetaComprobante = (venta) =>
  venta?.numeroDocumento || venta?.numeroComprobante || venta?.idVenta?.substring(0, 8) || '—';

export default function ConsultarComprobantes({ setVista, setVentaGenerada }) {
  const [ventas, setVentas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [filtro, setFiltro] = useState('');
  const [error, setError] = useState('');

  async function cargar() {
    setCargando(true);
    setError('');
    try {
      setVentas(await ventaService.listar());
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
  }, []);

  const hoy = new Date().toDateString();
  const delDia = ventas.filter((v) => new Date(v.fechaEmision).toDateString() === hoy);
  const limpiar = filtro.replace(/\s+/g, '').toUpperCase();
  const lista = limpiar
    ? delDia.filter(
        (v) =>
          etiquetaComprobante(v).replace(/\s+/g, '').toUpperCase().includes(limpiar) ||
          String(v.cliente || '').toLowerCase().includes(filtro.toLowerCase())
      )
    : delDia;

  function reimprimir(venta) {
    setVentaGenerada(venta);
    setVista('comprobante');
  }

  return (
    <section className="tzView">
      <div className="tzHero">
        <div>
          <span className="tzHeroTag">Post-venta</span>
          <h2>Consultar Comprobantes</h2>
          <p>Reimpresión rápida de los tickets emitidos hoy.</p>
        </div>
        <ReceiptText size={42} />
      </div>

      <div className="tzSearchBar">
        <Search size={20} />
        <input
          value={filtro}
          onChange={(e) => setFiltro(e.target.value)}
          placeholder="Filtrar por comprobante (ej. B001-00000001) o cliente..."
        />
        <button type="button" className="tzBtn tzBtnGhost" onClick={cargar}>
          <RefreshCw size={16} /> Actualizar
        </button>
      </div>

      {error && <div className="tzCard tzError">{error}</div>}

      <div className="tzCard">
        {cargando ? (
          <div className="tzEmpty">Cargando comprobantes...</div>
        ) : lista.length === 0 ? (
          <div className="tzEmpty">No hay comprobantes emitidos hoy que coincidan.</div>
        ) : (
          <table className="tzTable">
            <thead>
              <tr>
                <th>Comprobante</th>
                <th>Hora</th>
                <th>Cliente</th>
                <th>Tipo</th>
                <th>Total</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((venta) => (
                <tr key={venta.idVenta}>
                  <td><strong>{etiquetaComprobante(venta)}</strong></td>
                  <td>{formatoHora(venta.fechaEmision)}</td>
                  <td>{venta.cliente}</td>
                  <td>{venta.tipoDocumento}</td>
                  <td>{soles(venta.total)}</td>
                  <td>
                    <button
                      type="button"
                      className="tzBtn tzBtnPrimary tzBtnMini"
                      onClick={() => reimprimir(venta)}
                    >
                      <Printer size={15} /> Reimprimir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
