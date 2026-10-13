import { Landmark, RefreshCw, TriangleAlert } from 'lucide-react';
import { obtenerCaja, obtenerCierres } from '../../utils/caja';

const soles = (valor) => `S/ ${Number(valor || 0).toFixed(2)}`;

export default function AuditoriaCajas() {
  const caja = obtenerCaja();
  const cierres = obtenerCierres();

  const totalFaltantes = cierres
    .filter((c) => Number(c.diferencia) < -0.01)
    .reduce((acc, c) => acc + Number(c.diferencia), 0);
  const totalSobrantes = cierres
    .filter((c) => Number(c.diferencia) > 0.01)
    .reduce((acc, c) => acc + Number(c.diferencia), 0);

  return (
    <section className="tzView">
      <div className="tzHero">
        <div>
          <span className="tzHeroTag">Control fiscal</span>
          <h2>Auditoría de Cajas</h2>
          <p>Control de turnos de cajeros con resaltado de faltantes y sobrantes.</p>
        </div>
        <Landmark size={42} />
      </div>

      <div className="tzGrid2">
        <div className="tzCard">
          <h3 className="tzCardTitle"><Landmark size={18} /> Turno actual</h3>
          {caja ? (
            <>
              <div className="tzStat"><span>Cajero</span><strong>{caja.cajero || 'Cajero'}</strong></div>
              <div className="tzStat"><span>Fondo inicial</span><strong>{soles(caja.fondoInicial)}</strong></div>
              <div className="tzStat tzStatTotal"><span>Estado</span><strong>CAJA ABIERTA</strong></div>
            </>
          ) : (
            <div className="tzEmpty">No hay cajas abiertas en este momento.</div>
          )}
        </div>

        <div className="tzCardDark">
          <h3 className="tzCardTitle"><RefreshCw size={18} /> Resumen de cierres</h3>
          <div className="tzStat"><span>Turnos cerrados</span><strong>{cierres.length}</strong></div>
          <div className="tzStat"><span>Total faltantes</span><strong>{soles(totalFaltantes)}</strong></div>
          <div className="tzStat tzStatTotal"><span>Total sobrantes</span><strong>{soles(totalSobrantes)}</strong></div>
        </div>
      </div>

      <div className="tzCard">
        <h3 className="tzCardTitle"><TriangleAlert size={18} /> Historial de turnos</h3>
        {cierres.length === 0 ? (
          <div className="tzEmpty">Aún no se registran cierres de caja.</div>
        ) : (
          <table className="tzTable">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Cajero</th>
                <th>Fondo</th>
                <th>Ventas</th>
                <th>Sistema</th>
                <th>Físico</th>
                <th>Diferencia</th>
              </tr>
            </thead>
            <tbody>
              {cierres.map((c, i) => {
                const dif = Number(c.diferencia || 0);
                const ok = Math.abs(dif) < 0.01;
                return (
                  <tr key={i} className={ok ? '' : 'tzFilaAlerta'}>
                    <td>{new Date(c.fecha).toLocaleString('es-PE')}</td>
                    <td>{c.cajero}</td>
                    <td>{soles(c.fondoInicial)}</td>
                    <td>{c.cantidadVentas}</td>
                    <td>{soles(c.totalSistema)}</td>
                    <td>{soles(c.totalFisico)}</td>
                    <td>
                      <span className={ok ? 'tzPill tzPillOk' : 'tzPill tzPillBad'}>
                        {ok ? 'CUADRADO' : dif < 0 ? 'FALTANTE' : 'SOBRANTE'} · {soles(dif)}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
