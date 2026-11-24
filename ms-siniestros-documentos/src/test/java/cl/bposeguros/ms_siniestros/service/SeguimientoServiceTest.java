package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.AvisoRespuesta;
import cl.bposeguros.ms_siniestros.dto.GestionResponse;
import cl.bposeguros.ms_siniestros.dto.NuevaGestionRequest;
import cl.bposeguros.ms_siniestros.dto.NuevaSolicitudRequest;
import cl.bposeguros.ms_siniestros.dto.SolicitudResponse;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import cl.bposeguros.ms_siniestros.model.Aviso;
import cl.bposeguros.ms_siniestros.model.Gestion;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.model.SolicitudDocumento;
import cl.bposeguros.ms_siniestros.repository.AvisoRepository;
import cl.bposeguros.ms_siniestros.repository.GestionRepository;
import cl.bposeguros.ms_siniestros.repository.SiniestroRepository;
import cl.bposeguros.ms_siniestros.repository.SolicitudRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SeguimientoServiceTest {

    @Mock
    private GestionRepository gestionRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private AvisoRepository avisoRepository;

    @Mock
    private SiniestroRepository siniestroRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private SeguimientoService service;

    private Siniestro siniestro(Long id, int etapa, Long liquidadorId) {
        return Siniestro.builder().id(id).numero("SIN-26-00480" + id).aseguradoId(1L).companiaId(1L).tipoId(1L)
                .fechaOcurrencia("2026-09-02").fechaReporte("2026-09-02").comuna("Maipú").descripcion("Choque leve")
                .etapa(etapa).fechaCambioEtapa("2026-09-03").liquidadorId(liquidadorId)
                .fechaAsignacion(liquidadorId == null ? null : "2026-09-03 09:15").build();
    }

    private UsuarioDTO usuario(Long id, long rol, boolean activo, String equipo) {
        return new UsuarioDTO(id, "158423715", "Paulina Fernández", "LIQ-01", equipo, activo, rol);
    }

    private void conCasoDePaulina() {
        when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro(1L, 2, 9L)));
        lenient().when(usuarioClient.obtener(9L)).thenReturn(usuario(9L, 2L, true, "Vehículos"));
        lenient().when(gestionRepository.save(any(Gestion.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(solicitudRepository.save(any(SolicitudDocumento.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Gestion gestion(String fecha, boolean visible) {
        return Gestion.builder().siniestroId(1L).fechaHora(fecha).tipo("T").descripcion("D").responsable("R")
                .visibleAsegurado(visible).build();
    }

    private void assertEstado(HttpStatus esperado, String mensaje, org.junit.jupiter.api.function.Executable accion) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, e.getStatusCode());
        assertEquals(mensaje, e.getReason());
    }

    @Test
    void registrarGestion_laGuarda() {
        service.registrarGestion(1L, "Reporte", "Texto", "Asegurado", true);

        ArgumentCaptor<Gestion> captor = ArgumentCaptor.forClass(Gestion.class);
        verify(gestionRepository).save(captor.capture());
        assertEquals("Reporte", captor.getValue().getTipo());
        assertTrue(captor.getValue().isVisibleAsegurado());
    }

    @Test
    void registrarAviso_quedaSinLeer() {
        service.registrarAviso(1L, "Cierre", "Mensaje");

        ArgumentCaptor<Aviso> captor = ArgumentCaptor.forClass(Aviso.class);
        verify(avisoRepository).save(captor.capture());
        assertFalse(captor.getValue().isLeido());
    }

    @Test
    void listarGestiones_soloVisibles_ocultaLasNotasInternas() {
        when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro(1L, 2, 9L)));
        when(gestionRepository.findBySiniestroIdOrderByFechaHoraAscIdAsc(1L))
                .thenReturn(List.of(gestion("2026-09-01 10:00", true), gestion("2026-09-02 10:00", false)));

        assertEquals(1, service.listarGestiones(1L, true).size());
        assertEquals(2, service.listarGestiones(1L, false).size());
    }

    @Test
    void crearGestion_correcta_quedaComoNotaInterna() {
        conCasoDePaulina();

        GestionResponse r = service.crearGestion(1L, new NuevaGestionRequest(9L, "  Llamé al cliente por teléfono "));

        assertEquals("Llamé al cliente por teléfono", r.descripcion());
        assertEquals("P. Fernández (LIQ-01)", r.responsable());
        assertFalse(r.visibleAsegurado());
    }

    @Test
    void crearGestion_vacia_lanza400() {
        conCasoDePaulina();

        assertEstado(HttpStatus.BAD_REQUEST, "Describe lo que hiciste",
                () -> service.crearGestion(1L, new NuevaGestionRequest(9L, " ")));
    }

    @Test
    void crearGestion_corta_lanza400() {
        conCasoDePaulina();

        assertEstado(HttpStatus.BAD_REQUEST, "Escribe un poco más (mínimo 10 caracteres)",
                () -> service.crearGestion(1L, new NuevaGestionRequest(9L, "Llamé")));
    }

    @Test
    void crearGestion_deUnCasoAjeno_lanza403() {
        conCasoDePaulina();

        assertEstado(HttpStatus.FORBIDDEN, "Este caso no está asignado a ti",
                () -> service.crearGestion(1L, new NuevaGestionRequest(11L, "Llamé al cliente")));
    }

    @Test
    void solicitarDocumento_correcto_registraGestionYAviso() {
        conCasoDePaulina();

        SolicitudResponse r = service.solicitarDocumento(1L, new NuevaSolicitudRequest(9L, "Padrón del vehículo", "Legible"));

        assertEquals("Padrón del vehículo", r.documentoSolicitado());
        assertFalse(r.entregado());
        verify(gestionRepository).save(any(Gestion.class));
        verify(avisoRepository).save(any(Aviso.class));
    }

    @Test
    void solicitarDocumento_vacio_lanza400() {
        conCasoDePaulina();

        assertEstado(HttpStatus.BAD_REQUEST, "Elige qué documento necesitas",
                () -> service.solicitarDocumento(1L, new NuevaSolicitudRequest(9L, "", "")));
    }

    @Test
    void solicitarDocumento_repetido_lanza409() {
        conCasoDePaulina();
        when(solicitudRepository.existsBySiniestroIdAndDocumentoSolicitadoAndEntregadoFalse(1L, "Padrón")).thenReturn(true);

        assertEstado(HttpStatus.CONFLICT, "Ya pediste este documento y el asegurado aún no lo entrega",
                () -> service.solicitarDocumento(1L, new NuevaSolicitudRequest(9L, "Padrón", "")));
    }

    @Test
    void listarSolicitudes_lasDevuelve() {
        when(siniestroRepository.findById(1L)).thenReturn(Optional.of(siniestro(1L, 2, 9L)));
        when(solicitudRepository.findBySiniestroIdOrderByEntregadoAscFechaSolicitudAscIdAsc(1L))
                .thenReturn(List.of(SolicitudDocumento.builder().id(3L).siniestroId(1L).documentoSolicitado("Padrón").build()));

        assertEquals("Padrón", service.listarSolicitudes(1L).get(0).documentoSolicitado());
    }

    @Test
    void listarAvisos_sinSiniestros_devuelveVacio() {
        when(siniestroRepository.findByAseguradoId(1L)).thenReturn(List.of());

        assertTrue(service.listarAvisos(1L).isEmpty());
    }

    @Test
    void listarAvisos_muestraPrimeroElMasNuevo() {
        when(siniestroRepository.findByAseguradoId(1L)).thenReturn(List.of(siniestro(1L, 2, 9L)));
        when(avisoRepository.findBySiniestroIdIn(any())).thenReturn(List.of(
                Aviso.builder().id(1L).siniestroId(1L).fechaHora("2026-09-01 10:00").motivo("Viejo").build(),
                Aviso.builder().id(2L).siniestroId(1L).fechaHora("2026-09-05 10:00").motivo("Nuevo").build()));

        List<AvisoRespuesta> avisos = service.listarAvisos(1L);

        assertEquals("Nuevo", avisos.get(0).motivo());
        assertEquals("SIN-26-004801", avisos.get(0).numeroSiniestro());
    }

    @Test
    void marcarAvisoLeido_loMarca() {
        Aviso aviso = Aviso.builder().id(5L).leido(false).build();
        when(avisoRepository.findById(5L)).thenReturn(Optional.of(aviso));

        service.marcarAvisoLeido(5L);

        assertTrue(aviso.isLeido());
    }

    @Test
    void marcarAvisoLeido_inexistente_lanza404() {
        when(avisoRepository.findById(99L)).thenReturn(Optional.empty());

        assertEstado(HttpStatus.NOT_FOUND, "Aviso no encontrado", () -> service.marcarAvisoLeido(99L));
    }

    @Test
    void ultimaActualizacion_ignoraLasNotasInternas() {
        when(gestionRepository.findBySiniestroIdOrderByFechaHoraAscIdAsc(1L))
                .thenReturn(List.of(gestion("2026-09-01 10:00", true), gestion("2026-09-09 10:00", false)));

        assertEquals("2026-09-01 10:00", service.ultimaActualizacion(1L));
    }

    @Test
    void ultimaActualizacion_sinGestiones_esNula() {
        when(gestionRepository.findBySiniestroIdOrderByFechaHoraAscIdAsc(1L)).thenReturn(List.of());

        assertNull(service.ultimaActualizacion(1L));
    }

    @Test
    void exigirAnalista_conAnalistaActivo_loDevuelve() {
        when(usuarioClient.obtener(14L)).thenReturn(usuario(14L, 3L, true, ""));

        assertEquals(14L, service.exigirAnalista(14L).id());
    }

    @Test
    void exigirAnalista_sinId_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "Falta indicar quién realiza la acción", () -> service.exigirAnalista(null));
    }

    @Test
    void exigirAnalista_conOtroRolOBloqueado_lanza403() {
        when(usuarioClient.obtener(9L)).thenReturn(usuario(9L, 2L, true, ""));
        when(usuarioClient.obtener(15L)).thenReturn(usuario(15L, 3L, false, ""));

        assertEstado(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción", () -> service.exigirAnalista(9L));
        assertEstado(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción", () -> service.exigirAnalista(15L));
    }

    @Test
    void exigirLiquidadorDelCaso_bloqueado_lanza403() {
        when(usuarioClient.obtener(9L)).thenReturn(usuario(9L, 2L, false, ""));

        assertEstado(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción",
                () -> service.exigirLiquidadorDelCaso(siniestro(1L, 2, 9L), 9L));
    }

    @Test
    void exigirLiquidadorDelCaso_sinId_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "Falta indicar quién realiza la acción",
                () -> service.exigirLiquidadorDelCaso(siniestro(1L, 2, 9L), null));
    }

    @Test
    void buscarSiniestro_inexistente_lanza404() {
        when(siniestroRepository.findById(99L)).thenReturn(Optional.empty());

        assertEstado(HttpStatus.NOT_FOUND, "Siniestro no encontrado", () -> service.buscarSiniestro(99L));
    }
}
