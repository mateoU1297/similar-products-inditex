package com.similarproducts.inditex;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		info = @Info(
				title = "Similar Products API",
				version = "1.0.0",
				description = "API para mostrar productos similares"
		),
		servers = {
				@Server(url = "/", description = "Default")
		},
		tags = {
				@Tag(name = "Products", description = "Servicios de productos y similares")
		}
)
@SpringBootApplication
public class SimilarProductsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimilarProductsApplication.class, args);
	}

}
