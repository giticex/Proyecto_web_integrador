import { useEffect, useState } from 'react';
import { KeyRound, Mail, Phone, UserPlus } from 'lucide-react';
import { vendedorService } from '../../services/api';

const FORM_VACIO = {
  idPersona: '',
  nombre: '',
  apellido: '',
  fechaNacimiento: '',
  correo: '',
  telefono: '',
  usuario: '',
  contrasena: ''
};

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [form, setForm] = useState(FORM_VACIO);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(true);

  async function cargar() {
    setCargando(true);
    try {
      setUsuarios(await vendedorService.listar());
      setError('');
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
  }, []);

  function actualizar(campo, valor) {
    setForm((prev) => ({ ...prev, [campo]: valor }));
  }

  async function crear(e) {
    e.preventDefault();
    setMensaje('');
    setError('');
    try {
      const payload = {
        ...form,
        fechaNacimiento: form.fechaNacimiento || null,
        correo: form.correo || null,
        telefono: form.telefono || null
      };
      await vendedorService.crear(payload);
      setMensaje(`Usuario "${form.usuario}" creado correctamente.`);
      setForm(FORM_VACIO);
      cargar();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section className="tzView">
      <div className="tzHero">
        <div>
          <span className="tzHeroTag">Seguridad</span>
          <h2>Gestión de Usuarios</h2>
          <p>Control de credenciales y asignación de roles de vendedor.</p>
        </div>
        <KeyRound size={42} />
      </div>

      {error && <div className="tzCard tzError">{error}</div>}
      {mensaje && <div className="tzCard tzDiferencia tzDifOk">{mensaje}</div>}

      <div className="tzGrid2">
        <div className="tzCard">
          <h3 className="tzCardTitle"><UserPlus size={18} /> Registrar vendedor</h3>
          <form onSubmit={crear}>
            <div className="tzField">
              <label>ID Persona</label>
              <input value={form.idPersona} onChange={(e) => actualizar('idPersona', e.target.value)} placeholder="PERV02" required />
            </div>
            <div className="tzField">
              <label>Nombre</label>
              <input value={form.nombre} onChange={(e) => actualizar('nombre', e.target.value)} required />
            </div>
            <div className="tzField">
              <label>Apellido</label>
              <input value={form.apellido} onChange={(e) => actualizar('apellido', e.target.value)} required />
            </div>
            <div className="tzField">
              <label>Fecha de nacimiento</label>
              <input type="date" value={form.fechaNacimiento} onChange={(e) => actualizar('fechaNacimiento', e.target.value)} />
            </div>
            <div className="tzField">
              <label><Mail size={13} /> Correo</label>
              <input type="email" value={form.correo} onChange={(e) => actualizar('correo', e.target.value)} />
            </div>
            <div className="tzField">
              <label><Phone size={13} /> Teléfono</label>
              <input value={form.telefono} onChange={(e) => actualizar('telefono', e.target.value)} maxLength={9} />
            </div>
            <div className="tzField">
              <label>Usuario</label>
              <input value={form.usuario} onChange={(e) => actualizar('usuario', e.target.value)} required />
            </div>
            <div className="tzField">
              <label>Contraseña</label>
              <input type="password" value={form.contrasena} onChange={(e) => actualizar('contrasena', e.target.value)} minLength={6} required />
            </div>
            <button type="submit" className="tzBtn tzBtnPrimary tzBtnFull">
              <UserPlus size={18} /> Crear usuario
            </button>
          </form>
        </div>

        <div className="tzCard">
          <h3 className="tzCardTitle"><KeyRound size={18} /> Usuarios del sistema</h3>
          {cargando ? (
            <div className="tzEmpty">Cargando usuarios...</div>
          ) : usuarios.length === 0 ? (
            <div className="tzEmpty">No se encontraron usuarios.</div>
          ) : (
            <table className="tzTable">
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Nombre</th>
                  <th>Rol</th>
                  <th>Correo</th>
                  <th>Teléfono</th>
                </tr>
              </thead>
              <tbody>
                {usuarios.map((u) => (
                  <tr key={u.idUsuario || u.usuario}>
                    <td><strong>{u.usuario}</strong></td>
                    <td>{u.nombre} {u.apellido}</td>
                    <td>{u.rol}</td>
                    <td>{u.correo || '—'}</td>
                    <td>{u.telefono || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </section>
  );
}
