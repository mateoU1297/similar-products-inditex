package com.similarproducts.inditex.clients;

import com.similarproducts.inditex.errors.GatewayTimeoutException;
import com.similarproducts.inditex.errors.NotFoundException;
import com.similarproducts.inditex.errors.UpstreamException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class ProductsApiClientTest {

    static MockWebServer server;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        server.shutdown();
    }

    private ProductsApiClient newClient(long timeoutMs) {
        String baseUrl = server.url("/").toString().replaceAll("/$", "");
        return new ProductsApiClient(baseUrl, timeoutMs);
    }

    @Test
    void getSimilarIds_ok_devuelveLista() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[2,3,3]"));

        var client = newClient(2_000);

        StepVerifier.create(client.getSimilarIds("1"))
                .expectNext("2", "3", "3")
                .verifyComplete();
    }

    @Test
    void getSimilarIds_404_lanzaNotFound() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"Product not found\"}"));

        var client = newClient(2_000);

        StepVerifier.create(client.getSimilarIds("nope"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getSimilarIds_5xx_lanzaUpstream() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json"));

        var client = newClient(2_000);

        StepVerifier.create(client.getSimilarIds("1"))
                .expectError(UpstreamException.class)
                .verify();
    }

    @Test
    void getSimilarIds_timeout_lanzaGatewayTimeout() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[2,3]")
                .setBodyDelay(3, TimeUnit.SECONDS));

        var client = newClient(1_000);

        StepVerifier.create(client.getSimilarIds("1"))
                .expectError(GatewayTimeoutException.class)
                .verify();
    }

    @Test
    void getProductById_ok_mapeaDTO() {
        String json = """
                {"id":"4","name":"Boots","price":39.99,"availability":true}
                """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(json));

        var client = newClient(2_000);

        StepVerifier.create(client.getProductById("4"))
                .assertNext(p -> {
                    Assertions.assertEquals("4", p.getId());
                    Assertions.assertEquals("Boots", p.getName());
                    Assertions.assertEquals(39.99, p.getPrice());
                    Assertions.assertTrue(p.getAvailability());
                })
                .verifyComplete();
    }

    @Test
    void getProductById_404_lanzaNotFound() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"Product not found\"}"));

        var client = newClient(2_000);

        StepVerifier.create(client.getProductById("999"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getProductById_5xx_lanzaUpstream() {
        server.enqueue(new MockResponse()
                .setResponseCode(503)
                .addHeader("Content-Type", "application/json"));

        var client = newClient(2_000);

        StepVerifier.create(client.getProductById("4"))
                .expectError(UpstreamException.class)
                .verify();
    }

    @Test
    void getProductById_timeout_lanzaGatewayTimeout() {
        String json = """
                {"id":"1000","name":"Coat","price":89.99,"availability":true}
                """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(json)
                .setBodyDelay(3, TimeUnit.SECONDS)); // > 1s

        var client = newClient(1_000);

        StepVerifier.create(client.getProductById("1000"))
                .expectError(GatewayTimeoutException.class)
                .verify();
    }
}
