package cl.bposeguros.ms_usuarios.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bposeguros.ms_usuarios.client.NotificacionClient;
import cl.bposeguros.ms_usuarios.dto.ActualizarPerfilRequest;
import cl.bposeguros.ms_usuarios.dto.CambiarContrasenaRequest;
import cl.bposeguros.ms_usuarios.dto.CambiarEstadoRequest;
import cl.bposeguros.ms_usuarios.dto.CrearLiquidadorRequest;
import cl.bposeguros.ms_usuarios.dto.LoginRequest;
import cl.bposeguros.ms_usuarios.dto.RecuperarClaveRequest;
import cl.bposeguros.ms_usuarios.dto.RegistroAseguradoRequest;
import cl.bposeguros.ms_usuarios.dto.UsuarioResponse;
import cl.bposeguros.ms_usuarios.model.Rol;
import cl.bposeguros.ms_usuarios.model.Usuario;
import cl.bposeguros.ms_usuarios.repository.RolRepository;
import cl.bposeguros.ms_usuarios.repository.UsuarioRepository;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private ClaveService claveService;

    @Mock
    private NotificacionClient notificacionClient;

    @Mock
    private AlmacenamientoService almacenamientoService;

    @InjectMocks
    private UsuarioService service;

    @BeforeEach
    void preparar() {
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(rolRepository.findById(anyLong())).thenReturn(Optional.empty());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void limpiar() {
        RequestContextHolder.resetRequestAttributes();
    }

    private Usuario usuario(Long id, Long rolId) {
        return Usuario.builder().id(id).rut("158423715").nombre("Camila Rojas").correo("camila@correo.cl")
                .contrasena("hash").comuna("").telefono("").fechaNacimiento("").codigo("").equipo("")
                .activo(true).rolId(rolId).build();
    }

    private void existe(Usuario u) {
        when(usuarioRepository.findById(u.getId())).thenReturn(Optional.of(u));
    }

    private ActualizarPerfilRequest perfil(String nombre, String correo, String telefono, String fecha) {
        return new ActualizarPerfilRequest(nombre, correo, telefono, "Maipú", fecha);
    }

    private void assertEstado(HttpStatus esperado, String mensaje, org.junit.jupiter.api.function.Executable accion) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, accion);
        assertEquals(esperado, e.getStatusCode());
        assertEquals(mensaje, e.getReason());
    }

    @Test
    void listar_sinRol_devuelveTodos() {
        when(usuarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(usuario(1L, 1L), usuario(2L, 2L)));

        assertEquals(2, service.listar(null, null).size());
    }

    @Test
    void listar_conRolYTexto_filtraPorNombre() {
        Usuario otro = usuario(2L, 1L);
        otro.setNombre("Jorge Pizarro");
        otro.setCorreo("jorge@correo.cl");
        otro.setRut("94185521");
        when(usuarioRepository.findByRolIdOrderByNombreAsc(1L)).thenReturn(List.of(usuario(1L, 1L), otro));

        List<UsuarioResponse> lista = service.listar(1L, " JORGE ");

        assertEquals(1, lista.size());
        assertEquals("Jorge Pizarro", lista.get(0).nombre());
    }

    @Test
    void listar_buscandoRutConPuntos_loEncuentra() {
        when(usuarioRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(usuario(1L, 1L)));

        assertEquals(1, service.listar(null, "15.842.371-5").size());
    }

    @Test
    void obtener_existente_loDevuelveConSuRol() {
        existe(usuario(1L, 1L));
        when(rolRepository.findById(1L)).thenReturn(Optional.of(Rol.builder().id(1L).nombre("Asegurado").build()));

        UsuarioResponse r = service.obtener(1L);

        assertEquals("Asegurado", r.rolNombre());
        assertFalse(r.perfilCompleto());
        assertNull(r.avatarUrl());
    }

    @Test
    void obtener_inexistente_lanza404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertEstado(HttpStatus.NOT_FOUND, "Usuario no encontrado", () -> service.obtener(99L));
    }

    @Test
    void obtener_conTodosLosDatos_marcaElPerfilCompleto() {
        Usuario u = usuario(1L, 1L);
        u.setTelefono("912345678");
        u.setComuna("Maipú");
        u.setFechaNacimiento("1990-05-10");
        existe(u);

        assertTrue(service.obtener(1L).perfilCompleto());
    }

    @Test
    void crearAsegurado_correcto_guardaYEnviaLaClave() {
        existe(usuario(9L, Rol.ID_LIQUIDADOR));
        when(claveService.generarTemporal()).thenReturn("Temp#2026");
        when(claveService.hash("Temp#2026")).thenReturn("hash-temporal");

        UsuarioResponse r = service.crearAsegurado(new RegistroAseguradoRequest(9L, "15.842.371-5", " Camila@Correo.cl "));

        assertEquals("158423715", r.rut());
        assertEquals("camila@correo.cl", r.correo());
        assertTrue(r.claveTemporal());
        verify(notificacionClient).enviarClaveTemporal("camila@correo.cl", "Temp#2026", "CREACION", "ASEGURADO");
    }

    @Test
    void crearAsegurado_sinSolicitante_lanza400() {
        assertEstado(HttpStatus.BAD_REQUEST, "Falta indicar quién realiza la acción",
                () -> service.crearAsegurado(new RegistroAseguradoRequest(null, "15.842.371-5", "a@b.cl")));
    }

    @Test
    void crearAsegurado_siNoEsLiquidador_lanza403() {
        existe(usuario(1L, Rol.ID_ASEGURADO));

        assertEstado(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción",
                () -> service.crearAsegurado(new RegistroAseguradoRequest(1L, "15.842.371-5", "a@b.cl")));
    }

    @Test
    void crearAsegurado_siElLiquidadorEstaBloqueado_lanza403() {
        Usuario bloqueado = usuario(9L, Rol.ID_LIQUIDADOR);
        bloqueado.setActivo(false);
        existe(bloqueado);

        assertEstado(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción",
                () -> service.crearAsegurado(new RegistroAseguradoRequest(9L, "15.842.371-5", "a@b.cl")));
    }

    @Test
    void crearAsegurado_conRutInvalido_lanza400() {
        existe(usuario(9L, Rol.ID_LIQUIDADOR));

        assertEstado(HttpStatus.BAD_REQUEST, "El RUT no es válido. Revisa el dígito verificador",
                () -> service.crearAsegurado(new RegistroAseguradoRequest(9L, "15.842.371-0", "a@b.cl")));
    }

    @Test
    void crearAsegurado_conRutRepetido_lanza409YNoEnviaCorreo() {
        existe(usuario(9L, Rol.ID_LIQUIDADOR));
        when(usuarioRepository.existsByRut("158423715")).thenReturn(true);

        assertEstado(HttpStatus.CONFLICT, "Ya existe una cuenta con ese RUT",
                () -> service.crearAsegurado(new RegistroAseguradoRequest(9L, "15.842.371-5", "a@b.cl")));
        verify(notificacionClient, never()).enviarClaveTemporal(any(), any(), any(), any());
    }

    @Test
    void crearAsegurado_conCorreoRepetido_lanza409() {
        existe(usuario(9L, Rol.ID_LIQUIDADOR));
        when(usuarioRepository.existsByCorreo("a@b.cl")).thenReturn(true);

        assertEstado(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo",
                () -> service.crearAsegurado(new RegistroAseguradoRequest(9L, "15.842.371-5", "a@b.cl")));
    }

    @Test
    void crearLiquidador_correcto_generaElSiguienteCodigo() {
        existe(usuario(14L, Rol.ID_ANALISTA));
        Usuario existente = usuario(13L, Rol.ID_LIQUIDADOR);
        existente.setCodigo("LIQ-05");
        when(usuarioRepository.findByRolId(Rol.ID_LIQUIDADOR)).thenReturn(List.of(existente));
        when(claveService.generarTemporal()).thenReturn("Temp#2026");
        when(claveService.hash("Temp#2026")).thenReturn("h");

        UsuarioResponse r = service.crearLiquidador(
                new CrearLiquidadorRequest(14L, "11.111.111-1", "  Luis   Soto ", "luis@correo.cl", "hogar"));

        assertEquals("LIQ-06", r.codigo());
        assertEquals("Luis Soto", r.nombre());
        assertEquals("Hogar", r.equipo());
        verify(notificacionClient).enviarClaveTemporal("luis@correo.cl", "Temp#2026", "CREACION", "LIQUIDADOR");
    }

    @Test
    void crearLiquidador_siNoEsAnalista_lanza403() {
        existe(usuario(9L, Rol.ID_LIQUIDADOR));

        assertEstado(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción",
                () -> service.crearLiquidador(new CrearLiquidadorRequest(9L, "11.111.111-1", "Luis Soto", "a@b.cl", "Hogar")));
    }

    @Test
    void crearLiquidador_conEquipoDesconocido_lanza400() {
        existe(usuario(14L, Rol.ID_ANALISTA));

        assertEstado(HttpStatus.BAD_REQUEST, "El equipo debe ser Vehículos u Hogar",
                () -> service.crearLiquidador(new CrearLiquidadorRequest(14L, "11.111.111-1", "Luis Soto", "a@b.cl", "Barcos")));
    }

    @Test
    void cambiarEstado_bloqueaAlLiquidador() {
        existe(usuario(14L, Rol.ID_ANALISTA));
        existe(usuario(9L, Rol.ID_LIQUIDADOR));

        assertFalse(service.cambiarEstado(9L, new CambiarEstadoRequest(14L, false)).activo());
    }

    @Test
    void cambiarEstado_sinIndicarElEstado_lanza400() {
        existe(usuario(14L, Rol.ID_ANALISTA));

        assertEstado(HttpStatus.BAD_REQUEST, "Indica si la cuenta queda activa o bloqueada",
                () -> service.cambiarEstado(9L, new CambiarEstadoRequest(14L, null)));
    }

    @Test
    void cambiarEstado_deUnAsegurado_lanza403() {
        existe(usuario(14L, Rol.ID_ANALISTA));
        existe(usuario(1L, Rol.ID_ASEGURADO));

        assertEstado(HttpStatus.FORBIDDEN, "Solo se puede bloquear o habilitar a liquidadores",
                () -> service.cambiarEstado(1L, new CambiarEstadoRequest(14L, false)));
    }

    @Test
    void iniciarSesion_correcto_devuelveAlUsuario() {
        when(usuarioRepository.findByRut("158423715")).thenReturn(Optional.of(usuario(1L, 1L)));
        when(claveService.verificar("Clave#2026", "hash")).thenReturn(true);

        assertEquals(1L, service.iniciarSesion(new LoginRequest("15.842.371-5", "Clave#2026")).id());
    }

    @Test
    void iniciarSesion_conClaveEquivocada_lanza401() {
        when(usuarioRepository.findByRut("158423715")).thenReturn(Optional.of(usuario(1L, 1L)));
        when(claveService.verificar("Mala", "hash")).thenReturn(false);

        assertEstado(HttpStatus.UNAUTHORIZED, "RUT o contraseña incorrectos",
                () -> service.iniciarSesion(new LoginRequest("15.842.371-5", "Mala")));
    }

    @Test
    void iniciarSesion_conRutInexistente_lanzaElMismoMensaje() {
        when(usuarioRepository.findByRut("111111111")).thenReturn(Optional.empty());

        assertEstado(HttpStatus.UNAUTHORIZED, "RUT o contraseña incorrectos",
                () -> service.iniciarSesion(new LoginRequest("11.111.111-1", "Clave#2026")));
    }

    @Test
    void iniciarSesion_conCuentaBloqueada_lanza403() {
        Usuario bloqueado = usuario(13L, 2L);
        bloqueado.setActivo(false);
        when(usuarioRepository.findByRut("158423715")).thenReturn(Optional.of(bloqueado));
        when(claveService.verificar("Clave#2026", "hash")).thenReturn(true);

        assertEstado(HttpStatus.FORBIDDEN, "Tu cuenta no está activa. Comunícate con BPO Seguros",
                () -> service.iniciarSesion(new LoginRequest("15.842.371-5", "Clave#2026")));
    }

    @Test
    void recuperarClave_conCorreoRegistrado_enviaUnaTemporal() {
        Usuario u = usuario(1L, 1L);
        when(usuarioRepository.findByCorreo("camila@correo.cl")).thenReturn(Optional.of(u));
        when(claveService.generarTemporal()).thenReturn("Temp#2026");
        when(claveService.hash("Temp#2026")).thenReturn("nuevo-hash");

        service.recuperarClave(new RecuperarClaveRequest("Camila@correo.cl"));

        assertEquals("nuevo-hash", u.getContrasena());
        assertTrue(u.isClaveTemporal());
        verify(notificacionClient).enviarClaveTemporal("camila@correo.cl", "Temp#2026", "RECUPERACION", "ASEGURADO");
    }

    @Test
    void recuperarClave_conCorreoDesconocido_noHaceNada() {
        when(usuarioRepository.findByCorreo("nadie@correo.cl")).thenReturn(Optional.empty());

        service.recuperarClave(new RecuperarClaveRequest("nadie@correo.cl"));

        verify(notificacionClient, never()).enviarClaveTemporal(any(), any(), any(), any());
    }

    @Test
    void recuperarClave_deUnaCuentaBloqueada_noEnviaCorreo() {
        Usuario bloqueado = usuario(13L, 2L);
        bloqueado.setActivo(false);
        when(usuarioRepository.findByCorreo("camila@correo.cl")).thenReturn(Optional.of(bloqueado));

        service.recuperarClave(new RecuperarClaveRequest("camila@correo.cl"));

        verify(notificacionClient, never()).enviarClaveTemporal(any(), any(), any(), any());
    }

    @Test
    void cambiarContrasena_correcta_laGuardaYDejaDeSerTemporal() {
        Usuario u = usuario(1L, 1L);
        u.setClaveTemporal(true);
        existe(u);
        when(claveService.verificar("Vieja#2025", "hash")).thenReturn(true);
        when(claveService.hash("Nueva#2026")).thenReturn("nuevo-hash");

        service.cambiarContrasena(1L, new CambiarContrasenaRequest("Vieja#2025", "Nueva#2026"));

        assertEquals("nuevo-hash", u.getContrasena());
        assertFalse(u.isClaveTemporal());
    }

    @Test
    void cambiarContrasena_conActualIncorrecta_lanza401() {
        existe(usuario(1L, 1L));

        assertEstado(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta",
                () -> service.cambiarContrasena(1L, new CambiarContrasenaRequest("Mala", "Nueva#2026")));
    }

    @Test
    void cambiarContrasena_porLaMisma_lanza400() {
        existe(usuario(1L, 1L));
        when(claveService.verificar("Clave#2026", "hash")).thenReturn(true);

        assertEstado(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser distinta a la actual",
                () -> service.cambiarContrasena(1L, new CambiarContrasenaRequest("Clave#2026", "Clave#2026")));
    }

    @Test
    void cambiarContrasena_porUnaInsegura_lanza400() {
        existe(usuario(1L, 1L));
        when(claveService.verificar("Clave#2026", "hash")).thenReturn(true);

        assertEstado(HttpStatus.BAD_REQUEST, "Debe tener al menos 8 caracteres",
                () -> service.cambiarContrasena(1L, new CambiarContrasenaRequest("Clave#2026", "Ab#1")));
    }

    @Test
    void actualizarPerfil_correcto_guardaLosDatos() {
        existe(usuario(1L, 1L));

        UsuarioResponse r = service.actualizarPerfil(1L,
                perfil(" Camila   Rojas ", "Camila@correo.cl", "+56 9 12345678", "1990-05-10"));

        assertEquals("Camila Rojas", r.nombre());
        assertEquals("camila@correo.cl", r.correo());
        assertEquals("Maipú", r.comuna());
        assertTrue(r.perfilCompleto());
    }

    @Test
    void actualizarPerfil_conCorreoDeOtro_lanza409() {
        existe(usuario(1L, 1L));
        when(usuarioRepository.existsByCorreoAndIdNot("otro@correo.cl", 1L)).thenReturn(true);

        assertEstado(HttpStatus.CONFLICT, "Ya existe otra cuenta con ese correo",
                () -> service.actualizarPerfil(1L, perfil("", "otro@correo.cl", "", "")));
    }

    @Test
    void actualizarPerfil_conTelefonoInvalido_lanza400() {
        existe(usuario(1L, 1L));

        assertEstado(HttpStatus.BAD_REQUEST, "Ingresa un celular válido (ejemplo: +56 9 12345678)",
                () -> service.actualizarPerfil(1L, perfil("", "a@b.cl", "123", "")));
    }

    @Test
    void actualizarPerfil_conComunaMuyLarga_lanza400() {
        existe(usuario(1L, 1L));

        assertEstado(HttpStatus.BAD_REQUEST, "La comuna es demasiado larga", () -> service.actualizarPerfil(1L,
                new ActualizarPerfilRequest("", "a@b.cl", "", "x".repeat(61), "")));
    }

    @Test
    void subirAvatar_guardaLaNuevaYBorraLaAnterior() {
        Usuario u = usuario(1L, 1L);
        u.setAvatar("vieja.jpg");
        existe(u);
        MockMultipartFile foto = new MockMultipartFile("archivo", "f.jpg", "image/jpeg", new byte[]{1});
        when(almacenamientoService.guardar(foto)).thenReturn("nueva.jpg");

        UsuarioResponse r = service.subirAvatar(1L, foto);

        assertEquals("http://localhost/api/usuarios/1/avatar?v=nueva.jpg", r.avatarUrl());
        verify(almacenamientoService).eliminar("vieja.jpg");
    }

    @Test
    void obtenerAvatar_sinFoto_lanza404() {
        existe(usuario(1L, 1L));

        assertEstado(HttpStatus.NOT_FOUND, "El usuario no tiene foto", () -> service.obtenerAvatar(1L));
    }

    @Test
    void obtenerAvatar_conFoto_devuelveLosBytes() {
        Usuario u = usuario(1L, 1L);
        u.setAvatar("foto.jpg");
        existe(u);
        when(almacenamientoService.leer("foto.jpg")).thenReturn(new byte[]{7});

        assertArrayEquals(new byte[]{7}, service.obtenerAvatar(1L));
    }
}
