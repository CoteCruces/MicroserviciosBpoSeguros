package cl.bposeguros.ms_siniestros.repository;

import cl.bposeguros.ms_siniestros.model.DocumentoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdjuntoRepository extends JpaRepository<DocumentoAdjunto, Long> {

    List<DocumentoAdjunto> findBySiniestroIdOrderByFechaCargaDescIdDesc(Long siniestroId);

    long countBySiniestroId(Long siniestroId);
}