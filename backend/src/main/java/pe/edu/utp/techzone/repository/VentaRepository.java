package pe.edu.utp.techzone.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.techzone.entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, String> {

    // =========================================================
    // CONSULTAS EXISTENTES
    // Se conservan para no romper los consumidores actuales.
    // =========================================================

    List<Venta> findByClienteIdClienteOrderByFechaEmisionDesc(
            String idCliente
    );

    List<Venta> findByPersonaIdPersonaOrderByFechaEmisionDesc(
            String idPersona
    );

    List<Venta> findAllByOrderByFechaEmisionDesc();


    // =========================================================
    // PAGINACION SERVER-SIDE
    // =========================================================

    Page<Venta> findAllByOrderByFechaEmisionDesc(
            Pageable pageable
    );

    Page<Venta> findByCliente_IdClienteOrderByFechaEmisionDesc(
            String idCliente,
            Pageable pageable
    );

    Page<Venta> findByPersona_IdPersonaOrderByFechaEmisionDesc(
            String idPersona,
            Pageable pageable
    );


    // =========================================================
    // CORRELATIVO DE COMPROBANTES
    // Compatible con H2 y MySQL.
    // =========================================================

    @Query(value = """
        SELECT COALESCE(
            MAX(
                CAST(
                    SUBSTRING(
                        numero_documento,
                        LOCATE('-', numero_documento) + 1
                    ) AS DECIMAL(20,0)
                )
            ),
            0
        )
        FROM venta
        WHERE numero_documento LIKE CONCAT(:serie, '-%')
        """, nativeQuery = true)
    BigDecimal maxCorrelativoPorSerie(
            @Param("serie") String serie
    );


    // =========================================================
    // PRODUCTO MAS VENDIDO EN UN PERIODO
    // Consulta JPQL compatible con H2 y MySQL.
    // =========================================================

    @Query("""
        SELECT p.nombre
        FROM DetalleVenta d
        JOIN d.producto p
        JOIN d.venta v
        WHERE v.fechaEmision >= :desde
          AND v.fechaEmision < :hasta
        GROUP BY p.nombre
        ORDER BY SUM(d.cantidad) DESC
        """)
    List<String> obtenerProductoMasVendidoEnPeriodo(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );


    // =========================================================
    // TOTAL DE VENTAS EN UN PERIODO
    // =========================================================

    @Query("""
        SELECT COUNT(v)
        FROM Venta v
        WHERE v.fechaEmision >= :desde
          AND v.fechaEmision < :hasta
        """)
    long contarVentasEnPeriodo(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );


    // =========================================================
    // CLIENTES QUE COMPRARON
    // =========================================================

    @Query("""
        SELECT COUNT(DISTINCT v.cliente.idCliente)
        FROM Venta v
        """)
    long contarClientesCompraron();


    // =========================================================
    // INGRESOS EN UN PERIODO
    // =========================================================

    @Query("""
        SELECT COALESCE(SUM(d.subtotal), 0)
        FROM DetalleVenta d
        JOIN d.venta v
        WHERE v.fechaEmision >= :desde
          AND v.fechaEmision < :hasta
        """)
    BigDecimal obtenerIngresosEnPeriodo(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );


    // =========================================================
    // BLOQUEO DE VENTA PARA DEVOLUCIONES
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT v
        FROM Venta v
        WHERE v.idVenta = :idVenta
        """)
    Optional<Venta> buscarPorIdConBloqueo(
            @Param("idVenta") String idVenta
    );


    // =========================================================
    // VENTAS DE UNA CAJA AGRUPADAS POR MEDIO DE PAGO
    // =========================================================

    @Query(value = """
    SELECT
        mp.id_medio_pago,
        COALESCE(SUM(dv.subtotal), 0)
    FROM venta v
    INNER JOIN medio_pago mp
        ON v.id_medio_pago = mp.id_medio_pago
    INNER JOIN detalle_venta dv
        ON v.id_venta = dv.id_venta
    WHERE v.id_caja = :idCaja
    GROUP BY mp.id_medio_pago
    """, nativeQuery = true)
List<Object[]> obtenerTotalesPorMedioPagoDeCaja(
        @Param("idCaja") Long idCaja
);
}
