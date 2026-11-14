package com.fullstack.bookShop.controller.websocket;

import com.fullstack.bookShop.dto.websocket.RestWebSocketDTO;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);    

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.toAdmin")
    public void sendMessageToAdmin(@Payload RestWebSocketDTO dto) {
        if (!StringUtils.hasValue(dto.getSender())) {
            return;
        }

        if (!StringUtils.hasValue(dto.getTimestamp())) {
            dto.setTimestamp(java.time.Instant.now().toString());
        }
        dto.setFrom(AuthorityConstant.USER);

        LOGGER.info("========================================");
        LOGGER.info("📨 USER → ADMIN");
        LOGGER.info("From: {}", dto.getSender());
        LOGGER.info("User Avatar: {}", dto.getUserAvatar());
        LOGGER.info("Content: {}", dto.getContent());
        LOGGER.info("========================================");

        // Broadcast đến admin
        simpMessagingTemplate.convertAndSend("/topic/admin.messages", dto);
    }

    @MessageMapping("/chat.toUser")
    public void sendMessageToUser(@Payload RestWebSocketDTO dto) {
        if (!StringUtils.hasValue(dto.getSender())) {
            LOGGER.info("⚠️ Target user email (sender field) is missing!");
            return;
        }

        if (!StringUtils.hasValue(dto.getTimestamp())) {
            dto.setTimestamp(java.time.Instant.now().toString());
        }
        dto.setFrom(AuthorityConstant.ADMIN);

        LOGGER.info("========================================");
        LOGGER.info("📨 ADMIN → USER");
        LOGGER.info("To: {}", dto.getSender());
        LOGGER.info("Content: {}", dto.getContent());
        LOGGER.info("========================================");

        // ✅ Gửi đến topic riêng của user
        String userTopic = "/topic/user." + dto.getSender()
                .replace("@", ".")
                .replace(".", "_");

        simpMessagingTemplate.convertAndSend(userTopic, dto);

        LOGGER.info("✅ Sent to topic: {}", userTopic);
    }
}
