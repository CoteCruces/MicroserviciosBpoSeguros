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
@Table(name = "companias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "gestiona_vehiculos", nullable = false)
    private boolean gestionaVehiculos;

    @Column(name = "gestiona_hogar", nullable = false)
    private boolean gestionaHogar;

    
    @Column(name = "plazo_dias", nullable = false)
    private int plazoDias;

    @Column(nullable = false)
    private boolean activa;
}