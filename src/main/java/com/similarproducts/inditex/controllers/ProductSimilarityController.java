package com.similarproducts.inditex.controllers;

import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.errors.ErrorDto;
import com.similarproducts.inditex.interfaces.ProductSimilarityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Products", description = "Servicios de productos y similares")
@RestController
@RequestMapping(value = "/product", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProductSimilarityController {

    private final ProductSimilarityService productSimilarityService;

    @Operation(
            summary = "Obtiene productos similares (detalle)",
            description = "Devuelve la lista con el detalle de los productos similares al ID indicado",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID del producto base",
                            required = true,
                            example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de productos similares",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductDetailDTO.class)))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Producto no encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "Fallo al consultar upstream",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "504",
                            description = "Timeout llamando a servicios externos",
                            content = @Content(schema = @Schema(implementation = ErrorDto.class))
                    )
            }
    )
    @GetMapping("/{id}/similar")
    public ResponseEntity<List<ProductDetailDTO>> getSimilar(@PathVariable String id) {
        return ResponseEntity.ok(productSimilarityService.getSimilarProducts(id));
    }
}
