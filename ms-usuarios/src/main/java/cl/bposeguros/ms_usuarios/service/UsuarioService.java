package cl.bposeguros.ms_usuarios.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String MOTIVO_CREACION = "CREACION";
    private static final String MOTIVO_RECUPERACION = "RECUPERACION";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClaveService claveService;
    private final NotificacionClient notificacionClient;
    private final AlmacenamientoService almacenamientoService;

    

    public List<UsuarioResponse> listar(Long rolId, String buscar) {
        List<Usuario> usuarios = (rolId == null)
                ? usuarioRepository.findAllByOrderByNombreAsc()
                : usuarioRepository.findByRolIdOrderByNombreAsc(rolId);

        Map<Long, String> roles = nombresDeRoles();
        String filtro = buscar == null ? "" : buscar.trim().toLowerCase();

        return usuarios.stream()
                .filter(u -> filtro.isEmpty()
                        || u.getNombre().toLowerCase().contains(filtro)
                        || u.getCorreo().toLowerCase().contains(filtro)
                        || u.getRut().toLowerCase().contains(filtro.replace(".", "").replace("-", "")))
                .map(u -> aRespuesta(u, roles.getOrDefault(u.getRolId(), "")))
                .toList();
    }

    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    
    @Transactional
    public UsuarioResponse crearAsegurado(RegistroAseguradoRequest datos) {
        exigirSolicitante(datos.solicitanteId(), Rol.ID_LIQUIDADOR);

        exigirValido(Validaciones.errorRut(datos.rut()));
        exigirValido(Validaciones.errorCorreo(datos.correo()));
        String rut = Validaciones.limpiarRut(datos.rut());
        String correo = Validaciones.normalizarCorreo(datos.correo());
        exigirNoDuplicado(rut, correo);

        String temporal = claveService.generarTemporal();
        Usuario nuevo = usuarioRepository.save(Usuario.builder()
                .rut(rut)
                .nombre("")
                .correo(correo)
                .contrasena(claveService.hash(temporal))
                .claveTemporal(true)
                .comuna("")
                .telefono("")
                .fechaNacimiento("")
                .codigo("")
                .equipo("")
                .activo(true)
                .rolId(Rol.ID_ASEGURADO)
                .build());

        
        notificacionClient.enviarClaveTemporal(correo, temporal, MOTIVO_CREACION, "ASEGURADO");
        return aRespuesta(nuevo);
    }

    
    @Transactional
    public UsuarioResponse crearLiquidador(CrearLiquidadorRequest datos) {
        exigirSolicitante(datos.solicitanteId(), Rol.ID_ANALISTA);

        exigirValido(Validaciones.errorRut(datos.rut()));
        exigirValido(Validaciones.errorNombre(datos.nombre()));
        exigirValido(Validaciones.errorCorreo(datos.correo()));
        String equipo = Validaciones.equipoCanonico(datos.equipo());
        if (equipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo debe ser Vehículos u Hogar");
        }
        String rut = Validaciones.limpiarRut(datos.rut());
        String correo = Validaciones.normalizarCorreo(datos.correo());
        exigirNoDuplicado(rut, correo);

        String temporal = claveService.generarTemporal();
        Usuario nuevo = usuarioRepository.save(Usuario.builder()
                .rut(rut)
                .nombre(datos.nombre().trim().replaceAll("\\s+", " "))
                .correo(correo)
                .contrasena(claveService.hash(temporal))
                .claveTemporal(true)
                .comuna("")
                .telefono("")
                .fechaNacimiento("")
                .codigo(siguienteCodigoLiquidador())
                .equipo(equipo)
                .activo(true)
                .rolId(Rol.ID_LIQUIDADOR)
                .build());

        notificacionClient.enviarClaveTemporal(correo, temporal, MOTIVO_CREACION, "LIQUIDADOR");
        return aRespuesta(nuevo);
    }

    
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, CambiarEstadoRequest datos) {
        exigirSolicitante(datos.solicitanteId(), Rol.ID_ANALISTA);
        if (datos.activo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica si la cuenta queda activa o bloqueada");
        }
        Usuario usuario = buscar(id);
        if (!usuario.getRolId().equals(Rol.ID_LIQUIDADOR)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo se puede bloquear o habilitar a liquidadores");
        }
        usuario.setActivo(datos.activo());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    

        public UsuarioResponse iniciarSesion(LoginRequest datos) {
        String rut = Validaciones.limpiarRut(datos.rut());

        Usuario usuario = usuarioRepository.findByRut(rut)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "RUT incorrecto: no existe una cuenta con ese RUT"));

        if (!claveService.verificar(datos.contrasena(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta");
        }

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta no está activa. Comunícate con BPO Seguros");
        }
        return aRespuesta(usuario);
    }


    @Transactional
    public void recuperarClave(RecuperarClaveRequest datos) {
        exigirValido(Validaciones.errorCorreo(datos.correo()));

        usuarioRepository.findByCorreo(Validaciones.normalizarCorreo(datos.correo()))
                .filter(Usuario::isActivo)
                .ifPresent(usuario -> {
                    String temporal = claveService.generarTemporal();
                    usuario.setContrasena(claveService.hash(temporal));
                    usuario.setClaveTemporal(true);
                    usuarioRepository.save(usuario);
                    notificacionClient.enviarClaveTemporal(
                            usuario.getCorreo(), temporal, MOTIVO_RECUPERACION, nombreDeRol(usuario.getRolId()));
                });
    }

    @Transactional
    public void cambiarContrasena(Long id, CambiarContrasenaRequest datos) {
        Usuario usuario = buscar(id);

        if (!claveService.verificar(datos.actual(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta");
        }
        exigirValido(Validaciones.errorClaveNueva(datos.nueva()));
        if (datos.nueva().equals(datos.actual())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser distinta a la actual");
        }

        usuario.setContrasena(claveService.hash(datos.nueva()));
        usuario.setClaveTemporal(false);
        usuarioRepository.save(usuario);
    }

    

    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, ActualizarPerfilRequest datos) {
        Usuario usuario = buscar(id);

        exigirValido(Validaciones.errorCorreo(datos.correo()));
        String correo = Validaciones.normalizarCorreo(datos.correo());
        if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otra cuenta con ese correo");
        }

        String nombre = vacio(datos.nombre()) ? "" : datos.nombre().trim().replaceAll("\\s+", " ");
        if (!nombre.isEmpty()) {
            exigirValido(Validaciones.errorNombre(nombre));
        }
        String telefono = vacio(datos.telefono()) ? "" : datos.telefono().trim();
        if (!telefono.isEmpty()) {
            exigirValido(Validaciones.errorTelefono(telefono));
        }
        String fecha = vacio(datos.fechaNacimiento()) ? "" : datos.fechaNacimiento().trim();
        if (!fecha.isEmpty()) {
            exigirValido(Validaciones.errorFechaNacimiento(fecha, LocalDate.now()));
        }
        
        String comuna = vacio(datos.comuna()) ? "" : datos.comuna().trim();
        if (comuna.length() > 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La comuna es demasiado larga");
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setFechaNacimiento(fecha);
        usuario.setComuna(comuna);
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse subirAvatar(Long id, MultipartFile archivo) {
        Usuario usuario = buscar(id);
        String anterior = usuario.getAvatar();

        usuario.setAvatar(almacenamientoService.guardar(archivo));
        Usuario guardado = usuarioRepository.save(usuario);

        almacenamientoService.eliminar(anterior);
        return aRespuesta(guardado);
    }

    public byte[] obtenerAvatar(Long id) {
        Usuario usuario = buscar(id);
        if (usuario.getAvatar() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no tiene foto");
        }
        return almacenamientoService.leer(usuario.getAvatar());
    }

    

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    
    private void exigirSolicitante(Long solicitanteId, long rolRequerido) {
        if (solicitanteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta indicar quién realiza la acción");
        }
        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción"));
        if (!solicitante.isActivo() || !solicitante.getRolId().equals(rolRequerido)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
        }
    }

    private void exigirValido(String error) {
        if (error != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error);
        }
    }

    private void exigirNoDuplicado(String rut, String correo) {
        if (usuarioRepository.existsByRut(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese RUT");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
    }

    
    private String siguienteCodigoLiquidador() {
        int mayor = usuarioRepository.findByRolId(Rol.ID_LIQUIDADOR).stream()
                .map(Usuario::getCodigo)
                .filter(codigo -> codigo != null && codigo.matches("LIQ-\\d+"))
                .mapToInt(codigo -> Integer.parseInt(codigo.substring(4)))
                .max()
                .orElse(0);
        return String.format("LIQ-%02d", mayor + 1);
    }

    private Map<Long, String> nombresDeRoles() {
        return rolRepository.findAll().stream().collect(Collectors.toMap(Rol::getId, Rol::getNombre));
    }

    
    private String nombreDeRol(Long rolId) {
        return rolRepository.findById(rolId).map(r -> r.getNombre().toUpperCase()).orElse("ASEGURADO");
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        return aRespuesta(usuario, rolRepository.findById(usuario.getRolId()).map(Rol::getNombre).orElse(""));
    }

    private UsuarioResponse aRespuesta(Usuario usuario, String rolNombre) {
        boolean perfilCompleto = !usuario.getNombre().isBlank()
                && !usuario.getTelefono().isBlank()
                && !usuario.getComuna().isBlank()
                && !usuario.getFechaNacimiento().isBlank();
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRut(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getComuna(),
                usuario.getTelefono(),
                usuario.getFechaNacimiento(),
                usuario.getCodigo(),
                usuario.getEquipo(),
                usuario.isActivo(),
                usuario.isClaveTemporal(),
                perfilCompleto,
                usuario.getRolId(),
                rolNombre,
                urlAvatar(usuario)
        );
    }

    private String urlAvatar(Usuario usuario) {
        if (usuario.getAvatar() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/" + usuario.getId() + "/avatar")
                .queryParam("v", usuario.getAvatar())
                .toUriString();
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
