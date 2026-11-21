package cl.bposeguros.ms_siniestros.dto;


public record NuevaSolicitudRequest(Long liquidadorId, String documento, String comentario) {
}