package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bposeguros.ms_siniestros.dto.AdjuntoRespuesta;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import cl.bposeguros.ms_siniestros.model.DocumentoAdjunto;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.model.SolicitudDocumento;
import cl.bposeguros.ms_siniestros.repository.AdjuntoRepository;
import cl.bposeguros.ms_siniestros.repository.SolicitudRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private AdjuntoRepository adjuntoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private AlmacenamientoService almacenamientoService;

    @Mock
    private SeguimientoService seguimientoService;

    @InjectMocks
    private DocumentoService service;

    private final MockMultipartFile foto = new MockMultipartFile("archivo", "choque.jpg", "image/jpeg", new byte[]{1});

    @BeforeEach
    void preparar() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        lenient().when(adjuntoRepository.save(any(DocumentoAdjunto.class))).thenAnswer(i -> {
            DocumentoAdjunto a = i.getArgument(0);
            a.setId(50L);
            return a;
        });
        lenient().when(almacenamientoService.guardar(any())).thenReturn("guardado.jpg");
    }

    @AfterEach
    void limpiar() {
        RequestContextHolder.resetRequestAttributes();
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

    private void conCaso(int etapa) {
        when(seguimientoService.buscarSiniestro(1L)).thenReturn(siniestro(1L, etapa, 9L));
    }

    private void assertEstado(HttpStatus esperado, String mensaje, org.junit.jupiter.api.function.Executable accion) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, e.getStatusCode());
        assertEquals(mensaje, e.getReason());
    }

    @Test
    void subir_foto_laGuardaYDevuelveSuDireccion() {
        conCaso(2);

        AdjuntoRespuesta r = service.subir(1L, foto, " Fotografía del daño ", null, null, null);

        assertEquals("choque.jpg", r.nombreArchivo());
        assertEquals("Fotografía del daño", r.tipoDocumento());
        assertEquals("Asegurado", r.cargadoPor());
        assertEquals("", r.comentario());
        assertEquals("http://localhost/api/adjuntos/50/archivo", r.urlArchivo());
        verify(seguimientoService).registrarGestion(eq(1L), eq("Documento recibido"), anyString(), eq("Asegurado"), eq(true));
    }

    @Test
    void subir_enCasoCerrado_lanza409YNoGuardaNada() {
        conCaso(4);

        assertEstado(HttpStatus.CONFLICT, "El caso está cerrado: ya no se pueden subir documentos",
                () -> service.subir(1L, foto, "Foto", null, null, null));
        verify(almacenamientoService, never()).guardar(any());
    }

    @Test
    void subir_sinTipo_lanza400() {
        conCaso(2);

        assertEstado(HttpStatus.BAD_REQUEST, "Indica qué tipo de documento es",
                () -> service.subir(1L, foto, " ", null, null, null));
    }

    @Test
    void subir_respondiendoUnaSolicitud_laMarcaEntregada() {
        conCaso(2);
        SolicitudDocumento solicitud = SolicitudDocumento.builder().id(7L).siniestroId(1L).entregado(false).build();
        when(solicitudRepository.findById(7L)).thenReturn(Optional.of(solicitud));

        service.subir(1L, foto, "Padrón", "ok", "Camila", 7L);

        assertTrue(solicitud.isEntregado());
        assertNotNull(solicitud.getFechaEntrega());
    }

    @Test
    void subir_solicitudDeOtroCaso_lanza404() {
        conCaso(2);
        when(solicitudRepository.findById(7L))
                .thenReturn(Optional.of(SolicitudDocumento.builder().id(7L).siniestroId(2L).build()));

        assertEstado(HttpStatus.NOT_FOUND, "La solicitud no existe en este caso",
                () -> service.subir(1L, foto, "Padrón", null, null, 7L));
    }

    @Test
    void subir_solicitudYaEntregada_lanza409() {
        conCaso(2);
        when(solicitudRepository.findById(7L))
                .thenReturn(Optional.of(SolicitudDocumento.builder().id(7L).siniestroId(1L).entregado(true).build()));

        assertEstado(HttpStatus.CONFLICT, "Ese documento ya fue entregado",
                () -> service.subir(1L, foto, "Padrón", null, null, 7L));
    }

    @Test
    void subir_archivoSinNombre_usaElNombreGuardado() {
        conCaso(2);
        MockMultipartFile sinNombre = new MockMultipartFile("archivo", "", "image/jpeg", new byte[]{1});

        assertEquals("guardado.jpg", service.subir(1L, sinNombre, "Foto", null, null, null).nombreArchivo());
    }

    @Test
    void listar_documentoDeEjemplo_noTieneDireccion() {
        when(adjuntoRepository.findBySiniestroIdOrderByFechaCargaDescIdDesc(1L)).thenReturn(List.of(
                DocumentoAdjunto.builder().id(1L).siniestroId(1L).rutaArchivo("").build()));

        assertNull(service.listar(1L).get(0).urlArchivo());
    }

    @Test
    void buscar_inexistente_lanza404() {
        when(adjuntoRepository.findById(99L)).thenReturn(Optional.empty());

        assertEstado(HttpStatus.NOT_FOUND, "Documento no encontrado", () -> service.buscar(99L));
    }

    @Test
    void leerArchivo_conArchivo_devuelveLosBytes() {
        when(almacenamientoService.leer("f.jpg")).thenReturn(new byte[]{9});

        assertArrayEquals(new byte[]{9}, service.leerArchivo(DocumentoAdjunto.builder().rutaArchivo("f.jpg").build()));
    }

    @Test
    void leerArchivo_deEjemplo_lanza404() {
        assertEstado(HttpStatus.NOT_FOUND, "Este documento es solo un registro de ejemplo y no tiene archivo",
                () -> service.leerArchivo(DocumentoAdjunto.builder().rutaArchivo("").build()));
    }
}
