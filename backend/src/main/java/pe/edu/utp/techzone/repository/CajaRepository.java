package pe.edu.utp.techzone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.techzone.entity.Caja;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    Optional<Caja> findFirstByUsuario_IdUsuarioAndEstadoOrderByFechaAperturaDesc(
            Integer idUsuario,
            String estado
    );

    boolean existsByUsuario_IdUsuarioAndEstado(
            Integer idUsuario,
            String estado
    );

    Page<Caja> findAllByOrderByFechaAperturaDesc(Pageable pageable);

    Page<Caja> findByUsuario_IdUsuarioOrderByFechaAperturaDesc(
            Integer idUsuario,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Caja> findByIdCajaAndUsuario_Usuario(
        Long idCaja,
        String usuario
    );
}
