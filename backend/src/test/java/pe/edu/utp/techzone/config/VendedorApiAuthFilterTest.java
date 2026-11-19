package pe.edu.utp.techzone.config;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class VendedorApiAuthFilterTest {

    private final VendedorApiAuthFilter filtro = new VendedorApiAuthFilter();

    private MockFilterChain ejecutar(MockHttpServletRequest request, MockHttpServletResponse response) throws Exception {
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(request, response, cadena);
        return cadena;
    }

    @Test
    @DisplayName("Sin sesión: /api/vendedores responde 403 y no continúa la cadena")
    void sinSesion() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vendedores");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain cadena = ejecutar(request, response);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(cadena.getRequest()).isNull();
    }

    @Test
    @DisplayName("Con rol CLIENTE: /api/vendedores responde 403")
    void rolCliente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vendedores");
        HttpSession sesion = request.getSession(true);
        sesion.setAttribute("rol", "CLIENTE");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain cadena = ejecutar(request, response);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(cadena.getRequest()).isNull();
    }

    @Test
    @DisplayName("Con rol ADMIN: /api/vendedores deja pasar la petición")
    void rolAdmin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/vendedores");
        request.getSession(true).setAttribute("rol", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain cadena = ejecutar(request, response);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(cadena.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("Las rutas que no son /api/vendedores no se filtran")
    void otraRuta() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/productos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain cadena = ejecutar(request, response);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(cadena.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("Las peticiones OPTIONS (preflight CORS) pasan sin sesión")
    void preflightCors() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/vendedores");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain cadena = ejecutar(request, response);

        assertThat(cadena.getRequest()).isNotNull();
    }
}
