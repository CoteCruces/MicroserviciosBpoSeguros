package cl.bposeguros.ms_siniestros.repository;

import cl.bposeguros.ms_siniestros.model.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    List<Aviso> findBySiniestroIdIn(Collection<Long> siniestroIds);
}