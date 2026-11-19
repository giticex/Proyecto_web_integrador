import { NavLink } from 'react-router-dom';
import { RUTAS } from '../routes/rutas';

export default function SidebarMenu({ categorias }) {
  return (
    <nav className="menu">
      {categorias.map((grupo) => (
        <div className="menuGrupo" key={grupo.titulo}>
          <span className="menuGrupoTitulo">{grupo.titulo}</span>
          {grupo.items.map((opcion) => {
            const Icono = opcion.icono;
            const destino = RUTAS[opcion.id] || '/';
            return (
              <NavLink
                key={opcion.id}
                to={destino}
                end
                className={({ isActive }) => `menuBoton ${isActive ? 'activo' : ''}`}
              >
                <Icono size={19} />
                <span className="menuTexto">{opcion.texto}</span>
                {opcion.badge !== null && opcion.badge !== undefined && (
                  <span className="badgeContador">{opcion.badge}</span>
                )}
              </NavLink>
            );
          })}
        </div>
      ))}
    </nav>
  );
}
