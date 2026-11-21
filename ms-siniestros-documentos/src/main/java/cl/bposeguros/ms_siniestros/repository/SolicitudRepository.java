package cl.bposeguros.ms_siniestros.repository;

import cl.bposeguros.ms_siniestros.model.SolicitudDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<SolicitudDocumento, Long> {

    List<SolicitudDocumento> findBySiniestroIdOrderByEntregadoAscFechaSolicitudAscIdAsc(Long siniestroId);

    long countBySiniestroIdAndEntregadoFalse(Long siniestroId);

    boolean existsBySiniestroIdAndDocumentoSolicitadoAndEntregadoFalse(Long siniestroId, String documentoSolicitado);
}