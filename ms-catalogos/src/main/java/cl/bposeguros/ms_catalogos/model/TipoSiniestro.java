package cl.bposeguros.ms_catalogos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipos_siniestro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoSiniestro {

    public static final String LINEA_VEHICULO = "VEHICULO";
    public static final String LINEA_HOGAR = "HOGAR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** VEHICULO u HOGAR. */
    @Column(nullable = false, length = 10)
    private String linea;
}