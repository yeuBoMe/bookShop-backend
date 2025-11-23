package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.chatbot.RestChatbotDTO;
import com.fullstack.bookShop.service.ChatbotService;
import org.springframework.stereotype.Service;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Override
    public RestChatbotDTO handleChatbot(RestChatbotDTO restChatbotDTO) {
        return restChatbotDTO;
    }
}
