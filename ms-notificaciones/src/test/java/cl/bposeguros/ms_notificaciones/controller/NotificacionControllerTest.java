package cl.bposeguros.ms_notificaciones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.bposeguros.ms_notificaciones.dto.ClaveTemporalRequest;
import cl.bposeguros.ms_notificaciones.service.CorreoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    private static final String JSON =
            "{\"correo\":\"a@b.cl\",\"claveTemporal\":\"Temp#2026\",\"motivo\":\"CREACION\",\"rol\":\"ASEGURADO\"}";

    @Mock
    private CorreoService correoService;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new NotificacionController(correoService)).build();
    }

    @Test
    void postClaveTemporal_responde202() throws Exception {
        mvc.perform(post("/api/notificaciones/clave-temporal").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.mensaje").value("Correo enviado"));

        verify(correoService).enviarClaveTemporal(new ClaveTemporalRequest("a@b.cl", "Temp#2026", "CREACION", "ASEGURADO"));
    }

    @Test
    void postClaveTemporal_siElServicioRechaza_responde400() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo no es válido"))
                .when(correoService).enviarClaveTemporal(any());

        mvc.perform(post("/api/notificaciones/clave-temporal").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isBadRequest());
    }
}
