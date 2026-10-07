package com.josedev.gametracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableCaching
public class RawgConfig {

    @Bean
    RestClient rawgRestClient(@Value("${rawg.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

}
