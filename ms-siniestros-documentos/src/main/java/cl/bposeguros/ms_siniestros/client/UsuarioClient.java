package cl.bposeguros.ms_siniestros.client;

import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

//Habla con ms-usuarios (puerto 8081)
@Component
public class UsuarioClient {

    private final WebClient webClient;

    public UsuarioClient(@Value("${ms.usuarios.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    
    public UsuarioDTO obtener(Long id) {
        try {
            return webClient.get()
                    .uri("/api/usuarios/{id}", id)
                    .retrieve()
                    .bodyToMono(UsuarioDTO.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    
    public List<UsuarioDTO> listarPorRol(Long rolId) {
        try {
            return webClient.get()
                    .uri("/api/usuarios?rol={rol}", rolId)
                    .retrieve()
                    .bodyToFlux(UsuarioDTO.class)
                    .collectList()
                    .block();
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    private ResponseStatusException servicioNoDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de usuarios no está disponible");
    }
}