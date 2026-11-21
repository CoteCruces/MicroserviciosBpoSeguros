package cl.bposeguros.ms_siniestros.repository;

import cl.bposeguros.ms_siniestros.model.Gestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GestionRepository extends JpaRepository<Gestion, Long> {

    List<Gestion> findBySiniestroIdOrderByFechaHoraAscIdAsc(Long siniestroId);
}