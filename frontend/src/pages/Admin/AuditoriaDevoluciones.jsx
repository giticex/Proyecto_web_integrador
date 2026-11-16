
import { useEffect, useState } from 'react';
import {
  FileWarning,
  RotateCcw,
  TrendingDown,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Clock
} from 'lucide-react';

import { devolucionService } from '../../services/devolucionService';

const soles = (valor) =>
  `S/ ${Number(valor || 0).toFixed(2)}`;

function fechaLocal(valor) {
  if (!valor) return '—';

  const fecha = new Date(valor);

  return Number.isNaN(fecha.getTime())
    ? '—'
    : fecha.toLocaleString('es-PE');
}

export default function AuditoriaDevoluciones() {

  const [resultado, setResultado] = useState({
    content: [],
    number: 0,
    totalPages: 0,
    totalElements: 0
  });

  const [pagina, setPagina] = useState(0);
  const [recarga, setRecarga] = useState(0);

  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  // Consultar las devoluciones registradas en MySQL.
  useEffect(() => {

    let activo = true;

    async function cargarDevoluciones() {

      setCargando(true);
      setError('');

      try {

        const data = await devolucionService.listar(pagina, 20);

        if (activo) {
          setResultado(data);
        }

      } catch (err) {

        if (activo) {
          setError(
            err.message || 'No se pudo consultar el historial de devoluciones.'
          );
        }

      } finally {

        if (activo) {
          setCargando(false);
        }
      }
    }

    cargarDevoluciones();

    return () => {
      activo = false;
    };

  }, [pagina, recarga]);

  const devoluciones = resultado.content || [];

  // Indicadores de la página actual.
  const totalReembolsado = devoluciones.reduce(
    (total, item) =>
      total + Number(item.montoReembolsado || 0),
    0
  );

  const registradas = devoluciones.filter(
    item => item.estado === 'REGISTRADA'
  ).length;

  const itemsDevueltos = devoluciones.reduce(
    (total, item) =>
      total + (item.detalles || []).length,
    0
  );

  return (

    <section className="tzView">

      {/* ENCABEZADO */}

      <div className="tzHero">

        <div>
          <span className="tzHeroTag">
            Control fiscal
          </span>

          <h2>Auditoría de Devoluciones</h2>

          <p>
            Consulta las notas de crédito y los reembolsos
            registrados en la base de datos.
          </p>
        </div>

        <RotateCcw size={42} />

      </div>


      {/* ACTUALIZAR */}

      <div className="tzBtnRow">

        <button
          type="button"
          className="tzBtn tzBtnPrimary"
          disabled={cargando}
          onClick={() => setRecarga(value => value + 1)}
        >
          <RefreshCw size={17} />
          Actualizar historial
        </button>

        <span className="tzReadonly">
          Registros totales: {resultado.totalElements || 0}
        </span>

      </div>


      {/* ERROR */}

      {error && (

        <div className="tzCard tzError">

          <AlertCircle size={19} />

          <span>{error}</span>

          <button
            type="button"
            className="tzBtn tzBtnGhost"
            onClick={() => setRecarga(value => value + 1)}
          >
            Reintentar
          </button>

        </div>

      )}


      {/* INDICADORES */}

      {!error && (

        <div className="tzGrid2">

          <div className="tzCard">

            <h3 className="tzCardTitle">
              <FileWarning size={18} />
              Notas de crédito
            </h3>

            <div className="tzStat">
              <span>Notas registradas en esta página</span>
              <strong>{devoluciones.length}</strong>
            </div>

            <div className="tzStat">
              <span>Estado REGISTRADA</span>
              <strong>{registradas}</strong>
            </div>

            <div className="tzStat tzStatTotal">
              <span>Registros totales</span>
              <strong>{resultado.totalElements || 0}</strong>
            </div>

          </div>


          <div className="tzCardDark">

            <h3 className="tzCardTitle">
              <TrendingDown size={18} />
              Impacto de devoluciones
            </h3>

            <div className="tzStat">
              <span>Productos devueltos (líneas)</span>
              <strong>{itemsDevueltos}</strong>
            </div>

            <div className="tzStat tzStatTotal">
              <span>Total reembolsado en esta página</span>
              <strong>{soles(totalReembolsado)}</strong>
            </div>

            <p>
              Los indicadores corresponden a los registros
              de la página consultada.
            </p>

          </div>

        </div>

      )}


      {/* TABLA DE AUDITORIA */}

      <div className="tzCard">

        <h3 className="tzCardTitle">
          <Clock size={18} />
          Historial de notas de crédito
        </h3>


        {cargando ? (

          <div className="tzEmpty">
            Consultando devoluciones en MySQL...
          </div>

        ) : error ? (

          <div className="tzEmpty">
            No se pudo cargar el historial.
          </div>

        ) : devoluciones.length === 0 ? (

          <div className="tzEmpty">
            Todavía no se han registrado devoluciones.
          </div>

        ) : (

          <div style={{ overflowX: 'auto' }}>

            <table className="tzTable">

              <thead>
                <tr>
                  <th>Nota de crédito</th>
                  <th>Fecha</th>
                  <th>Comprobante original</th>
                  <th>Cliente</th>
                  <th>Motivo</th>
                  <th>Productos</th>
                  <th>Reembolso</th>
                  <th>Medio de reembolso</th>
                  <th>Estado</th>
                </tr>
              </thead>

              <tbody>

                {devoluciones.map(devolucion => (

                  <tr key={devolucion.idDevolucion}>

                    <td>
                      <strong>
                        {devolucion.numeroNotaCredito}
                      </strong>
                    </td>

                    <td>
                      {fechaLocal(devolucion.fechaRegistro)}
                    </td>

                    <td>
                      {devolucion.numeroDocumento || '—'}
                    </td>

                    <td>
                      {devolucion.cliente || '—'}
                    </td>

                    <td>
                      {devolucion.motivo || '—'}
                    </td>

                    <td>

                      {(devolucion.detalles || []).length > 0
                        ? devolucion.detalles.map(
                            detalle => `${detalle.cantidad} × ${detalle.producto}`
                          ).join(', ')
                        : '—'}

                    </td>

                    <td>
                      <strong>
                        {soles(devolucion.montoReembolsado)}
                      </strong>
                    </td>

                    <td>
                        {devolucion.medioPagoReembolso || '—'}
                    </td>

                    <td>

                      {devolucion.estado === 'REGISTRADA' ? (

                        <span className="tzPill tzPillOk">
                          <CheckCircle2
                            size={13}
                            style={{
                              display: 'inline',
                              verticalAlign: 'middle'
                            }}
                          />
                          {' '}REGISTRADA
                        </span>

                      ) : (

                        <span className="tzPill">
                          {devolucion.estado || 'SIN ESTADO'}
                        </span>

                      )}

                    </td>

                  </tr>

                ))}

              </tbody>

            </table>

          </div>

        )}


        {/* PAGINACION */}

        {!cargando && !error && resultado.totalPages > 0 && (

          <div
            className="tzBtnRow"
            style={{ marginTop: 20 }}
          >

            <button
              type="button"
              className="tzBtn tzBtnGhost"
              disabled={pagina <= 0}
              onClick={() => setPagina(p => p - 1)}
            >
              Anterior
            </button>

            <span>
              Página {pagina + 1} de {resultado.totalPages}
            </span>

            <button
              type="button"
              className="tzBtn tzBtnPrimary"
              disabled={pagina + 1 >= resultado.totalPages}
              onClick={() => setPagina(p => p + 1)}
            >
              Siguiente
            </button>

          </div>

        )}

      </div>

    </section>

  );
}
