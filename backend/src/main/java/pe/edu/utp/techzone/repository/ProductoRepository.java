package pe.edu.utp.techzone.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.utp.techzone.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository
        extends JpaRepository<Producto, Integer> {

    List<Producto> findAllByOrderByNombreAsc();

    long countByStockLessThanEqual(Integer stock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.idProducto = :id
        """)
    Optional<Producto> buscarPorIdConBloqueo(
            @Param("id") Integer id
    );
}
