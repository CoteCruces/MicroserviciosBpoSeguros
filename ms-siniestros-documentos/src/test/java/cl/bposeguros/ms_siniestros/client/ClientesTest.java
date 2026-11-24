package cl.bposeguros.ms_siniestros.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ClientesTest {

    private HttpServer servidor;
    private String url;

    @BeforeEach
    void encender() throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        url = "http://localhost:" + servidor.getAddress().getPort();
        servidor.start();
    }

    @AfterEach
    void apagar() {
        servidor.stop(0);
    }

    private void responder(String ruta, int codigo, String json) {
        servidor.createContext(ruta, intercambio -> {
            byte[] cuerpo = json.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(codigo, cuerpo.length == 0 ? -1 : cuerpo.length);
            intercambio.getResponseBody().write(cuerpo);
            intercambio.close();
        });
    }

    @Test
    void usuario_existente_seConvierte() {
        responder("/api/usuarios/9", 200, "{\"id\":9,\"nombre\":\"Paulina\",\"activo\":true,\"rolId\":2}");

        assertEquals("Paulina", new UsuarioClient(url).obtener(9L).nombre());
    }

    @Test
    void usuario_inexistente_devuelveNulo() {
        responder("/api/usuarios/99", 404, "{}");

        assertNull(new UsuarioClient(url).obtener(99L));
    }

    @Test
    void usuariosPorRol_devuelveLaLista() {
        responder("/api/usuarios", 200, "[{\"id\":9,\"activo\":true},{\"id\":10,\"activo\":true}]");

        assertEquals(2, new UsuarioClient(url).listarPorRol(2L).size());
    }

    @Test
    void companiaYTipo_seConvierten() {
        responder("/api/companias/1", 200, "{\"id\":1,\"nombre\":\"Cordillera\",\"gestionaVehiculos\":true,\"gestionaHogar\":true,\"plazoDias\":10,\"activa\":true}");
        responder("/api/tipos-siniestro/1", 200, "{\"id\":1,\"linea\":\"VEHICULO\"}");
        CatalogoClient cliente = new CatalogoClient(url);

        assertEquals("Cordillera", cliente.obtenerCompania(1L).nombre());
        assertEquals("VEHICULO", cliente.obtenerTipo(1L).linea());
    }

    @Test
    void companiaYTipo_inexistentes_devuelvenNulo() {
        responder("/api/companias/9", 404, "{}");
        responder("/api/tipos-siniestro/9", 404, "{}");
        CatalogoClient cliente = new CatalogoClient(url);

        assertNull(cliente.obtenerCompania(9L));
        assertNull(cliente.obtenerTipo(9L));
    }

    @Test
    void conElServicioApagado_lanza503() {
        servidor.stop(0);

        assertEquals("El servicio de usuarios no está disponible",
                assertThrows(ResponseStatusException.class, () -> new UsuarioClient(url).obtener(1L)).getReason());
        assertEquals("El servicio de catálogos no está disponible",
                assertThrows(ResponseStatusException.class, () -> new CatalogoClient(url).obtenerTipo(1L)).getReason());
    }
}
