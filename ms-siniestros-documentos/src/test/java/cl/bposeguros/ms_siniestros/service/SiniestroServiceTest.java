package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bposeguros.ms_siniestros.client.CatalogoClient;
import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.AsignarSiniestro;
import cl.bposeguros.ms_siniestros.dto.AvanzarEtapaSiniestro;
import cl.bposeguros.ms_siniestros.dto.CompaniaDTO;
import cl.bposeguros.ms_siniestros.dto.LiquidadorPropuesto;
import cl.bposeguros.ms_siniestros.dto.ReportarSiniestroRequest;
import cl.bposeguros.ms_siniestros.dto.RespuestaCerrarSiniestro;
import cl.bposeguros.ms_siniestros.dto.SiniestroRespuesta;
import cl.bposeguros.ms_siniestros.dto.TipoSiniestroDTO;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import cl.bposeguros.ms_siniestros.model.Asignacion;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.repository.AsignacionRepository;
import cl.bposeguros.ms_siniestros.repository.SiniestroRepository;
import cl.bposeguros.ms_siniestros.repository.SolicitudRepository;
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
class SiniestroServiceTest {

    @Mock
    private SiniestroRepository siniestroRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private SeguimientoService seguimientoService;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private SiniestroService service;

    @BeforeEach
    void preparar() {
        lenient().when(siniestroRepository.save(any(Siniestro.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(usuarioClient.obtener(1L)).thenReturn(usuario(1L, 1L, true, ""));
        lenient().when(usuarioClient.obtener(9L)).thenReturn(usuario(9L, 2L, true, "Vehículos"));
        lenient().when(catalogoClient.obtenerCompania(1L))
                .thenReturn(new CompaniaDTO(1L, "ASG-01", "Seguros Cordillera", true, false, 10, true));
        lenient().when(catalogoClient.obtenerTipo(1L)).thenReturn(new TipoSiniestroDTO(1L, "TS-01", "Choque", "VEHICULO"));
        lenient().when(catalogoClient.obtenerTipo(4L)).thenReturn(new TipoSiniestroDTO(4L, "TS-04", "Agua", "HOGAR"));
        lenient().when(seguimientoService.exigirAnalista(14L)).thenReturn(usuario(14L, 3L, true, ""));
        lenient().when(seguimientoService.exigirLiquidadorDelCaso(any(), eq(9L))).thenReturn(usuario(9L, 2L, true, "Vehículos"));
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

    private void existe(Siniestro s) {
        when(seguimientoService.buscarSiniestro(s.getId())).thenReturn(s);
    }

    private ReportarSiniestroRequest reporte(Long tipo, String fecha, String comuna, String descripcion) {
        return new ReportarSiniestroRequest(1L, 1L, tipo, fecha, comuna, descripcion);
    }

    private ReportarSiniestroRequest reporteCorrecto() {
        return reporte(1L, Fechas.hoy().toString(), " Colina ", "Me chocaron en un semáforo");
    }

    private List<SiniestroRespuesta> listar(boolean sinAsignar, boolean porReasignar, boolean soloAbiertos,
                                            String desde, String hasta, String buscar, Integer limite) {
        return service.listar(null, null, sinAsignar, porReasignar, soloAbiertos, desde, hasta, buscar, null, limite);
    }

    private void assertEstado(HttpStatus esperado, String mensaje, org.junit.jupiter.api.function.Executable accion) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, e.getStatusCode());
        assertEquals(mensaje, e.getReason());
    }

    @Test
    void listar_porAsegurado_usaSuConsulta() {
        when(siniestroRepository.findByAseguradoId(1L)).thenReturn(List.of(siniestro(1L, 2, 9L)));

        assertEquals(1, service.listar(1L, null, false, false, false, null, null, null, "reporte", null).size());
    }

    @Test
    void listar_porLiquidador_dejaLosCerradosAlFinal() {
        when(siniestroRepository.findByLiquidadorId(9L)).thenReturn(List.of(siniestro(1L, 4, 9L), siniestro(2L, 2, 9L)));

        assertEquals(2L, service.listar(null, 9L, false, false, false, null, null, null, null, null).get(0).id());
    }

    @Test
    void listar_sinAsignar_soloTraeLosQueNoTienenLiquidador() {
        when(siniestroRepository.findAll()).thenReturn(List.of(siniestro(1L, 2, 9L), siniestro(2L, 1, null)));

        assertEquals(List.of(2L), listar(true, false, false, null, null, null, null).stream().map(SiniestroRespuesta::id).toList());
    }

    @Test
    void listar_porReasignar_soloTraeLosQueTuvieronLiquidador() {
        Siniestro liberado = siniestro(3L, 2, null);
        liberado.setLiquidadorAnteriorId(9L);
        when(siniestroRepository.findAll()).thenReturn(List.of(siniestro(2L, 1, null), liberado));

        assertEquals(List.of(3L), listar(false, true, false, null, null, null, null).stream().map(SiniestroRespuesta::id).toList());
    }

    @Test
    void listar_soloAbiertos_dejaFueraLosCerrados() {
        when(siniestroRepository.findAll()).thenReturn(List.of(siniestro(1L, 4, 9L), siniestro(2L, 2, 9L)));

        assertEquals(1, listar(false, false, true, null, null, null, null).size());
    }

    @Test
    void listar_entreFechas_filtraPorFechaDeOcurrencia() {
        when(siniestroRepository.findAll()).thenReturn(List.of(siniestro(1L, 2, 9L)));

        assertEquals(1, listar(false, false, false, "2026-09-01", "2026-09-30", null, null).size());
        assertEquals(0, listar(false, false, false, "2026-10-01", "2026-10-30", null, null).size());
    }

    @Test
    void listar_conFechasAlReves_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "La fecha desde no puede ser posterior a la fecha hasta",
                () -> listar(false, false, false, "2026-10-01", "2026-09-01", null, null));
    }

    @Test
    void listar_buscando_encuentraPorNumeroYPorRut() {
        when(siniestroRepository.findAll()).thenReturn(List.of(siniestro(1L, 2, 9L)));

        assertEquals(1, listar(false, false, false, null, null, "4801", null).size());
        assertEquals(1, listar(false, false, false, null, null, "15.842.371-5", null).size());
        assertEquals(0, listar(false, false, false, null, null, "nadie", null).size());
    }

    @Test
    void listar_conLimite_recortaLaLista() {
        when(siniestroRepository.findAll()).thenReturn(List.of(siniestro(1L, 2, 9L), siniestro(2L, 2, 9L), siniestro(3L, 2, 9L)));

        assertEquals(2, listar(false, false, false, null, null, null, 2).size());
    }

    @Test
    void obtener_propio_loDevuelveConLosNombres() {
        existe(siniestro(1L, 2, 9L));

        SiniestroRespuesta r = service.obtener(1L, 1L, null);

        assertEquals("Seguros Cordillera", r.companiaNombre());
        assertEquals("Choque", r.tipoNombre());
        assertEquals("En evaluación", r.etapaEtiqueta());
    }

    @Test
    void obtener_deOtroAsegurado_lanza404() {
        existe(siniestro(1L, 2, 9L));

        assertEstado(HttpStatus.NOT_FOUND, "Siniestro no encontrado", () -> service.obtener(1L, 2L, null));
    }

    @Test
    void obtener_deOtroLiquidador_lanza404() {
        existe(siniestro(1L, 2, 9L));

        assertEstado(HttpStatus.NOT_FOUND, "Siniestro no encontrado", () -> service.obtener(1L, null, 11L));
    }

    @Test
    void listarCandidatos_soloDelEquipoActivosYPrimeroElMasLibre() {
        existe(siniestro(1L, 1, null));
        when(usuarioClient.listarPorRol(2L)).thenReturn(List.of(
                usuario(9L, 2L, true, "Vehículos"), usuario(10L, 2L, true, "Hogar"),
                usuario(11L, 2L, true, "Vehículos"), usuario(13L, 2L, false, "Vehículos")));
        when(siniestroRepository.countByLiquidadorIdAndEtapaNot(9L, 4)).thenReturn(5L);
        when(siniestroRepository.countByLiquidadorIdAndEtapaNot(11L, 4)).thenReturn(1L);

        assertEquals(List.of(11L, 9L), service.listarCandidatos(1L).stream().map(LiquidadorPropuesto::id).toList());
    }

    @Test
    void reportar_correcto_creaElSiguienteNumero() {
        Siniestro ultimo = siniestro(1L, 2, 9L);
        String prefijo = "SIN-" + String.format("%02d", Fechas.hoy().getYear() % 100) + "-";
        ultimo.setNumero(prefijo + "000041");
        when(siniestroRepository.findTopByNumeroStartingWithOrderByNumeroDesc(prefijo)).thenReturn(Optional.of(ultimo));

        SiniestroRespuesta r = service.reportar(reporteCorrecto());

        assertEquals(prefijo + "000042", r.numero());
        assertEquals("Colina", r.comuna());
        assertEquals("RECIBIDO", r.etapa());
        assertFalse(r.fueraDePlazo());
    }

    @Test
    void reportar_despuesDelPlazo_quedaFueraDePlazo() {
        SiniestroRespuesta r = service.reportar(reporte(1L, Fechas.hoy().minusDays(40).toString(), "Colina", "Me chocaron en un semáforo"));

        assertTrue(r.fueraDePlazo());
    }

    @Test
    void reportar_sinDatos_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "Faltan datos del siniestro",
                () -> service.reportar(new ReportarSiniestroRequest(null, 1L, 1L, "", "", "")));
    }

    @Test
    void reportar_siNoEsAsegurado_lanza403() {
        when(usuarioClient.obtener(1L)).thenReturn(usuario(1L, 2L, true, ""));

        assertEstado(HttpStatus.FORBIDDEN, "Solo un asegurado puede reportar un siniestro", () -> service.reportar(reporteCorrecto()));
    }

    @Test
    void reportar_conCuentaInactiva_lanza403() {
        when(usuarioClient.obtener(1L)).thenReturn(usuario(1L, 1L, false, ""));

        assertEstado(HttpStatus.FORBIDDEN, "Tu cuenta no está activa", () -> service.reportar(reporteCorrecto()));
    }

    @Test
    void reportar_conCompaniaInactiva_lanza400() {
        when(catalogoClient.obtenerCompania(1L)).thenReturn(new CompaniaDTO(1L, "A", "X", true, true, 10, false));

        assertEstado(HttpStatus.BAD_REQUEST, "La compañía elegida no está disponible", () -> service.reportar(reporteCorrecto()));
    }

    @Test
    void reportar_conTipoInexistente_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "El tipo de siniestro no existe",
                () -> service.reportar(reporte(99L, "2026-11-20", "Colina", "Me chocaron en un semáforo")));
    }

    @Test
    void reportar_hogarEnCompaniaDeVehiculos_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "Esa compañía no gestiona siniestros de hogar",
                () -> service.reportar(reporte(4L, "2026-11-20", "Colina", "Se inundó la cocina")));
    }

    @Test
    void reportar_vehiculoEnCompaniaDeHogar_lanza400() {
        when(catalogoClient.obtenerCompania(1L)).thenReturn(new CompaniaDTO(1L, "A", "X", false, true, 10, true));

        assertEstado(HttpStatus.BAD_REQUEST, "Esa compañía no gestiona siniestros de vehículos", () -> service.reportar(reporteCorrecto()));
    }

    @Test
    void reportar_conFechaFutura_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "La fecha de ocurrencia no puede ser futura",
                () -> service.reportar(reporte(1L, Fechas.hoy().plusDays(1).toString(), "Colina", "Me chocaron en un semáforo")));
    }

    @Test
    void reportar_sinComuna_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "Indica la comuna donde ocurrió",
                () -> service.reportar(reporte(1L, "2026-09-02", " ", "Me chocaron en un semáforo")));
    }

    @Test
    void reportar_conDescripcionCortaOLarga_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "Describe lo ocurrido (mínimo 10 caracteres)",
                () -> service.reportar(reporte(1L, "2026-09-02", "Colina", "Choque")));
        assertEstado(HttpStatus.BAD_REQUEST, "La descripción es muy larga (máximo 1000 caracteres)",
                () -> service.reportar(reporte(1L, "2026-09-02", "Colina", "a".repeat(1001))));
    }

    @Test
    void reportar_repetido_lanza409ConElNumero() {
        when(siniestroRepository.findFirstByAseguradoIdAndCompaniaIdAndTipoIdAndFechaOcurrenciaAndEtapaNot(
                anyLong(), anyLong(), anyLong(), anyString(), anyInt())).thenReturn(Optional.of(siniestro(1L, 2, 9L)));

        assertEstado(HttpStatus.CONFLICT, "Ya reportaste un siniestro igual que sigue abierto: SIN-26-004801",
                () -> service.reportar(reporteCorrecto()));
    }

    @Test
    void asignar_casoNuevo_pasaAEvaluacion() {
        Siniestro s = siniestro(1L, 1, null);
        existe(s);

        SiniestroRespuesta r = service.asignar(1L, new AsignarSiniestro(14L, 9L));

        assertEquals("EN_EVALUACION", r.etapa());
        assertEquals(9L, r.liquidadorId());
        verify(asignacionRepository).save(any(Asignacion.class));
        verify(seguimientoService).registrarAviso(eq(1L), eq("Liquidador asignado"), anyString());
    }

    @Test
    void asignar_reasignacion_mantieneLaEtapaYLoDice() {
        Siniestro s = siniestro(1L, 3, null);
        s.setLiquidadorAnteriorId(11L);
        existe(s);

        SiniestroRespuesta r = service.asignar(1L, new AsignarSiniestro(14L, 9L));

        assertEquals("EN_LIQUIDACION", r.etapa());
        assertNull(r.liquidadorAnteriorId());
        verify(seguimientoService).registrarGestion(eq(1L), eq("Asignación"), contains("reasignado"), anyString(), eq(true));
    }

    @Test
    void asignar_casoCerradoOYaAsignado_lanza409() {
        existe(siniestro(1L, 4, 9L));
        existe(siniestro(2L, 2, 9L));

        assertEstado(HttpStatus.CONFLICT, "El caso ya está cerrado", () -> service.asignar(1L, new AsignarSiniestro(14L, 9L)));
        assertEstado(HttpStatus.CONFLICT, "El caso ya tiene un liquidador asignado", () -> service.asignar(2L, new AsignarSiniestro(14L, 9L)));
    }

    @Test
    void asignar_sinElegirLiquidador_lanza400() {
        existe(siniestro(1L, 1, null));

        assertEstado(HttpStatus.BAD_REQUEST, "Elige a quién asignar el caso", () -> service.asignar(1L, new AsignarSiniestro(14L, null)));
    }

    @Test
    void asignar_aAlguienQueNoEsLiquidador_lanza400() {
        existe(siniestro(1L, 1, null));

        assertEstado(HttpStatus.BAD_REQUEST, "El liquidador elegido no existe", () -> service.asignar(1L, new AsignarSiniestro(14L, 1L)));
    }

    @Test
    void asignar_aUnBloqueado_lanza409() {
        existe(siniestro(1L, 1, null));
        when(usuarioClient.obtener(13L)).thenReturn(usuario(13L, 2L, false, "Vehículos"));

        assertEstado(HttpStatus.CONFLICT, "El liquidador está bloqueado", () -> service.asignar(1L, new AsignarSiniestro(14L, 13L)));
    }

    @Test
    void asignar_aOtroEquipo_lanza400() {
        existe(siniestro(1L, 1, null));
        when(usuarioClient.obtener(10L)).thenReturn(usuario(10L, 2L, true, "Hogar"));

        assertEstado(HttpStatus.BAD_REQUEST, "El liquidador debe ser del equipo Vehículos",
                () -> service.asignar(1L, new AsignarSiniestro(14L, 10L)));
    }

    @Test
    void avanzarEtapa_correcta_pasaALiquidacion() {
        existe(siniestro(1L, 2, 9L));

        assertEquals("EN_LIQUIDACION", service.avanzarEtapa(1L, new AvanzarEtapaSiniestro(9L)).etapa());
    }

    @Test
    void avanzarEtapa_desdeOtraEtapa_lanza409() {
        existe(siniestro(1L, 3, 9L));

        assertEstado(HttpStatus.CONFLICT, "Este caso no se puede avanzar desde su etapa actual",
                () -> service.avanzarEtapa(1L, new AvanzarEtapaSiniestro(9L)));
    }

    @Test
    void avanzarEtapa_conDocumentosPendientes_lanza409() {
        existe(siniestro(1L, 2, 9L));
        when(solicitudRepository.countBySiniestroIdAndEntregadoFalse(1L)).thenReturn(2L);

        assertEstado(HttpStatus.CONFLICT, "Primero deben estar entregados todos los documentos pendientes",
                () -> service.avanzarEtapa(1L, new AvanzarEtapaSiniestro(9L)));
    }

    @Test
    void cerrar_correcto_guardaResultadoYCierraLaAsignacion() {
        existe(siniestro(1L, 3, 9L));
        Asignacion abierta = Asignacion.builder().id(1L).siniestroId(1L).build();
        when(asignacionRepository.findFirstBySiniestroIdAndFechaFinIsNull(1L)).thenReturn(Optional.of(abierta));

        SiniestroRespuesta r = service.cerrar(1L, new RespuestaCerrarSiniestro(9L, " aprobado ", "Tu siniestro fue aprobado"));

        assertEquals("CERRADO", r.etapa());
        assertEquals("APROBADO", r.resultado());
        assertEquals("CIERRE", abierta.getMotivoFin());
    }

    @Test
    void cerrar_sinResultado_lanza400() {
        existe(siniestro(1L, 3, 9L));

        assertEstado(HttpStatus.BAD_REQUEST, "Elige el resultado",
                () -> service.cerrar(1L, new RespuestaCerrarSiniestro(9L, null, "Tu siniestro fue aprobado")));
    }

    @Test
    void cerrar_conResultadoNoPermitido_lanza400() {
        existe(siniestro(1L, 2, 9L));

        assertEstado(HttpStatus.BAD_REQUEST, "Ese resultado no es válido para un caso en etapa \"En evaluación\"",
                () -> service.cerrar(1L, new RespuestaCerrarSiniestro(9L, "APROBADO", "Tu siniestro fue aprobado")));
    }

    @Test
    void cerrar_sinMensajeOConMensajeCorto_lanza400() {
        existe(siniestro(1L, 3, 9L));

        assertEstado(HttpStatus.BAD_REQUEST, "Escribe un mensaje para el asegurado",
                () -> service.cerrar(1L, new RespuestaCerrarSiniestro(9L, "APROBADO", " ")));
        assertEstado(HttpStatus.BAD_REQUEST, "Explica un poco más (mínimo 15 caracteres)",
                () -> service.cerrar(1L, new RespuestaCerrarSiniestro(9L, "APROBADO", "Listo")));
    }
}
