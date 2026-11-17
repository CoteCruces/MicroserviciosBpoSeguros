package cl.bposeguros.ms_usuarios.controller;

import cl.bposeguros.ms_usuarios.dto.ActualizarPerfilRequest;
import cl.bposeguros.ms_usuarios.dto.CambiarContrasenaRequest;
import cl.bposeguros.ms_usuarios.dto.CambiarEstadoRequest;
import cl.bposeguros.ms_usuarios.dto.CrearLiquidadorRequest;
import cl.bposeguros.ms_usuarios.dto.LoginRequest;
import cl.bposeguros.ms_usuarios.dto.RecuperarClaveRequest;
import cl.bposeguros.ms_usuarios.dto.RegistroAseguradoRequest;
import cl.bposeguros.ms_usuarios.dto.RespuestaDto;
import cl.bposeguros.ms_usuarios.dto.UsuarioResponse;
import cl.bposeguros.ms_usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Long rol,
                                        @RequestParam(required = false) String buscar) {
        return usuarioService.listar(rol, buscar);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping("/asegurados")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearAsegurado(@RequestBody RegistroAseguradoRequest datos) {
        return usuarioService.crearAsegurado(datos);
    }

    @PostMapping("/liquidadores")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearLiquidador(@RequestBody CrearLiquidadorRequest datos) {
        return usuarioService.crearLiquidador(datos);
    }

    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(@RequestBody LoginRequest datos) {
        return usuarioService.iniciarSesion(datos);
    }

    @PostMapping("/recuperar-clave")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RespuestaDto recuperarClave(@RequestBody RecuperarClaveRequest datos) {
        usuarioService.recuperarClave(datos);
        return new RespuestaDto("Si el correo está registrado, enviamos una contraseña temporal");
    }

    @PutMapping("/{id}/perfil")
    public UsuarioResponse actualizarPerfil(@PathVariable Long id, @RequestBody ActualizarPerfilRequest datos) {
        return usuarioService.actualizarPerfil(id, datos);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable Long id, @RequestBody CambiarContrasenaRequest datos) {
        usuarioService.cambiarContrasena(id, datos);
    }

    @PutMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @RequestBody CambiarEstadoRequest datos) {
        return usuarioService.cambiarEstado(id, datos);
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UsuarioResponse subirAvatar(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        return usuarioService.subirAvatar(id, archivo);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> obtenerAvatar(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(usuarioService.obtenerAvatar(id));
    }
}
