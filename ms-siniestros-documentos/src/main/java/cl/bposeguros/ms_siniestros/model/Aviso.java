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

/** Notificacion para el asegurado. */
@Entity
@Table(name = "avisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "siniestro_id", nullable = false)
    private Long siniestroId;

    @Column(name = "fecha_hora", nullable = false, length = 16)
    private String fechaHora;

    @Column(nullable = false, length = 60)
    private String motivo;

    @Column(nullable = false, length = 300)
    private String mensaje;

    @Column(nullable = false)
    private boolean leido;
}
