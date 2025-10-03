package com.similarproducts.inditex.clients;

import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.errors.GatewayTimeoutException;
import com.similarproducts.inditex.errors.NotFoundException;
import com.similarproducts.inditex.errors.UpstreamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ProductsApiClient {

    private final RestClient restClient;

    public ProductsApiClient(
            @Value("${clients.products.base-url}") String baseUrl,
            @Value("${clients.products.timeout-ms}") int timeoutMs) {

        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        log.info("ProductsApiClient baseUrl={} timeoutMs={}", baseUrl, timeoutMs);
    }

    public List<String> getSimilarIds(String id) {
        try {
            Integer[] ids = restClient.get()
                    .uri("/product/{id}/similarids", id)
                    .retrieve()
                    .body(Integer[].class);

            return Arrays.stream(ids != null ? ids : new Integer[0])
                    .map(String::valueOf)
                    .toList();

        } catch (NotFound e) {
            throw new NotFoundException();
        } catch (HttpServerErrorException e) {
            throw new UpstreamException("5xx from upstream on /similarids: " + e.getStatusCode());
        } catch (RestClientResponseException e) {
            throw new UpstreamException("4xx from upstream on /similarids: " + e.getStatusCode());
        } catch (Exception e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new GatewayTimeoutException("Timeout calling /similarids");
            }
            throw new UpstreamException("Error calling /similarids", e);
        }
    }

    public ProductDetailDTO getProductById(String id) {
        try {
            return restClient.get()
                    .uri("/product/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, (rq, rs) -> { throw new UpstreamException("5xx from upstream on /product/"+id); })
                    .body(ProductDetailDTO.class);

        } catch (NotFound e) {
            throw new NotFoundException();
        } catch (HttpServerErrorException e) {
            throw new UpstreamException("5xx from upstream on /product/" + id + ": " + e.getStatusCode());
        } catch (RestClientResponseException e) {
            throw new UpstreamException("4xx from upstream on /product/" + id + ": " + e.getStatusCode());
        } catch (Exception e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new GatewayTimeoutException("Timeout calling /product/" + id);
            }
            throw new UpstreamException("Error calling /product/" + id, e);
        }
    }
}
