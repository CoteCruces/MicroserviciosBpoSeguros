package cl.bposeguros.ms_catalogos.repository;

import cl.bposeguros.ms_catalogos.model.TipoSiniestro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoSiniestroRepository extends JpaRepository<TipoSiniestro, Long> {

    List<TipoSiniestro> findAllByOrderByIdAsc();

    List<TipoSiniestro> findByLineaOrderByIdAsc(String linea);
}