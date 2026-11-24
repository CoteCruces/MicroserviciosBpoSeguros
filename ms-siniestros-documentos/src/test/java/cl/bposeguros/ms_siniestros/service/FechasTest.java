package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class FechasTest {

    @Test
    void ahora_tieneFechaYHora() {
        assertTrue(Fechas.ahora().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }

    @Test
    void parsear_correcta_devuelveLaFecha() {
        assertEquals(LocalDate.of(2026, 11, 23), Fechas.parsear(" 2026-11-23 ", "fecha"));
    }

    @Test
    void parsear_vacia_lanza400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> Fechas.parsear("", "fecha desde"));
        assertEquals("Falta la fecha desde", e.getReason());
    }

    @Test
    void parsear_conOtroFormato_lanza400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> Fechas.parsear("23-11-2026", "fecha desde"));
        assertEquals("La fecha desde debe tener formato AAAA-MM-DD", e.getReason());
    }

    @Test
    void diasEntre_cuentaLosDias() {
        assertEquals(18, Fechas.diasEntre("2026-09-03 09:15", "2026-09-21 12:30"));
    }

    @Test
    void diasEntre_sinFin_cuentaHastaHoy() {
        assertEquals(5, Fechas.diasEntre(Fechas.hoy().minusDays(5).toString(), null));
    }

    @Test
    void diasEntre_alReves_nuncaEsNegativo() {
        assertEquals(0, Fechas.diasEntre("2026-09-21", "2026-09-03"));
    }

    @Test
    void diasHabilesEntre_noCuentaElFinDeSemana() {
        LocalDate viernes = LocalDate.of(2026, 11, 20);

        assertEquals(1, Fechas.diasHabilesEntre(viernes, viernes.plusDays(3)));
        assertEquals(5, Fechas.diasHabilesEntre(viernes, viernes.plusDays(7)));
    }

    @Test
    void diasHabilesEntre_mismoDia_esCero() {
        assertEquals(0, Fechas.diasHabilesEntre(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20)));
    }
}
