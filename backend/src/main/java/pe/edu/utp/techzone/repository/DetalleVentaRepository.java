package pe.edu.utp.techzone.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.techzone.entity.DetalleVenta;

import java.util.List;
import java.util.Optional;

public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByVentaIdVenta(String idVenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT d
        FROM DetalleVenta d
        WHERE d.idDetalle = :id
        """)
    Optional<DetalleVenta> buscarPorIdConBloqueo(
            @Param("id") Integer id
    );
}
