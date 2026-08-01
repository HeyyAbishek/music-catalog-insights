package com.catalog.music;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MusicCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicCatalogApplication.class, args);
    }
}
