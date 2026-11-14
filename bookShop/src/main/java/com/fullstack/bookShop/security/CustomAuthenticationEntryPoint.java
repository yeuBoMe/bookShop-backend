package com.fullstack.bookShop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.bookShop.dto.response.global.GlobalResponse;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.ContentTypeConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fullstack.bookShop.util.message.MessageTranslator;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper objectMapper;

    private final MessageTranslator messageTranslator;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException authException
    ) throws IOException, ServletException {

        delegate.commence(request, response, authException);
        response.setContentType(ContentTypeConstant.APPLICATION_JSON_UTF8);

        String authErrorMessage = Optional.ofNullable(authException.getCause())
                .map(Throwable::getMessage)
                .orElse(authException.getMessage());

        GlobalResponse<Object> globalResponse = new GlobalResponse<>();
        globalResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        globalResponse.setError(authErrorMessage);
        globalResponse.setMessage(messageTranslator.toLocale(CommonMessage.JWT_FAILED));

        objectMapper.writeValue(response.getWriter(), globalResponse);
    }
}
