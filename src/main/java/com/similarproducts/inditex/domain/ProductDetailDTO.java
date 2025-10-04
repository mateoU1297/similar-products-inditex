package com.similarproducts.inditex.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "ProductDetail", description = "Detalle de producto")
public class ProductDetailDTO {

    @Schema(description = "Identificador del producto", example = "1")
    private String id;

    @Schema(description = "Nombre del producto", example = "Shirt")
    private String name;

    @Schema(description = "Precio del producto", example = "19.99")
    private Double price;

    @Schema(description = "Disponibilidad en inventario", example = "true")
    private Boolean availability;
}
