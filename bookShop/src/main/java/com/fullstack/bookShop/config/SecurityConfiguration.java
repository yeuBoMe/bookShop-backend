package com.fullstack.bookShop.config;

import com.fullstack.bookShop.security.CustomDefaultOauth2UserService;
import com.fullstack.bookShop.security.CustomAuthenticationSuccessHandler;
import com.fullstack.bookShop.util.security.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    // random key base64
    @Value("${bookshop.jwt.base64-secret}")
    private String jwtKey;

    // decode base 64 key to byte[] and wrap to secret key
    @Bean
    public SecretKey getSecretKey() {
        byte[] bytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(
                bytes,
                0,
                bytes.length,
                SecurityUtil.JWT_ALGORITHM.getName()
        );
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM)
                .build();

        return token -> {
            try {
                return nimbusJwtDecoder.decode(token);
            } catch (Exception ex) {
                System.out.println(">>> Jwt error: " + ex.getMessage());
                throw ex;
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Tạo converter mặc định của Spring để trích xuất quyền (authorities) từ JWT
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("permission");
        // Xóa prefix mặc định "SCOPE_" của quyền
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        // Tạo converter chính mà Spring Security dùng
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomDefaultOauth2UserService customDefaultOauth2UserService,
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler
    ) throws Exception {
        String[] whiteList = {
                "/",
                "/upload/**",
                "/api/v1/auth/login",
                "/api/v1/auth/login/google",
                "/api/v1/auth/register",
                "/api/v1/auth/refresh",
                "/oauth2/**",
                "/login/oauth2/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
        };

        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ratings/book/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/vnpay/vnpay-return").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customDefaultOauth2UserService)
                        )
                        .successHandler(customAuthenticationSuccessHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return httpSecurity.build();
    }
}
