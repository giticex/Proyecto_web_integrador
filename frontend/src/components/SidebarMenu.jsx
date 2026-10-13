export default function SidebarMenu({ categorias, vista, setVista }) {
  return (
    <nav className="menu">
      {categorias.map((grupo) => (
        <div className="menuGrupo" key={grupo.titulo}>
          <span className="menuGrupoTitulo">{grupo.titulo}</span>
          {grupo.items.map((opcion) => {
            const Icono = opcion.icono;
            const activo = vista === opcion.id;
            return (
              <button
                key={opcion.id}
                type="button"
                className={`menuBoton ${activo ? 'activo' : ''}`}
                onClick={() => setVista(opcion.id)}
              >
                <Icono size={19} />
                <span className="menuTexto">{opcion.texto}</span>
                {opcion.badge !== null && opcion.badge !== undefined && (
                  <span className="badgeContador">{opcion.badge}</span>
                )}
              </button>
            );
          })}
        </div>
      ))}
    </nav>
  );
}
