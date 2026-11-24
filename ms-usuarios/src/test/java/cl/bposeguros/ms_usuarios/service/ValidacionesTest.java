package cl.bposeguros.ms_usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidacionesTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 23);

    @ParameterizedTest
    @CsvSource({"15.842.371-5,158423715", "'  11223344-k ',11223344K", "158423715,158423715"})
    void limpiarRut_quitaPuntosGuionYEspacios(String rut, String esperado) {
        assertEquals(esperado, Validaciones.limpiarRut(rut));
    }

    @Test
    void limpiarRut_conNulo_devuelveVacio() {
        assertEquals("", Validaciones.limpiarRut(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"15.842.371-5", "16234567-2", "11.223.344-K", "11.223.344-k", "109876542"})
    void errorRut_conRutValido_devuelveNulo(String rut) {
        assertNull(Validaciones.errorRut(rut));
    }

    @ParameterizedTest
    @ValueSource(strings = {"15.842.371-0", "1234", "1234567890", "ABCDEFGH-1"})
    void errorRut_conRutInvalido_devuelveMensaje(String rut) {
        assertEquals("El RUT no es válido. Revisa el dígito verificador", Validaciones.errorRut(rut));
    }

    @Test
    void errorRut_vacio_esObligatorio() {
        assertEquals("El RUT es obligatorio", Validaciones.errorRut(" "));
        assertEquals("El RUT es obligatorio", Validaciones.errorRut(null));
    }

    @Test
    void normalizarCorreo_quitaEspaciosYMayusculas() {
        assertEquals("camila@correo.cl", Validaciones.normalizarCorreo("  Camila@Correo.CL "));
        assertEquals("", Validaciones.normalizarCorreo(null));
    }

    @Test
    void errorCorreo_valido_devuelveNulo() {
        assertNull(Validaciones.errorCorreo("camila.rojas+1@correo.cl"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"camila", "camila@", "camila@correo", "@correo.cl"})
    void errorCorreo_invalido_devuelveMensaje(String correo) {
        assertEquals("Ingresa un correo válido (ejemplo: usuario@dominio.com)", Validaciones.errorCorreo(correo));
    }

    @Test
    void errorCorreo_muyLargo_devuelveMensaje() {
        assertNotNull(Validaciones.errorCorreo("a".repeat(100) + "@correo.cl"));
    }

    @Test
    void errorCorreo_vacio_esObligatorio() {
        assertEquals("El correo es obligatorio", Validaciones.errorCorreo(""));
    }

    @Test
    void errorNombre_valido_devuelveNulo() {
        assertNull(Validaciones.errorNombre("Camila Núñez"));
    }

    @ParameterizedTest
    @CsvSource({
            "'',El nombre es obligatorio",
            "Camila,Escribe nombre y apellido",
            "Camila 2 Rojas,El nombre no puede tener números ni símbolos",
            "C Rojas,Cada palabra debe tener al menos 2 letras"
    })
    void errorNombre_invalido_devuelveElMensajeQueCorresponde(String nombre, String mensaje) {
        assertEquals(mensaje, Validaciones.errorNombre(nombre));
    }

    @Test
    void errorNombre_muyLargo_devuelveMensaje() {
        assertEquals("El nombre es demasiado largo (máximo 100 caracteres)",
                Validaciones.errorNombre("a".repeat(60) + " " + "b".repeat(60)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+56 9 12345678", "912345678", "56912345678"})
    void errorTelefono_valido_devuelveNulo(String telefono) {
        assertNull(Validaciones.errorTelefono(telefono));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "812345678", "", "+54 9 12345678"})
    void errorTelefono_invalido_devuelveMensaje(String telefono) {
        assertEquals("Ingresa un celular válido (ejemplo: +56 9 12345678)", Validaciones.errorTelefono(telefono));
    }

    @Test
    void errorFechaNacimiento_deUnAdulto_devuelveNulo() {
        assertNull(Validaciones.errorFechaNacimiento("1990-05-10", HOY));
    }

    @Test
    void errorFechaNacimiento_queCumple18Hoy_devuelveNulo() {
        assertNull(Validaciones.errorFechaNacimiento("2008-11-23", HOY));
    }

    @ParameterizedTest
    @CsvSource({
            "2008-11-24,Debes tener al menos 18 años",
            "2027-01-01,La fecha de nacimiento no puede ser futura",
            "1900-01-01,Revisa el año: no puede ser de hace más de 120 años",
            "10-05-1990,La fecha debe tener el formato AAAA-MM-DD"
    })
    void errorFechaNacimiento_invalida_devuelveElMensajeQueCorresponde(String fecha, String mensaje) {
        assertEquals(mensaje, Validaciones.errorFechaNacimiento(fecha, HOY));
    }

    @Test
    void errorFechaNacimiento_nula_devuelveMensajeDeFormato() {
        assertEquals("La fecha debe tener el formato AAAA-MM-DD", Validaciones.errorFechaNacimiento(null, HOY));
    }

    @ParameterizedTest
    @CsvSource({"vehiculos,Vehículos", "VEHÍCULOS,Vehículos", "' Hogar ',Hogar"})
    void equipoCanonico_conocido_devuelveElNombreOficial(String equipo, String esperado) {
        assertEquals(esperado, Validaciones.equipoCanonico(equipo));
    }

    @Test
    void equipoCanonico_desconocido_devuelveNulo() {
        assertNull(Validaciones.equipoCanonico("Barcos"));
        assertNull(Validaciones.equipoCanonico(null));
    }

    @Test
    void errorClaveNueva_segura_devuelveNulo() {
        assertNull(Validaciones.errorClaveNueva("Clave#2026"));
    }

    @ParameterizedTest
    @CsvSource({
            "'',La contraseña es obligatoria",
            "Ab#1,Debe tener al menos 8 caracteres",
            "clave#2026,Debe incluir al menos una mayúscula",
            "ClaveNueva#,Debe incluir al menos un número"
    })
    void errorClaveNueva_insegura_devuelveElMensajeQueCorresponde(String clave, String mensaje) {
        assertEquals(mensaje, Validaciones.errorClaveNueva(clave));
    }

    @Test
    void errorClaveNueva_sinSimbolo_devuelveMensaje() {
        assertEquals("Debe incluir al menos un carácter especial (por ejemplo @, # o !)",
                Validaciones.errorClaveNueva("Clave2026"));
    }

    @Test
    void errorClaveNueva_muyLarga_devuelveMensaje() {
        assertEquals("Debe tener como máximo 64 caracteres", Validaciones.errorClaveNueva("Aa#1" + "a".repeat(61)));
    }
}
