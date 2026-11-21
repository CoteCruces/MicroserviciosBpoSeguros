package cl.bposeguros.ms_catalogos.repository;

import cl.bposeguros.ms_catalogos.model.Compania;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompaniaRepository extends JpaRepository<Compania, Long> {

    List<Compania> findAllByOrderByNombreAsc();
}