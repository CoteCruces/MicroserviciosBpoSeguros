package cl.bposeguros.ms_siniestros.repository;

import cl.bposeguros.ms_siniestros.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    List<Asignacion> findByLiquidadorIdOrderByFechaInicioDescIdDesc(Long liquidadorId);

    Optional<Asignacion> findFirstBySiniestroIdAndFechaFinIsNull(Long siniestroId);
}