package cl.bposeguros.ms_usuarios.dto;


public record ClaveTemporalRequest(String correo, String claveTemporal, String motivo, String rol) {
}
