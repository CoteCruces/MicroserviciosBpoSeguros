package cl.bposeguros.ms_usuarios.config;

import cl.bposeguros.ms_usuarios.model.Rol;
import cl.bposeguros.ms_usuarios.model.Usuario;
import cl.bposeguros.ms_usuarios.repository.RolRepository;
import cl.bposeguros.ms_usuarios.repository.UsuarioRepository;
import cl.bposeguros.ms_usuarios.service.ClaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private static final String CLAVE_DEMO = "Tem_sin2026%";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClaveService claveService;

    @Override
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            rolRepository.saveAll(List.of(
                    Rol.builder().id(Rol.ID_ASEGURADO).nombre("Asegurado").build(),
                    Rol.builder().id(Rol.ID_LIQUIDADOR).nombre("Liquidador").build(),
                    Rol.builder().id(Rol.ID_ANALISTA).nombre("Analista").build()
            ));
        }

        if (usuarioRepository.count() == 0) {
            
            usuarioRepository.saveAll(List.of(
                    
                    asegurado("158423715", "Camila Andrea Rojas Fuentes", "camila.rojas@correo.ejemplo", "Maipú"),
                    asegurado("94185521", "Jorge Luis Pizarro Contreras", "jorge.pizarro@correo.ejemplo", "Puente Alto"),
                    asegurado("192058848", "Valentina Soto Muñoz", "vale.soto@correo.ejemplo", "Ñuñoa"),
                    asegurado("78362197", "Héctor Manuel Araya Díaz", "h.araya@correo.ejemplo", "Viña del Mar"),
                    asegurado("175520937", "Daniela Paz Vergara Lagos", "daniela.vergara@correo.ejemplo", "La Florida"),
                    asegurado("136647083", "Ricardo Ignacio Espinoza Tapia", "r.espinoza@correo.ejemplo", "Concepción"),
                    asegurado("201184657", "Francisca Belén Morales Henríquez", "fran.morales@correo.ejemplo", "Quilicura"),
                    asegurado("112903372", "Luis Alberto Carrasco Peña", "luis.carrasco@correo.ejemplo", "Rancagua"),
                    
                    liquidador("162345672", "Paulina Fernández Ruiz", "paulina.fernandez@andes.ejemplo", "LIQ-01", "Vehículos", true),
                    liquidador("138765431", "Matías Gallardo Silva", "matias.gallardo@andes.ejemplo", "LIQ-02", "Hogar", true),
                    liquidador("183459120", "Rodrigo Salinas Ortega", "rodrigo.salinas@andes.ejemplo", "LIQ-03", "Vehículos", true),
                    liquidador("145678900", "Constanza Ibarra Núñez", "constanza.ibarra@andes.ejemplo", "LIQ-04", "Hogar", true),
                    liquidador("11223344K", "Andrés Bravo Medina", "andres.bravo@andes.ejemplo", "LIQ-05", "Vehículos", false),
                    
                    analista("198761230", "Natalia Reyes Campos", "natalia.reyes@andes.ejemplo", "ADM-01"),
                    analista("109876542", "Felipe Cortés Valdés", "felipe.cortes@andes.ejemplo", "ADM-02")
            ));
        }
    }

    private Usuario asegurado(String rut, String nombre, String correo, String comuna) {
        return base(rut, nombre, correo, Rol.ID_ASEGURADO).comuna(comuna).build();
    }

    private Usuario liquidador(String rut, String nombre, String correo, String codigo, String equipo, boolean activo) {
        return base(rut, nombre, correo, Rol.ID_LIQUIDADOR).codigo(codigo).equipo(equipo).activo(activo).build();
    }

    private Usuario analista(String rut, String nombre, String correo, String codigo) {
        return base(rut, nombre, correo, Rol.ID_ANALISTA).codigo(codigo).equipo("Administración").build();
    }

    
    private Usuario.UsuarioBuilder base(String rut, String nombre, String correo, Long rolId) {
        return Usuario.builder()
                .rut(rut)
                .nombre(nombre)
                .correo(correo)
                .contrasena(claveService.hash(CLAVE_DEMO))
                .claveTemporal(false)
                .comuna("")
                .telefono("")
                .fechaNacimiento("")
                .codigo("")
                .equipo("")
                .activo(true)
                .rolId(rolId);
    }
}
