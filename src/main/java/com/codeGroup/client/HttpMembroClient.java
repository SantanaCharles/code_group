package com.codeGroup.client;

import com.codeGroup.dto.membro.MembroResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Implementacao HTTP do {@link MembroClient}. Consome a API externa (mockada)
 * de membros atraves de um {@link RestClient} apontado para a URL base
 * configuravel (propriedade {@code app.membros.api.base-url}).
 */
@Component
public class HttpMembroClient implements MembroClient {

    private final RestClient restClient;

    public HttpMembroClient(@Value("${app.membros.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Optional<MembroResponse> buscarPorId(Long id) {
        MembroResponse membro = restClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    // Membro inexistente na API externa -> tratamos como ausencia.
                })
                .body(MembroResponse.class);
        return Optional.ofNullable(membro);
    }
}
