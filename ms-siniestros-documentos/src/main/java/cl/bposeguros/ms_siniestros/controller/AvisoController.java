package cl.bposeguros.ms_siniestros.controller;

import cl.bposeguros.ms_siniestros.dto.AvisoRespuesta;
import cl.bposeguros.ms_siniestros.service.SeguimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/avisos")
@RequiredArgsConstructor
public class AvisoController {

    private final SeguimientoService seguimientoService;

    @GetMapping
    public List<AvisoRespuesta> listar(@RequestParam Long aseguradoId) {
        return seguimientoService.listarAvisos(aseguradoId);
    }

    @PutMapping("/{id}/leido")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLeido(@PathVariable Long id) {
        seguimientoService.marcarAvisoLeido(id);
    }
}