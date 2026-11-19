package pe.edu.utp.techzone.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.utp.techzone.dto.AuthResponseDTO;
import pe.edu.utp.techzone.dto.LoginRequest;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.exception.GlobalExceptionHandler;
import pe.edu.utp.techzone.service.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Login correcto responde 200 y guarda rol y usuario en la sesión")
    void loginCorrecto() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(
                AuthResponseDTO.builder().id("PERADM").nombre("Administrador").rol("ADMIN").usuario("admin").build());

        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCliente":"admin","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andReturn();

        HttpSession sesion = resultado.getRequest().getSession(false);
        assertThat(sesion).isNotNull();
        assertThat(sesion.getAttribute("rol")).isEqualTo("ADMIN");
        assertThat(sesion.getAttribute("usuario")).isEqualTo("admin");
    }

    @Test
    @DisplayName("Credenciales incorrectas responden 400 y no crean sesión con rol")
    void loginIncorrecto() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new BusinessException("Credenciales incorrectas"));

        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCliente":"admin","password":"mala"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Credenciales incorrectas"))
                .andReturn();

        HttpSession sesion = resultado.getRequest().getSession(false);
        assertThat(sesion == null || sesion.getAttribute("rol") == null).isTrue();
    }

    @Test
    @DisplayName("Login sin password responde 400")
    void loginSinPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCliente":"admin","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}
