package cl.bposeguros.ms_usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    public static final long ID_ASEGURADO = 1L;
    public static final long ID_LIQUIDADOR = 2L;
    public static final long ID_ANALISTA = 3L;

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
}
