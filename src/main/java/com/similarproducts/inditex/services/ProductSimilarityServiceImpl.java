package com.similarproducts.inditex.services;

import com.similarproducts.inditex.clients.ProductsApiClient;
import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.interfaces.ProductSimilarityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSimilarityServiceImpl implements ProductSimilarityService {

    private final ProductsApiClient client;

    @Override
    public List<ProductDetailDTO> getSimilarProducts(String id) {
        log.info("Obteniendo productos similares para el producto {}", id);

        var ids = client.getSimilarIds(id);
        log.info("IDs similares: {}", ids);

        Set<String> seen = ConcurrentHashMap.newKeySet();
        var uniqueIds = ids.stream().filter(seen::add).toList();

        return uniqueIds.stream()
                .map(client::getProductById)
                .toList();
    }
}
