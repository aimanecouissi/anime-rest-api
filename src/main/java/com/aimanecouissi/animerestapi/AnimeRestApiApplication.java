package com.aimanecouissi.animerestapi;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Anime REST APIs",
                description = "Welcome to the Anime REST APIs, designed for managing your personal watchlist of Anime and Manga.",
                version = "1.0",
                contact = @Contact(
                        name = "Aimane Couissi",
                        email = "contact@aimanecouissi.com",
                        url = "https://www.aimanecouissi.com/"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://github.com/aimanecouissi/anime-rest-api/blob/main/LICENSE"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Explore more on GitHub",
                url = "https://github.com/aimanecouissi/anime-rest-api"
        )
)
public class AnimeRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeRestApiApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
