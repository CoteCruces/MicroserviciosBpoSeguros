package cl.bposeguros.ms_usuarios.dto;


public record CrearLiquidadorRequest(Long solicitanteId, String rut, String nombre, String correo, String equipo) {
}
