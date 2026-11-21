package cl.bposeguros.ms_siniestros.dto;

//Para acciones que solo puede hacer un analista (ej. liberar los casos de un liquidador bloqueado)
public record SolicitanteRequest(Long solicitanteId) {
    
}
