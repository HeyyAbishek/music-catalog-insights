package com.catalog.music.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();

        List<MediaType> mediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
        mediaTypes.add(MediaType.parseMediaType("text/javascript"));
        mediaTypes.add(MediaType.parseMediaType("text/javascript;charset=utf-8"));
        mediaTypes.add(MediaType.parseMediaType("application/x-javascript"));
        jacksonConverter.setSupportedMediaTypes(mediaTypes);

        return builder
                .messageConverters(converters -> converters.add(0, jacksonConverter))
                .build();
    }
}