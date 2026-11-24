package cl.bposeguros.ms_notificaciones.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import cl.bposeguros.ms_notificaciones.dto.ClaveTemporalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CorreoServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private CorreoService service;

    @BeforeEach
    void preparar() {
        ReflectionTestUtils.setField(service, "remitente", "bpo@prueba.cl");
    }

    private ClaveTemporalRequest datos(String correo, String clave, String motivo, String rol) {
        return new ClaveTemporalRequest(correo, clave, motivo, rol);
    }

    private SimpleMailMessage enviado() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private void assertRechazado(String mensaje, ClaveTemporalRequest datos) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.enviarClaveTemporal(datos));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertEquals(mensaje, e.getReason());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void enviar_creacionSinRol_usaElTextoDeAsegurado() {
        service.enviarClaveTemporal(datos(" cliente@correo.cl ", "Temp#2026", "CREACION", null));

        SimpleMailMessage correo = enviado();
        assertEquals("Bienvenido a BPO Seguros: tu contraseña temporal", correo.getSubject());
        assertEquals("cliente@correo.cl", correo.getTo()[0]);
        assertEquals("bpo@prueba.cl", correo.getFrom());
        assertTrue(correo.getText().contains("Temp#2026"));
    }

    @Test
    void enviar_creacionDeLiquidador_cambiaElAsunto() {
        service.enviarClaveTemporal(datos("a@b.cl", "Temp#2026", "CREACION", "liquidador"));

        assertEquals("Bienvenido a BPO Seguros: tu cuenta de liquidador", enviado().getSubject());
    }

    @Test
    void enviar_creacionDeAnalista_cambiaElAsunto() {
        service.enviarClaveTemporal(datos("a@b.cl", "Temp#2026", "CREACION", "ANALISTA"));

        assertEquals("Bienvenido a BPO Seguros: tu cuenta de analista", enviado().getSubject());
    }

    @Test
    void enviar_recuperacion_usaElAsuntoDeRecuperacion() {
        service.enviarClaveTemporal(datos("a@b.cl", "Temp#2026", "RECUPERACION", "LIQUIDADOR"));

        assertEquals("BPO Seguros: recuperación de tu contraseña", enviado().getSubject());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "sinarroba.cl", "a@b", "a b@c.cl"})
    void enviar_conCorreoInvalido_seRechaza(String correo) {
        assertRechazado("El correo no es válido", datos(correo, "Temp#2026", "CREACION", null));
    }

    @Test
    void enviar_conCorreoNulo_seRechaza() {
        assertRechazado("El correo no es válido", datos(null, "Temp#2026", "CREACION", null));
    }

    @Test
    void enviar_sinClave_seRechaza() {
        assertRechazado("La clave temporal es obligatoria", datos("a@b.cl", " ", "CREACION", null));
    }

    @Test
    void enviar_conMotivoDesconocido_seRechaza() {
        assertRechazado("El motivo debe ser CREACION o RECUPERACION", datos("a@b.cl", "Temp#2026", "OTRO", null));
    }

    @Test
    void enviar_conRolDesconocido_seRechaza() {
        assertRechazado("El rol debe ser ASEGURADO, LIQUIDADOR o ANALISTA", datos("a@b.cl", "Temp#2026", "CREACION", "JEFE"));
    }

    @Test
    void enviar_siFallaElServidorDeCorreo_lanza502() {
        doThrow(new MailSendException("sin conexión")).when(mailSender).send(any(SimpleMailMessage.class));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.enviarClaveTemporal(datos("a@b.cl", "Temp#2026", "CREACION", null)));
        assertEquals(HttpStatus.BAD_GATEWAY, e.getStatusCode());
    }
}
