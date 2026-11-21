package cl.bposeguros.ms_siniestros.dto;


public record GestionResponse(Long id, Long siniestroId, String fechaHora, String tipo, String descripcion, String responsable, boolean visibleAsegurado) {
}