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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Component
public class ProductsApiClient {

    private final WebClient webClient;
    private final Duration requestTimeout;

    public ProductsApiClient(@Value("${clients.products.base-url}") String baseUrl,
                             @Value("${clients.products.timeout-ms}") long timeoutMs) {
        this.requestTimeout = Duration.ofMillis(timeoutMs);

        HttpClient http = HttpClient.create()
                .responseTimeout(this.requestTimeout)
                .compress(true);

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(http))
                .build();

        log.info("ProductsApiClient baseUrl={} timeoutMs={}", baseUrl, timeoutMs);
    }

    public Flux<String> getSimilarIds(String id) {
        return webClient.get()
                .uri("/product/{id}/similarids", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, rsp ->
                        rsp.createException().map(ex -> {
                            if (rsp.statusCode() == HttpStatus.NOT_FOUND) return new NotFoundException();
                            return new UpstreamException("4xx from upstream on /similarids: " + rsp.statusCode(), ex);
                        })
                )
                .onStatus(HttpStatusCode::is5xxServerError, rsp ->
                        rsp.createException().map(ex -> new UpstreamException("5xx from upstream on /similarids: " + rsp.statusCode(), ex))
                )
                .bodyToFlux(Integer.class)
                .map(String::valueOf)
                .timeout(requestTimeout)
                .onErrorMap(throwable ->
                        (throwable instanceof java.util.concurrent.TimeoutException)
                                ? new GatewayTimeoutException()
                                : throwable
                );
    }

    public Mono<ProductDetailDTO> getProductById(String id) {
        return webClient.get()
                .uri("/product/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, rsp ->
                        rsp.createException().map(ex -> {
                            if (rsp.statusCode() == HttpStatus.NOT_FOUND) return new NotFoundException();
                            return new UpstreamException("4xx from upstream on /product/" + id + ": " + rsp.statusCode(), ex);
                        })
                )
                .onStatus(HttpStatusCode::is5xxServerError, rsp ->
                        rsp.createException().map(ex -> new UpstreamException("5xx from upstream on /product/" + id, ex))
                )
                .bodyToMono(ProductDetailDTO.class)
                .timeout(requestTimeout)
                .onErrorMap(throwable ->
                        (throwable instanceof java.util.concurrent.TimeoutException)
                                ? new GatewayTimeoutException()
                                : throwable
                );
    }
}
