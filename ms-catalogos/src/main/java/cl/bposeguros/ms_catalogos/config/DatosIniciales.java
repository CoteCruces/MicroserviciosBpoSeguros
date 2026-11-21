package cl.bposeguros.ms_catalogos.config;

import cl.bposeguros.ms_catalogos.model.Comuna;
import cl.bposeguros.ms_catalogos.model.Compania;
import cl.bposeguros.ms_catalogos.model.TipoSiniestro;
import cl.bposeguros.ms_catalogos.repository.ComunaRepository;
import cl.bposeguros.ms_catalogos.repository.CompaniaRepository;
import cl.bposeguros.ms_catalogos.repository.TipoSiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final ComunaRepository comunaRepository;
    private final CompaniaRepository companiaRepository;
    private final TipoSiniestroRepository tipoRepository;

    @Override
    public void run(String... args) {
        if (comunaRepository.count() == 0) {
            List<String> nombres = List.of(
                    "Cerrillos", "Cerro Navia", "Colina", "Concepción", "Conchalí", "El Bosque",
                    "Estación Central", "Huechuraba", "Independencia", "La Cisterna", "La Florida",
                    "La Granja", "La Pintana", "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo",
                    "Lo Prado", "Macul", "Maipú", "Ñuñoa", "Pedro Aguirre Cerda", "Peñalolén",
                    "Providencia", "Pudahuel", "Puente Alto", "Quilicura", "Quinta Normal",
                    "Rancagua", "Recoleta", "Renca", "San Bernardo", "San Joaquín", "San Miguel",
                    "San Ramón", "Santiago", "Valparaíso", "Viña del Mar", "Vitacura");
            comunaRepository.saveAll(nombres.stream()
                    .map(n -> Comuna.builder().nombre(n).build())
                    .toList());
        }

        if (companiaRepository.count() == 0) {
            companiaRepository.saveAll(List.of(
                    compania("ASG-01", "Seguros Cordillera", true, true, 10, true),
                    compania("ASG-02", "Seguros Litoral", false, true, 12, true),
                    compania("ASG-03", "Aseguradora Patagonia Sur", true, true, 10, true),
                    compania("ASG-04", "Seguros Valle Central", true, false, 8, true),
                    compania("ASG-05", "Protege Seguros", false, true, 12, false)
            ));
        }

        if (tipoRepository.count() == 0) {
            tipoRepository.saveAll(List.of(
                    tipo("TS-01", "Vehículo – choque o colisión", TipoSiniestro.LINEA_VEHICULO),
                    tipo("TS-02", "Vehículo – robo", TipoSiniestro.LINEA_VEHICULO),
                    tipo("TS-03", "Vehículo – rotura de cristales", TipoSiniestro.LINEA_VEHICULO),
                    tipo("TS-04", "Hogar – daño por agua", TipoSiniestro.LINEA_HOGAR),
                    tipo("TS-05", "Hogar – robo", TipoSiniestro.LINEA_HOGAR),
                    tipo("TS-06", "Hogar – incendio", TipoSiniestro.LINEA_HOGAR)
            ));
        }
    }

    private Compania compania(String codigo, String nombre, boolean vehiculos, boolean hogar, int plazo, boolean activa) {
        return Compania.builder()
                .codigo(codigo).nombre(nombre)
                .gestionaVehiculos(vehiculos).gestionaHogar(hogar)
                .plazoDias(plazo).activa(activa)
                .build();
    }

    private TipoSiniestro tipo(String codigo, String nombre, String linea) {
        return TipoSiniestro.builder().codigo(codigo).nombre(nombre).linea(linea).build();
    }
}