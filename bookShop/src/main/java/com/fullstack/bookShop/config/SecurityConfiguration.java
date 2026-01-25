package com.fullstack.bookShop.config;

import com.fullstack.bookShop.security.CustomAuthenticationEntryPoint;
import com.fullstack.bookShop.security.CustomDefaultOauth2UserService;
import com.fullstack.bookShop.security.CustomAuthenticationSuccessHandler;
import com.fullstack.bookShop.util.constant.auth.EndpointConstant;
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
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

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

        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Permit all endpoints
                        .requestMatchers(EndpointConstant.WHITELIST).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ratings/book/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/vnpay/vnpay-return").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/momo/momo-return").permitAll()

                        // Authenticate endpoints
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
