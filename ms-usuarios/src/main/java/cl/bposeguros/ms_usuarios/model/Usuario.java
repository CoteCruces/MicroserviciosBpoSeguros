package cl.bposeguros.ms_usuarios.model;

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
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(nullable = false, unique = true, length = 9)
    private String rut;

    
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    
    @Column(nullable = false, length = 200)
    private String contrasena;

    
    @Column(name = "clave_temporal", nullable = false)
    private boolean claveTemporal;

    @Column(nullable = false, length = 60)
    private String comuna;

    @Column(nullable = false, length = 20)
    private String telefono;

    
    @Column(name = "fecha_nacimiento", nullable = false, length = 10)
    private String fechaNacimiento;

   
    @Column(nullable = false, length = 10)
    private String codigo;

    
    @Column(nullable = false, length = 30)
    private String equipo;

    
    @Column(nullable = false)
    private boolean activo;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    
    @Column(length = 100)
    private String avatar;
}
