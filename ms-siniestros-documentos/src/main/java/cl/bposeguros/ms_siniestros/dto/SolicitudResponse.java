package cl.bposeguros.ms_siniestros.dto;


public record SolicitudResponse(Long id, Long siniestroId, String documentoSolicitado, String comentario, String fechaSolicitud, boolean entregado, String fechaEntrega) {
}