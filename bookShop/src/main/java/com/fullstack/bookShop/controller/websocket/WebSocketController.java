package com.fullstack.bookShop.controller.websocket;

import com.fullstack.bookShop.dto.websocket.RestWebSocketDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.toAdmin")
    public void sendMessageToAdmin(@Payload RestWebSocketDTO dto) {
        if (dto.getSender() == null || dto.getSender().isBlank()) {
            return;
        }

        if (dto.getTimestamp() == null || dto.getTimestamp().isBlank()) {
            dto.setTimestamp(java.time.Instant.now().toString());
        }
        dto.setFrom("USER");

        System.out.println("========================================");
        System.out.println("📨 USER → ADMIN");
        System.out.println("From: " + dto.getSender());
        System.out.println("User Avatar: " + dto.getUserAvatar());
        System.out.println("Content: " + dto.getContent());
        System.out.println("========================================");

        // Broadcast đến admin
        simpMessagingTemplate.convertAndSend("/topic/admin.messages", dto);
    }

    @MessageMapping("/chat.toUser")
    public void sendMessageToUser(@Payload RestWebSocketDTO dto) {
        if (dto.getSender() == null || dto.getSender().isBlank()) {
            System.out.println("⚠️ Target user email (sender field) is missing!");
            return;
        }

        if (dto.getTimestamp() == null || dto.getTimestamp().isBlank()) {
            dto.setTimestamp(java.time.Instant.now().toString());
        }
        dto.setFrom("ADMIN");

        System.out.println("========================================");
        System.out.println("📨 ADMIN → USER");
        System.out.println("To: " + dto.getSender());
        System.out.println("Content: " + dto.getContent());
        System.out.println("========================================");

        // ✅ Gửi đến topic riêng của user
        String userTopic = "/topic/user." + dto.getSender()
                .replace("@", ".")
                .replace(".", "_");

        simpMessagingTemplate.convertAndSend(userTopic, dto);

        System.out.println("✅ Sent to topic: " + userTopic);
    }
}
