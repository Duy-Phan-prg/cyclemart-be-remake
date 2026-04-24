package com.example.cyclemartberemake.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", "dmnxpj8r9",
                "api_key", "192339635988295",
                "api_secret", "_nc2TL_caKpv_QqhFBb-Ein2HIQ"
        ));
    }
}