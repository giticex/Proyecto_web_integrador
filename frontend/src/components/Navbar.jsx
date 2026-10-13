import { FileCheck, History, LogOut, Package, ShoppingCart, UserRound } from 'lucide-react';
import { useApp } from '../context/AppContext';
import SidebarAdmin from './SidebarAdmin';
import SidebarVendedor from './SidebarVendedor';
import SidebarMenu from './SidebarMenu';

export default function Navbar({ vista, setVista }) {
  const { sesion, esAdmin, esVendedor, carrito, cerrarSesion } = useApp();

  const cantidadTotalItems = carrito.reduce((acc, item) => acc + item.cantidad, 0);

  const categoriasCliente = [
    {
      titulo: 'Tienda',
      items: [
        { id: 'productos', texto: 'Catálogo de Compras', icono: Package },
        {
          id: 'carrito',
          texto: 'Mi Carrito',
          icono: ShoppingCart,
          badge: cantidadTotalItems > 0 ? cantidadTotalItems : null
        },
        { id: 'comprobante', texto: 'Facturas y Boletas', icono: FileCheck },
        { id: 'historial', texto: 'Historial de Compras', icono: History }
      ]
    }
  ];

  return (
    <aside className="sidebar no-print">
      <div className="logoHeaderContainer">
        <div className="navbarLogoBox">
          <img
            src="/logo.png"
            alt="TechZone - Tienda Virtual & Sistemas"
            className="navbarLogoImg"
            draggable={false}
          />
        </div>
        <div className="navbarRolBadgeBox">
          <span className="navbarRolBadge">{esAdmin ? 'MODO ADMINISTRADOR' : esVendedor ? 'MODO VENDEDOR' : 'TIENDA & FACTURACIÓN'}</span>
        </div>
      </div>

      {esAdmin ? (
        <SidebarAdmin vista={vista} setVista={setVista} />
      ) : esVendedor ? (
        <SidebarVendedor vista={vista} setVista={setVista} />
      ) : (
        <SidebarMenu categorias={categoriasCliente} vista={vista} setVista={setVista} />
      )}

      <div className="usuarioBox">
        <div className="usuarioInfoFila">
          <div className="usuarioAvatar">
            <UserRound size={18} />
          </div>
          <div className="usuarioNombres">
            <strong>{sesion?.nombre} {sesion?.apellido}</strong>
            <span className="usuarioRolBadge">
              {sesion?.rol === 'ADMIN' ? 'Administrador General' : sesion?.rol === 'VENDEDOR' ? 'Vendedor' : 'Cliente'}
            </span>
          </div>
        </div>

        <button className="btnSalir" onClick={cerrarSesion}>
          <LogOut size={16} /> Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
