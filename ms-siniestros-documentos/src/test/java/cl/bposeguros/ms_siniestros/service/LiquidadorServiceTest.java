package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bposeguros.ms_siniestros.client.CatalogoClient;
import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.ConteoCasosResponse;
import cl.bposeguros.ms_siniestros.dto.HistorialResponse;
import cl.bposeguros.ms_siniestros.dto.SolicitanteRequest;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import cl.bposeguros.ms_siniestros.model.Asignacion;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.repository.AsignacionRepository;
import cl.bposeguros.ms_siniestros.repository.SiniestroRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LiquidadorServiceTest {

    @Mock
    private SiniestroRepository siniestroRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    @Mock
    private SiniestroService siniestroService;

    @Mock
    private SeguimientoService seguimientoService;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private LiquidadorService service;

    @BeforeEach
    void preparar() {
        lenient().when(usuarioClient.obtener(9L)).thenReturn(usuario(9L, 2L, true, "Vehículos"));
        lenient().when(siniestroService.nuevoResolutor()).thenReturn(new Resolutor(usuarioClient, catalogoClient));
    }

    private Siniestro siniestro(Long id, int etapa, Long liquidadorId) {
        return Siniestro.builder().id(id).numero("SIN-26-00480" + id).aseguradoId(1L).companiaId(1L).tipoId(1L)
                .fechaOcurrencia("2026-09-02").fechaReporte("2026-09-02").comuna("Maipú").descripcion("Choque leve")
                .etapa(etapa).fechaCambioEtapa("2026-09-03").liquidadorId(liquidadorId)
                .fechaAsignacion(liquidadorId == null ? null : "2026-09-03 09:15").build();
    }

    private UsuarioDTO usuario(Long id, long rol, boolean activo, String equipo) {
        return new UsuarioDTO(id, "158423715", "Paulina Fernández", "LIQ-01", equipo, activo, rol);
    }

    private Asignacion asignacion(Long id, String inicio, String fin) {
        return Asignacion.builder().id(id).siniestroId(1L).liquidadorId(9L).fechaInicio(inicio).fechaFin(fin).build();
    }

    private void assertEstado(HttpStatus esperado, String mensaje, org.junit.jupiter.api.function.Executable accion) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, e.getStatusCode());
        assertEquals(mensaje, e.getReason());
    }

    @Test
    void casosAbiertosPorLiquidador_cuentaLosDeCadaUno() {
        when(usuarioClient.listarPorRol(2L)).thenReturn(List.of(usuario(9L, 2L, true, "Vehículos")));
        when(siniestroRepository.countByLiquidadorIdAndEtapaNot(9L, 4)).thenReturn(3L);

        assertEquals(new ConteoCasosResponse(9L, 3L), service.casosAbiertosPorLiquidador().get(0));
    }

    @Test
    void casosActivos_devuelveLosCasosVigentes() {
        when(siniestroRepository.findByLiquidadorIdAndEtapaNot(9L, 4)).thenReturn(List.of(siniestro(1L, 2, 9L)));
        when(asignacionRepository.findFirstBySiniestroIdAndFechaFinIsNull(1L))
                .thenReturn(Optional.of(asignacion(5L, "2026-09-03 09:15", null)));

        HistorialResponse fila = service.casosActivos(9L).get(0);

        assertEquals(5L, fila.asignacionId());
        assertTrue(fila.vigente());
    }

    @Test
    void casosActivos_deAlguienQueNoEsLiquidador_lanza404() {
        when(usuarioClient.obtener(1L)).thenReturn(usuario(1L, 1L, true, ""));

        assertEstado(HttpStatus.NOT_FOUND, "Liquidador no encontrado", () -> service.casosActivos(1L));
    }

    @Test
    void historial_filtraPorFechasYLimite() {
        when(asignacionRepository.findByLiquidadorIdOrderByFechaInicioDescIdDesc(9L)).thenReturn(List.of(
                asignacion(1L, "2026-09-03 09:15", "2026-09-21 12:30"),
                asignacion(2L, "2026-07-16 09:00", "2026-07-20 10:00"),
                asignacion(3L, "2026-09-10 09:00", null)));
        when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro(1L, 2, 9L)));

        assertEquals(2, service.historial(9L, "2026-09-01", "2026-09-30", null).size());
        assertEquals(1, service.historial(9L, null, null, 1).size());
    }

    @Test
    void historial_conFechasAlReves_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "La fecha desde no puede ser posterior a la fecha hasta",
                () -> service.historial(9L, "2026-10-01", "2026-09-01", null));
    }

    @Test
    void liberarCasos_dejaLosCasosPorReasignar() {
        Siniestro s = siniestro(1L, 2, 9L);
        Asignacion abierta = asignacion(5L, "2026-09-03 09:15", null);
        when(seguimientoService.exigirAnalista(14L)).thenReturn(usuario(14L, 3L, true, ""));
        when(siniestroRepository.findByLiquidadorIdAndEtapaNot(9L, 4)).thenReturn(List.of(s));
        when(asignacionRepository.findFirstBySiniestroIdAndFechaFinIsNull(1L)).thenReturn(Optional.of(abierta));

        assertEquals(1, service.liberarCasos(9L, new SolicitanteRequest(14L)).size());

        assertNull(s.getLiquidadorId());
        assertEquals(9L, s.getLiquidadorAnteriorId());
        assertEquals("BLOQUEO", abierta.getMotivoFin());
        verify(seguimientoService).registrarAviso(eq(1L), eq("Reasignación pendiente"), anyString());
    }
}
