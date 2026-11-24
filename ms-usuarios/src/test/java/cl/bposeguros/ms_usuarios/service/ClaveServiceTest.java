package cl.bposeguros.ms_usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClaveServiceTest {

    private final ClaveService service = new ClaveService();

    @Test
    void generarTemporal_cumpleLasReglasDeUnaClaveSegura() {
        for (int i = 0; i < 50; i++) {
            String clave = service.generarTemporal();

            assertEquals(12, clave.length());
            assertNull(Validaciones.errorClaveNueva(clave));
        }
    }

    @Test
    void generarTemporal_dosVeces_daClavesDistintas() {
        assertNotEquals(service.generarTemporal(), service.generarTemporal());
    }

    @Test
    void hash_noGuardaLaClaveEnTextoPlano() {
        String hash = service.hash("Clave#2026");

        assertTrue(hash.startsWith("pbkdf2$"));
        assertFalse(hash.contains("Clave#2026"));
    }

    @Test
    void hash_deLaMismaClave_esDistintoPorLaSal() {
        assertNotEquals(service.hash("Clave#2026"), service.hash("Clave#2026"));
    }

    @Test
    void verificar_conLaClaveCorrecta_devuelveTrue() {
        assertTrue(service.verificar("Clave#2026", service.hash("Clave#2026")));
    }

    @Test
    void verificar_conOtraClave_devuelveFalse() {
        assertFalse(service.verificar("Otra#2026", service.hash("Clave#2026")));
    }

    @Test
    void verificar_conNulos_devuelveFalse() {
        assertFalse(service.verificar(null, service.hash("Clave#2026")));
        assertFalse(service.verificar("Clave#2026", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "texto", "md5$1$abc$def", "pbkdf2$abc$def", "pbkdf2$x$abc$def"})
    void verificar_conHashMalFormado_devuelveFalse(String almacenado) {
        assertFalse(service.verificar("Clave#2026", almacenado));
    }
}
