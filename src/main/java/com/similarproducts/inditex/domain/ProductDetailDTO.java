package com.similarproducts.inditex.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDetailDTO {
    private String id;
    private String name;
    private Double price;
    private Boolean availability;
}
