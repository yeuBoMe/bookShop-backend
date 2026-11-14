package com.fullstack.bookShop.util.response;

import com.fullstack.bookShop.dto.response.global.GlobalResponse;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.auth.EndpointConstant;
import com.fullstack.bookShop.util.message.MessageTranslator;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class FormatGlobalResponse implements ResponseBodyAdvice<Object> {

    private final MessageTranslator messageTranslator;

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        // Apply for all controllers
        return true;
    }

    @Override
    public @Nullable Object beforeBodyWrite(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response
    ) {
        if (body instanceof String || body instanceof Resource) {
            return body;
        }

        String path = request.getURI().getPath();
        if (isExcludedPath(path)) {
            return body;
        }

        HttpServletResponse httpServletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = httpServletResponse.getStatus();

        GlobalResponse<Object> globalResponse = new GlobalResponse<>();
        globalResponse.setStatusCode(statusCode);

        boolean isImportEndpoint = path.contains(EndpointConstant.IMPORT);
        if (statusCode >= 400 && !isImportEndpoint) {
            return body;
        } else {
            globalResponse.setData(body);
            ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
            String messageKey = apiMessage != null
                    ? apiMessage.value()
                    : CommonMessage.API_CALL_SUCCESS;
            globalResponse.setMessage(messageTranslator.toLocale(messageKey));
        }

        return globalResponse;
    }

    private boolean isExcludedPath(String path) {
        return EndpointConstant.LIBRARY_PATHS
                .stream()
                .anyMatch(path::startsWith);
    }
}
