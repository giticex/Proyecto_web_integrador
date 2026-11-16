
import { useEffect, useState } from 'react';
import {
  Landmark,
  RefreshCw,
  TriangleAlert,
  CheckCircle2,
  Clock
} from 'lucide-react';

import { cajaService } from '../../services/cajaService';

const soles = (valor) =>
  `S/ ${Number(valor || 0).toFixed(2)}`;

function obtenerDescuadre(caja) {
  return [
    caja.diferenciaEfectivo,
    caja.diferenciaTarjeta,
    caja.diferenciaTransferencia,
    caja.diferenciaOtros
  ].reduce(
    (total, valor) => total + Math.abs(Number(valor || 0)),
    0
  );
}

function obtenerFaltante(caja) {
  return [
    caja.diferenciaEfectivo,
    caja.diferenciaTarjeta,
    caja.diferenciaTransferencia,
    caja.diferenciaOtros
  ].reduce(
    (total, valor) => total + Math.max(0, -Number(valor || 0)),
    0
  );
}

function obtenerSobrante(caja) {
  return [
    caja.diferenciaEfectivo,
    caja.diferenciaTarjeta,
    caja.diferenciaTransferencia,
    caja.diferenciaOtros
  ].reduce(
    (total, valor) => total + Math.max(0, Number(valor || 0)),
    0
  );
}

function obtenerEsperado(caja) {
  return (
    Number(caja.efectivoEsperado || 0) +
    Number(caja.tarjetaEsperada || 0) +
    Number(caja.transferenciaEsperada || 0) +
    Number(caja.otrosEsperados || 0)
  );
}

function obtenerContado(caja) {
  return (
    Number(caja.efectivoContado || 0) +
    Number(caja.tarjetaContada || 0) +
    Number(caja.transferenciaContada || 0) +
    Number(caja.otrosContados || 0)
  );
}

export default function AuditoriaCajas() {

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

  // Consultar historial real del backend.
  useEffect(() => {

    let activo = true;

    async function cargarHistorial() {

      setCargando(true);
      setError('');

      try {

        const data = await cajaService.historial(pagina, 20);

        if (activo) {
          setResultado(data);
        }

      } catch (err) {

        if (activo) {
          setError(err.message || 'No se pudo cargar el historial de cajas.');
        }

      } finally {

        if (activo) {
          setCargando(false);
        }
      }
    }

    cargarHistorial();

    return () => {
      activo = false;
    };

  }, [pagina, recarga]);

  const cajas = resultado.content || [];

  // Métricas correspondientes a la página consultada.
  const abiertas = cajas.filter(
    caja => caja.estado === 'ABIERTA'
  );

  const cerradas = cajas.filter(
    caja => caja.estado === 'CERRADA'
  );

  const totalFaltantes = cerradas.reduce(
    (total, caja) => total + obtenerFaltante(caja),
    0
  );

  const totalSobrantes = cerradas.reduce(
    (total, caja) => total + obtenerSobrante(caja),
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

          <h2>Auditoría de Cajas</h2>

          <p>
            Consulta los turnos, cierres, arqueos
            y diferencias registradas en el sistema.
          </p>
        </div>

        <Landmark size={42} />

      </div>

      {/* BOTÓN DE ACTUALIZACIÓN */}

      <div className="tzBtnRow">

        <button
          type="button"
          className="tzBtn tzBtnPrimary"
          onClick={() => setRecarga(value => value + 1)}
          disabled={cargando}
        >
          <RefreshCw size={17} />
          Actualizar información
        </button>

        <span className="tzReadonly">
          Registros totales: {resultado.totalElements || 0}
        </span>

      </div>

      {/* MENSAJES */}

      {error && (

        <div className="tzCard tzError">

          <TriangleAlert size={18} />

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
              <Landmark size={18} />
              Estado de las cajas
            </h3>

            <div className="tzStat">
              <span>Cajas abiertas en esta página</span>
              <strong>{abiertas.length}</strong>
            </div>

            <div className="tzStat">
              <span>Cajas cerradas en esta página</span>
              <strong>{cerradas.length}</strong>
            </div>

            <div className="tzStat tzStatTotal">
              <span>Registros totales</span>
              <strong>{resultado.totalElements || 0}</strong>
            </div>

          </div>

          <div className="tzCardDark">

            <h3 className="tzCardTitle">
              <TriangleAlert size={18} />
              Resumen de diferencias
            </h3>

            <div className="tzStat">
              <span>Faltantes acumulados</span>
              <strong>{soles(totalFaltantes)}</strong>
            </div>

            <div className="tzStat tzStatTotal">
              <span>Sobrantes acumulados</span>
              <strong>{soles(totalSobrantes)}</strong>
            </div>

            <p>
              Importes calculados sobre las cajas cerradas
              de la página actual.
            </p>

          </div>

        </div>

      )}

      {/* HISTORIAL */}

      <div className="tzCard">

        <h3 className="tzCardTitle">
          <Clock size={18} />
          Historial de turnos
        </h3>

        {cargando ? (

          <div className="tzEmpty">
            Consultando registros en MySQL...
          </div>

        ) : error ? (

          <div className="tzEmpty">
            No se pudo obtener el historial.
          </div>

        ) : cajas.length === 0 ? (

          <div className="tzEmpty">
            Todavía no existen registros de caja.
          </div>

        ) : (

          <div style={{ overflowX: 'auto' }}>

            <table className="tzTable">

              <thead>
                <tr>
                  <th>ID</th>
                  <th>Apertura</th>
                  <th>Cierre</th>
                  <th>Cajero</th>
                  <th>Fondo inicial</th>
                  <th>Esperado</th>
                  <th>Contado</th>
                  <th>Descuadre acumulado</th>
                  <th>Estado</th>
                </tr>
              </thead>

              <tbody>

                {cajas.map(caja => {

                  const descuadre = obtenerDescuadre(caja);
const esperado = obtenerEsperado(caja);
const contado = obtenerContado(caja);

const cuadrado = descuadre < 0.01;

                  const abierta =
                    caja.estado === 'ABIERTA';

                  return (

                    <tr key={caja.idCaja}>

                      <td>
                        <strong>#{caja.idCaja}</strong>
                      </td>

                      <td>
                        {caja.fechaApertura
                          ? new Date(caja.fechaApertura).toLocaleString('es-PE')
                          : '—'}
                      </td>

                      <td>
                        {caja.fechaCierre
                          ? new Date(caja.fechaCierre).toLocaleString('es-PE')
                          : '—'}
                      </td>

                      <td>
                        {caja.cajero || 'Sin información'}
                      </td>

                      <td>
                        {soles(caja.fondoInicial)}
                      </td>

                      <td>
                        {abierta ? 'Pendiente' : soles(esperado)}
                      </td>

                      <td>
                        {abierta ? 'Pendiente' : soles(contado)}
                      </td>

                      <td>

                        {abierta ? (

                          <span className="tzPill">
                            PENDIENTE
                          </span>

                        ) : (

                          <span
                            className={
                              cuadrado
                                ? 'tzPill tzPillOk'
                                : 'tzPill tzPillBad'
                            }
                          >
                            {soles(descuadre)}
                          </span>

                        )}

                      </td>

                      <td>

                        {abierta ? (

                          <span className="tzPill">
                            ABIERTA
                          </span>

                        ) : (

                          <span className="tzPill tzPillOk">
                            <CheckCircle2
                              size={13}
                              style={{ display: 'inline', verticalAlign: 'middle' }}
                            />
                            {' '}CERRADA
                          </span>

                        )}

                      </td>

                    </tr>

                  );

                })}

              </tbody>

            </table>

          </div>

        )}

        {/* PAGINACIÓN */}

        {!cargando && !error && resultado.totalPages > 0 && (

          <div className="tzBtnRow" style={{ marginTop: 20 }}>

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
