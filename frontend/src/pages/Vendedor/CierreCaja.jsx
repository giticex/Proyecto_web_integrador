
import { useEffect, useState } from 'react';
import {
  Banknote,
  CreditCard,
  Lock,
  Printer,
  Smartphone,
  TrendingUp
} from 'lucide-react';

import { cajaService } from '../../services/cajaService';

const soles = (valor) =>
  `S/ ${Number(valor || 0).toFixed(2)}`;

const CAMPOS = [
  {
    key: 'efectivoContado',
    label: 'Efectivo contado',
    icon: Banknote
  },
  {
    key: 'tarjetaContada',
    label: 'Tarjetas / vouchers',
    icon: CreditCard
  },
  {
    key: 'transferenciaContada',
    label: 'Yape / Plin / transferencias',
    icon: Smartphone
  },
  {
    key: 'otrosContados',
    label: 'Otros medios de pago',
    icon: TrendingUp
  }
];

export default function CierreCaja({ setVista }) {

  const [caja, setCaja] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [cerrando, setCerrando] = useState(false);

  const [mensaje, setMensaje] = useState('');
  const [errorAutenticacion, setErrorAutenticacion] = useState(false);

  const [observaciones, setObservaciones] = useState('');

  const [fisico, setFisico] = useState({
    efectivoContado: '0.00',
    tarjetaContada: '0.00',
    transferenciaContada: '0.00',
    otrosContados: '0.00'
  });

  const [reporte, setReporte] = useState(null);

  // Consultar la caja real del usuario.
  useEffect(() => {

    let activo = true;

    async function cargarCaja() {

      try {

        const data = await cajaService.actual();

        if (activo) {
          setCaja(data);
        }

      } catch (error) {

        if (!activo) return;

        if (error.status === 404) {
          setCaja(null);
        } else {

          setMensaje(error.message);

          if (error.status === 401 || error.status === 403) {
            setErrorAutenticacion(true);
          }
        }

      } finally {

        if (activo) {
          setCargando(false);
        }
      }
    }

    cargarCaja();

    return () => {
      activo = false;
    };

  }, []);

  function cambiarImporte(campo, valor) {

    setFisico(prev => ({
      ...prev,
      [campo]: valor
    }));
  }

  // Registrar el cierre en el backend.
  async function generarReporteZ(e) {

    e.preventDefault();

    if (!caja?.idCaja) {
      setMensaje('No se encontró una caja válida para cerrar.');
      return;
    }

    for (const campo of CAMPOS) {

      const valor = fisico[campo.key];

      if (
        valor.trim() === ''
        || !Number.isFinite(Number(valor))
        || Number(valor) < 0
      ) {
        setMensaje(`Ingresa un importe válido para ${campo.label}.`);
        return;
      }
    }

    setMensaje('');
    setCerrando(true);

    try {

      const resultado = await cajaService.cerrar(
        caja.idCaja,
        {
          efectivoContado: Number(fisico.efectivoContado),
          tarjetaContada: Number(fisico.tarjetaContada),
          transferenciaContada: Number(fisico.transferenciaContada),
          otrosContados: Number(fisico.otrosContados),
          observaciones: observaciones.trim()
        }
      );

      // La respuesta proviene de MySQL.
      setCaja(resultado);
      setReporte(resultado);

    } catch (error) {

      setMensaje(error.message);

      if (error.status === 401 || error.status === 403) {
        setErrorAutenticacion(true);
      }

    } finally {

      setCerrando(false);
    }
  }

  const totalEsperado = reporte
    ? Number(reporte.efectivoEsperado || 0)
      + Number(reporte.tarjetaEsperada || 0)
      + Number(reporte.transferenciaEsperada || 0)
      + Number(reporte.otrosEsperados || 0)
    : 0;

  const totalContado = reporte
    ? Number(reporte.efectivoContado || 0)
      + Number(reporte.tarjetaContada || 0)
      + Number(reporte.transferenciaContada || 0)
      + Number(reporte.otrosContados || 0)
    : 0;

  const diferencias = reporte
  ? [
      Number(reporte.diferenciaEfectivo || 0),
      Number(reporte.diferenciaTarjeta || 0),
      Number(reporte.diferenciaTransferencia || 0),
      Number(reporte.diferenciaOtros || 0)
    ]
  : [];

const totalFaltante = diferencias.reduce(
  (total, valor) => total + Math.max(0, -valor),
  0
);

const totalSobrante = diferencias.reduce(
  (total, valor) => total + Math.max(0, valor),
  0
);

const descuadreTotal = totalFaltante + totalSobrante;

  if (cargando) {
    return (
      <section className="tzView">
        <div className="tzCard tzEmpty">
          Consultando caja en el servidor...
        </div>
      </section>
    );
  }

  // No existe caja abierta.
  if (!caja && !reporte) {

    return (
      <section className="tzView">

        <div className="tzHero">
          <div>
            <span className="tzHeroTag">
              Operaciones de caja
            </span>
            <h2>Arqueo y Cierre de Caja</h2>
            <p>No hay una caja abierta en este momento.</p>
          </div>

          <Lock size={42} />
        </div>

        {mensaje && (
          <div className="tzCard tzError">{mensaje}</div>
        )}

        <div className="tzCard tzAviso">
          <span>
            Primero debes realizar la apertura de caja.
          </span>

          <button
            type="button"
            className="tzBtn tzBtnPrimary"
            onClick={() => setVista('apertura')}
          >
            Ir a Apertura de Caja
          </button>
        </div>

      </section>
    );
  }

  // Reporte devuelto por el backend.
  if (reporte) {

    const filas = [
      ['Fondo inicial', reporte.fondoInicial],
      ['Efectivo esperado', reporte.efectivoEsperado],
      ['Tarjetas esperadas', reporte.tarjetaEsperada],
      ['Transferencias esperadas', reporte.transferenciaEsperada],
      ['Otros esperados', reporte.otrosEsperados],
      ['Efectivo contado', reporte.efectivoContado],
      ['Tarjetas contadas', reporte.tarjetaContada],
      ['Transferencias contadas', reporte.transferenciaContada],
      ['Otros contados', reporte.otrosContados]
    ];

    return (
      <section className="tzView">

        <div className="tzHero">
          <div>
            <span className="tzHeroTag">Reporte Z</span>
            <h2>Turno cerrado correctamente</h2>
            <p>
              El cierre fue registrado en la base de datos.
            </p>
          </div>

          <Lock size={42} />
        </div>

        <div className="tzReporteZ">

          <div className="tzReporteZHead">
            <strong>TECHZONE - REPORTE Z</strong>
            <span>
              {reporte.fechaCierre
                ? new Date(reporte.fechaCierre).toLocaleString('es-PE')
                : 'Fecha no disponible'}
            </span>
          </div>

          <div className="tzStat">
            <span>Identificador de caja</span>
            <strong>{reporte.idCaja}</strong>
          </div>

          <div className="tzStat">
            <span>Cajero</span>
            <strong>{reporte.cajero}</strong>
          </div>

          {filas.map(([label, valor]) => (
            <div className="tzStat" key={label}>
              <span>{label}</span>
              <strong>{soles(valor)}</strong>
            </div>
          ))}

          <div className="tzStat tzStatTotal">
            <span>Total esperado</span>
            <strong>{soles(totalEsperado)}</strong>
          </div>

          <div className="tzStat tzStatTotal">
            <span>Total contado</span>
            <strong>{soles(totalContado)}</strong>
          </div>

          <div className={`tzDiferencia ${
  descuadreTotal < 0.01
    ? 'tzDifOk'
    : 'tzDifBad'
}`}>
  <span>DESCUADRE TOTAL</span>
  <strong>{soles(descuadreTotal)}</strong>
</div>

<div className="tzStat">
  <span>Faltantes acumulados</span>
  <strong>{soles(totalFaltante)}</strong>
</div>

<div className="tzStat">
  <span>Sobrantes acumulados</span>
  <strong>{soles(totalSobrante)}</strong>
</div>

          <div className="tzBtnRow">

            <button
              type="button"
              className="tzBtn tzBtnPrimary"
              onClick={() => window.print()}
            >
              <Printer size={18} />
              Imprimir Reporte
            </button>

            <button
              type="button"
              className="tzBtn tzBtnGhost"
              onClick={() => setVista('apertura')}
            >
              Nueva Apertura
            </button>

          </div>

        </div>

      </section>
    );
  }

  // Formulario de arqueo.
  return (
    <section className="tzView">

      <div className="tzHero">
        <div>
          <span className="tzHeroTag">
            Operaciones de caja
          </span>

          <h2>Arqueo y Cierre de Caja</h2>

          <p>
            Registra el dinero y los comprobantes
            físicos para cerrar el turno.
          </p>
        </div>

        <Lock size={42} />
      </div>

      <div className="tzCard">
        <h3 className="tzCardTitle">
          <TrendingUp size={20} />
          Información de la caja
        </h3>

        <div className="tzStat">
          <span>Número de caja</span>
          <strong>{caja.idCaja}</strong>
        </div>

        <div className="tzStat">
          <span>Cajero responsable</span>
          <strong>{caja.cajero}</strong>
        </div>

        <div className="tzStat">
          <span>Fondo inicial</span>
          <strong>{soles(caja.fondoInicial)}</strong>
        </div>

        <div className="tzStat">
          <span>Fecha de apertura</span>
          <strong>
            {new Date(caja.fechaApertura).toLocaleString('es-PE')}
          </strong>
        </div>
      </div>

      <form onSubmit={generarReporteZ}>

        <div className="tzGrid2">

          <div className="tzCard">
            <h3 className="tzCardTitle">
              <Banknote size={20} />
              Conteo físico
            </h3>

            {CAMPOS.map(({ key, label, icon: Icon }) => (

              <div className="tzField" key={key}>

                <label>
                  <Icon size={15} />
                  {label} (S/)
                </label>

                <div className="tzMoney tzMoneyLight">
                  <span>S/</span>

                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={fisico[key]}
                    onChange={(e) =>
                      cambiarImporte(key, e.target.value)
                    }
                    disabled={cerrando}
                    required
                  />
                </div>

              </div>

            ))}

          </div>

          <div className="tzCardDark">

            <h3 className="tzCardTitle">
              <TrendingUp size={20} />
              Arqueo del sistema
            </h3>

            <div className="tzStat">
              <span>Fondo inicial</span>
              <strong>{soles(caja.fondoInicial)}</strong>
            </div>

            <div className="tzStat">
              <span>Ventas asociadas</span>
              <strong>
                Se calcularán al cerrar
              </strong>
            </div>

            <div className="tzStat">
              <span>Estado</span>
              <strong>{caja.estado}</strong>
            </div>

            <p>
              Los importes esperados se calcularán
              en el servidor utilizando las ventas
              vinculadas a esta caja.
            </p>

          </div>

        </div>

        <div className="tzCard">

          <div className="tzField">
            <label>Observaciones del cierre</label>

            <textarea
              rows={3}
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              maxLength={500}
              placeholder="Observaciones, faltantes, sobrantes..."
              disabled={cerrando}
            />
          </div>

        </div>

        {errorAutenticacion && (
          <div className="tzCard tzError">
            Se requiere una sesión autenticada.
            La integración JWT está pendiente.
          </div>
        )}

        {mensaje && (
          <div className="tzCard tzError">
            {mensaje}
          </div>
        )}

        <button
          type="submit"
          className="tzBtn tzBtnGreen tzBtnFull"
          disabled={cerrando || errorAutenticacion}
        >
          <Printer size={18} />

          {cerrando
            ? 'REGISTRANDO CIERRE...'
            : 'REGISTRAR CIERRE Y GENERAR REPORTE Z'}

        </button>

      </form>

    </section>
  );
}
