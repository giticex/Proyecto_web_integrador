import { Lock, ReceiptText, RotateCcw, ShoppingCart, Unlock } from 'lucide-react';
import SidebarMenu from './SidebarMenu';

export const CATEGORIAS_VENDEDOR = [
  {
    titulo: 'Operación de Caja',
    items: [
      { id: 'puntoventa', texto: 'Punto de Venta', icono: ShoppingCart },
      { id: 'apertura', texto: 'Apertura de Caja', icono: Unlock },
      { id: 'cierre', texto: 'Arqueo y Cierre', icono: Lock }
    ]
  },
  {
    titulo: 'Post-venta',
    items: [
      { id: 'devoluciones', texto: 'Devoluciones', icono: RotateCcw },
      { id: 'comprobantes', texto: 'Consultar Comprobantes', icono: ReceiptText }
    ]
  }
];

export default function SidebarVendedor({ vista, setVista }) {
  return <SidebarMenu categorias={CATEGORIAS_VENDEDOR} vista={vista} setVista={setVista} />;
}
