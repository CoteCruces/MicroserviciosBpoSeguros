package cl.bposeguros.ms_siniestros.model;

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
@Table(name = "gestiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gestion {

    public static final String TIPO_NOTA_LIQUIDADOR = "Gestión del liquidador";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "siniestro_id", nullable = false)
    private Long siniestroId;

    @Column(name = "fecha_hora", nullable = false, length = 16)
    private String fechaHora;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false, length = 80)
    private String responsable;

    @Column(name = "visible_asegurado", nullable = false)
    private boolean visibleAsegurado;
}
