package cl.bposeguros.ms_usuarios.client;

import cl.bposeguros.ms_usuarios.dto.ClaveTemporalRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;


@Component
public class NotificacionClient {

    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(20);

    private final WebClient webClient;

    public NotificacionClient(@Value("${ms.notificaciones.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    
    public void enviarClaveTemporal(String correo, String claveTemporal, String motivo, String rol) {
        try {
            webClient.post()
                    .uri("/api/notificaciones/clave-temporal")
                    .bodyValue(new ClaveTemporalRequest(correo, claveTemporal, motivo, rol))
                    .retrieve()
                    .toBodilessEntity()
                    .block(ESPERA_MAXIMA);
        } catch (WebClientRequestException e) {
            
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de notificaciones no está disponible");
        } catch (WebClientResponseException e) {
            
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo enviar el correo. Inténtalo más tarde");
        } catch (IllegalStateException e) {
            
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                    "El servicio de notificaciones tardó demasiado en responder");
        }
    }
}
