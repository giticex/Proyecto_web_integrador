import { FileWarning, RotateCcw, TrendingDown } from 'lucide-react';
import { obtenerNotasCredito } from '../../utils/caja';

const soles = (valor) => `S/ ${Number(valor || 0).toFixed(2)}`;

export default function AuditoriaDevoluciones() {
  const notas = obtenerNotasCredito();
  const totalReembolsado = notas.reduce((acc, n) => acc + Number(n.total || 0), 0);

  return (
    <section className="tzView">
      <div className="tzHero">
        <div>
          <span className="tzHeroTag">Control fiscal</span>
          <h2>Gestión de Devoluciones</h2>
          <p>Auditoría y reporte de las notas de crédito emitidas.</p>
        </div>
        <RotateCcw size={42} />
      </div>

      <div className="tzGrid2">
        <div className="tzCard">
          <h3 className="tzCardTitle"><FileWarning size={18} /> Notas de crédito emitidas</h3>
          <div className="tzStat"><span>Cantidad de notas</span><strong>{notas.length}</strong></div>
        </div>
        <div className="tzCardDark">
          <h3 className="tzCardTitle"><TrendingDown size={18} /> Impacto acumulado</h3>
          <div className="tzStat tzStatTotal">
            <span>Total reembolsado</span>
            <strong>{soles(totalReembolsado)}</strong>
          </div>
        </div>
      </div>

      <div className="tzCard">
        <h3 className="tzCardTitle"><RotateCcw size={18} /> Historial de devoluciones</h3>
        {notas.length === 0 ? (
          <div className="tzEmpty">Aún no se han emitido notas de crédito.</div>
        ) : (
          <table className="tzTable">
            <thead>
              <tr>
                <th>Nota</th>
                <th>Fecha</th>
                <th>Comprobante original</th>
                <th>Cliente</th>
                <th>Ítems</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              {notas.map((nota, i) => (
                <tr key={nota.idNota || i}>
                  <td><strong>{nota.idNota}</strong></td>
                  <td>{new Date(nota.fecha).toLocaleString('es-PE')}</td>
                  <td>{nota.comprobante}</td>
                  <td>{nota.cliente}</td>
                  <td>{nota.items?.length || 0}</td>
                  <td>{soles(nota.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
