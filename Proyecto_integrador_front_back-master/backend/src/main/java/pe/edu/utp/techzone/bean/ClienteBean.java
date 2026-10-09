package pe.edu.utp.techzone.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import pe.edu.utp.techzone.dto.ClienteDTO;
import pe.edu.utp.techzone.service.ClienteService;

import java.util.List;

/** Flujo CRUD 100% JSF, reutilizando el servicio de clientes existente. */
@Component("clienteBean")
@SessionScope
@Getter
public class ClienteBean {
    private final ClienteService clienteService;
    private List<ClienteDTO> clientes;
    private ClienteDTO clienteSeleccionado;
    private boolean editando;

    public ClienteBean(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostConstruct
    public void init() {
        nuevo();
        cargarClientes();
    }

    public void cargarClientes() {
        clientes = clienteService.listar();
    }

    public void nuevo() {
        clienteSeleccionado = ClienteDTO.builder()
                .idCliente("").nombre("").apellido("").telefono("")
                .correo("").password("").build();
        editando = false;
    }

    public void seleccionar(ClienteDTO cliente) {
        // Copia separada: no modificar la fila original hasta presionar Guardar.
        clienteSeleccionado = ClienteDTO.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .telefono(cliente.getTelefono())
                .correo(cliente.getCorreo())
                .build();
        editando = true;
    }

    public void guardar() {
        try {
            if (clienteSeleccionado.getIdCliente() == null || clienteSeleccionado.getIdCliente().isBlank()
                    || clienteSeleccionado.getNombre() == null || clienteSeleccionado.getNombre().isBlank()
                    || clienteSeleccionado.getApellido() == null || clienteSeleccionado.getApellido().isBlank()) {
                throw new IllegalArgumentException("ID, nombre y apellido son obligatorios");
            }
            if (editando) {
                clienteService.actualizar(clienteSeleccionado.getIdCliente(), clienteSeleccionado);
            } else {
                if (clienteSeleccionado.getPassword() == null || clienteSeleccionado.getPassword().isBlank()) {
                    throw new IllegalArgumentException("La contraseña es obligatoria para crear clientes");
                }
                clienteService.registrar(clienteSeleccionado);
            }
            String mensaje = editando ? "Cliente actualizado" : "Cliente registrado";
            cargarClientes();
            nuevo();
            informar(mensaje);
        } catch (IllegalArgumentException ex) {
            fallar(ex.getMessage());
        } catch (RuntimeException ex) {
            fallar("No se pudo guardar. Revisa si el ID o el correo ya existen.");
        }
    }

    public void eliminar(String idCliente) {
        try {
            clienteService.eliminar(idCliente);
            cargarClientes();
            nuevo();
            informar("Cliente eliminado");
        } catch (RuntimeException ex) {
            fallar("No se pudo eliminar; verifica si el cliente tiene ventas asociadas.");
        }
    }

    private void informar(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
    }

    private void fallar(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }
}
