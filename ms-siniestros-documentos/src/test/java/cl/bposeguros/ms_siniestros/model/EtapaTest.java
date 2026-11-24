package cl.bposeguros.ms_siniestros.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EtapaTest {

    @ParameterizedTest
    @CsvSource({"1,RECIBIDO", "2,EN_EVALUACION", "3,EN_LIQUIDACION", "4,CERRADO"})
    void desdeId_devuelveLaEtapa(int id, Etapa esperada) {
        assertEquals(esperada, Etapa.desdeId(id));
        assertEquals(id, esperada.getId());
    }

    @Test
    void desdeId_desconocido_lanzaError() {
        assertThrows(IllegalArgumentException.class, () -> Etapa.desdeId(9));
    }

    @Test
    void resultadosPermitidos_dependenDeLaEtapa() {
        assertEquals(List.of("DESISTIDO"), Etapa.RECIBIDO.resultadosPermitidos());
        assertEquals(List.of("RECHAZADO", "DESISTIDO"), Etapa.EN_EVALUACION.resultadosPermitidos());
        assertEquals(List.of("APROBADO", "RECHAZADO"), Etapa.EN_LIQUIDACION.resultadosPermitidos());
        assertTrue(Etapa.CERRADO.resultadosPermitidos().isEmpty());
    }

    @Test
    void etiqueta_esElTextoParaMostrar() {
        assertEquals("En evaluación", Etapa.EN_EVALUACION.getEtiqueta());
    }
}
