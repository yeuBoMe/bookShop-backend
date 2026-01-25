package com.fullstack.bookShop.util.constant.auth;

import java.util.Arrays;
import java.util.List;

public final class CorsConstant {

    private CorsConstant() {}

    public static final String METHOD_GET = "GET";

    public static final String METHOD_POST = "POST";

    public static final String METHOD_PUT = "PUT";

    public static final String METHOD_DELETE = "DELETE";

    public static final String METHOD_OPTIONS = "OPTIONS";

    public static final String METHOD_PATCH = "PATCH";

    public static final List<String> ALLOWED_METHODS = Arrays.asList(
            METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_OPTIONS, METHOD_PATCH
    );

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String HEADER_ACCEPT = "Accept";

    public static final List<String> ALLOWED_HEADERS = Arrays.asList(
            HEADER_AUTHORIZATION, HEADER_CONTENT_TYPE, HEADER_ACCEPT
    );

    public static final Long MAX_AGE = 3600L;
}
