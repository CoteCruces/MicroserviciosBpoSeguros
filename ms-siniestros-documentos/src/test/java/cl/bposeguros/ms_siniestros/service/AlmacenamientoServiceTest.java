package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class AlmacenamientoServiceTest {

    @TempDir
    private Path carpeta;

    private AlmacenamientoService service;

    @BeforeEach
    void preparar() {
        service = new AlmacenamientoService(carpeta.resolve("documentos").toString());
    }

    @ParameterizedTest
    @CsvSource({"image/jpeg,.jpg", "image/png,.png", "application/pdf,.pdf"})
    void guardar_usaLaExtensionDelTipo(String tipo, String extension) {
        String nombre = service.guardar(new MockMultipartFile("archivo", "x", tipo, new byte[]{1, 2}));

        assertTrue(nombre.endsWith(extension));
        assertArrayEquals(new byte[]{1, 2}, service.leer(nombre));
    }

    @Test
    void guardar_nuloOVacio_lanza400() {
        MockMultipartFile vacio = new MockMultipartFile("archivo", "x.jpg", "image/jpeg", new byte[0]);

        assertEquals("No se recibió ningún archivo",
                assertThrows(ResponseStatusException.class, () -> service.guardar(null)).getReason());
        assertEquals("No se recibió ningún archivo",
                assertThrows(ResponseStatusException.class, () -> service.guardar(vacio)).getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/plain", "video/mp4", "application/zip"})
    void guardar_otroTipoDeArchivo_lanza400(String tipo) {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "x", tipo, new byte[]{1});

        assertEquals("El archivo debe ser una imagen o un PDF",
                assertThrows(ResponseStatusException.class, () -> service.guardar(archivo)).getReason());
    }

    @Test
    void leer_inexistente_lanza404() {
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> service.leer("nada.jpg")).getStatusCode());
    }
}
