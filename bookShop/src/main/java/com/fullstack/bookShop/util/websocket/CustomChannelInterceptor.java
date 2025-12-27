package com.fullstack.bookShop.util.websocket;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // ✅ Lấy email từ header (frontend gửi)
            String userEmail = accessor.getFirstNativeHeader("user-email");

            // ✅ HOẶC lấy từ JWT token nếu frontend gửi
            String authToken = accessor.getFirstNativeHeader("Authorization");

            String authenticatedEmail = null;

            // Ưu tiên dùng JWT nếu có
            if (authToken != null && authToken.startsWith("Bearer ")) {
                try {
                    String token = authToken.substring(7);
                    Jwt jwt = jwtDecoder.decode(token);
                    authenticatedEmail = jwt.getSubject(); // email từ JWT
                    System.out.println("✅ WebSocket authenticated via JWT: " + authenticatedEmail);
                } catch (Exception e) {
                    System.out.println("⚠️ JWT validation failed: " + e.getMessage());
                }
            }

            // Fallback về user-email header
            if (authenticatedEmail == null && userEmail != null && !userEmail.isEmpty()) {
                authenticatedEmail = userEmail;
                System.out.println("✅ WebSocket authenticated via header: " + authenticatedEmail);
            }

            if (authenticatedEmail != null) {
                // ✅ Set principal để Spring WebSocket biết user là ai
                Principal principal = new UsernamePasswordAuthenticationToken(
                        authenticatedEmail,
                        null,
                        Collections.emptyList()
                );
                accessor.setUser(principal);

                System.out.println("✅ WebSocket principal set: " + authenticatedEmail);
                System.out.println("   Session ID: " + accessor.getSessionId());
            } else {
                System.out.println("⚠️ No authentication provided for WebSocket connection");
            }
        }

        return message;
    }
}
