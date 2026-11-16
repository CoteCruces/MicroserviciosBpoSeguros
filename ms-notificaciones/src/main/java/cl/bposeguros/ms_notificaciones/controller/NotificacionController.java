package cl.bposeguros.ms_notificaciones.controller;

import cl.bposeguros.ms_notificaciones.dto.ClaveTemporalRequest;
import cl.bposeguros.ms_notificaciones.dto.RespuestaDto;
import cl.bposeguros.ms_notificaciones.service.CorreoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final CorreoService correoService;

    @PostMapping("/clave-temporal")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RespuestaDto enviarClaveTemporal(@RequestBody ClaveTemporalRequest datos) {
        correoService.enviarClaveTemporal(datos);
        return new RespuestaDto("Correo enviado");
    }
}
