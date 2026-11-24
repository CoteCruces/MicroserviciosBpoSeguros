package cl.bposeguros.ms_usuarios.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class AlmacenamientoServiceTest {

    @TempDir
    private Path carpeta;

    private AlmacenamientoService service;

    @BeforeEach
    void preparar() {
        service = new AlmacenamientoService(carpeta.resolve("avatares").toString());
    }

    private MockMultipartFile imagen() {
        return new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    void guardarYLeer_devuelveLosMismosBytes() {
        String nombre = service.guardar(imagen());

        assertTrue(nombre.endsWith(".jpg"));
        assertArrayEquals(new byte[]{1, 2, 3}, service.leer(nombre));
    }

    @Test
    void guardar_archivoVacio_lanza400() {
        MockMultipartFile vacio = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", new byte[0]);

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.guardar(vacio));
        assertEquals("No se recibió ninguna imagen", e.getReason());
    }

    @Test
    void guardar_queNoEsImagen_lanza400() {
        MockMultipartFile texto = new MockMultipartFile("archivo", "a.txt", "text/plain", new byte[]{1});

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.guardar(texto));
        assertEquals("El archivo debe ser una imagen", e.getReason());
    }

    @Test
    void leer_inexistente_lanza404() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.leer("nada.jpg"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void eliminar_borraElArchivo() {
        String nombre = service.guardar(imagen());

        service.eliminar(nombre);

        assertThrows(ResponseStatusException.class, () -> service.leer(nombre));
    }

    @Test
    void eliminar_conNuloOInexistente_noFalla() {
        assertDoesNotThrow(() -> service.eliminar(null));
        assertDoesNotThrow(() -> service.eliminar("nada.jpg"));
    }
}
