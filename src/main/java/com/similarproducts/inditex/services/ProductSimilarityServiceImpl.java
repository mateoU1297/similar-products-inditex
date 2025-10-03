package com.similarproducts.inditex.services;

import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.interfaces.ProductSimilarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSimilarityServiceImpl implements ProductSimilarityService {

    @Override
    public List<ProductDetailDTO> getSimilarProducts(String id) {
        return List.of();
    }
}
