package cl.bposeguros.ms_catalogos.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.bposeguros.ms_catalogos.config.ManejadorErrores;
import cl.bposeguros.ms_catalogos.model.Comuna;
import cl.bposeguros.ms_catalogos.model.TipoSiniestro;
import cl.bposeguros.ms_catalogos.service.CatalogoService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CatalogosControllerTest {

    @Mock
    private CatalogoService catalogoService;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new CompaniaController(catalogoService),
                        new ComunaController(catalogoService), new TipoSiniestroController(catalogoService))
                .setControllerAdvice(new ManejadorErrores()).build();
    }

    @Test
    void getComunas_responde200ConLaLista() throws Exception {
        when(catalogoService.listarComunas()).thenReturn(List.of(Comuna.builder().id(1L).nombre("Santiago").build()));

        mvc.perform(get("/api/comunas")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Santiago"));
    }

    @Test
    void getCompanias_enviaLosFiltrosAlServicio() throws Exception {
        when(catalogoService.listarCompanias("HOGAR", true)).thenReturn(List.of());

        mvc.perform(get("/api/companias?linea=HOGAR&soloActivas=true")).andExpect(status().isOk());

        verify(catalogoService).listarCompanias("HOGAR", true);
    }

    @Test
    void getCompanias_sinParametros_usaLosValoresPorDefecto() throws Exception {
        mvc.perform(get("/api/companias")).andExpect(status().isOk());

        verify(catalogoService).listarCompanias(null, false);
    }

    @Test
    void getCompania_inexistente_responde404ConMensaje() throws Exception {
        when(catalogoService.obtenerCompania(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Compañía no encontrada"));

        mvc.perform(get("/api/companias/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Compañía no encontrada"));
    }

    @Test
    void getTipos_enviaLaLinea() throws Exception {
        mvc.perform(get("/api/tipos-siniestro?linea=VEHICULO")).andExpect(status().isOk());

        verify(catalogoService).listarTipos("VEHICULO");
    }

    @Test
    void getTipo_existente_responde200() throws Exception {
        when(catalogoService.obtenerTipo(1L)).thenReturn(TipoSiniestro.builder().id(1L).codigo("TS-01").build());

        mvc.perform(get("/api/tipos-siniestro/1")).andExpect(status().isOk()).andExpect(jsonPath("$.codigo").value("TS-01"));
    }
}
