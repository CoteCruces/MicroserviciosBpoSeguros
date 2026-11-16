package cl.bposeguros.ms_notificaciones.dto;

/**
 * JSON que recibe el servicio. Lo envía msUsuarios (o la app en las pruebas).
 * motivo: "CREACION" (cuenta nueva) o "RECUPERACION" (olvidó su clave).
 */
public record ClaveTemporalRequest(String correo, String claveTemporal, String motivo) {
}
