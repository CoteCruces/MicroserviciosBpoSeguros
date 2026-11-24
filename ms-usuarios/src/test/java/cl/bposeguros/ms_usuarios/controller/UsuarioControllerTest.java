package cl.bposeguros.ms_usuarios.controller;

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

import cl.bposeguros.ms_usuarios.config.ManejadorErrores;
import cl.bposeguros.ms_usuarios.dto.CambiarContrasenaRequest;
import cl.bposeguros.ms_usuarios.dto.CambiarEstadoRequest;
import cl.bposeguros.ms_usuarios.dto.RecuperarClaveRequest;
import cl.bposeguros.ms_usuarios.dto.UsuarioResponse;
import cl.bposeguros.ms_usuarios.service.UsuarioService;
import java.util.List;
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
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mvc;

    private final UsuarioResponse camila = new UsuarioResponse(1L, "158423715", "Camila Rojas", "camila@correo.cl",
            "Maipú", "", "", "", "", true, false, false, 1L, "Asegurado", null);

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new ManejadorErrores()).build();
    }

    @Test
    void getUsuarios_enviaLosFiltros() throws Exception {
        when(usuarioService.listar(2L, "ana")).thenReturn(List.of(camila));

        mvc.perform(get("/api/usuarios?rol=2&buscar=ana")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Camila Rojas"));
    }

    @Test
    void getUsuario_inexistente_responde404ConMensaje() throws Exception {
        when(usuarioService.obtener(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mvc.perform(get("/api/usuarios/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    @Test
    void getUsuario_nuncaDevuelveLaContrasena() throws Exception {
        when(usuarioService.obtener(1L)).thenReturn(camila);

        mvc.perform(get("/api/usuarios/1")).andExpect(status().isOk()).andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void postAsegurado_responde201() throws Exception {
        when(usuarioService.crearAsegurado(any())).thenReturn(camila);

        mvc.perform(post("/api/usuarios/asegurados").contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitanteId\":9,\"rut\":\"158423715\",\"correo\":\"a@b.cl\"}")).andExpect(status().isCreated());
    }

    @Test
    void postLiquidador_responde201() throws Exception {
        when(usuarioService.crearLiquidador(any())).thenReturn(camila);

        mvc.perform(post("/api/usuarios/liquidadores").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void postLogin_conError_responde401() throws Exception {
        when(usuarioService.iniciarSesion(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "RUT o contraseña incorrectos"));

        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postRecuperarClave_responde202() throws Exception {
        mvc.perform(post("/api/usuarios/recuperar-clave").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"a@b.cl\"}")).andExpect(status().isAccepted());

        verify(usuarioService).recuperarClave(new RecuperarClaveRequest("a@b.cl"));
    }

    @Test
    void putContrasena_responde204() throws Exception {
        mvc.perform(put("/api/usuarios/1/contrasena").contentType(MediaType.APPLICATION_JSON)
                .content("{\"actual\":\"a\",\"nueva\":\"b\"}")).andExpect(status().isNoContent());

        verify(usuarioService).cambiarContrasena(1L, new CambiarContrasenaRequest("a", "b"));
    }

    @Test
    void putPerfil_responde200() throws Exception {
        when(usuarioService.actualizarPerfil(eq(1L), any())).thenReturn(camila);

        mvc.perform(put("/api/usuarios/1/perfil").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void putEstado_responde200() throws Exception {
        when(usuarioService.cambiarEstado(9L, new CambiarEstadoRequest(14L, false))).thenReturn(camila);

        mvc.perform(put("/api/usuarios/9/estado").contentType(MediaType.APPLICATION_JSON)
                .content("{\"solicitanteId\":14,\"activo\":false}")).andExpect(status().isOk());
    }

    @Test
    void postAvatar_recibeElArchivo() throws Exception {
        when(usuarioService.subirAvatar(eq(1L), any())).thenReturn(camila);

        mvc.perform(multipart("/api/usuarios/1/avatar").file("archivo", new byte[]{1})).andExpect(status().isOk());
    }

    @Test
    void getAvatar_devuelveUnaImagen() throws Exception {
        when(usuarioService.obtenerAvatar(1L)).thenReturn(new byte[]{1, 2});

        mvc.perform(get("/api/usuarios/1/avatar")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }
}
