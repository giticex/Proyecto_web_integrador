package pe.edu.utp.techzone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.techzone.dto.AbrirCajaRequest;
import pe.edu.utp.techzone.dto.CajaDTO;
import pe.edu.utp.techzone.dto.CerrarCajaRequest;

import pe.edu.utp.techzone.entity.Caja;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.Usuario;

import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.exception.NotFoundException;

import pe.edu.utp.techzone.repository.CajaRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;
import pe.edu.utp.techzone.repository.VentaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CajaService {

    private static final String ABIERTA = "ABIERTA";
    private static final String CERRADA = "CERRADA";

    private static final BigDecimal CERO = new BigDecimal("0.00");

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;


    // =========================================================
    // 1. ABRIR CAJA
    // =========================================================

    @Transactional
    public CajaDTO abrirCaja(AbrirCajaRequest request, String nombreUsuario) {

        // Bloquea el registro del usuario para evitar
        // aperturas simultaneas de una misma persona.
        Usuario usuario = usuarioRepository
                .findByUsuarioForUpdate(nombreUsuario)
                .orElseThrow(() ->
                        new NotFoundException("Usuario autenticado no encontrado"));

        validarUsuarioOperativo(usuario);

        boolean tieneCajaAbierta = cajaRepository
                .existsByUsuario_IdUsuarioAndEstado(
                        usuario.getIdUsuario(),
                        ABIERTA
                );

        if (tieneCajaAbierta) {
            throw new BusinessException(
                    "El usuario ya tiene una caja abierta."
            );
        }

        Caja caja = Caja.builder()
                .usuario(usuario)
                .fechaApertura(LocalDateTime.now())
                .fondoInicial(request.getFondoInicial().setScale(2, RoundingMode.HALF_UP))
                .observacionesApertura(request.getObservaciones())
                .estado(ABIERTA)
                .build();

        return convertirDTO(cajaRepository.save(caja));
    }


    // =========================================================
    // 2. CONSULTAR CAJA ACTUAL
    // =========================================================

    @Transactional(readOnly = true)
    public CajaDTO obtenerCajaActual(String nombreUsuario) {

        Usuario usuario = obtenerUsuario(nombreUsuario);

        validarUsuarioOperativo(usuario);

        Caja caja = cajaRepository
                .findFirstByUsuario_IdUsuarioAndEstadoOrderByFechaAperturaDesc(
                        usuario.getIdUsuario(),
                        ABIERTA
                )
                .orElseThrow(() ->
                        new NotFoundException("No existe una caja abierta."));

        return convertirDTO(caja);
    }


    // =========================================================
    // 3. CERRAR CAJA
    // =========================================================

    @Transactional
    public CajaDTO cerrarCaja(
            Long idCaja,
            CerrarCajaRequest request,
            String nombreUsuario
    ) {

        // Bloqueo de la caja y validacion de propiedad.
        Caja caja = cajaRepository
                .findByIdCajaAndUsuario_Usuario(idCaja, nombreUsuario)
                .orElseThrow(() ->
                        new NotFoundException(
                                "No se encontro la caja del usuario autenticado."
                        ));

        if (!ABIERTA.equals(caja.getEstado())) {
            throw new BusinessException(
                    "La caja ya se encuentra cerrada."
            );
        }

        // Calcular ventas reales asociadas a esta caja.
        calcularVentasEsperadas(caja);

        // Registrar cantidades contadas.
        caja.setEfectivoContado(request.getEfectivoContado());
        caja.setTarjetaContada(request.getTarjetaContada());
        caja.setTransferenciaContada(request.getTransferenciaContada());
        caja.setOtrosContados(request.getOtrosContados());

        // Calcular diferencias: fisico - sistema.
        caja.setDiferenciaEfectivo(
                diferencia(request.getEfectivoContado(), caja.getEfectivoEsperado())
        );

        caja.setDiferenciaTarjeta(
                diferencia(request.getTarjetaContada(), caja.getTarjetaEsperada())
        );

        caja.setDiferenciaTransferencia(
                diferencia(request.getTransferenciaContada(), caja.getTransferenciaEsperada())
        );

        caja.setDiferenciaOtros(
                diferencia(request.getOtrosContados(), caja.getOtrosEsperados())
        );

        caja.setFechaCierre(LocalDateTime.now());
        caja.setObservacionesCierre(request.getObservaciones());
        caja.setEstado(CERRADA);

        return convertirDTO(cajaRepository.save(caja));
    }


    // =========================================================
    // 4. CALCULAR VENTAS ESPERADAS POR MEDIO DE PAGO
    // =========================================================

    private void calcularVentasEsperadas(Caja caja) {

        BigDecimal efectivo = CERO;
        BigDecimal tarjeta = CERO;
        BigDecimal transferencia = CERO;
        BigDecimal otros = CERO;

        List<Object[]> resultados =
                ventaRepository.obtenerTotalesPorMedioPagoDeCaja(
                        caja.getIdCaja()
                );

        for (Object[] fila : resultados) {

            String medioPago = String.valueOf(fila[0])
                    .toLowerCase(Locale.ROOT);

            BigDecimal total = convertirDecimal(fila[1]);

            if (medioPago.contains("efectivo")) {

                efectivo = efectivo.add(total);

            } else if (
                    medioPago.contains("tarjeta")
                    || medioPago.contains("visa")
                    || medioPago.contains("mastercard")
                    || medioPago.contains("american express")
            ) {

                tarjeta = tarjeta.add(total);

            } else if (
                    medioPago.contains("yape")
                    || medioPago.contains("plin")
                    || medioPago.contains("transferencia")
                    || medioPago.contains("deposito")
                    || medioPago.contains("depósito")
                    || medioPago.contains("billetera")
                    || medioPago.contains("paypal")
            ) {

                transferencia = transferencia.add(total);

            } else {

                otros = otros.add(total);
            }
        }

        // El fondo inicial forma parte del efectivo esperado.
        caja.setEfectivoEsperado(
                caja.getFondoInicial().add(efectivo)
        );

        caja.setTarjetaEsperada(tarjeta);
        caja.setTransferenciaEsperada(transferencia);
        caja.setOtrosEsperados(otros);
    }


    // =========================================================
    // 5. HISTORIAL PAGINADO
    // =========================================================

    @Transactional(readOnly = true)
    public Page<CajaDTO> listarHistorial(
            String nombreUsuario,
            Pageable pageable
    ) {

        Usuario usuario = obtenerUsuario(nombreUsuario);

        if (usuario.getRol() == RolUsuario.ADMIN) {

            return cajaRepository
                    .findAllByOrderByFechaAperturaDesc(pageable)
                    .map(this::convertirDTO);
        }

        validarUsuarioOperativo(usuario);

        return cajaRepository
                .findByUsuario_IdUsuarioOrderByFechaAperturaDesc(
                        usuario.getIdUsuario(),
                        pageable
                )
                .map(this::convertirDTO);
    }


    // =========================================================
    // 6. VALIDACIONES Y UTILIDADES
    // =========================================================

    private Usuario obtenerUsuario(String nombreUsuario) {

        return usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() ->
                        new NotFoundException("Usuario no encontrado."));
    }


    private void validarUsuarioOperativo(Usuario usuario) {

        if (
                usuario.getRol() != RolUsuario.ADMIN
                && usuario.getRol() != RolUsuario.VENDEDOR
        ) {
            throw new BusinessException(
                    "El usuario no tiene permisos para operar una caja."
            );
        }
    }


    private BigDecimal diferencia(
            BigDecimal contado,
            BigDecimal esperado
    ) {

        return contado.subtract(esperado)
                .setScale(2, RoundingMode.HALF_UP);
    }


    private BigDecimal convertirDecimal(Object valor) {

        if (valor == null) {
            return CERO;
        }

        if (valor instanceof BigDecimal decimal) {
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }

        return new BigDecimal(valor.toString())
                .setScale(2, RoundingMode.HALF_UP);
    }


    // =========================================================
    // 7. CONVERTIR ENTIDAD A DTO
    // =========================================================

    private CajaDTO convertirDTO(Caja caja) {

        Usuario usuario = caja.getUsuario();

        String cajero = usuario.getPersona().getNombre()
                + " "
                + usuario.getPersona().getApellido();

        return CajaDTO.builder()
                .idCaja(caja.getIdCaja())
                .idUsuario(usuario.getIdUsuario())
                .cajero(cajero)

                .fechaApertura(caja.getFechaApertura())
                .fechaCierre(caja.getFechaCierre())

                .fondoInicial(caja.getFondoInicial())

                .efectivoEsperado(caja.getEfectivoEsperado())
                .tarjetaEsperada(caja.getTarjetaEsperada())
                .transferenciaEsperada(caja.getTransferenciaEsperada())
                .otrosEsperados(caja.getOtrosEsperados())

                .efectivoContado(caja.getEfectivoContado())
                .tarjetaContada(caja.getTarjetaContada())
                .transferenciaContada(caja.getTransferenciaContada())
                .otrosContados(caja.getOtrosContados())

                .diferenciaEfectivo(caja.getDiferenciaEfectivo())
                .diferenciaTarjeta(caja.getDiferenciaTarjeta())
                .diferenciaTransferencia(caja.getDiferenciaTransferencia())
                .diferenciaOtros(caja.getDiferenciaOtros())

                .observacionesApertura(caja.getObservacionesApertura())
                .observacionesCierre(caja.getObservacionesCierre())

                .estado(caja.getEstado())
                .build();
    }
}

