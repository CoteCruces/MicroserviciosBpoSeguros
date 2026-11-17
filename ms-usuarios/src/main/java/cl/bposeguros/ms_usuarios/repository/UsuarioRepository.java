package cl.bposeguros.ms_usuarios.repository;

import cl.bposeguros.ms_usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByNombreAsc();

    List<Usuario> findByRolIdOrderByNombreAsc(Long rolId);

    List<Usuario> findByRolId(Long rolId);

    Optional<Usuario> findByRut(String rut);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByRut(String rut);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Long id);
}
