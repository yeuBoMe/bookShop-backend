package com.fullstack.bookShop.util.constant.auth;

import java.util.List;

public final class EndpointConstant {

    private EndpointConstant() {}

    public static final String[] WHITELIST = {
            "/",
            "/upload/**",
            "/api/v1/auth/login",
            "/api/v1/auth/login/google",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
//                "/api/v1/chatbot",
            "/oauth2/**",
            "/login/oauth2/**",
            "/chat-websocket/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
    };

    public static final String UPLOAD = "/upload/**";

    public static final String IMPORT = "/import";

    public static final String ALL_ENDPOINTS = "/**";

    public static final List<String> LIBRARY_PATHS = List.of("/v3/api-docs", "/swagger-ui");
}
