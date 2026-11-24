package cl.bposeguros.ms_catalogos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bposeguros.ms_catalogos.model.Compania;
import cl.bposeguros.ms_catalogos.model.Comuna;
import cl.bposeguros.ms_catalogos.model.TipoSiniestro;
import cl.bposeguros.ms_catalogos.repository.CompaniaRepository;
import cl.bposeguros.ms_catalogos.repository.ComunaRepository;
import cl.bposeguros.ms_catalogos.repository.TipoSiniestroRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private CompaniaRepository companiaRepository;

    @Mock
    private TipoSiniestroRepository tipoRepository;

    @InjectMocks
    private CatalogoService service;

    private Compania compania(String nombre, boolean vehiculos, boolean hogar, boolean activa) {
        return Compania.builder().codigo("ASG").nombre(nombre).gestionaVehiculos(vehiculos)
                .gestionaHogar(hogar).plazoDias(10).activa(activa).build();
    }

    private void conCompanias() {
        when(companiaRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(
                compania("Cordillera", true, true, true),
                compania("Litoral", false, true, true),
                compania("Valle Central", true, false, true),
                compania("Protege", false, true, false)));
    }

    @Test
    void listarComunas_devuelveLasComunasDelRepositorio() {
        when(comunaRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(Comuna.builder().nombre("Maipú").build()));

        assertEquals("Maipú", service.listarComunas().get(0).getNombre());
    }

    @Test
    void listarCompanias_sinFiltros_devuelveTodas() {
        conCompanias();

        assertEquals(4, service.listarCompanias(null, false).size());
    }

    @Test
    void listarCompanias_soloActivas_dejaFueraLasInactivas() {
        conCompanias();

        assertEquals(3, service.listarCompanias(null, true).size());
    }

    @Test
    void listarCompanias_porVehiculo_devuelveLasQueGestionanVehiculos() {
        conCompanias();

        assertEquals(List.of("Cordillera", "Valle Central"),
                service.listarCompanias("vehiculo", false).stream().map(Compania::getNombre).toList());
    }

    @Test
    void listarCompanias_porHogarYActivas_aplicaLosDosFiltros() {
        conCompanias();

        assertEquals(List.of("Cordillera", "Litoral"),
                service.listarCompanias(" HOGAR ", true).stream().map(Compania::getNombre).toList());
    }

    @Test
    void listarCompanias_conLineaDesconocida_devuelveListaVacia() {
        conCompanias();

        assertTrue(service.listarCompanias("BARCO", false).isEmpty());
    }

    @Test
    void obtenerCompania_existente_laDevuelve() {
        when(companiaRepository.findById(1L)).thenReturn(Optional.of(compania("Cordillera", true, true, true)));

        assertEquals("Cordillera", service.obtenerCompania(1L).getNombre());
    }

    @Test
    void obtenerCompania_inexistente_lanza404() {
        when(companiaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.obtenerCompania(99L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void listarTipos_sinLinea_devuelveTodos() {
        service.listarTipos(" ");

        verify(tipoRepository).findAllByOrderByIdAsc();
    }

    @Test
    void listarTipos_conLinea_buscaEnMayusculas() {
        service.listarTipos(" hogar ");

        verify(tipoRepository).findByLineaOrderByIdAsc("HOGAR");
    }

    @Test
    void obtenerTipo_existente_loDevuelve() {
        when(tipoRepository.findById(1L)).thenReturn(Optional.of(TipoSiniestro.builder().nombre("Choque").build()));

        assertEquals("Choque", service.obtenerTipo(1L).getNombre());
    }

    @Test
    void obtenerTipo_inexistente_lanza404() {
        when(tipoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.obtenerTipo(99L));
        assertEquals("Tipo de siniestro no encontrado", e.getReason());
    }
}
