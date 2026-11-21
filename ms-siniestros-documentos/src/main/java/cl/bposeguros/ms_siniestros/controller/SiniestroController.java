package cl.bposeguros.ms_siniestros.controller;


import cl.bposeguros.ms_siniestros.dto.AdjuntoRespuesta;
import cl.bposeguros.ms_siniestros.dto.AsignarSiniestro;
import cl.bposeguros.ms_siniestros.dto.AvanzarEtapaSiniestro;
import cl.bposeguros.ms_siniestros.dto.LiquidadorPropuesto;
import cl.bposeguros.ms_siniestros.dto.RespuestaCerrarSiniestro;
import cl.bposeguros.ms_siniestros.dto.GestionResponse;
import cl.bposeguros.ms_siniestros.dto.NuevaGestionRequest;
import cl.bposeguros.ms_siniestros.dto.NuevaSolicitudRequest;
import cl.bposeguros.ms_siniestros.dto.ReportarSiniestroRequest;
import cl.bposeguros.ms_siniestros.dto.SiniestroRespuesta;
import cl.bposeguros.ms_siniestros.dto.SolicitudResponse;
import cl.bposeguros.ms_siniestros.service.DocumentoService;
import cl.bposeguros.ms_siniestros.service.SeguimientoService;
import cl.bposeguros.ms_siniestros.service.SiniestroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
@RequiredArgsConstructor
public class SiniestroController {

    private final SiniestroService siniestroService;
    private final SeguimientoService seguimientoService;
    private final DocumentoService documentoService;

    @GetMapping
    public List<SiniestroRespuesta> listar(@RequestParam(required = false) Long aseguradoId,
                                          @RequestParam(required = false) Long liquidadorId,
                                          @RequestParam(defaultValue = "false") boolean sinAsignar,
                                          @RequestParam(defaultValue = "false") boolean porReasignar,
                                          @RequestParam(defaultValue = "false") boolean soloAbiertos,
                                          @RequestParam(required = false) String desde,
                                          @RequestParam(required = false) String hasta,
                                          @RequestParam(required = false) String buscar,
                                          @RequestParam(required = false) String orden,
                                          @RequestParam(required = false) Integer limite) {
        return siniestroService.listar(aseguradoId, liquidadorId, sinAsignar, porReasignar, soloAbiertos,
                desde, hasta, buscar, orden, limite);
    }

    @GetMapping("/{id}")
    public SiniestroRespuesta obtener(@PathVariable Long id,
                                     @RequestParam(required = false) Long aseguradoId,
                                     @RequestParam(required = false) Long liquidadorId) {
        return siniestroService.obtener(id, aseguradoId, liquidadorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SiniestroRespuesta reportar(@RequestBody ReportarSiniestroRequest datos) {
        return siniestroService.reportar(datos);
    }

    @GetMapping("/{id}/candidatos")
    public List<LiquidadorPropuesto> candidatos(@PathVariable Long id) {
        return siniestroService.listarCandidatos(id);
    }

    @PutMapping("/{id}/asignacion")
    public SiniestroRespuesta asignar(@PathVariable Long id, @RequestBody AsignarSiniestro datos) {
        return siniestroService.asignar(id, datos);
    }

    @PutMapping("/{id}/etapa")
    public SiniestroRespuesta avanzarEtapa(@PathVariable Long id, @RequestBody AvanzarEtapaSiniestro datos) {
        return siniestroService.avanzarEtapa(id, datos);
    }

    @PutMapping("/{id}/cierre")
    public SiniestroRespuesta cerrar(@PathVariable Long id, @RequestBody RespuestaCerrarSiniestro datos) {
        return siniestroService.cerrar(id, datos);
    }


    @GetMapping("/{id}/gestiones")
    public List<GestionResponse> gestiones(@PathVariable Long id,
                                           @RequestParam(defaultValue = "false") boolean soloVisibles) {
        return seguimientoService.listarGestiones(id, soloVisibles);
    }

    @PostMapping("/{id}/gestiones")
    @ResponseStatus(HttpStatus.CREATED)
    public GestionResponse crearGestion(@PathVariable Long id, @RequestBody NuevaGestionRequest datos) {
        return seguimientoService.crearGestion(id, datos);
    }


    @GetMapping("/{id}/solicitudes")
    public List<SolicitudResponse> solicitudes(@PathVariable Long id) {
        return seguimientoService.listarSolicitudes(id);
    }

    @PostMapping("/{id}/solicitudes")
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponse solicitarDocumento(@PathVariable Long id, @RequestBody NuevaSolicitudRequest datos) {
        return seguimientoService.solicitarDocumento(id, datos);
    }


    @GetMapping("/{id}/adjuntos")
    public List<AdjuntoRespuesta> adjuntos(@PathVariable Long id) {
        return documentoService.listar(id);
    }

    @PostMapping(value = "/{id}/adjuntos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AdjuntoRespuesta subirAdjunto(@PathVariable Long id,
                                        @RequestParam("archivo") MultipartFile archivo,
                                        @RequestParam String tipoDocumento,
                                        @RequestParam(required = false) String comentario,
                                        @RequestParam(required = false) String cargadoPor,
                                        @RequestParam(required = false) Long solicitudId) {
        return documentoService.subir(id, archivo, tipoDocumento, comentario, cargadoPor, solicitudId);
    }
}