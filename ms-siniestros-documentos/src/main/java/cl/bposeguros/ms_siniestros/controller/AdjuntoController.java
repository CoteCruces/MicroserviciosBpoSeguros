package cl.bposeguros.ms_siniestros.controller;

import cl.bposeguros.ms_siniestros.model.DocumentoAdjunto;
import cl.bposeguros.ms_siniestros.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/adjuntos")
@RequiredArgsConstructor
public class AdjuntoController {

    private final DocumentoService documentoService;

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable Long id) {
        DocumentoAdjunto adjunto = documentoService.buscar(id);
        byte[] contenido = documentoService.leerArchivo(adjunto);
        String ruta = adjunto.getRutaArchivo().toLowerCase();
        MediaType tipo = ruta.endsWith(".pdf") ? MediaType.APPLICATION_PDF
                : ruta.endsWith(".png") ? MediaType.IMAGE_PNG
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(contenido);
    }
}