package cl.bposeguros.ms_siniestros.dto;


public record AdjuntoRespuesta(Long id, Long siniestroId, String nombreArchivo, String tipoDocumento, String fechaCarga, String cargadoPor, String comentario, Long solicitudId, String urlArchivo) {
}