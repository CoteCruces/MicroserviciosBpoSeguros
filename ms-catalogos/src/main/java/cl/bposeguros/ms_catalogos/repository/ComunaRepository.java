package cl.bposeguros.ms_catalogos.repository;

import cl.bposeguros.ms_catalogos.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {

    List<Comuna> findAllByOrderByNombreAsc();
}