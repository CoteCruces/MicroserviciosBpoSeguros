package cl.bposeguros.ms_usuarios.service;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Pattern;


public final class Validaciones {

    private static final Pattern FORMATO_CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validaciones() {
    }

    
    public static String limpiarRut(String rut) {
        if (rut == null) {
            return "";
        }
        return rut.replace(".", "").replace("-", "").trim().toUpperCase();
    }

    
    public static String errorRut(String rut) {
        if (rut == null || rut.isBlank()) {
            return "El RUT es obligatorio";
        }
        String limpio = limpiarRut(rut);
        if (limpio.length() < 8 || limpio.length() > 9) {
            return "El RUT no es válido. Revisa el dígito verificador";
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char digitoVerificador = limpio.charAt(limpio.length() - 1);
        for (char c : cuerpo.toCharArray()) {
            if (!Character.isDigit(c) || c > '9') {
                return "El RUT no es válido. Revisa el dígito verificador";
            }
        }
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = (resto == 11) ? '0' : (resto == 10) ? 'K' : (char) ('0' + resto);
        return digitoVerificador == esperado ? null : "El RUT no es válido. Revisa el dígito verificador";
    }

    

    public static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    public static String errorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "El correo es obligatorio";
        }
        String limpio = correo.trim();
        if (limpio.length() > 100 || !FORMATO_CORREO.matcher(limpio).matches()) {
            return "Ingresa un correo válido (ejemplo: usuario@dominio.com)";
        }
        return null;
    }

    

    
    public static String errorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "El nombre es obligatorio";
        }
        String limpio = nombre.trim();
        if (limpio.length() > 100) {
            return "El nombre es demasiado largo (máximo 100 caracteres)";
        }
        for (char c : limpio.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                return "El nombre no puede tener números ni símbolos";
            }
        }
        String[] palabras = limpio.split("\\s+");
        if (palabras.length < 2) {
            return "Escribe nombre y apellido";
        }
        for (String palabra : palabras) {
            if (palabra.length() < 2) {
                return "Cada palabra debe tener al menos 2 letras";
            }
        }
        return null;
    }

    

    
    public static String errorTelefono(String telefono) {
        String digitos = soloDigitos(telefono);
        boolean valido = (digitos.length() == 11 && digitos.startsWith("569"))
                || (digitos.length() == 9 && digitos.startsWith("9"));
        return valido ? null : "Ingresa un celular válido (ejemplo: +56 9 12345678)";
    }

    private static String soloDigitos(String texto) {
        StringBuilder sb = new StringBuilder();
        if (texto != null) {
            for (char c : texto.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    

    
    public static String errorFechaNacimiento(String fecha, LocalDate hoy) {
        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(fecha == null ? "" : fecha.trim());
        } catch (DateTimeException e) {
            return "La fecha debe tener el formato AAAA-MM-DD";
        }
        if (nacimiento.isAfter(hoy)) {
            return "La fecha de nacimiento no puede ser futura";
        }
        if (nacimiento.isAfter(hoy.minusYears(18))) {
            return "Debes tener al menos 18 años";
        }
        if (nacimiento.isBefore(hoy.minusYears(120))) {
            return "Revisa el año: no puede ser de hace más de 120 años";
        }
        return null;
    }

    

    
    public static String equipoCanonico(String equipo) {
        if (equipo == null) {
            return null;
        }
        String sinTildes = Normalizer.normalize(equipo.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return switch (sinTildes) {
            case "vehiculos" -> "Vehículos";
            case "hogar" -> "Hogar";
            default -> null;
        };
    }

    
    public static String errorClaveNueva(String clave) {
        if (clave == null || clave.isBlank()) {
            return "La contraseña es obligatoria";
        }
        if (clave.length() < 8) {
            return "Debe tener al menos 8 caracteres";
        }
        if (clave.length() > 64) {
            return "Debe tener como máximo 64 caracteres";
        }
        if (clave.chars().noneMatch(Character::isUpperCase)) {
            return "Debe incluir al menos una mayúscula";
        }
        if (clave.chars().noneMatch(Character::isDigit)) {
            return "Debe incluir al menos un número";
        }
        if (clave.chars().allMatch(Character::isLetterOrDigit)) {
            return "Debe incluir al menos un carácter especial (por ejemplo @, # o !)";
        }
        return null;
    }
}
