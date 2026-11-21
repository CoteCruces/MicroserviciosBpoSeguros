package cl.bposeguros.ms_siniestros.service;

import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.AvisoRespuesta;
import cl.bposeguros.ms_siniestros.dto.GestionResponse;
import cl.bposeguros.ms_siniestros.dto.NuevaGestionRequest;
import cl.bposeguros.ms_siniestros.dto.NuevaSolicitudRequest;
import cl.bposeguros.ms_siniestros.dto.SolicitudResponse;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import cl.bposeguros.ms_siniestros.model.Aviso;
import cl.bposeguros.ms_siniestros.model.Gestion;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.model.SolicitudDocumento;
import cl.bposeguros.ms_siniestros.repository.AvisoRepository;
import cl.bposeguros.ms_siniestros.repository.GestionRepository;
import cl.bposeguros.ms_siniestros.repository.SiniestroRepository;
import cl.bposeguros.ms_siniestros.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final GestionRepository gestionRepository;
    private final SolicitudRepository solicitudRepository;
    private final AvisoRepository avisoRepository;
    private final SiniestroRepository siniestroRepository;
    private final UsuarioClient usuarioClient;

    

    public void registrarGestion(Long siniestroId, String tipo, String descripcion, String responsable, boolean visible) {
        gestionRepository.save(Gestion.builder()
                .siniestroId(siniestroId)
                .fechaHora(Fechas.ahora())
                .tipo(tipo)
                .descripcion(descripcion)
                .responsable(responsable)
                .visibleAsegurado(visible)
                .build());
    }

    public void registrarAviso(Long siniestroId, String motivo, String mensaje) {
        avisoRepository.save(Aviso.builder()
                .siniestroId(siniestroId)
                .fechaHora(Fechas.ahora())
                .motivo(motivo)
                .mensaje(mensaje)
                .leido(false)
                .build());
    }

    

    public List<GestionResponse> listarGestiones(Long siniestroId, boolean soloVisibles) {
        buscarSiniestro(siniestroId);
        return gestionRepository.findBySiniestroIdOrderByFechaHoraAscIdAsc(siniestroId).stream()
                .filter(g -> !soloVisibles || g.isVisibleAsegurado())
                .map(this::aRespuesta)
                .toList();
    }

    
    @Transactional
    public GestionResponse crearGestion(Long siniestroId, NuevaGestionRequest datos) {
        Siniestro siniestro = buscarSiniestro(siniestroId);
        UsuarioDTO liquidador = exigirLiquidadorDelCaso(siniestro, datos.liquidadorId());

        String descripcion = datos.descripcion() == null ? "" : datos.descripcion().trim();
        if (descripcion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Describe lo que hiciste");
        }
        if (descripcion.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe un poco más (mínimo 10 caracteres)");
        }
        Gestion gestion = gestionRepository.save(Gestion.builder()
                .siniestroId(siniestroId)
                .fechaHora(Fechas.ahora())
                .tipo(Gestion.TIPO_NOTA_LIQUIDADOR)
                .descripcion(descripcion)
                .responsable(Nombres.responsable(liquidador.nombre(), liquidador.codigo()))
                .visibleAsegurado(false)
                .build());
        return aRespuesta(gestion);
    }

    

    public List<SolicitudResponse> listarSolicitudes(Long siniestroId) {
        buscarSiniestro(siniestroId);
        return solicitudRepository.findBySiniestroIdOrderByEntregadoAscFechaSolicitudAscIdAsc(siniestroId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public SolicitudResponse solicitarDocumento(Long siniestroId, NuevaSolicitudRequest datos) {
        Siniestro siniestro = buscarSiniestro(siniestroId);
        UsuarioDTO liquidador = exigirLiquidadorDelCaso(siniestro, datos.liquidadorId());

        String documento = datos.documento() == null ? "" : datos.documento().trim();
        if (documento.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige qué documento necesitas");
        }
        if (solicitudRepository.existsBySiniestroIdAndDocumentoSolicitadoAndEntregadoFalse(siniestroId, documento)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya pediste este documento y el asegurado aún no lo entrega");
        }
        String comentario = datos.comentario() == null ? "" : datos.comentario().trim();
        String ahora = Fechas.ahora();

        SolicitudDocumento solicitud = solicitudRepository.save(SolicitudDocumento.builder()
                .siniestroId(siniestroId)
                .documentoSolicitado(documento)
                .comentario(comentario)
                .fechaSolicitud(ahora.substring(0, 10))
                .entregado(false)
                .build());

        String texto = "Necesitamos: " + documento.toLowerCase() + "." + (comentario.isEmpty() ? "" : " " + comentario);
        registrarGestion(siniestroId, "Solicitud de documento", texto,
                Nombres.responsable(liquidador.nombre(), liquidador.codigo()), true);
        registrarAviso(siniestroId, "Solicitud de documento",
                "Necesitamos " + documento.toLowerCase() + " para tu siniestro " + siniestro.getNumero() + ".");
        return aRespuesta(solicitud);
    }

    

    public List<AvisoRespuesta> listarAvisos(Long aseguradoId) {
        List<Siniestro> siniestros = siniestroRepository.findByAseguradoId(aseguradoId);
        Map<Long, String> numeros = siniestros.stream()
                .collect(Collectors.toMap(Siniestro::getId, Siniestro::getNumero));
        if (numeros.isEmpty()) {
            return List.of();
        }
        return avisoRepository.findBySiniestroIdIn(numeros.keySet()).stream()
                .sorted(Comparator.comparing(Aviso::getFechaHora).thenComparing(Aviso::getId).reversed())
                .map(a -> new AvisoRespuesta(a.getId(), a.getSiniestroId(), numeros.get(a.getSiniestroId()),
                        a.getFechaHora(), a.getMotivo(), a.getMensaje(), a.isLeido()))
                .toList();
    }

    @Transactional
    public void marcarAvisoLeido(Long avisoId) {
        Aviso aviso = avisoRepository.findById(avisoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aviso no encontrado"));
        aviso.setLeido(true);
        avisoRepository.save(aviso);
    }

    

    
    public String ultimaActualizacion(Long siniestroId) {
        return gestionRepository.findBySiniestroIdOrderByFechaHoraAscIdAsc(siniestroId).stream()
                .filter(Gestion::isVisibleAsegurado)
                .map(Gestion::getFechaHora)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    
    public UsuarioDTO exigirAnalista(Long solicitanteId) {
        if (solicitanteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta indicar quién realiza la acción");
        }
        UsuarioDTO solicitante = usuarioClient.obtener(solicitanteId);
        if (solicitante == null || !solicitante.activo() || solicitante.rolId() == null || solicitante.rolId() != 3L) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
        }
        return solicitante;
    }

    public Siniestro buscarSiniestro(Long id) {
        return siniestroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Siniestro no encontrado"));
    }

    
    public UsuarioDTO exigirLiquidadorDelCaso(Siniestro siniestro, Long liquidadorId) {
        if (liquidadorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta indicar quién realiza la acción");
        }
        if (!liquidadorId.equals(siniestro.getLiquidadorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este caso no está asignado a ti");
        }
        UsuarioDTO liquidador = usuarioClient.obtener(liquidadorId);
        if (liquidador == null || !liquidador.activo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
        }
        return liquidador;
    }

    private GestionResponse aRespuesta(Gestion g) {
        return new GestionResponse(g.getId(), g.getSiniestroId(), g.getFechaHora(), g.getTipo(),
                g.getDescripcion(), g.getResponsable(), g.isVisibleAsegurado());
    }

    private SolicitudResponse aRespuesta(SolicitudDocumento s) {
        return new SolicitudResponse(s.getId(), s.getSiniestroId(), s.getDocumentoSolicitado(), s.getComentario(),
                s.getFechaSolicitud(), s.isEntregado(), s.getFechaEntrega());
    }
}