package pe.edu.utp.techzone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
