import { Navigate, Outlet } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { RUTAS, rutaInicio } from './rutas';

/**
 * Protege rutas por autenticacion y, opcionalmente, por rol.
 * Si no hay sesion redirige a /login; si el rol no esta permitido redirige al
 * inicio natural de ese rol.
 */
export default function ProtectedRoute({ roles }) {
  const { sesion } = useApp();

  if (!sesion) {
    return <Navigate to={RUTAS.login} replace />;
  }

  if (roles && !roles.includes(sesion.rol)) {
    return <Navigate to={rutaInicio(sesion)} replace />;
  }

  return <Outlet />;
}
