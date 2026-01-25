package com.fullstack.bookShop.controller.chatbot;

import com.fullstack.bookShop.dto.request.ChatbotRequest;
import com.fullstack.bookShop.dto.response.chatbot.ChatbotResponse;
import com.fullstack.bookShop.service.interfaces.ChatbotService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import jakarta.validation.Valid;
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
    @ApiMessage(ActionConstant.CHATBOT)
    public ResponseEntity<ChatbotResponse> chatBotRequest(@Valid @RequestBody ChatbotRequest chatbotRequest) {
        return ResponseEntity.ok(chatbotService.chatbot(chatbotRequest));
    }
}
