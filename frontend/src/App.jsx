import { useEffect, useState } from 'react';
import { Menu as MenuIcon, X } from 'lucide-react';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Productos from './pages/Productos';
import Carrito from './pages/Carrito';
import Historial from './pages/Historial';
import Dashboard from './pages/Dashboard';
import Comprobante from './pages/Comprobante';
import Reportes from './pages/Reportes';
import Inventario from './pages/Inventario';
import Clientes from './pages/Clientes';
import PuntoVenta from './pages/Vendedor/PuntoVenta';
import AperturaCaja from './pages/Vendedor/AperturaCaja';
import CierreCaja from './pages/Vendedor/CierreCaja';
import Devoluciones from './pages/Vendedor/Devoluciones';
import ConsultarComprobantes from './pages/Vendedor/ConsultarComprobantes';
import Usuarios from './pages/Admin/Usuarios';
import AuditoriaCajas from './pages/Admin/AuditoriaCajas';
import AuditoriaDevoluciones from './pages/Admin/AuditoriaDevoluciones';
import { useApp } from './context/AppContext';
import { obtenerCaja } from './utils/caja';

export default function App() {
  const { sesion, esAdmin, esVendedor } = useApp();
  const [vista, setVista] = useState('login');
  const [ventaGenerada, setVentaGenerada] = useState(null);
  const [menuAbierto, setMenuAbierto] = useState(false);

  useEffect(() => {
    if (!sesion) {
      setVista('login');
      return;
    }
    setVista(esAdmin ? 'dashboard' : esVendedor ? (obtenerCaja() ? 'puntoventa' : 'apertura') : 'productos');
  }, [sesion, esAdmin, esVendedor]);

  function cambiarVista(nuevaVista) {
    if (!sesion) {
      setVista('login');
      return;
    }
    setVista(nuevaVista);
    setMenuAbierto(false);
  }

  function renderVista() {
    if (!sesion || vista === 'login') return <Login />;
    if (vista === 'productos') return <Productos />;
    if (vista === 'carrito') return <Carrito setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    if (vista === 'historial') return <Historial setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    if (vista === 'dashboard') return <Dashboard setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    if (vista === 'reportes') return <Reportes setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    if (vista === 'inventario') return <Inventario />;
    if (vista === 'clientes') return <Clientes setVista={cambiarVista} />;
    if (vista === 'puntoventa') return <PuntoVenta setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    if (vista === 'apertura') return <AperturaCaja setVista={cambiarVista} />;
    if (vista === 'cierre') return <CierreCaja setVista={cambiarVista} />;
    if (vista === 'devoluciones') return esAdmin ? <AuditoriaDevoluciones /> : <Devoluciones setVista={cambiarVista} />;
    if (vista === 'comprobantes') return <ConsultarComprobantes setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    if (vista === 'usuarios') return <Usuarios />;
    if (vista === 'auditoriacajas') return <AuditoriaCajas />;
    if (vista === 'comprobante') return <Comprobante venta={ventaGenerada} setVista={cambiarVista} />;
    if (esAdmin) return <Dashboard setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    if (esVendedor) return <PuntoVenta setVista={cambiarVista} setVentaGenerada={setVentaGenerada} />;
    return <Productos />;
  }

  if (!sesion) {
    return <Login />;
  }

  return (
    <div className={`appLayout ${menuAbierto ? 'menuAbierto' : ''}`}>
      <header className="mobileTopbar no-print">
        <button
          type="button"
          className="mobileMenuBoton"
          onClick={() => setMenuAbierto((abierto) => !abierto)}
          aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
        >
          {menuAbierto ? <X size={22} /> : <MenuIcon size={22} />}
        </button>
        <span className="mobileLogo">
          Tech<span>Zone</span>
        </span>
      </header>
      <Navbar vista={vista} setVista={cambiarVista} />
      {menuAbierto && (
        <div
          className="sidebarOverlay no-print"
          onClick={() => setMenuAbierto(false)}
          aria-hidden="true"
        />
      )}
      <main className="contenido">
        {renderVista()}
      </main>
    </div>
  );
}
