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

/**
 * No guarda objetos de otros microservicios: solo sus ids (aseguradoId, companiaId, tipoId, liquidadorId).
 * Los nombres se piden por HTTP a ms-usuarios y ms-catalogos cuando se arma la respuesta.*/
@Entity
@Table(name = "siniestros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Siniestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String numero;

    @Column(name = "asegurado_id", nullable = false)
    private Long aseguradoId;

    @Column(name = "compania_id", nullable = false)
    private Long companiaId;

    @Column(name = "tipo_id", nullable = false)
    private Long tipoId;

    @Column(name = "fecha_ocurrencia", nullable = false, length = 10)
    private String fechaOcurrencia;

    @Column(name = "fecha_reporte", nullable = false, length = 10)
    private String fechaReporte;

    @Column(nullable = false, length = 60)
    private String comuna;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    /** 1 Recibido, 2 En evaluacion, 3 En liquidacion, 4 Cerrado (ver Etapa). */
    @Column(nullable = false)
    private int etapa;

    @Column(name = "fecha_cambio_etapa", nullable = false, length = 10)
    private String fechaCambioEtapa;

    
    @Column(name = "liquidador_id")
    private Long liquidadorId;

    
    @Column(name = "fecha_asignacion", length = 16)
    private String fechaAsignacion;

    
    @Column(name = "liquidador_anterior_id")
    private Long liquidadorAnteriorId;

    @Column(name = "fuera_de_plazo", nullable = false)
    private boolean fueraDePlazo;

    /** APROBADO, RECHAZADO o DESISTIDO. */
    @Column(length = 20)
    private String resultado;

    @Column(name = "mensaje_cierre", length = 600)
    private String mensajeCierre;
}