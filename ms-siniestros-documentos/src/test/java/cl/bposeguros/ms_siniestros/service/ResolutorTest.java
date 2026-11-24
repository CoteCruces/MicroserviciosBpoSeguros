package cl.bposeguros.ms_siniestros.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bposeguros.ms_siniestros.client.CatalogoClient;
import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolutorTest {

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private CatalogoClient catalogoClient;

    private Resolutor resolutor;

    @BeforeEach
    void preparar() {
        resolutor = new Resolutor(usuarioClient, catalogoClient);
    }

    @Test
    void conIdNulo_devuelveNuloSinLlamarANadie() {
        assertNull(resolutor.usuario(null));
        assertNull(resolutor.compania(null));
        assertNull(resolutor.tipo(null));
        verify(usuarioClient, never()).obtener(any());
    }

    @Test
    void usuario_pedidoDosVeces_soloLlamaUnaVez() {
        when(usuarioClient.obtener(1L)).thenReturn(new UsuarioDTO(1L, "1", "Camila", "", "", true, 1L));

        resolutor.usuario(1L);
        resolutor.usuario(1L);

        verify(usuarioClient, times(1)).obtener(1L);
    }

    @Test
    void usuarioInexistente_tampocoSeVuelveAPedir() {
        assertNull(resolutor.usuario(99L));
        assertNull(resolutor.usuario(99L));

        verify(usuarioClient, times(1)).obtener(99L);
    }

    @Test
    void companiaYTipo_tambienSeRecuerdan() {
        resolutor.compania(1L);
        resolutor.compania(1L);
        resolutor.tipo(2L);
        resolutor.tipo(2L);

        verify(catalogoClient, times(1)).obtenerCompania(1L);
        verify(catalogoClient, times(1)).obtenerTipo(2L);
    }
}
