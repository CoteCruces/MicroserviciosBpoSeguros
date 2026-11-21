package cl.bposeguros.ms_siniestros.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;


public final class Fechas {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Fechas() {
    }

    
    public static String ahora() {
        return LocalDateTime.now(ZONA).format(FORMATO_HORA);
    }

    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    
    public static LocalDate parsear(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la " + campo);
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La " + campo + " debe tener formato AAAA-MM-DD");
        }
    }

    
    public static long diasEntre(String desde, String hasta) {
        LocalDate inicio = LocalDate.parse(desde.substring(0, 10));
        LocalDate fin = (hasta == null) ? hoy() : LocalDate.parse(hasta.substring(0, 10));
        return Math.max(0, ChronoUnit.DAYS.between(inicio, fin));
    }

    
    public static long diasHabilesEntre(LocalDate desde, LocalDate hasta) {
        long cuenta = 0;
        LocalDate dia = desde.plusDays(1);
        while (!dia.isAfter(hasta)) {
            DayOfWeek d = dia.getDayOfWeek();
            if (d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY) {
                cuenta++;
            }
            dia = dia.plusDays(1);
        }
        return cuenta;
    }
}