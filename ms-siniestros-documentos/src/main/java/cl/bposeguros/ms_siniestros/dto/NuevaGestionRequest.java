package cl.bposeguros.ms_siniestros.dto;

// Nota interna del liquidador
public record NuevaGestionRequest(Long liquidadorId, String descripcion) {
}