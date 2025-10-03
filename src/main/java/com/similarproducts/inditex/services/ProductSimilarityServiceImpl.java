package com.similarproducts.inditex.services;

import com.similarproducts.inditex.clients.ProductsApiClient;
import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.errors.GatewayTimeoutException;
import com.similarproducts.inditex.errors.NotFoundException;
import com.similarproducts.inditex.errors.UpstreamException;
import com.similarproducts.inditex.interfaces.ProductSimilarityService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSimilarityServiceImpl implements ProductSimilarityService {

    private final ProductsApiClient client;
    private final CircuitBreakerRegistry cbRegistry;
    private final TimeLimiterRegistry tlRegistry;

    private static final int MAX_CONCURRENCY = 8;

    @Override
    public List<ProductDetailDTO> getSimilarProducts(String id) {
        return getSimilarProductsReactive(id).collectList().block();
    }

    public Flux<ProductDetailDTO> getSimilarProductsReactive(String id) {
        var cbIds = cbRegistry.circuitBreaker("similarIds");
        var tlIds = tlRegistry.timeLimiter("similarIds");

        var cbDetail = cbRegistry.circuitBreaker("productById");
        var tlDetail = tlRegistry.timeLimiter("productById");

        log.info("Obteniendo productos similares para el producto {}", id);

        return client.getSimilarIds(id)
                .transformDeferred(CircuitBreakerOperator.of(cbIds))
                .transformDeferred(TimeLimiterOperator.of(tlIds))
                .distinct()
                .flatMap(simId ->
                                client.getProductById(simId)
                                        .transformDeferred(CircuitBreakerOperator.of(cbDetail))
                                        .transformDeferred(TimeLimiterOperator.of(tlDetail))
                                        .retryWhen(reactor.util.retry.Retry.max(1)
                                                .filter(ex -> ex instanceof UpstreamException || ex instanceof GatewayTimeoutException)
                                                .transientErrors(true))
                                        .onErrorResume(ex -> {
                                            if (ex instanceof NotFoundException) {
                                                log.info("Producto {} no encontrado, se omite", simId);
                                            } else {
                                                log.warn("Omitiendo producto {} por error: {}", simId, ex.toString());
                                            }
                                            return Mono.empty();
                                        }),
                        MAX_CONCURRENCY
                )
                .doOnComplete(() -> log.info("Finalizada construcción de similares para {}", id));
    }
}
