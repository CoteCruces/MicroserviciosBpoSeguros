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
@Table(name = "solicitudes_documento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "siniestro_id", nullable = false)
    private Long siniestroId;

    @Column(name = "documento_solicitado", nullable = false, length = 100)
    private String documentoSolicitado;

    @Column(nullable = false, length = 300)
    private String comentario;

    @Column(name = "fecha_solicitud", nullable = false, length = 10)
    private String fechaSolicitud;

    @Column(nullable = false)
    private boolean entregado;

    @Column(name = "fecha_entrega", length = 16)
    private String fechaEntrega;
}