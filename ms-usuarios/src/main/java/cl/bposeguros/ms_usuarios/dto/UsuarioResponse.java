package cl.bposeguros.ms_usuarios.dto;


public record UsuarioResponse(
        Long id,
        String rut,
        String nombre,
        String correo,
        String comuna,
        String telefono,
        String fechaNacimiento,
        String codigo,
        String equipo,
        boolean activo,
        boolean claveTemporal,
        boolean perfilCompleto,
        Long rolId,
        String rolNombre,
        String avatarUrl
) {
}
