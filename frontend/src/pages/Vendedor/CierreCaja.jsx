import { useEffect, useMemo, useState } from 'react';
import { Banknote, CreditCard, Lock, Printer, Smartphone, TrendingUp } from 'lucide-react';
import { useApp } from '../../context/AppContext';
import { ventaService } from '../../services/api';
import { cerrarCaja, obtenerCaja, registrarCierre } from '../../utils/caja';

const soles = (valor) => `S/ ${Number(valor || 0).toFixed(2)}`;

function clasificarMedio(descripcion) {
  const d = (descripcion || '').toLowerCase();
  if (d.includes('efectivo')) return 'efectivo';
  if (d.includes('tarjeta') || d.includes('visa') || d.includes('mastercard') || d.includes('american')) return 'tarjeta';
  if (d.includes('yape') || d.includes('plin') || d.includes('transferencia') || d.includes('dep')) return 'transferencia';
  return 'otros';
}

export default function CierreCaja({ setVista }) {
  const { sesion } = useApp();
  const caja = obtenerCaja();

  const [ventas, setVentas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [fisicoEfectivo, setFisicoEfectivo] = useState('');
  const [fisicoTarjeta, setFisicoTarjeta] = useState('');
  const [fisicoTransferencia, setFisicoTransferencia] = useState('');
  const [reporte, setReporte] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const data = await ventaService.listar();
        setVentas(data || []);
      } finally {
        setCargando(false);
      }
    })();
  }, []);

  const ventasDelDia = useMemo(() => {
    const hoy = new Date().toDateString();
    return ventas.filter((v) => v.fechaEmision && new Date(v.fechaEmision).toDateString() === hoy);
  }, [ventas]);

  const sistema = useMemo(() => {
    const acumulado = { efectivo: 0, tarjeta: 0, transferencia: 0, otros: 0 };
    ventasDelDia.forEach((v) => {
      acumulado[clasificarMedio(v.medioPago)] += Number(v.total || 0);
    });
    return {
      ...acumulado,
      efectivo: acumulado.efectivo + Number(caja?.fondoInicial || 0)
    };
  }, [ventasDelDia, caja]);

  const fisico = {
    efectivo: Number(fisicoEfectivo || 0),
    tarjeta: Number(fisicoTarjeta || 0),
    transferencia: Number(fisicoTransferencia || 0)
  };

  const totalSistema = sistema.efectivo + sistema.tarjeta + sistema.transferencia + sistema.otros;
  const totalFisico = fisico.efectivo + fisico.tarjeta + fisico.transferencia;
  const diferencia = totalFisico - totalSistema;
  const cuadrado = Math.abs(diferencia) < 0.01;

  const cajero = caja?.cajero || `${sesion?.nombre || ''} ${sesion?.apellido || ''}`.trim() || 'Cajero';

  function generarReporteZ() {
    const z = {
      cajero,
      fecha: new Date().toISOString(),
      fondoInicial: Number(caja?.fondoInicial || 0),
      cantidadVentas: ventasDelDia.length,
      sistema: {
        efectivo: sistema.efectivo,
        tarjeta: sistema.tarjeta,
        transferencia: sistema.transferencia,
        otros: sistema.otros
      },
      fisico,
      totalSistema,
      totalFisico,
      diferencia
    };
    registrarCierre(z);
    cerrarCaja();
    setReporte(z);
  }

  if (!caja && !reporte) {
    return (
      <section className="tzView">
        <div className="tzHero">
          <div>
            <span className="tzHeroTag">Operaciones de caja</span>
            <h2>Arqueo y Cierre de Caja</h2>
            <p>No hay una caja abierta en este momento.</p>
          </div>
          <Lock size={42} />
        </div>
        <div className="tzCard tzAviso">
          <span>Primero debes realizar la apertura de caja para poder cerrarla.</span>
          <button type="button" className="tzBtn tzBtnPrimary" onClick={() => setVista('apertura')}>
            Ir a Apertura de Caja
          </button>
        </div>
      </section>
    );
  }

  if (reporte) {
    return (
      <section className="tzView">
        <div className="tzHero">
          <div>
            <span className="tzHeroTag">Reporte Z</span>
            <h2>Turno cerrado correctamente</h2>
            <p>Comprobante de cierre generado. Puedes imprimirlo o archivarlo.</p>
          </div>
          <Lock size={42} />
        </div>

        <div className="tzReporteZ">
          <div className="tzReporteZHead">
            <strong>TECHZONE - REPORTE Z</strong>
            <span>{new Date(reporte.fecha).toLocaleString('es-PE')}</span>
          </div>
          <div className="tzStat"><span>Cajero</span><strong>{reporte.cajero}</strong></div>
          <div className="tzStat"><span>Ventas del turno</span><strong>{reporte.cantidadVentas}</strong></div>
          <div className="tzStat"><span>Fondo inicial</span><strong>{soles(reporte.fondoInicial)}</strong></div>
          <div className="tzStat"><span>Efectivo (sistema)</span><strong>{soles(reporte.sistema.efectivo)}</strong></div>
          <div className="tzStat"><span>Tarjeta (sistema)</span><strong>{soles(reporte.sistema.tarjeta)}</strong></div>
          <div className="tzStat"><span>Transferencias (sistema)</span><strong>{soles(reporte.sistema.transferencia)}</strong></div>
          <div className="tzStat"><span>Otros medios (sistema)</span><strong>{soles(reporte.sistema.otros)}</strong></div>
          <div className="tzStat"><span>Total físico contado</span><strong>{soles(reporte.totalFisico)}</strong></div>
          <div className={`tzDiferencia ${Math.abs(reporte.diferencia) < 0.01 ? 'tzDifOk' : 'tzDifBad'}`}>
            <span>DIFERENCIA</span>
            <strong>{soles(reporte.diferencia)}</strong>
          </div>
          <div className="tzBtnRow">
            <button type="button" className="tzBtn tzBtnPrimary" onClick={() => window.print()}>
              <Printer size={18} /> Imprimir Reporte
            </button>
            <button type="button" className="tzBtn tzBtnGhost" onClick={() => setVista('apertura')}>
              Nueva Apertura
            </button>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="tzView">
      <div className="tzHero">
        <div>
          <span className="tzHeroTag">Operaciones de caja</span>
          <h2>Arqueo y Cierre de Caja</h2>
          <p>Concilia el conteo físico con los montos registrados por el sistema.</p>
        </div>
        <Lock size={42} />
      </div>

      {cargando ? (
        <div className="tzCard tzEmpty">Cargando ventas del turno...</div>
      ) : (
        <div className="tzGrid2">
          <div className="tzCard">
            <h3 className="tzCardTitle">
              <Banknote size={20} /> Conteo físico
            </h3>
            <div className="tzField">
              <label>
                <Banknote size={14} /> Efectivo contado (S/)
              </label>
              <div className="tzMoney tzMoneyLight">
                <span>S/</span>
                <input
                  inputMode="decimal"
                  value={fisicoEfectivo}
                  onChange={(e) => setFisicoEfectivo(e.target.value.replace(/[^0-9.]/g, ''))}
                  placeholder="0.00"
                />
              </div>
            </div>
            <div className="tzField">
              <label>
                <CreditCard size={14} /> Vouchers de tarjeta (S/)
              </label>
              <div className="tzMoney tzMoneyLight">
                <span>S/</span>
                <input
                  inputMode="decimal"
                  value={fisicoTarjeta}
                  onChange={(e) => setFisicoTarjeta(e.target.value.replace(/[^0-9.]/g, ''))}
                  placeholder="0.00"
                />
              </div>
            </div>
            <div className="tzField">
              <label>
                <Smartphone size={14} /> Transferencias / Yape / Plin (S/)
              </label>
              <div className="tzMoney tzMoneyLight">
                <span>S/</span>
                <input
                  inputMode="decimal"
                  value={fisicoTransferencia}
                  onChange={(e) => setFisicoTransferencia(e.target.value.replace(/[^0-9.]/g, ''))}
                  placeholder="0.00"
                />
              </div>
            </div>
          </div>

          <div className="tzCardDark">
            <h3 className="tzCardTitle">
              <TrendingUp size={20} /> Resumen del sistema
            </h3>
            <div className="tzStat"><span>Fondo inicial</span><strong>{soles(caja?.fondoInicial)}</strong></div>
            <div className="tzStat"><span>Ventas en efectivo</span><strong>{soles(sistema.efectivo - Number(caja?.fondoInicial || 0))}</strong></div>
            <div className="tzStat"><span>Ventas con tarjeta</span><strong>{soles(sistema.tarjeta)}</strong></div>
            <div className="tzStat"><span>Yape / Plin / Transferencia</span><strong>{soles(sistema.transferencia)}</strong></div>
            <div className="tzStat"><span>Otros medios</span><strong>{soles(sistema.otros)}</strong></div>
            <div className="tzStat tzStatTotal"><span>Total sistema (efectivo esperado)</span><strong>{soles(sistema.efectivo)}</strong></div>
            <div className="tzStat"><span>Cantidad de ventas</span><strong>{ventasDelDia.length}</strong></div>
          </div>
        </div>
      )}

      <div className={`tzDiferencia ${cuadrado ? 'tzDifOk' : 'tzDifBad'}`}>
        <span>DIFERENCIA (FÍSICO - SISTEMA)</span>
        <strong>{soles(diferencia)}</strong>
      </div>

      <button
        type="button"
        className="tzBtn tzBtnGreen tzBtnFull"
        disabled={cargando}
        onClick={generarReporteZ}
      >
        <Printer size={18} /> IMPRIMIR TIRILLA DE CIERRE (REPORTE Z) Y CERRAR TURNO
      </button>
    </section>
  );
}