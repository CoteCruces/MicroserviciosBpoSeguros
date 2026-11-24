package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NombresTest {

    @ParameterizedTest
    @CsvSource({"Paulina Fernández Ruiz,Paulina F.", "Paulina,Paulina", "'  Paulina   Fernández ',Paulina F."})
    void abreviado_dejaNombreEInicial(String nombre, String esperado) {
        assertEquals(esperado, Nombres.abreviado(nombre));
    }

    @Test
    void abreviado_vacio_devuelveVacio() {
        assertEquals("", Nombres.abreviado(null));
        assertEquals("", Nombres.abreviado(" "));
    }

    @Test
    void visible_agregaElEquipo() {
        assertEquals("Paulina F. – Equipo Vehículos", Nombres.visible("Paulina Fernández", "Vehículos"));
    }

    @Test
    void visible_sinEquipo_soloMuestraElNombre() {
        assertEquals("Paulina F.", Nombres.visible("Paulina Fernández", ""));
    }

    @Test
    void responsable_muestraInicialApellidoYCodigo() {
        assertEquals("N. Reyes (ADM-01)", Nombres.responsable("Natalia Reyes Campos", "ADM-01"));
    }

    @Test
    void responsable_sinCodigo_omiteElParentesis() {
        assertEquals("N. Reyes", Nombres.responsable("Natalia Reyes", null));
    }

    @Test
    void responsable_sinNombre_devuelveElCodigo() {
        assertEquals("ADM-01", Nombres.responsable("", "ADM-01"));
        assertEquals("", Nombres.responsable(null, null));
    }
}
