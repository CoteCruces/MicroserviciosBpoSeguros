package cl.bposeguros.ms_siniestros.dto;


public record AvisoRespuesta(Long id, Long siniestroId, String numeroSiniestro, String fechaHora, String motivo, String mensaje, boolean leido) {
}