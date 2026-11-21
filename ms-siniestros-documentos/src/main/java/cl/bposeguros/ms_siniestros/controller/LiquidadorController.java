package cl.bposeguros.ms_siniestros.controller;

import cl.bposeguros.ms_siniestros.dto.ConteoCasosResponse;
import cl.bposeguros.ms_siniestros.dto.HistorialResponse;
import cl.bposeguros.ms_siniestros.dto.SiniestroRespuesta;
import cl.bposeguros.ms_siniestros.dto.SolicitanteRequest;
import cl.bposeguros.ms_siniestros.service.LiquidadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/liquidadores")
@RequiredArgsConstructor
public class LiquidadorController {

    private final LiquidadorService liquidadorService;

    @GetMapping("/casos-abiertos")
    public List<ConteoCasosResponse> casosAbiertos() {
        return liquidadorService.casosAbiertosPorLiquidador();
    }

    @GetMapping("/{id}/casos-activos")
    public List<HistorialResponse> casosActivos(@PathVariable Long id) {
        return liquidadorService.casosActivos(id);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialResponse> historial(@PathVariable Long id,
                                             @RequestParam(required = false) String desde,
                                             @RequestParam(required = false) String hasta,
                                             @RequestParam(required = false) Integer limite) {
        return liquidadorService.historial(id, desde, hasta, limite);
    }

    @PostMapping("/{id}/liberar-casos")
    public List<SiniestroRespuesta> liberarCasos(@PathVariable Long id, @RequestBody SolicitanteRequest datos) {
        return liquidadorService.liberarCasos(id, datos);
    }
}