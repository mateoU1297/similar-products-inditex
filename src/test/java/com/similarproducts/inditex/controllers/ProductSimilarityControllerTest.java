package com.similarproducts.inditex.controllers;

import com.similarproducts.inditex.domain.ProductDetailDTO;
import com.similarproducts.inditex.errors.GlobalExceptionHandler;
import com.similarproducts.inditex.errors.NotFoundException;
import com.similarproducts.inditex.interfaces.ProductSimilarityService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductSimilarityController.class)
@Import(GlobalExceptionHandler.class)
class ProductSimilarityControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ProductSimilarityService productSimilarityService;

    @Test
    void getSimilar_200_ok() throws Exception {
        var p2 = ProductDetailDTO.builder().id("2").name("Dress").price(19.99).availability(true).build();
        var p3 = ProductDetailDTO.builder().id("3").name("Blazer").price(29.99).availability(false).build();

        Mockito.when(productSimilarityService.getSimilarProducts(eq("1")))
                .thenReturn(List.of(p2, p3));

        mvc.perform(get("/product/1/similar").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].name").value("Dress"))
                .andExpect(jsonPath("$[1].availability").value(false));
    }

    @Test
    void getSimilar_404_mapeadoPorHandler() throws Exception {
        Mockito.when(productSimilarityService.getSimilarProducts(eq("5")))
                .thenThrow(new NotFoundException());

        mvc.perform(get("/product/5/similar").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

