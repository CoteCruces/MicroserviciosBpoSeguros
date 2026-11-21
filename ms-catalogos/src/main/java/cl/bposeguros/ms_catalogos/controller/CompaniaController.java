package cl.bposeguros.ms_catalogos.controller;

import cl.bposeguros.ms_catalogos.model.Compania;
import cl.bposeguros.ms_catalogos.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companias")
@RequiredArgsConstructor
public class CompaniaController {

    private final CatalogoService catalogoService;

    @GetMapping
    public List<Compania> listar(@RequestParam(required = false) String linea,
                                 @RequestParam(defaultValue = "false") boolean soloActivas) {
        return catalogoService.listarCompanias(linea, soloActivas);
    }

    @GetMapping("/{id}")
    public Compania obtener(@PathVariable Long id) {
        return catalogoService.obtenerCompania(id);
    }
}
