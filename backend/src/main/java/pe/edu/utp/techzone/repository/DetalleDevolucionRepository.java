package pe.edu.utp.techzone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.techzone.entity.DetalleDevolucion;

import java.util.List;

public interface DetalleDevolucionRepository
        extends JpaRepository<DetalleDevolucion, Long> {

    List<DetalleDevolucion> findByDevolucion_IdDevolucion(
            Long idDevolucion
    );

    @Query("""
    SELECT COALESCE(SUM(d.cantidad), 0)
    FROM DetalleDevolucion d
    WHERE d.detalleVenta.idDetalle = :idDetalleVenta
      AND d.devolucion.estado = 'REGISTRADA'
    """)
long totalDevueltoPorDetalleVenta(
        @Param("idDetalleVenta") Integer idDetalleVenta
);

// =========================================================
// RESUMEN DE CANTIDADES DEVUELTAS POR VENTA
// =========================================================

@Query("""
    SELECT
        d.detalleVenta.idDetalle,
        COALESCE(SUM(d.cantidad), 0)
    FROM DetalleDevolucion d
    WHERE d.detalleVenta.venta.idVenta = :idVenta
      AND d.devolucion.estado = 'REGISTRADA'
    GROUP BY d.detalleVenta.idDetalle
    """)
List<Object[]> obtenerCantidadesDevueltasPorVenta(
        @Param("idVenta") String idVenta
);

}
