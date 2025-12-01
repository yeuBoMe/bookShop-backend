package com.fullstack.bookShop.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> mapContainsCloudinaryInfo = new HashMap<>();
        mapContainsCloudinaryInfo.put("cloud_name", "dvglvfask");
        mapContainsCloudinaryInfo.put("api_key", "563926418957636");
        mapContainsCloudinaryInfo.put("api_secret", "WMZlEubGeSxkjNRIQI8ghVEY3O4");
        mapContainsCloudinaryInfo.put("secure", true);
        return new Cloudinary(mapContainsCloudinaryInfo);
    }
}
