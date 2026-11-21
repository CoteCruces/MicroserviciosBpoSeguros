package cl.bposeguros.ms_catalogos.service;

import cl.bposeguros.ms_catalogos.model.Comuna;
import cl.bposeguros.ms_catalogos.model.Compania;
import cl.bposeguros.ms_catalogos.model.TipoSiniestro;
import cl.bposeguros.ms_catalogos.repository.ComunaRepository;
import cl.bposeguros.ms_catalogos.repository.CompaniaRepository;
import cl.bposeguros.ms_catalogos.repository.TipoSiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ComunaRepository comunaRepository;
    private final CompaniaRepository companiaRepository;
    private final TipoSiniestroRepository tipoRepository;


    public List<Comuna> listarComunas() {
        return comunaRepository.findAllByOrderByNombreAsc();
    }


    public List<Compania> listarCompanias(String linea, boolean soloActivas) {
        String filtro = linea == null ? "" : linea.trim().toUpperCase();
        return companiaRepository.findAllByOrderByNombreAsc().stream()
                .filter(c -> !soloActivas || c.isActiva())
                .filter(c -> filtro.isEmpty()
                        || (TipoSiniestro.LINEA_VEHICULO.equals(filtro) && c.isGestionaVehiculos())
                        || (TipoSiniestro.LINEA_HOGAR.equals(filtro) && c.isGestionaHogar()))
                .toList();
    }

    public Compania obtenerCompania(Long id) {
        return companiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compañía no encontrada"));
    }


    public List<TipoSiniestro> listarTipos(String linea) {
        if (linea == null || linea.isBlank()) {
            return tipoRepository.findAllByOrderByIdAsc();
        }
        return tipoRepository.findByLineaOrderByIdAsc(linea.trim().toUpperCase());
    }

    public TipoSiniestro obtenerTipo(Long id) {
        return tipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de siniestro no encontrado"));
    }
}