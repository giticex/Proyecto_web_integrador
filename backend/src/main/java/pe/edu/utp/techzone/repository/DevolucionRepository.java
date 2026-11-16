package pe.edu.utp.techzone.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.techzone.entity.Devolucion;

public interface DevolucionRepository
        extends JpaRepository<Devolucion, Long> {

    Page<Devolucion> findAllByOrderByFechaRegistroDesc(
            Pageable pageable
    );

    Page<Devolucion> findByUsuario_UsuarioOrderByFechaRegistroDesc(
            String usuario,
            Pageable pageable
    );

    Page<Devolucion> findByVenta_IdVentaOrderByFechaRegistroDesc(
            String idVenta,
            Pageable pageable
    );

    Page<Devolucion> findByVenta_IdVentaAndUsuario_UsuarioOrderByFechaRegistroDesc(
        String idVenta,
        String usuario,
        Pageable pageable
    );

    // REEMBOLSOS AGRUPADOS POR MEDIO DE PAGO Y CAJA
   @Query("""
    SELECT
        d.medioPagoReembolso.idMedioPago,
        COALESCE(SUM(d.montoReembolsado), 0)
    FROM Devolucion d
    WHERE d.caja.idCaja = :idCaja
      AND d.estado = 'REGISTRADA'
    GROUP BY d.medioPagoReembolso.idMedioPago
    """)
List<Object[]> obtenerTotalesReembolsadosPorMedioPagoDeCaja(
        @Param("idCaja") Long idCaja
);

   // Obtener el importe acumulado de las devoluciones de una venta.
@Query("""
    SELECT COALESCE(SUM(d.montoReembolsado), 0)
    FROM Devolucion d
    WHERE d.venta.idVenta = :idVenta
      AND d.estado = 'REGISTRADA'
    """)
BigDecimal totalReembolsadoPorVenta(
        @Param("idVenta") String idVenta
);
}
