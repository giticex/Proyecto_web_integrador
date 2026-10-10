import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

const PuntoVenta = () => {
  const [carrito, setCarrito] = useState([]);
  const [codigoIngresado, setCodigoIngresado] = useState('');

  const agregarProducto = (e) => {
    e.preventDefault();
    if (!codigoIngresado) return;
    
    const nuevoProducto = { 
        id: Date.now(), 
        nombre: `Producto SKU: ${codigoIngresado}`, 
        precio: 25.50, 
        cantidad: 1 
    };
    setCarrito([...carrito, nuevoProducto]);
    setCodigoIngresado('');
  };

  const total = carrito.reduce((acc, item) => acc + (item.precio * item.cantidad), 0);
  const igv = total * 0.18;
  const subtotal = total - igv;

  return (
    <div className="container-fluid bg-light vh-100 p-3">
      <div className="row h-100">
        
        <div className="col-md-8 d-flex flex-column">
          <div className="bg-white p-3 shadow-sm rounded mb-3 flex-grow-1">
            <h4 className="text-primary mb-3">🛒 Terminal de Venta</h4>
            <table className="table table-striped table-hover">
              <thead className="table-dark">
                <tr>
                  <th>Cant.</th>
                  <th>Descripción</th>
                  <th>Precio Unit.</th>
                  <th>Importe</th>
                </tr>
              </thead>
              <tbody>
                {carrito.map((item) => (
                  <tr key={item.id}>
                    <td>{item.cantidad}</td>
                    <td>{item.nombre}</td>
                    <td>S/ {item.precio.toFixed(2)}</td>
                    <td>S/ {(item.precio * item.cantidad).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="col-md-4">
          <div className="bg-white p-4 shadow-sm rounded h-100 d-flex flex-column">
            <form onSubmit={agregarProducto} className="mb-4">
              <label className="form-label fw-bold">Escanear Producto / Código</label>
              <input 
                type="text" 
                className="form-control form-control-lg border-primary" 
                placeholder="Ingrese código o SKU..." 
                value={codigoIngresado}
                onChange={(e) => setCodigoIngresado(e.target.value)}
                autoFocus
              />
            </form>

            <div className="mt-auto">
              <div className="d-flex justify-content-between mb-2 fs-5">
                <span className="text-muted">Subtotal:</span>
                <span>S/ {subtotal.toFixed(2)}</span>
              </div>
              <div className="d-flex justify-content-between mb-2 fs-5">
                <span className="text-muted">IGV (18%):</span>
                <span>S/ {igv.toFixed(2)}</span>
              </div>
              <hr />
              <div className="d-flex justify-content-between mb-4 fs-2 fw-bold text-success">
                <span>TOTAL:</span>
                <span>S/ {total.toFixed(2)}</span>
              </div>
              <button className="btn btn-success btn-lg w-100 py-3 fw-bold fs-4">
                COBRAR AHORA
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default PuntoVenta;