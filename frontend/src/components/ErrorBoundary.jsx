import { Component } from 'react';

/**
 * ErrorBoundary global: evita que un fallo de renderizado deje la SPA en
 * blanco y muestra una salida amigable para el usuario.
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error, info) {
    console.error('ErrorBoundary capturo un error:', error, info);
  }

  render() {
    if (this.state.error) {
      return (
        <main className="loginShell">
          <section className="authCard authCardModerna">
            <div className="authHeader">
              <span className="miniTag">Error inesperado</span>
              <h2>Algo salio mal</h2>
              <p>La aplicacion encontro un problema al renderizar esta pantalla.</p>
            </div>
            <pre className="panelVacio" style={{ whiteSpace: 'pre-wrap', fontSize: 12 }}>
              {this.state.error?.message}
            </pre>
            <button
              type="button"
              className="btnPrincipal btnGrande"
              onClick={() => window.location.reload()}
            >
              Recargar aplicacion
            </button>
          </section>
        </main>
      );
    }

    return this.props.children;
  }
}
