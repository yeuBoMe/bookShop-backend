package com.fullstack.bookShop.controller.chatbot;

import com.fullstack.bookShop.dto.chatbot.RestChatbotDTO;
import com.fullstack.bookShop.service.ChatbotService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    @ApiMessage("Chatbot response successfully")
    public ResponseEntity<RestChatbotDTO> chatBotRequest(@RequestBody RestChatbotDTO restChatbotDTO) {
        return ResponseEntity.ok(chatbotService.handleChatbot(restChatbotDTO));
    }
}
