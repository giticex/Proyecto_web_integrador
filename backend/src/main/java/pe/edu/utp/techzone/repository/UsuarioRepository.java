package pe.edu.utp.techzone.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @EntityGraph(attributePaths = "persona")
    Optional<Usuario> findByUsuario(String usuario);

    @EntityGraph(attributePaths = "persona")
    List<Usuario> findAllByRol(RolUsuario rol);
}
