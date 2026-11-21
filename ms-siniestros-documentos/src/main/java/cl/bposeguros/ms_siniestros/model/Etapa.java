package cl.bposeguros.ms_siniestros.model;

import java.util.List;


public enum Etapa {
    RECIBIDO(1, "Recibido"),
    EN_EVALUACION(2, "En evaluación"),
    EN_LIQUIDACION(3, "En liquidación"),
    CERRADO(4, "Cerrado");

    private final int id;
    private final String etiqueta;

    Etapa(int id, String etiqueta) {
        this.id = id;
        this.etiqueta = etiqueta;
    }

    public int getId() {
        return id;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Etapa desdeId(int id) {
        for (Etapa e : values()) {
            if (e.id == id) {
                return e;
            }
        }
        throw new IllegalArgumentException("Etapa desconocida: " + id);
    }

    
    public List<String> resultadosPermitidos() {
        return switch (this) {
            case RECIBIDO -> List.of("DESISTIDO");
            case EN_EVALUACION -> List.of("RECHAZADO", "DESISTIDO");
            case EN_LIQUIDACION -> List.of("APROBADO", "RECHAZADO");
            case CERRADO -> List.of();
        };
    }
}
