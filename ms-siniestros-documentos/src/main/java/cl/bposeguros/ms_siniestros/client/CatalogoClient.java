package cl.bposeguros.ms_siniestros.client;

import cl.bposeguros.ms_siniestros.dto.CompaniaDTO;
import cl.bposeguros.ms_siniestros.dto.TipoSiniestroDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

//Se comunica con ms-catalogos (puerto 8082)
@Component
public class CatalogoClient {

    private final WebClient webClient;

    public CatalogoClient(@Value("${ms.catalogos.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    
    public CompaniaDTO obtenerCompania(Long id) {
        try {
            return webClient.get()
                    .uri("/api/companias/{id}", id)
                    .retrieve()
                    .bodyToMono(CompaniaDTO.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    
    public TipoSiniestroDTO obtenerTipo(Long id) {
        try {
            return webClient.get()
                    .uri("/api/tipos-siniestro/{id}", id)
                    .retrieve()
                    .bodyToMono(TipoSiniestroDTO.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                    .block();
        } catch (WebClientRequestException e) {
            throw servicioNoDisponible();
        }
    }

    private ResponseStatusException servicioNoDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de catálogos no está disponible");
    }
}