import { useEffect, useState } from 'react';
import {
  Check,
  FileWarning,
  Printer,
  RotateCcw,
  Search,
  X
} from 'lucide-react';

import {
  ventaService,
  catalogoService
} from '../../services/api';
import { devolucionService } from '../../services/devolucionService';

const MEDIOS_PAGO_BLOQUEADOS = new Set([
  'MP007',
  'MP013',
  'MP014'
]);

const soles = (valor) =>
  `S/ ${Number(valor || 0).toFixed(2)}`;

const MOTIVOS = [
  'Producto defectuoso',
  'Producto equivocado',
  'Error de cobro',
  'Cliente arrepentido',
  'Otro'
];

const normalizar = (valor) =>
  String(valor || '')
    .replace(/\s+/g, '')
    .toUpperCase();

export default function Devoluciones() {

  const [ventas, setVentas] = useState([]);
  const [cargandoVentas, setCargandoVentas] = useState(true);

  const [termino, setTermino] = useState('');
  const [ventaSel, setVentaSel] = useState(null);

  const [seleccion, setSeleccion] = useState([]);
  const [cargandoDevoluciones, setCargandoDevoluciones] = useState(false);

  const [motivoGeneral, setMotivoGeneral] = useState(MOTIVOS[0]);
  const [medioPagoReembolso, setMedioPagoReembolso] = useState('MP001');
  const [mediosPago, setMediosPago] = useState([]);
  const [cargandoMediosPago, setCargandoMediosPago] = useState(true);
  const [observaciones, setObservaciones] = useState('');

  const [mensaje, setMensaje] = useState('');
  const [errorHistorial, setErrorHistorial] = useState(false);

  const [registrando, setRegistrando] = useState(false);
  const [nota, setNota] = useState(null);


  // =========================================================
  // 1. CARGAR VENTAS REALES
  // =========================================================

  useEffect(() => {

    let activo = true;

    async function cargarVentas() {

      try {

        const data = await ventaService.listar();

        if (activo) {
          setVentas(Array.isArray(data) ? data : []);
        }

      } catch (error) {

        if (activo) {
          setMensaje(error.message);
        }

      } finally {

        if (activo) {
          setCargandoVentas(false);
        }
      }
    }

    cargarVentas();

    return () => {
      activo = false;
    };

  }, []);


  // =========================================================
  // 2. CARGAR MEDIOS DE PAGO DESDE MYSQL
  // =========================================================

  useEffect(() => {

    let activo = true;

    async function cargarMediosPago() {

      try {

        const data = await catalogoService.mediosPago();

        if (!activo) return;

        const lista = (Array.isArray(data) ? data : [])
          .filter(medio =>
            !MEDIOS_PAGO_BLOQUEADOS.has(medio.idMedioPago)
          );

        setMediosPago(lista);

        const efectivo = lista.find(
          medio => medio.idMedioPago === 'MP001'
        );

        setMedioPagoReembolso(
          efectivo?.idMedioPago || lista[0]?.idMedioPago || ''
        );

      } catch (error) {

        if (activo) {
          setMensaje(
            `No se pudieron cargar los medios de pago: ${error.message}`
          );
        }

      } finally {

        if (activo) {
          setCargandoMediosPago(false);
        }
      }
    }

    cargarMediosPago();

    return () => {
      activo = false;
    };

  }, []);

  // =========================================================
  // 3. BUSCAR COMPROBANTE
  // =========================================================

  async function buscar(e) {

    e.preventDefault();

    setMensaje('');
    setVentaSel(null);
    setNota(null);
    setSeleccion([]);
    setErrorHistorial(false);

    if (!termino.trim()) {
      setMensaje('Ingresa un número de comprobante o ID de venta.');
      return;
    }

    const criterio = normalizar(termino);

    const encontrado = ventas.find(venta => {

      const comprobante = normalizar(venta.numeroDocumento);
      const id = normalizar(venta.idVenta);

      return (
        comprobante === criterio ||
        comprobante.includes(criterio) ||
        id === criterio
      );

    });

    if (!encontrado) {

      setMensaje(
        `No se encontró el comprobante "${termino}".`
      );

      return;
    }

    await seleccionarVenta(encontrado);
  }


  // =========================================================
  // 3. SELECCIONAR VENTA Y CONSULTAR DEVOLUCIONES PREVIAS
  // =========================================================

  async function seleccionarVenta(venta) {

    setVentaSel(venta);
    setNota(null);
    setMensaje('');
    setSeleccion([]);
    setErrorHistorial(false);
    setCargandoDevoluciones(true);

    try {

      const detalles = venta.detalles || [];

      if (
        detalles.length === 0 ||
        detalles.some(detalle => !Number.isInteger(Number(detalle.idDetalle)))
      ) {
        throw new Error(
          'La venta no contiene identificadores de detalle válidos. Verifica la actualización de VentaService.'
        );
      }

      // Consultar devoluciones anteriores de esta venta.
      // Consultar cantidades devueltas de todos los operadores,
// sin exponer los detalles de sus notas de crédito.
const resumen = await devolucionService.resumenPorVenta(
  venta.idVenta
);

const acumulados = new Map(
  (Array.isArray(resumen) ? resumen : []).map(item => [
    Number(item.idDetalleVenta),
    Number(item.cantidadDevuelta || 0)
  ])
);

      const nuevaSeleccion = detalles.map(detalle => {

        const devuelto = acumulados.get(detalle.idDetalle) || 0;

        const disponible = Math.max(
          0,
          Number(detalle.cantidad) - devuelto
        );

        return {
          idDetalleVenta: detalle.idDetalle,
          checked: false,
          cantidad: disponible > 0 ? 1 : 0,
          motivo: MOTIVOS[0],
          devuelto,
          disponible
        };

      });

      setSeleccion(nuevaSeleccion);

    } catch (error) {

      setErrorHistorial(true);
      setMensaje(error.message);

    } finally {

      setCargandoDevoluciones(false);
    }
  }


  // =========================================================
  // 4. CONTROLES DE SELECCION
  // =========================================================

  function alternar(indice) {

    setSeleccion(prev =>
      prev.map((item, i) => {

        if (i !== indice) return item;

        if (item.disponible <= 0) return item;

        return {
          ...item,
          checked: !item.checked
        };

      })
    );
  }


  function cambiarCantidad(indice, valor) {

    const numero = Number.parseInt(valor, 10);

    setSeleccion(prev =>
      prev.map((item, i) => {

        if (i !== indice) return item;

        return {
          ...item,
          cantidad: Math.max(
            1,
            Math.min(item.disponible, Number.isNaN(numero) ? 1 : numero)
          )
        };

      })
    );
  }


  function cambiarMotivo(indice, valor) {

    setSeleccion(prev =>
      prev.map((item, i) =>
        i === indice
          ? { ...item, motivo: valor }
          : item
      )
    );
  }


  // =========================================================
  // 5. CALCULAR REEMBOLSO
  // =========================================================

  const totalReembolso = ventaSel
    ? ventaSel.detalles.reduce((total, detalle, indice) => {

        const item = seleccion[indice];

        if (!item?.checked) return total;

        return total +
          Number(detalle.precioUnitario) *
          item.cantidad;

      }, 0)
    : 0;


  // =========================================================
  // 6. REGISTRAR DEVOLUCION EN MYSQL
  // =========================================================

async function generarNota() {
  setMensaje('');

  if (!ventaSel || errorHistorial || cargandoDevoluciones) {
    setMensaje('No se pudo validar el historial de devoluciones.');
    return;
  }

  if (cargandoMediosPago || mediosPago.length === 0) {
    setMensaje('No se pudieron cargar los medios de pago.');
    return;
  }

  const medioValido = mediosPago.some(
    medio => medio.idMedioPago === medioPagoReembolso
  );

  if (!medioPagoReembolso || !medioValido) {
    setMensaje('Selecciona un medio de pago válido.');
    return;
  }

  const items = seleccion
    .filter(item => item.checked)
    .map(item => ({
      idDetalleVenta: item.idDetalleVenta,
      cantidad: item.cantidad,
      motivo: item.motivo
    }));

  if (items.length === 0) {
    setMensaje('Selecciona al menos un producto para devolver.');
    return;
  }

  if (!motivoGeneral.trim()) {
    setMensaje('Debes indicar el motivo general de la devolución.');
    return;
  }

  if (registrando) return;

  setRegistrando(true);

  try {

    const resultado = await devolucionService.registrar({

      idVenta: ventaSel.idVenta,

      idMedioPagoReembolso: medioPagoReembolso,

      motivo: motivoGeneral,

      observaciones: observaciones.trim(),

      items

    });

    setNota(resultado);
    setMensaje('');

  } catch (error) {

    setMensaje(
      error.message || 'No se pudo registrar la devolución.'
    );

  } finally {

    setRegistrando(false);

  }
}


  // =========================================================
  // 7. RENDERIZAR
  // =========================================================

  return (

    <section className="tzView">

      <div className="tzHero">

        <div>
          <span className="tzHeroTag">
            Post-venta
          </span>

          <h2>Devoluciones y Anulaciones</h2>

          <p>
            Busca un comprobante y registra
            una devolución con recuperación de inventario.
          </p>
        </div>

        <RotateCcw size={42} />

      </div>


      {/* BUSCADOR */}

      <form
        className="tzSearchBar"
        onSubmit={buscar}
      >

        <Search size={20} />

        <input
          value={termino}
          onChange={e => setTermino(e.target.value)}
          placeholder="Buscar comprobante o ID de venta..."
          disabled={cargandoVentas || registrando}
        />

        <button
          type="submit"
          className="tzBtn tzBtnPrimary"
          disabled={cargandoVentas || registrando}
        >
          Buscar
        </button>

      </form>


      {cargandoVentas && (
        <div className="tzCard tzEmpty">
          Consultando comprobantes...
        </div>
      )}


      {mensaje && (
        <div className="tzCard tzError">
          {mensaje}
        </div>
      )}


      {/* COMPROBANTES DISPONIBLES */}

      {!ventaSel && !nota && !cargandoVentas && ventas.length > 0 && (

        <div className="tzCard">

          <h3 className="tzCardTitle">
            <FileWarning size={18} />
            Comprobantes disponibles
          </h3>

          <div className="tzSugerencias">

            {ventas.slice(0, 10).map(venta => (

              <button
                key={venta.idVenta}
                type="button"
                className="tzSugerencia"
                onClick={() => {
                  setTermino(venta.numeroDocumento);
                  seleccionarVenta(venta);
                }}
                disabled={registrando}
              >

                <strong>
                  {venta.numeroDocumento}
                </strong>

                <span>
                  {venta.cliente} · {venta.tipoDocumento} · {soles(venta.total)}
                </span>

              </button>

            ))}

          </div>

        </div>

      )}


      {/* VENTA SELECCIONADA */}

      {ventaSel && !nota && (

        <>

          <div className="tzCard tzVentaHead">

            <div>
              <span className="tzHeroTag">
                Comprobante original
              </span>

              <h3>{ventaSel.numeroDocumento}</h3>

              <p>
                {ventaSel.tipoDocumento}
                {' · '}
                {ventaSel.cliente}
                {' · '}
                {ventaSel.medioPago}
              </p>
            </div>

            <div className="tzVentaHeadTotal">

              <span>Total de la venta</span>

              <strong>
                {soles(ventaSel.total)}
              </strong>

            </div>

          </div>


          {cargandoDevoluciones ? (

            <div className="tzCard tzEmpty">
              Consultando devoluciones anteriores...
            </div>

          ) : !errorHistorial && (

            <>

              <div className="tzCard">

                <h3 className="tzCardTitle">
                  Productos de la venta
                </h3>

                <div style={{ overflowX: 'auto' }}>

                  <table className="tzTable">

                    <thead>
                      <tr>
                        <th></th>
                        <th>Producto</th>
                        <th>Comprado</th>
                        <th>Devuelto</th>
                        <th>Disponible</th>
                        <th>Precio</th>
                        <th>Cantidad a devolver</th>
                        <th>Motivo</th>
                      </tr>
                    </thead>

                    <tbody>

                      {ventaSel.detalles.map((detalle, i) => {

                        const item = seleccion[i];

                        if (!item) return null;

                        return (

                          <tr
                            key={detalle.idDetalle}
                            className={
                              item.checked ? 'tzFilaActiva' : ''
                            }
                          >

                            <td>
                              <input
                                type="checkbox"
                                className="tzCheck"
                                checked={item.checked}
                                disabled={
                                  item.disponible <= 0 || registrando
                                }
                                onChange={() => alternar(i)}
                              />
                            </td>

                            <td>{detalle.producto}</td>

                            <td>{detalle.cantidad}</td>

                            <td>{item.devuelto}</td>

                            <td>
                              <strong>{item.disponible}</strong>
                            </td>

                            <td>
                              {soles(detalle.precioUnitario)}
                            </td>

                            <td>

                              <input
                                type="number"
                                className="tzCantidad"
                                min="1"
                                max={item.disponible}
                                value={item.cantidad}
                                disabled={!item.checked || registrando}
                                onChange={e =>
                                  cambiarCantidad(i, e.target.value)
                                }
                              />

                            </td>

                            <td>

                              <select
                                className="tzSelectInline"
                                value={item.motivo}
                                disabled={!item.checked || registrando}
                                onChange={e =>
                                  cambiarMotivo(i, e.target.value)
                                }
                              >

                                {MOTIVOS.map(motivo => (
                                  <option key={motivo} value={motivo}>
                                    {motivo}
                                  </option>
                                ))}

                              </select>

                            </td>

                          </tr>

                        );

                      })}

                    </tbody>

                  </table>

                </div>

              </div>


              <div className="tzCard">

                <div className="tzField">

                  <label>Motivo general de devolución</label>

                  <select
                    value={motivoGeneral}
                    onChange={e => setMotivoGeneral(e.target.value)}
                    disabled={registrando}
                  >

                    {MOTIVOS.map(motivo => (
                      <option key={motivo} value={motivo}>
                        {motivo}
                      </option>
                    ))}

                  </select>

                </div>

<div className="tzField">

  <label>Medio de pago del reembolso</label>

  <select
    value={medioPagoReembolso}
    onChange={e => setMedioPagoReembolso(e.target.value)}
    disabled={
      registrando ||
      cargandoMediosPago ||
      mediosPago.length === 0
    }
    required
  >

    <option value="">
      Selecciona un medio de pago
    </option>

    {mediosPago
  .filter(
    medio => !MEDIOS_PAGO_BLOQUEADOS.has(medio.idMedioPago)
  )
  .map(medio => (

      <option
        key={medio.idMedioPago}
        value={medio.idMedioPago}
      >
        {medio.descripcion}
      </option>

    ))}

  </select>

  {cargandoMediosPago && (
    <small>Cargando medios de pago...</small>
  )}

</div>



                <div className="tzField">

                  <label>Observaciones adicionales</label>

                  <textarea
                    rows={3}
                    maxLength={500}
                    value={observaciones}
                    onChange={e => setObservaciones(e.target.value)}
                    placeholder="Detalles adicionales de la devolución..."
                    disabled={registrando}
                  />

                </div>

              </div>


              <div className="tzDevolucionFooter">

                <div className="tzDevolucionTotal">

                  <span>Total a reembolsar</span>

                  <strong>
                    {soles(totalReembolso)}
                  </strong>

                </div>

                <button
                  type="button"
                  className="tzBtn tzBtnGreen"
                  onClick={generarNota}
                  disabled={
                    registrando ||
                    totalReembolso <= 0 ||
                    !medioPagoReembolso ||
                    cargandoMediosPago
                  }
                >

                  <FileWarning size={18} />

                  {registrando
                    ? 'REGISTRANDO DEVOLUCIÓN...'
                    : 'REGISTRAR DEVOLUCIÓN'}

                </button>

              </div>

            </>

          )}

        </>

      )}


      {/* NOTA DE CREDITO DEVUELTA POR EL BACKEND */}

      {nota && (

        <div className="tzReporteZ">

          <div className="tzReporteZHead">

            <strong>
              NOTA DE CRÉDITO {nota.numeroNotaCredito}
            </strong>

            <span>
              {nota.fechaRegistro
                ? new Date(nota.fechaRegistro).toLocaleString('es-PE')
                : ''}
            </span>

          </div>

          <div className="tzStat">
            <span>Comprobante original</span>
            <strong>{nota.numeroDocumento}</strong>
          </div>

          <div className="tzStat">
            <span>Cliente</span>
            <strong>{nota.cliente}</strong>
          </div>

          <div className="tzStat">
            <span>Estado</span>
            <strong>{nota.estado}</strong>
          </div>

          {(nota.detalles || []).map((item, i) => (

            <div className="tzStat" key={item.idDetalleDevolucion || i}>

              <span>
                {item.cantidad} × {item.producto}
                {' · '}
                {item.motivo}
              </span>

              <strong>
                {soles(item.subtotal)}
              </strong>

            </div>

          ))}

          <div className="tzDiferencia tzDifOk">

            <span>TOTAL REEMBOLSADO</span>

            <strong>
              {soles(nota.montoReembolsado)}
            </strong>

          </div>

          <div className="tzBtnRow">

            <button
              type="button"
              className="tzBtn tzBtnPrimary"
              onClick={() => window.print()}
            >
              <Printer size={18} />
              Imprimir Nota
            </button>

            <button
              type="button"
              className="tzBtn tzBtnGhost"
              onClick={() => {setMedioPagoReembolso('MP001');
                setVentaSel(null);
                setSeleccion([]);
                setNota(null);
                setTermino('');
                setObservaciones('');
                setMensaje('');
                setErrorHistorial(false);
                setMotivoGeneral(MOTIVOS[0]);
              }}
            >
              <X size={18} />
              Nueva Devolución
            </button>

          </div>

          <div className="tzCard tzAviso">
            La devolución fue registrada en el backend.
            El inventario se actualizó mediante la operación
            transaccional de Spring Boot.
          </div>

        </div>

      )}

    </section>

  );
}
