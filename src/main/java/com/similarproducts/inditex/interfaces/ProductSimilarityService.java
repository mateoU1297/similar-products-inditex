package com.similarproducts.inditex.interfaces;

import com.similarproducts.inditex.domain.ProductDetailDTO;

import java.util.List;

public interface ProductSimilarityService {

    List<ProductDetailDTO> getSimilarProducts(String id);
}
