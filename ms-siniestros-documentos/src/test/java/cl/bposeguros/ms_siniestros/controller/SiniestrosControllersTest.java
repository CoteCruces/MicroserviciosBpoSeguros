package cl.bposeguros.ms_siniestros.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.bposeguros.ms_siniestros.config.ManejadorErrores;
import cl.bposeguros.ms_siniestros.dto.AsignarSiniestro;
import cl.bposeguros.ms_siniestros.dto.AvanzarEtapaSiniestro;
import cl.bposeguros.ms_siniestros.dto.RespuestaCerrarSiniestro;
import cl.bposeguros.ms_siniestros.dto.SolicitanteRequest;
import cl.bposeguros.ms_siniestros.model.DocumentoAdjunto;
import cl.bposeguros.ms_siniestros.service.DocumentoService;
import cl.bposeguros.ms_siniestros.service.LiquidadorService;
import cl.bposeguros.ms_siniestros.service.SeguimientoService;
import cl.bposeguros.ms_siniestros.service.SiniestroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SiniestrosControllersTest {

    @Mock
    private SiniestroService siniestroService;

    @Mock
    private SeguimientoService seguimientoService;

    @Mock
    private DocumentoService documentoService;

    @Mock
    private LiquidadorService liquidadorService;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(
                        new SiniestroController(siniestroService, seguimientoService, documentoService),
                        new LiquidadorController(liquidadorService), new AvisoController(seguimientoService),
                        new AdjuntoController(documentoService))
                .setControllerAdvice(new ManejadorErrores()).build();
    }

    private void verArchivo(String ruta, MediaType tipo) throws Exception {
        DocumentoAdjunto adjunto = DocumentoAdjunto.builder().id(1L).rutaArchivo(ruta).build();
        when(documentoService.buscar(1L)).thenReturn(adjunto);
        when(documentoService.leerArchivo(adjunto)).thenReturn(new byte[]{1});

        mvc.perform(get("/api/adjuntos/1/archivo")).andExpect(status().isOk()).andExpect(content().contentType(tipo));
    }

    @Test
    void getSiniestros_enviaLosFiltros() throws Exception {
        mvc.perform(get("/api/siniestros?aseguradoId=1&orden=reporte&limite=2")).andExpect(status().isOk());

        verify(siniestroService).listar(1L, null, false, false, false, null, null, null, "reporte", 2);
    }

    @Test
    void getSiniestro_ajeno_responde404() throws Exception {
        when(siniestroService.obtener(1L, 2L, null))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Siniestro no encontrado"));

        mvc.perform(get("/api/siniestros/1?aseguradoId=2")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Siniestro no encontrado"));
    }

    @Test
    void postSiniestro_responde201() throws Exception {
        mvc.perform(post("/api/siniestros").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
    }

    @Test
    void putAsignacion_enviaLosDatos() throws Exception {
        mvc.perform(put("/api/siniestros/1/asignacion").contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitanteId\":14,\"liquidadorId\":9}")).andExpect(status().isOk());

        verify(siniestroService).asignar(1L, new AsignarSiniestro(14L, 9L));
    }

    @Test
    void putEtapaYCierre_responden200() throws Exception {
        mvc.perform(put("/api/siniestros/1/etapa").contentType(MediaType.APPLICATION_JSON).content("{\"liquidadorId\":9}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/siniestros/1/cierre").contentType(MediaType.APPLICATION_JSON)
                .content("{\"liquidadorId\":9,\"resultado\":\"APROBADO\",\"mensaje\":\"m\"}")).andExpect(status().isOk());

        verify(siniestroService).avanzarEtapa(1L, new AvanzarEtapaSiniestro(9L));
        verify(siniestroService).cerrar(1L, new RespuestaCerrarSiniestro(9L, "APROBADO", "m"));
    }

    @Test
    void getGestiones_soloVisibles_enviaElFiltro() throws Exception {
        mvc.perform(get("/api/siniestros/1/gestiones?soloVisibles=true")).andExpect(status().isOk());

        verify(seguimientoService).listarGestiones(1L, true);
    }

    @Test
    void postGestionYSolicitud_responden201() throws Exception {
        mvc.perform(post("/api/siniestros/1/gestiones").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/siniestros/1/solicitudes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getSolicitudesCandidatosYAdjuntos_responden200() throws Exception {
        mvc.perform(get("/api/siniestros/1/solicitudes")).andExpect(status().isOk());
        mvc.perform(get("/api/siniestros/1/candidatos")).andExpect(status().isOk());
        mvc.perform(get("/api/siniestros/1/adjuntos")).andExpect(status().isOk());
    }

    @Test
    void postAdjunto_recibeLaFoto() throws Exception {
        mvc.perform(multipart("/api/siniestros/1/adjuntos").file("archivo", new byte[]{1})
                .param("tipoDocumento", "Foto").param("solicitudId", "7")).andExpect(status().isCreated());

        verify(documentoService).subir(eq(1L), any(), eq("Foto"), eq(null), eq(null), eq(7L));
    }

    @Test
    void getArchivo_respetaElTipoDelDocumento() throws Exception {
        verArchivo("a.jpg", MediaType.IMAGE_JPEG);
        verArchivo("a.PNG", MediaType.IMAGE_PNG);
        verArchivo("a.pdf", MediaType.APPLICATION_PDF);
    }

    @Test
    void getAvisos_yMarcarLeido() throws Exception {
        mvc.perform(get("/api/avisos?aseguradoId=1")).andExpect(status().isOk());
        mvc.perform(put("/api/avisos/5/leido")).andExpect(status().isNoContent());

        verify(seguimientoService).listarAvisos(1L);
        verify(seguimientoService).marcarAvisoLeido(5L);
    }

    @Test
    void endpointsDeLiquidadores_llamanAlServicio() throws Exception {
        mvc.perform(get("/api/liquidadores/casos-abiertos")).andExpect(status().isOk());
        mvc.perform(get("/api/liquidadores/9/casos-activos")).andExpect(status().isOk());
        mvc.perform(get("/api/liquidadores/9/historial?desde=2026-09-01&limite=3")).andExpect(status().isOk());
        mvc.perform(post("/api/liquidadores/9/liberar-casos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitanteId\":14}")).andExpect(status().isOk());

        verify(liquidadorService).historial(9L, "2026-09-01", null, 3);
        verify(liquidadorService).liberarCasos(9L, new SolicitanteRequest(14L));
    }
}
