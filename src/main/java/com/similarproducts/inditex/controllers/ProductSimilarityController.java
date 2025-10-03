package com.similarproducts.inditex.controllers;

import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.interfaces.ProductSimilarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductSimilarityController {

    private final ProductSimilarityService productSimilarityService;

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<ProductDetailDTO>> getSimilar(@PathVariable String id) {
        return ResponseEntity.ok(productSimilarityService.getSimilarProducts(id));
    }
}
