package cl.bposeguros.ms_catalogos.controller;

import cl.bposeguros.ms_catalogos.model.TipoSiniestro;
import cl.bposeguros.ms_catalogos.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-siniestro")
@RequiredArgsConstructor
public class TipoSiniestroController {

    private final CatalogoService catalogoService;

    @GetMapping
    public List<TipoSiniestro> listar(@RequestParam(required = false) String linea) {
        return catalogoService.listarTipos(linea);
    }

    @GetMapping("/{id}")
    public TipoSiniestro obtener(@PathVariable Long id) {
        return catalogoService.obtenerTipo(id);
    }
}