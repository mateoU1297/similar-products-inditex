package com.similarproducts.inditex.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Error", description = "Estructura de error")
public class ErrorDto {

    @Schema(description = "Código de error interno", example = "PRODUCT_NOT_FOUND")
    private String code;

    @Schema(description = "Mensaje legible", example = "Product not found")
    private String message;
}
