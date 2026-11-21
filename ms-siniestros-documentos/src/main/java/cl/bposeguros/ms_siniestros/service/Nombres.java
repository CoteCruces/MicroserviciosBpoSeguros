package cl.bposeguros.ms_siniestros.service;


public final class Nombres {

    private Nombres() {
    }

    /** "Paulina Fernández Ruiz" -> "Paulina F." */
    public static String abreviado(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "";
        }
        String[] partes = nombre.trim().split("\\s+");
        return partes.length >= 2 ? partes[0] + " " + partes[1].charAt(0) + "." : partes[0];
    }

    /** "Paulina F. – Equipo Vehículos" (lo que ve el asegurado). */
    public static String visible(String nombre, String equipo) {
        String abreviado = abreviado(nombre);
        return (equipo == null || equipo.isBlank()) ? abreviado : abreviado + " – Equipo " + equipo;
    }

    /** "Natalia Reyes Campos", "ADM-01" -> "N. Reyes (ADM-01)" */
    public static String responsable(String nombre, String codigo) {
        if (nombre == null || nombre.isBlank()) {
            return codigo == null ? "" : codigo;
        }
        String[] partes = nombre.trim().split("\\s+");
        String corto = partes.length >= 2 ? partes[0].charAt(0) + ". " + partes[1] : partes[0];
        return (codigo == null || codigo.isBlank()) ? corto : corto + " (" + codigo + ")";
    }
}