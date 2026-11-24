package cl.bposeguros.ms_usuarios.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class NotificacionClientTest {

    private HttpServer servidor;

    private NotificacionClient clienteQueResponde(int codigo) throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/notificaciones/clave-temporal", intercambio -> {
            intercambio.sendResponseHeaders(codigo, -1);
            intercambio.close();
        });
        servidor.start();
        return new NotificacionClient("http://localhost:" + servidor.getAddress().getPort());
    }

    @AfterEach
    void apagar() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void enviar_siElServicioAcepta_noLanzaError() throws Exception {
        NotificacionClient cliente = clienteQueResponde(202);

        assertDoesNotThrow(() -> cliente.enviarClaveTemporal("a@b.cl", "Temp#2026", "CREACION", "ASEGURADO"));
    }

    @Test
    void enviar_siElServicioFalla_lanza502() throws Exception {
        NotificacionClient cliente = clienteQueResponde(500);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> cliente.enviarClaveTemporal("a@b.cl", "Temp#2026", "CREACION", "ASEGURADO"));
        assertEquals(HttpStatus.BAD_GATEWAY, e.getStatusCode());
    }

    @Test
    void enviar_siElServicioEstaApagado_lanza503() throws Exception {
        NotificacionClient cliente = clienteQueResponde(202);
        servidor.stop(0);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> cliente.enviarClaveTemporal("a@b.cl", "Temp#2026", "CREACION", "ASEGURADO"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
    }
}
