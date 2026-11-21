package cl.bposeguros.ms_siniestros.dto;

//Lo que devuelve ms-catalogos en /api/tipos-siniestro
public record TipoSiniestroDTO(Long id, String codigo, String nombre, String linea) {
    
}
