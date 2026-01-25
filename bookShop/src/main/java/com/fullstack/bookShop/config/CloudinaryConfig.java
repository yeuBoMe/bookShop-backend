package com.fullstack.bookShop.config;

import com.cloudinary.Cloudinary;
import com.fullstack.bookShop.util.constant.CloudinaryConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.name}")
    private String cloudinaryName;

    @Value("${cloudinary.api.key}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api.secret}")
    private String cloudinaryApiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put(CloudinaryConstant.CLOUD_NAME, cloudinaryName);
        config.put(CloudinaryConstant.API_KEY, cloudinaryApiKey);
        config.put(CloudinaryConstant.API_SECRET, cloudinaryApiSecret);
        return new Cloudinary(config);
    }
}
