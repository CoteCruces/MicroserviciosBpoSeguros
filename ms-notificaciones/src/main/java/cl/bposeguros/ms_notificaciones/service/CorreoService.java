package cl.bposeguros.ms_notificaciones.service;

import cl.bposeguros.ms_notificaciones.dto.ClaveTemporalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorreoService {

    private static final String CREACION = "CREACION";
    private static final String RECUPERACION = "RECUPERACION";
    private static final String ASEGURADO = "ASEGURADO";
    private static final String LIQUIDADOR = "LIQUIDADOR";
    private static final String ANALISTA = "ANALISTA";
    private static final Pattern FORMATO_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final JavaMailSender mailSender;

    
    @Value("${spring.mail.username}")
    private String remitente;

    public void enviarClaveTemporal(ClaveTemporalRequest datos) {
        validar(datos);

        boolean esCreacion = CREACION.equals(datos.motivo());
        String rol = rolDe(datos);

        String asunto;
        String intro;
        if (!esCreacion) {
            asunto = "BPO Seguros: recuperación de tu contraseña";
            intro = "Hola. Recibimos una solicitud para recuperar el acceso a tu cuenta.";
        } else if (LIQUIDADOR.equals(rol)) {
            asunto = "Bienvenido a BPO Seguros: tu cuenta de liquidador";
            intro = "Hola. Creamos tu cuenta de liquidador para que gestiones desde la app BPO Seguros "
                    + "los siniestros que se te asignen.";
        } else if (ANALISTA.equals(rol)) {
            asunto = "Bienvenido a BPO Seguros: tu cuenta de analista";
            intro = "Hola. Creamos tu cuenta de analista para que asignes los siniestros desde la app BPO Seguros.";
        } else {
            asunto = "Bienvenido a BPO Seguros: tu contraseña temporal";
            intro = "Hola. Creamos tu cuenta para que sigas tus siniestros desde la app BPO Seguros.";
        }

        String cuerpo = intro + "\n\n"
                + "Tu contraseña temporal es:\n"
                + datos.claveTemporal() + "\n\n"
                + "Úsala para ingresar a la app. Por seguridad, te pediremos que elijas una contraseña nueva "
                + "en tu primer ingreso.\n\n"
                + "Si no esperabas este correo, ignóralo.\n\n"
                + "Equipo BPO Seguros";

        enviar(datos.correo(), asunto, cuerpo);
    }

    private void validar(ClaveTemporalRequest datos) {
        if (vacio(datos.correo()) || !FORMATO_CORREO.matcher(datos.correo().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo no es válido");
        }
        if (vacio(datos.claveTemporal())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave temporal es obligatoria");
        }
        if (!CREACION.equals(datos.motivo()) && !RECUPERACION.equals(datos.motivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo debe ser CREACION o RECUPERACION");
        }
        String rol = rolDe(datos);
        if (!ASEGURADO.equals(rol) && !LIQUIDADOR.equals(rol) && !ANALISTA.equals(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol debe ser ASEGURADO, LIQUIDADOR o ANALISTA");
        }
    }

    
    private String rolDe(ClaveTemporalRequest datos) {
        return vacio(datos.rol()) ? ASEGURADO : datos.rol().trim().toUpperCase();
    }

    private void enviar(String para, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(para.trim());
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            log.warn("Fallo SMTP: {}", e.getMessage()); 
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo enviar el correo. Inténtalo más tarde");
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
