package pe.edu.utp.techzone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.Usuario;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsuario(String usuario);

    List<Usuario> findAllByRol(RolUsuario rol);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.usuario = :usuario")
    Optional<Usuario> findByUsuarioForUpdate(
        @Param("usuario") String usuario
    );
}
