package cl.bposeguros.ms_siniestros.dto;


public record HistorialResponse(Long asignacionId,
        Long siniestroId,
        String numero,
        String aseguradoNombre,
        String companiaNombre,
        String tipoNombre,
        String etapa,
        String etapaEtiqueta,
        String resultado,
        String fechaInicio,
        String fechaFin,
        long diasTrabajados,
        boolean vigente,
        String motivoFin) {
}