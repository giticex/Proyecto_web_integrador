import { useEffect, useState } from 'react';
import { BrowserRouter, Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom';
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
import ProtectedRoute from './routes/ProtectedRoute';
import { RUTAS, rutaInicio } from './routes/rutas';
import { useApp } from './context/AppContext';

function Inicio() {
  const { sesion } = useApp();
  return <Navigate to={rutaInicio(sesion)} replace />;
}

function LoginRoute() {
  const { sesion } = useApp();
  if (sesion) {
    return <Navigate to={rutaInicio(sesion)} replace />;
  }
  return <Login />;
}

function ComprobanteRuta() {
  const location = useLocation();
  return <Comprobante venta={location.state?.venta || null} />;
}

function DevolucionesPorRol() {
  const { esAdmin } = useApp();
  return esAdmin ? <AuditoriaDevoluciones /> : <Devoluciones />;
}

function AppLayout() {
  const [menuAbierto, setMenuAbierto] = useState(false);
  const location = useLocation();

  useEffect(() => {
    setMenuAbierto(false);
  }, [location.pathname]);

  return (
    <div className={`appLayout ${menuAbierto ? 'menuAbierto' : ''}`}>
      <header className="mobileTopbar no-print">
        <button
          type="button"
          className="mobileMenuBoton"
          onClick={() => setMenuAbierto((abierto) => !abierto)}
          aria-label={menuAbierto ? 'Cerrar menu' : 'Abrir menu'}
        >
          {menuAbierto ? <X size={22} /> : <MenuIcon size={22} />}
        </button>
        <span className="mobileLogo">
          Tech<span>Zone</span>
        </span>
      </header>

      <Navbar />

      {menuAbierto && (
        <div
          className="sidebarOverlay no-print"
          onClick={() => setMenuAbierto(false)}
          aria-hidden="true"
        />
      )}

      <main className="contenido">
        <Outlet />
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path={RUTAS.login} element={<LoginRoute />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route index element={<Inicio />} />

            <Route element={<ProtectedRoute roles={['CLIENTE']} />}>
              <Route path={RUTAS.productos} element={<Productos />} />
              <Route path={RUTAS.carrito} element={<Carrito />} />
            </Route>

            <Route element={<ProtectedRoute roles={['ADMIN', 'VENDEDOR']} />}>
              <Route path={RUTAS.puntoventa} element={<PuntoVenta />} />
              <Route path={RUTAS.devoluciones} element={<DevolucionesPorRol />} />
              <Route path={RUTAS.comprobantes} element={<ConsultarComprobantes />} />
            </Route>

            <Route element={<ProtectedRoute roles={['VENDEDOR']} />}>
              <Route path={RUTAS.apertura} element={<AperturaCaja />} />
              <Route path={RUTAS.cierre} element={<CierreCaja />} />
            </Route>

            <Route element={<ProtectedRoute roles={['ADMIN']} />}>
              <Route path={RUTAS.dashboard} element={<Dashboard />} />
              <Route path={RUTAS.inventario} element={<Inventario />} />
              <Route path={RUTAS.clientes} element={<Clientes />} />
              <Route path={RUTAS.reportes} element={<Reportes />} />
              <Route path={RUTAS.usuarios} element={<Usuarios />} />
              <Route path={RUTAS.auditoriacajas} element={<AuditoriaCajas />} />
            </Route>

            <Route element={<ProtectedRoute roles={['ADMIN', 'CLIENTE']} />}>
              <Route path={RUTAS.historial} element={<Historial />} />
            </Route>

            <Route element={<ProtectedRoute roles={['ADMIN', 'VENDEDOR', 'CLIENTE']} />}>
              <Route path={RUTAS.comprobante} element={<ComprobanteRuta />} />
            </Route>

            <Route path="*" element={<Inicio />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
