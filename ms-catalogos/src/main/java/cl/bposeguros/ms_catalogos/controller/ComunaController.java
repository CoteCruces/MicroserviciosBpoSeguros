package cl.bposeguros.ms_catalogos.controller;

import cl.bposeguros.ms_catalogos.model.Comuna;
import cl.bposeguros.ms_catalogos.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comunas")
@RequiredArgsConstructor
public class ComunaController {

    private final CatalogoService catalogoService;

    @GetMapping
    public List<Comuna> listar() {
        return catalogoService.listarComunas();
    }
}