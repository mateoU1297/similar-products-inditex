package com.similarproducts.inditex.services;

import com.similarproducts.inditex.clients.ProductsApiClient;
import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.errors.GatewayTimeoutException;
import com.similarproducts.inditex.errors.NotFoundException;
import com.similarproducts.inditex.errors.UpstreamException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductSimilarityServiceImplTest {

    ProductsApiClient client;
    ProductSimilarityServiceImpl service;

    @BeforeEach
    void setup() {
        client = Mockito.mock(ProductsApiClient.class);
        CircuitBreakerRegistry cb = CircuitBreakerRegistry.ofDefaults();
        TimeLimiterRegistry tl = TimeLimiterRegistry.ofDefaults();
        service = new ProductSimilarityServiceImpl(client, cb, tl);
    }

    @Test
    void devuelveSoloProductosValidos_yEliminaDuplicados() {

        when(client.getSimilarIds("1")).thenReturn(Flux.just("2", "3", "3"));

        var p2 = ProductDetailDTO.builder().id("2").name("Dress").price(19.99).availability(true).build();
        when(client.getProductById("2")).thenReturn(Mono.just(p2));

        when(client.getProductById("3")).thenReturn(Mono.error(new NotFoundException()));

        List<ProductDetailDTO> result = service.getSimilarProducts("1");
        assertThat(result).containsExactly(p2);
        verify(client, times(1)).getProductById("2");
        verify(client, times(1)).getProductById("3");
    }

    @Test
    void reintentaUnaVez_anteErroresTransient_yLuegoRecupera() {
        when(client.getSimilarIds("1")).thenReturn(Flux.just("4"));

        var p4 = ProductDetailDTO.builder().id("4").name("Boots").price(39.99).availability(true).build();

        when(client.getProductById("4"))
                .thenReturn(Mono.error(new UpstreamException("boom")))
                .thenReturn(Mono.just(p4));

        List<ProductDetailDTO> result = service.getSimilarProducts("1");
        assertThat(result).containsExactly(p4);

        verify(client, times(2)).getProductById("4");
    }

    @Test
    void siTrasReintento_sigueFALLANDO_seOmite() {
        when(client.getSimilarIds("1")).thenReturn(Flux.just("1000"));

        when(client.getProductById("1000"))
                .thenReturn(Mono.error(new GatewayTimeoutException()))
                .thenReturn(Mono.error(new GatewayTimeoutException()));

        List<ProductDetailDTO> result = service.getSimilarProducts("1");
        assertThat(result).isEmpty();

        verify(client, times(2)).getProductById("1000");
    }
}
