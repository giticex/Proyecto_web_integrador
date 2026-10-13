import { useState } from 'react';
import { CalendarClock, Unlock, UserRound } from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { abrirCaja, obtenerCaja } from '../../utils/caja';

const soles = (valor) => `S/ ${Number(valor || 0).toFixed(2)}`;

export default function AperturaCaja({ setVista }) {
  const { sesion } = useApp();
  const cajaActual = obtenerCaja();
  const fechaApertura = new Date();

  const [fondo, setFondo] = useState('');
  const [observaciones, setObservaciones] = useState('');
  const [mensaje, setMensaje] = useState('');

  const cajero =
    `${sesion?.nombre || ''} ${sesion?.apellido || ''}`.trim() ||
    sesion?.usuario ||
    'Cajero';
  const fechaHora = fechaApertura.toLocaleString('es-PE', {
    dateStyle: 'long',
    timeStyle: 'short'
  });

  function abrir(e) {
    e.preventDefault();
    if (fondo === '' || Number(fondo) < 0) {
      setMensaje('Ingresa un monto de fondo inicial válido.');
      return;
    }
    abrirCaja({
      cajero,
      usuario: sesion?.usuario,
      fondoInicial: Number(fondo),
      observaciones: observaciones.trim(),
      fechaApertura: fechaApertura.toISOString()
    });
    setVista('puntoventa');
  }

  return (
    <section className="tzView">
      <div className="tzHero">
        <div>
          <span className="tzHeroTag">Operaciones de caja</span>
          <h2>Apertura de Caja</h2>
          <p>Declara el fondo inicial en efectivo para iniciar el turno de ventas.</p>
        </div>
        <Unlock size={42} />
      </div>

      {cajaActual && (
        <div className="tzCard tzAviso">
          <span>
            Ya existe una caja abierta por <strong>{cajaActual.cajero}</strong> con fondo de{' '}
            <strong>{soles(cajaActual.fondoInicial)}</strong>.
          </span>
          <button type="button" className="tzBtn tzBtnPrimary" onClick={() => setVista('puntoventa')}>
            Ir al Punto de Venta
          </button>
        </div>
      )}

      <form className="tzCardDark tzFormCentrada" onSubmit={abrir}>
        <div className="tzField">
          <label>Fecha y hora de apertura</label>
          <div className="tzReadonly">
            <CalendarClock size={16} /> {fechaHora}
          </div>
        </div>

        <div className="tzField">
          <label>Cajero responsable</label>
          <div className="tzReadonly">
            <UserRound size={16} /> {cajero}
          </div>
        </div>

        <div className="tzField">
          <label>Fondo inicial en efectivo (S/)</label>
          <div className="tzMoney">
            <span>S/</span>
            <input
              inputMode="decimal"
              value={fondo}
              onChange={(e) => {
                setFondo(e.target.value.replace(/[^0-9.]/g, ''));
                setMensaje('');
              }}
              placeholder="0.00"
            />
          </div>
        </div>

        <div className="tzField">
          <label>Observaciones / desglose de billetes</label>
          <textarea
            rows={3}
            value={observaciones}
            onChange={(e) => setObservaciones(e.target.value)}
            placeholder="Ej.: 2 billetes de 100, 4 de 50, 10 de 20..."
          />
        </div>

        {mensaje && <div className="tzError">{mensaje}</div>}

        <button type="submit" className="tzBtn tzBtnGreen tzBtnFull">
          <Unlock size={18} /> ABRIR CAJA E INICIAR VENTAS
        </button>
      </form>
    </section>
  );
}