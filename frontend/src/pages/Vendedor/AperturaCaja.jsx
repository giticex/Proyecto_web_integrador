
import { useEffect, useState } from 'react';
import { CalendarClock, Unlock, UserRound } from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { cajaService } from '../../services/cajaService';

const soles = (valor) =>
  `S/ ${Number(valor || 0).toFixed(2)}`;

export default function AperturaCaja({ setVista }) {

  const { sesion } = useApp();

  const [cajaActual, setCajaActual] = useState(null);
  const [cargandoActual, setCargandoActual] = useState(true);
  const [abriendo, setAbriendo] = useState(false);

  const [fondo, setFondo] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const [mensaje, setMensaje] = useState('');
  const [errorAutenticacion, setErrorAutenticacion] = useState(false);

  const cajeroSesion =
    `${sesion?.nombre || ''} ${sesion?.apellido || ''}`.trim()
    || sesion?.usuario
    || 'Cajero';

  const fechaHora = cajaActual?.fechaApertura
    ? new Date(cajaActual.fechaApertura).toLocaleString('es-PE')
    : new Date().toLocaleString('es-PE', {
        dateStyle: 'long',
        timeStyle: 'short'
      });

  // Consultar caja directamente en el backend.
  useEffect(() => {

    let activo = true;

    async function cargarCaja() {

      try {

        const data = await cajaService.actual();

        if (activo) {
          setCajaActual(data);
        }

      } catch (error) {

        if (!activo) return;

        if (error.status === 404) {
          setCajaActual(null);
          return;
        }

        if (error.status === 401 || error.status === 403) {
          setErrorAutenticacion(true);
        }

        setMensaje(error.message);

      } finally {

        if (activo) {
          setCargandoActual(false);
        }
      }
    }

    cargarCaja();

    return () => {
      activo = false;
    };

  }, []);

  // Registrar apertura en MySQL.
  async function abrir(e) {

    e.preventDefault();

    const monto = Number(fondo);

    if (
      fondo.trim() === ''
      || !Number.isFinite(monto)
      || monto < 0
    ) {
      setMensaje('Ingresa un monto inicial válido.');
      return;
    }

    setMensaje('');
    setAbriendo(true);

    try {

      const nuevaCaja = await cajaService.abrir({
        fondoInicial: monto,
        observaciones: observaciones.trim()
      });

      setCajaActual(nuevaCaja);

      setVista('puntoventa');

    } catch (error) {

      if (error.status === 401 || error.status === 403) {
        setErrorAutenticacion(true);
      }

      setMensaje(error.message);

    } finally {

      setAbriendo(false);
    }
  }

  if (cargandoActual) {
    return (
      <section className="tzView">
        <div className="tzCard tzEmpty">
          Consultando estado de la caja...
        </div>
      </section>
    );
  }

  return (
    <section className="tzView">

      <div className="tzHero">
        <div>
          <span className="tzHeroTag">
            Operaciones de caja
          </span>

          <h2>Apertura de Caja</h2>

          <p>
            Declara el fondo inicial en efectivo
            para iniciar el turno de ventas.
          </p>
        </div>

        <Unlock size={42} />
      </div>

      {cajaActual && (

        <div className="tzCard tzAviso">

          <div>
            Ya existe una caja abierta por{' '}
            <strong>{cajaActual.cajero}</strong>.

            <br />

            Fondo inicial:{' '}
            <strong>
              {soles(cajaActual.fondoInicial)}
            </strong>
          </div>

          <button
            type="button"
            className="tzBtn tzBtnPrimary"
            onClick={() => setVista('puntoventa')}
          >
            Ir al Punto de Venta
          </button>

        </div>
      )}

      {!cajaActual && (

        <form
          className="tzCardDark tzFormCentrada"
          onSubmit={abrir}
        >

          <div className="tzField">

            <label>Fecha y hora</label>

            <div className="tzReadonly">
              <CalendarClock size={16} />
              {fechaHora}
            </div>

          </div>

          <div className="tzField">

            <label>Cajero responsable</label>

            <div className="tzReadonly">
              <UserRound size={16} />
              {cajeroSesion}
            </div>

          </div>

          <div className="tzField">

            <label>
              Fondo inicial en efectivo (S/)
            </label>

            <div className="tzMoney">
              <span>S/</span>

              <input
                type="number"
                min="0"
                step="0.01"
                value={fondo}
                onChange={(e) => {
                  setFondo(e.target.value);
                  setMensaje('');
                }}
                placeholder="0.00"
                disabled={errorAutenticacion || abriendo}
              />

            </div>

          </div>

          <div className="tzField">

            <label>
              Observaciones / desglose de billetes
            </label>

            <textarea
              rows={3}
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              placeholder="Ej.: 2 billetes de 100, 4 de 50..."
              disabled={errorAutenticacion || abriendo}
            />

          </div>

          {errorAutenticacion && (
            <div className="tzError">
              La operación requiere autenticación.
              Estamos preparando la integración JWT
              de DEV 1 y el interceptor HTTP de DEV 3.
            </div>
          )}

          {mensaje && (
            <div className="tzError">
              {mensaje}
            </div>
          )}

          <button
            type="submit"
            className="tzBtn tzBtnGreen tzBtnFull"
            disabled={abriendo || errorAutenticacion}
          >
            <Unlock size={18} />

            {abriendo
              ? 'REGISTRANDO APERTURA...'
              : 'ABRIR CAJA E INICIAR VENTAS'}

          </button>

        </form>
      )}

    </section>
  );
}
