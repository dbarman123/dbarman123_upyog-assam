package org.upyog.gis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration class for Swagger documentation for the GIS Service API.
 * Sets up the Swagger Docket with service metadata and API information.
 */
@Configuration
@EnableSwagger2
public class OpenApiConfig {

    /**
     * Configures the Swagger documentation for the GIS Service API.
     *
     * @return a Docket instance with custom API info
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.upyog.gis.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("GIS Service API")
                .description("API details of the GIS service")
                .version("1.0.0")
                .build();
    }
}