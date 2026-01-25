package com.fullstack.bookShop.config;

import com.fullstack.bookShop.util.constant.auth.TokenConstant;
import com.fullstack.bookShop.util.security.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtConfiguration.class);

    @Value("${bookshop.jwt.base64-secret}")
    private String jwtSecretKey;

    // decode base64 key to byte[] and wrap to secret key
    @Bean
    public SecretKey secretKey() {
        byte[] bytes = Base64.from(jwtSecretKey).decode();
        return new SecretKeySpec(
                bytes,
                0,
                bytes.length,
                SecurityUtil.JWT_ALGORITHM.getName()
        );
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM)
                .build();

        return token -> {
            try {
                return nimbusJwtDecoder.decode(token);
            } catch (Exception ex) {
                LOGGER.error(">>> JWT decode failed: {}", ex.getMessage(), ex);
                throw ex;
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Extract authorities from JWT
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(TokenConstant.SCOPE);
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); // Remove default prefix "SCOPE_" of authority

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
