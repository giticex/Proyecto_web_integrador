import { BarChart3, Boxes, FileText, KeyRound, Landmark, ReceiptText, RotateCcw, ShoppingCart, Users } from 'lucide-react';
import SidebarMenu from './SidebarMenu';

export const CATEGORIAS_ADMIN = [
  {
    titulo: 'General',
    items: [
      { id: 'dashboard', texto: 'Dashboard & Métricas', icono: BarChart3 },
      { id: 'puntoventa', texto: 'Punto de Venta (POS)', icono: ShoppingCart }
    ]
  },
  {
    titulo: 'Inventario & Clientes',
    items: [
      { id: 'inventario', texto: 'Gestión de Productos', icono: Boxes },
      { id: 'clientes', texto: 'Directorio de Clientes', icono: Users }
    ]
  },
  {
    titulo: 'Fiscal & Control',
    items: [
      { id: 'comprobante', texto: 'Facturas y Boletas', icono: ReceiptText },
      { id: 'auditoriacajas', texto: 'Auditoría de Cajas', icono: Landmark },
      { id: 'devoluciones', texto: 'Gestión de Devoluciones', icono: RotateCcw },
      { id: 'reportes', texto: 'Reportes SUNAT', icono: FileText }
    ]
  },
  {
    titulo: 'Seguridad',
    items: [{ id: 'usuarios', texto: 'Gestión de Usuarios', icono: KeyRound }]
  }
];

export default function SidebarAdmin({ vista, setVista }) {
  return <SidebarMenu categorias={CATEGORIAS_ADMIN} vista={vista} setVista={setVista} />;
}
