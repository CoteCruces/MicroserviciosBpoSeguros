package cl.bposeguros.ms_siniestros.service;

import cl.bposeguros.ms_siniestros.dto.AdjuntoRespuesta;
import cl.bposeguros.ms_siniestros.model.DocumentoAdjunto;
import cl.bposeguros.ms_siniestros.model.Etapa;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.model.SolicitudDocumento;
import cl.bposeguros.ms_siniestros.repository.AdjuntoRepository;
import cl.bposeguros.ms_siniestros.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final AdjuntoRepository adjuntoRepository;
    private final SolicitudRepository solicitudRepository;
    private final AlmacenamientoService almacenamientoService;
    private final SeguimientoService seguimientoService;

    public List<AdjuntoRespuesta> listar(Long siniestroId) {
        seguimientoService.buscarSiniestro(siniestroId);
        return adjuntoRepository.findBySiniestroIdOrderByFechaCargaDescIdDesc(siniestroId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    
    @Transactional
    public AdjuntoRespuesta subir(Long siniestroId, MultipartFile archivo, String tipoDocumento,
                                 String comentario, String cargadoPor, Long solicitudId) {
        Siniestro siniestro = seguimientoService.buscarSiniestro(siniestroId);
        if (siniestro.getEtapa() == Etapa.CERRADO.getId()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El caso está cerrado: ya no se pueden subir documentos");
        }
        if (tipoDocumento == null || tipoDocumento.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica qué tipo de documento es");
        }

        SolicitudDocumento solicitud = null;
        if (solicitudId != null) {
            solicitud = solicitudRepository.findById(solicitudId)
                    .filter(s -> s.getSiniestroId().equals(siniestroId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La solicitud no existe en este caso"));
            if (solicitud.isEntregado()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese documento ya fue entregado");
            }
        }

        String guardado = almacenamientoService.guardar(archivo);
        String ahora = Fechas.ahora();
        String original = archivo.getOriginalFilename() == null || archivo.getOriginalFilename().isBlank()
                ? guardado : archivo.getOriginalFilename();
        String autor = (cargadoPor == null || cargadoPor.isBlank()) ? "Asegurado" : cargadoPor.trim();

        DocumentoAdjunto adjunto = adjuntoRepository.save(DocumentoAdjunto.builder()
                .siniestroId(siniestroId)
                .nombreArchivo(original)
                .rutaArchivo(guardado)
                .tipoDocumento(tipoDocumento.trim())
                .fechaCarga(ahora.substring(0, 10))
                .cargadoPor(autor)
                .comentario(comentario == null ? "" : comentario.trim())
                .solicitudId(solicitudId)
                .build());

        if (solicitud != null) {
            solicitud.setEntregado(true);
            solicitud.setFechaEntrega(ahora);
            solicitudRepository.save(solicitud);
        }
        seguimientoService.registrarGestion(siniestroId, "Documento recibido",
                "Recibimos tu documento: " + tipoDocumento.trim().toLowerCase() + ".", autor, true);
        return aRespuesta(adjunto);
    }

    public DocumentoAdjunto buscar(Long adjuntoId) {
        return adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento no encontrado"));
    }

    public byte[] leerArchivo(DocumentoAdjunto adjunto) {
        if (adjunto.getRutaArchivo() == null || adjunto.getRutaArchivo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Este documento es solo un registro de ejemplo y no tiene archivo");
        }
        return almacenamientoService.leer(adjunto.getRutaArchivo());
    }

    private AdjuntoRespuesta aRespuesta(DocumentoAdjunto a) {
        String url = null;
        if (a.getRutaArchivo() != null && !a.getRutaArchivo().isBlank()) {
            url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/adjuntos/{id}/archivo")
                    .buildAndExpand(a.getId())
                    .toUriString();
        }
        return new AdjuntoRespuesta(a.getId(), a.getSiniestroId(), a.getNombreArchivo(), a.getTipoDocumento(),
                a.getFechaCarga(), a.getCargadoPor(), a.getComentario(), a.getSolicitudId(), url);
    }
}