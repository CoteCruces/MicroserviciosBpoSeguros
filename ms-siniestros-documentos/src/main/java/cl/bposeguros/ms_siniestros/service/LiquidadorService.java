package cl.bposeguros.ms_siniestros.service;

import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.ConteoCasosResponse;
import cl.bposeguros.ms_siniestros.dto.HistorialResponse;
import cl.bposeguros.ms_siniestros.dto.SiniestroRespuesta;
import cl.bposeguros.ms_siniestros.dto.SolicitanteRequest;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import cl.bposeguros.ms_siniestros.model.Asignacion;
import cl.bposeguros.ms_siniestros.model.Etapa;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.repository.AsignacionRepository;
import cl.bposeguros.ms_siniestros.repository.SiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class LiquidadorService {

    private static final long ROL_LIQUIDADOR = 2L;

    private final SiniestroRepository siniestroRepository;
    private final AsignacionRepository asignacionRepository;
    private final SiniestroService siniestroService;
    private final SeguimientoService seguimientoService;
    private final UsuarioClient usuarioClient;

    
    public List<ConteoCasosResponse> casosAbiertosPorLiquidador() {
        return usuarioClient.listarPorRol(ROL_LIQUIDADOR).stream()
                .map(u -> new ConteoCasosResponse(u.id(),
                        siniestroRepository.countByLiquidadorIdAndEtapaNot(u.id(), Etapa.CERRADO.getId())))
                .toList();
    }

    
    public List<HistorialResponse> casosActivos(Long liquidadorId) {
        exigirLiquidador(liquidadorId);
        Resolutor resolutor = siniestroService.nuevoResolutor();
        return siniestroRepository.findByLiquidadorIdAndEtapaNot(liquidadorId, Etapa.CERRADO.getId()).stream()
                .filter(s -> s.getFechaAsignacion() != null)
                .sorted(Comparator.comparing(Siniestro::getFechaAsignacion))
                .map(s -> {
                    Optional<Asignacion> abierta = asignacionRepository.findFirstBySiniestroIdAndFechaFinIsNull(s.getId());
                    return aFila(abierta.map(Asignacion::getId).orElse(null), s, s.getFechaAsignacion(), null, null, resolutor);
                })
                .toList();
    }

    
    public List<HistorialResponse> historial(Long liquidadorId, String desde, String hasta, Integer limite) {
        exigirLiquidador(liquidadorId);
        String desdeTexto = (desde == null || desde.isBlank()) ? null : Fechas.parsear(desde, "fecha desde").toString();
        String hastaTexto = (hasta == null || hasta.isBlank()) ? null : Fechas.parsear(hasta, "fecha hasta").toString();
        if (desdeTexto != null && hastaTexto != null && desdeTexto.compareTo(hastaTexto) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha desde no puede ser posterior a la fecha hasta");
        }

        Resolutor resolutor = siniestroService.nuevoResolutor();
        List<HistorialResponse> filas = asignacionRepository.findByLiquidadorIdOrderByFechaInicioDescIdDesc(liquidadorId).stream()
                .filter(a -> hastaTexto == null || a.getFechaInicio().substring(0, 10).compareTo(hastaTexto) <= 0)
                .filter(a -> desdeTexto == null || a.getFechaFin() == null
                        || a.getFechaFin().substring(0, 10).compareTo(desdeTexto) >= 0)
                .map(a -> siniestroRepository.findById(a.getSiniestroId())
                        .map(s -> aFila(a.getId(), s, a.getFechaInicio(), a.getFechaFin(), a.getMotivoFin(), resolutor))
                        .orElse(null))
                .filter(f -> f != null)
                .toList();

        if (limite != null && limite > 0 && filas.size() > limite) {
            return filas.subList(0, limite);
        }
        return filas;
    }

    
    @Transactional
    public List<SiniestroRespuesta> liberarCasos(Long liquidadorId, SolicitanteRequest datos) {
        UsuarioDTO analista = seguimientoService.exigirAnalista(datos.solicitanteId());
        exigirLiquidador(liquidadorId);

        String ahora = Fechas.ahora();
        String responsable = Nombres.responsable(analista.nombre(), analista.codigo());
        Resolutor resolutor = siniestroService.nuevoResolutor();

        List<Siniestro> abiertos = siniestroRepository.findByLiquidadorIdAndEtapaNot(liquidadorId, Etapa.CERRADO.getId());
        for (Siniestro s : abiertos) {
            asignacionRepository.findFirstBySiniestroIdAndFechaFinIsNull(s.getId()).ifPresent(a -> {
                a.setFechaFin(ahora);
                a.setMotivoFin("BLOQUEO");
                asignacionRepository.save(a);
            });
            s.setLiquidadorAnteriorId(liquidadorId);
            s.setLiquidadorId(null);
            s.setFechaAsignacion(null);
            siniestroRepository.save(s);

            seguimientoService.registrarGestion(s.getId(), "Reasignación pendiente",
                    "Tu caso será reasignado a otro liquidador. Te avisaremos cuando esté asignado.", responsable, true);
            seguimientoService.registrarAviso(s.getId(), "Reasignación pendiente",
                    "Tu siniestro " + s.getNumero() + " será reasignado a otro liquidador.");
        }
        return abiertos.stream().map(s -> siniestroService.aRespuesta(s, resolutor)).toList();
    }

    

    private UsuarioDTO exigirLiquidador(Long liquidadorId) {
        UsuarioDTO u = usuarioClient.obtener(liquidadorId);
        if (u == null || u.rolId() == null || u.rolId() != ROL_LIQUIDADOR) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Liquidador no encontrado");
        }
        return u;
    }

    private HistorialResponse aFila(Long asignacionId, Siniestro s, String inicio, String fin, String motivoFin, Resolutor r) {
        UsuarioDTO asegurado = r.usuario(s.getAseguradoId());
        var compania = r.compania(s.getCompaniaId());
        var tipo = r.tipo(s.getTipoId());
        Etapa etapa = Etapa.desdeId(s.getEtapa());
        return new HistorialResponse(
                asignacionId, s.getId(), s.getNumero(),
                asegurado == null ? "" : asegurado.nombre(),
                compania == null ? "" : compania.nombre(),
                tipo == null ? "" : tipo.nombre(),
                etapa.name(), etapa.getEtiqueta(), s.getResultado(),
                inicio, fin, Fechas.diasEntre(inicio, fin), fin == null, motivoFin);
    }
}