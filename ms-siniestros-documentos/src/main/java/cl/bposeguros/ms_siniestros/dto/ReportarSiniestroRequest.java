package cl.bposeguros.ms_siniestros.dto;


public record ReportarSiniestroRequest(Long aseguradoId,
        Long companiaId,
        Long tipoId,
        String fechaOcurrencia,
        String comuna,
        String descripcion) {
}