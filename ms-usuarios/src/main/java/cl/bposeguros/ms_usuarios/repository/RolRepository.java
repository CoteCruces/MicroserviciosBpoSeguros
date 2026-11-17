package cl.bposeguros.ms_usuarios.repository;

import cl.bposeguros.ms_usuarios.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
}
