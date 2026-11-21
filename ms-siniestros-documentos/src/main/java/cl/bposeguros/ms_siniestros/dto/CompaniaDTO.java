package cl.bposeguros.ms_siniestros.dto;


public record CompaniaDTO(
        Long id,
        String codigo,
        String nombre,
        boolean gestionaVehiculos,
        boolean gestionaHogar,
        int plazoDias,
        boolean activa
) {
}