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
@Table(name = "documentos_adjuntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "siniestro_id", nullable = false)
    private Long siniestroId;

    @Column(name = "nombre_archivo", nullable = false, length = 150)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 100)
    private String rutaArchivo;

    @Column(name = "tipo_documento", nullable = false, length = 100)
    private String tipoDocumento;

    @Column(name = "fecha_carga", nullable = false, length = 10)
    private String fechaCarga;

    @Column(name = "cargado_por", nullable = false, length = 80)
    private String cargadoPor;

    @Column(nullable = false, length = 300)
    private String comentario;

    @Column(name = "solicitud_id")
    private Long solicitudId;
}