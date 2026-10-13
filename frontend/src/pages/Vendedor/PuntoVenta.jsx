import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Banknote,
  Barcode,
  CreditCard,
  Plus,
  Minus,
  Trash2,
  ShoppingBag,
  Smartphone
} from 'lucide-react';
import { clienteService, productoService, ventaService } from '../../services/api';

const soles = (valor) => `S/ ${Number(valor || 0).toFixed(2)}`;

const METODOS_PAGO = [
  { id: 'MP001', nombre: 'Efectivo', icono: Banknote },
  { id: 'MP003', nombre: 'Tarjeta', icono: CreditCard },
  { id: 'MP004', nombre: 'Yape', icono: Smartphone },
  { id: 'MP005', nombre: 'Plin', icono: Smartphone }
];

export default function PuntoVenta({ setVista, setVentaGenerada }) {
  const [productos, setProductos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [codigoBusqueda, setCodigoBusqueda] = useState('');
  const [carrito, setCarrito] = useState([]);
  const [idCliente, setIdCliente] = useState('');
  const [tipoDocumento, setTipoDocumento] = useState('TD001');
  const [numeroDocumento, setNumeroDocumento] = useState('');
  const [razonSocial, setRazonSocial] = useState('');
  const [medioPago, setMedioPago] = useState('MP001');
  const [montoRecibido, setMontoRecibido] = useState('');
  const [docError, setDocError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [cargando, setCargando] = useState(false);
  const [cargandoDatos, setCargandoDatos] = useState(true);

  const searchRef = useRef(null);
  const cobrarRef = useRef(null);

  useEffect(() => {
    (async () => {
      try {
        const [dataProductos, dataClientes] = await Promise.all([
          productoService.listar(),
          clienteService.listar()
        ]);
        setProductos(dataProductos || []);
        setClientes(dataClientes || []);
        if (dataClientes?.length) {
          setIdCliente(dataClientes[0].idCliente);
        }
      } catch (error) {
        setMensaje(error.message);
      } finally {
        setCargandoDatos(false);
      }
    })();
  }, []);

  const enfocarBuscador = () => requestAnimationFrame(() => searchRef.current?.focus());

  useEffect(() => {
    enfocarBuscador();
  }, []);

  useEffect(() => {
    function alPresionarTecla(e) {
      if (e.key === 'F12') {
        e.preventDefault();
        cobrarRef.current?.();
        return;
      }
      const activo = document.activeElement?.tagName;
      const escribiendo = activo === 'INPUT' || activo === 'SELECT' || activo === 'TEXTAREA';
      if (!escribiendo && e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
        searchRef.current?.focus();
      }
    }
    window.addEventListener('keydown', alPresionarTecla);
    return () => window.removeEventListener('keydown', alPresionarTecla);
  }, []);

  const total = useMemo(
    () => carrito.reduce((acc, item) => acc + Number(item.precio) * item.cantidad, 0),
    [carrito]
  );
  const totalItems = carrito.reduce((acc, item) => acc + item.cantidad, 0);
  const subtotal = total / 1.18;
  const igv = total - subtotal;
  const vuelto = Number(montoRecibido || 0) - total;

  function agregarProducto(producto) {
    const stock = Number(producto.stock);
    if (stock <= 0) {
      setMensaje(`"${producto.nombre}" no tiene stock disponible.`);
      return;
    }
    const existente = carrito.find((item) => item.idProducto === producto.idProducto);
    if (existente && existente.cantidad >= stock) {
      setMensaje(`Stock máximo alcanzado para "${producto.nombre}" (${stock} unid.).`);
      return;
    }
    setMensaje('');
    setCarrito((prev) => {
      const encontrado = prev.find((item) => item.idProducto === producto.idProducto);
      if (encontrado) {
        return prev.map((item) =>
          item.idProducto === producto.idProducto
            ? { ...item, cantidad: item.cantidad + 1 }
            : item
        );
      }
      return [
        ...prev,
        {
          idProducto: producto.idProducto,
          nombre: producto.nombre,
          precio: Number(producto.precio),
          stock,
          cantidad: 1
        }
      ];
    });
  }

  function escanear(e) {
    e.preventDefault();
    const termino = codigoBusqueda.trim();
    if (!termino) return;

    const enMinusculas = termino.toLowerCase();
    let producto =
      productos.find((p) => String(p.idProducto) === termino) ||
      productos.find((p) => (p.nombre || '').toLowerCase() === enMinusculas) ||
      productos.find(
        (p) =>
          (p.nombre || '').toLowerCase().includes(enMinusculas) ||
          String(p.idProducto).startsWith(termino)
      );

    if (!producto) {
      setMensaje(`No se encontró ningún producto para "${termino}".`);
      setCodigoBusqueda('');
      enfocarBuscador();
      return;
    }

    agregarProducto(producto);
    setCodigoBusqueda('');
    enfocarBuscador();
  }

  function cambiarCantidad(idProducto, delta) {
    setMensaje('');
    setCarrito((prev) =>
      prev.map((item) => {
        if (item.idProducto !== idProducto) return item;
        const nueva = Math.min(item.stock, Math.max(1, item.cantidad + delta));
        return { ...item, cantidad: nueva };
      })
    );
    enfocarBuscador();
  }

  function quitarProducto(idProducto) {
    setMensaje('');
    setCarrito((prev) => prev.filter((item) => item.idProducto !== idProducto));
    enfocarBuscador();
  }

  function volverAlInicio() {
    setMensaje('');
    setCarrito([]);
    setNumeroDocumento('');
    setRazonSocial('');
    setDocError('');
    setMontoRecibido('');
    setMedioPago('MP001');
    setTipoDocumento('TD001');
    enfocarBuscador();
  }

  function validarDocumento(tipo, valor) {
    if (!valor) {
      setDocError('');
      return true;
    }
    if (tipo === 'TD001') {
      if (!/^\d{8}$/.test(valor)) {
        setDocError('El DNI debe tener exactamente 8 dígitos.');
        return false;
      }
    } else if (tipo === 'TD002') {
      if (!/^(10|20)\d{9}$/.test(valor)) {
        setDocError('El RUC debe tener 11 dígitos y empezar con 10 o 20.');
        return false;
      }
    }
    setDocError('');
    return true;
  }

  async function cobrar() {
    setMensaje('');

    if (!idCliente) {
      setMensaje('Selecciona un cliente para la venta.');
      return;
    }
    if (carrito.length === 0) {
      setMensaje('La faja está vacía. Escanea o busca un producto.');
      return;
    }
    if (!/^\d{8}$/.test(numeroDocumento) && tipoDocumento === 'TD001') {
      setMensaje('El DNI debe tener exactamente 8 dígitos.');
      return;
    }
    if (!/^(10|20)\d{9}$/.test(numeroDocumento) && tipoDocumento === 'TD002') {
      setMensaje('El RUC debe tener 11 dígitos y empezar con 10 o 20.');
      return;
    }
    if (tipoDocumento === 'TD002' && !razonSocial.trim()) {
      setMensaje('La Razón Social es obligatoria para Factura.');
      return;
    }
    if (medioPago === 'MP001' && Number(montoRecibido || 0) < total) {
      setMensaje('El monto recibido es menor al total a cobrar.');
      return;
    }

    const request = {
      idCliente,
      tipoDocumento,
      medioPago,
      numeroDocumento,
      razonSocial: tipoDocumento === 'TD002' ? razonSocial : undefined,
      items: carrito.map((item) => ({ idProducto: item.idProducto, cantidad: item.cantidad }))
    };

    setCargando(true);
    try {
      const venta = await ventaService.registrar(request);
      setCarrito([]);
      setVentaGenerada(venta);
      setVista('comprobante');
    } catch (error) {
      setMensaje(error.message);
    } finally {
      setCargando(false);
    }
  }
  cobrarRef.current = cobrar;

  if (cargandoDatos) {
    return <section className="panelVacio">Cargando terminal de venta...</section>;
  }

  return (
    <div className="posShell">
      <header className="posHeader">
        <div className="posBrand">
          <ShoppingBag size={26} />
          <strong>TechZone</strong>
          <span>| CAJA #01</span>
        </div>
        <form className="posSearch" onSubmit={escanear}>
          <Barcode size={24} />
          <input
            ref={searchRef}
            value={codigoBusqueda}
            onChange={(e) => setCodigoBusqueda(e.target.value)}
            placeholder="ESCANEAR CÓDIGO DE BARRAS / BUSCAR PRODUCTO [ENTER]"
            aria-label="Buscar o escanear producto"
            autoComplete="off"
          />
        </form>
      </header>

      <div className="posBody">
        <section className="posLeft">
          <div className="posTableWrap">
            {carrito.length === 0 ? (
              <div className="posEmpty">
                Escanea un código de barras o busca un producto para iniciar la venta.
              </div>
            ) : (
              <table className="posTable">
                <thead>
                  <tr>
                    <th className="posColNum">#</th>
                    <th>Descripción del artículo</th>
                    <th>Cant.</th>
                    <th>P. Unit</th>
                    <th>Subtotal</th>
                    <th>Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {carrito.map((item, indice) => (
                    <tr key={item.idProducto}>
                      <td className="posColNum">{indice + 1}</td>
                      <td className="posDesc">{item.nombre}</td>
                      <td>
                        <div className="posStepper">
                          <button type="button" onClick={() => cambiarCantidad(item.idProducto, -1)} aria-label="Quitar una unidad">
                            <Minus size={15} />
                          </button>
                          <span>{item.cantidad}</span>
                          <button type="button" onClick={() => cambiarCantidad(item.idProducto, 1)} aria-label="Agregar una unidad">
                            <Plus size={15} />
                          </button>
                        </div>
                      </td>
                      <td className="posUnit">{soles(item.precio)}</td>
                      <td className="posSub">{soles(item.precio * item.cantidad)}</td>
                      <td>
                        <button type="button" className="posDelete" onClick={() => quitarProducto(item.idProducto)} aria-label="Eliminar artículo">
                          <Trash2 size={16} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <footer className="posFaja">
            <span className="posFajaItems">{totalItems} {totalItems === 1 ? 'ITEM' : 'ITEMS'}</span>
            <span className="posFajaTotal">TOTAL {soles(total)}</span>
          </footer>
        </section>

        <aside className="posRight">
          {mensaje && <div className="posAlert">{mensaje}</div>}

          <div className="posField">
            <label htmlFor="posCliente">Cliente</label>
            <select id="posCliente" value={idCliente} onChange={(e) => setIdCliente(e.target.value)}>
              {clientes.map((cliente) => (
                <option key={cliente.idCliente} value={cliente.idCliente}>
                  {cliente.idCliente} - {cliente.nombre} {cliente.apellido}
                </option>
              ))}
            </select>
          </div>

          <div className="posRow2">
            <div className="posField">
              <label htmlFor="posTipoDoc">Documento</label>
              <select
                id="posTipoDoc"
                value={tipoDocumento}
                onChange={(e) => {
                  setTipoDocumento(e.target.value);
                  setNumeroDocumento('');
                  setRazonSocial('');
                  setDocError('');
                }}
              >
                <option value="TD001">Boleta</option>
                <option value="TD002">Factura</option>
              </select>
            </div>
            <div className="posField">
              <label htmlFor="posNumDoc">{tipoDocumento === 'TD001' ? 'DNI' : 'RUC'}</label>
              <input
                id="posNumDoc"
                inputMode="numeric"
                value={numeroDocumento}
                onChange={(e) => {
                  const soloNumeros = e.target.value.replace(/\D/g, '').slice(0, tipoDocumento === 'TD001' ? 8 : 11);
                  setNumeroDocumento(soloNumeros);
                  validarDocumento(tipoDocumento, soloNumeros);
                }}
                placeholder={tipoDocumento === 'TD001' ? '8 dígitos' : '11 dígitos'}
              />
            </div>
          </div>
          {docError && <div className="posError">{docError}</div>}

          {tipoDocumento === 'TD002' && (
            <div className="posField">
              <label htmlFor="posRazon">Razón Social</label>
              <input
                id="posRazon"
                value={razonSocial}
                onChange={(e) => setRazonSocial(e.target.value)}
                placeholder="Razón Social del cliente"
              />
            </div>
          )}

          <div className="posField">
            <label>Método de pago</label>
            <div className="posChips">
              {METODOS_PAGO.map((metodo) => {
                const Icono = metodo.icono;
                return (
                  <button
                    type="button"
                    key={metodo.id}
                    className={`posChip ${medioPago === metodo.id ? 'activo' : ''}`}
                    onClick={() => setMedioPago(metodo.id)}
                  >
                    <Icono size={16} /> {metodo.nombre}
                  </button>
                );
              })}
            </div>
          </div>

          {medioPago === 'MP001' && (
            <>
              <div className="posField">
                <label htmlFor="posMonto">Monto recibido</label>
                <input
                  id="posMonto"
                  inputMode="decimal"
                  value={montoRecibido}
                  onChange={(e) => setMontoRecibido(e.target.value.replace(/[^0-9.]/g, ''))}
                  placeholder="0.00"
                />
              </div>
              <div className={`posVuelto ${vuelto < 0 ? 'negativo' : ''}`}>
                <span>Vuelto</span>
                <strong>{soles(Math.max(0, vuelto))}</strong>
              </div>
            </>
          )}

          <div className="posFiscal">
            <div>
              <span>Op. Gravada</span>
              <span>{soles(subtotal)}</span>
            </div>
            <div>
              <span>IGV (18%)</span>
              <span>{soles(igv)}</span>
            </div>
            <div className="posFiscalTotal">
              <span>Total</span>
              <strong>{soles(total)}</strong>
            </div>
          </div>

          <div className="posActions">
            <button type="button" className="posBtnCancelar" onClick={volverAlInicio}>
              CANCELAR
            </button>
            <button type="button" className="posBtnCobrar" disabled={cargando} onClick={cobrar}>
              {cargando ? 'Cobrando...' : 'COBRAR [F12]'}
            </button>
          </div>
        </aside>
      </div>
    </div>
  );
}