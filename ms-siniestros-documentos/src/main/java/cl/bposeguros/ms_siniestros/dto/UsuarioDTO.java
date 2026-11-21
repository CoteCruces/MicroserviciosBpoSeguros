package cl.bposeguros.ms_siniestros.dto;

public record UsuarioDTO (
        Long id,
        String rut,
        String nombre,
        String codigo,
        String equipo,
        boolean activo,
        Long rolId
){
    
}
