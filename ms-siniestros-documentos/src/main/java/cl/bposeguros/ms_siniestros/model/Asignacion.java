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

/** Historial de quien trabajo cada caso y durante cuanto tiempo (sirve para auditorias).*/
@Entity
@Table(name = "asignaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "siniestro_id", nullable = false)
    private Long siniestroId;

    @Column(name = "liquidador_id", nullable = false)
    private Long liquidadorId;

    @Column(name = "fecha_inicio", nullable = false, length = 16)
    private String fechaInicio;

    @Column(name = "fecha_fin", length = 16)
    private String fechaFin;

    @Column(name = "motivo_fin", length = 20)
    private String motivoFin;
}