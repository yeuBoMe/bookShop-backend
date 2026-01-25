package com.fullstack.bookShop.config;

import com.fullstack.bookShop.util.constant.auth.CorsConstant;
import com.fullstack.bookShop.util.constant.auth.EndpointConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${bookshop.frontend.url}")
    private String frontendUrl;

    @Bean
    public UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(CorsConstant.ALLOWED_METHODS);
        configuration.setAllowedHeaders(CorsConstant.ALLOWED_HEADERS);
        configuration.setAllowCredentials(true); // Cho phép trình duyệt gửi cookies hoặc Authorization headers (như JWT) sang server.
        configuration.setMaxAge(CorsConstant.MAX_AGE); // Bộ nhớ đệm (cache) cấu hình CORS trong trình duyệt trong 3600 giây

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(EndpointConstant.ALL_ENDPOINTS, configuration);
        return source;
    }
}
