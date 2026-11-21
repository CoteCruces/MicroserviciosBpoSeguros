package cl.bposeguros.ms_siniestros.repository;

import cl.bposeguros.ms_siniestros.model.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiniestroRepository extends JpaRepository<Siniestro, Long> {

    List<Siniestro> findByAseguradoId(Long aseguradoId);

    List<Siniestro> findByLiquidadorId(Long liquidadorId);

    
    List<Siniestro> findByLiquidadorIdAndEtapaNot(Long liquidadorId, int etapa);

    long countByLiquidadorIdAndEtapaNot(Long liquidadorId, int etapa);

    Optional<Siniestro> findTopByNumeroStartingWithOrderByNumeroDesc(String prefijo);

    /** Para avisar de un posible siniestro duplicado. */
    Optional<Siniestro> findFirstByAseguradoIdAndCompaniaIdAndTipoIdAndFechaOcurrenciaAndEtapaNot(
            Long aseguradoId, Long companiaId, Long tipoId, String fechaOcurrencia, int etapa);
}