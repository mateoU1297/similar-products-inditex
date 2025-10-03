package com.similarproducts.inditex.clients;

import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.errors.GatewayTimeoutException;
import com.similarproducts.inditex.errors.NotFoundException;
import com.similarproducts.inditex.errors.UpstreamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class ProductsApiClient {

    private final WebClient webClient;
    private final Duration timeout;

    public ProductsApiClient(@Value("${clients.products.base-url}") String baseUrl,
                             @Value("${clients.products.timeout-ms:500}") long timeoutMs) {

        this.timeout = Duration.ofMillis(timeoutMs);

        HttpClient http = HttpClient.create()
                .responseTimeout(this.timeout)
                .compress(true);

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(http))
                .build();
    }

    public List<String> getSimilarIds(String id) {
        try {
            return webClient.get()
                    .uri("/product/{id}/similarids", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, rsp -> {
                        if (rsp.statusCode() == HttpStatus.NOT_FOUND) return rsp.createException().map(e -> new NotFoundException());
                        return rsp.createException().map(e -> new UpstreamException("4xx from upstream"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, rsp -> rsp.createException().map(e -> new UpstreamException("5xx from upstream")))
                    .bodyToFlux(String.class)
                    .collectList()
                    .block(timeout);
        } catch (WebClientResponseException.NotFound e) {
            throw new NotFoundException();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw new GatewayTimeoutException();
            }
            throw new UpstreamException("Error calling similarids", e);
        }
    }

    public ProductDetailDTO getProductById(String id) {
        try {
            return webClient.get()
                    .uri("/product/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, rsp -> {
                        if (rsp.statusCode() == HttpStatus.NOT_FOUND) return rsp.createException().map(e -> new NotFoundException());
                        return rsp.createException().map(e -> new UpstreamException("4xx from upstream"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, rsp -> rsp.createException().map(e -> new UpstreamException("5xx from upstream")))
                    .bodyToMono(ProductDetailDTO.class)
                    .block(timeout);
        } catch (WebClientResponseException.NotFound e) {
            throw new NotFoundException();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw new GatewayTimeoutException();
            }
            throw new UpstreamException("Error calling product by id", e);
        }
    }

}
