package com.fullstack.bookShop.service;

import com.fullstack.bookShop.dto.chatbot.RestChatbotDTO;

public interface ChatbotService {
    RestChatbotDTO handleChatbot(RestChatbotDTO restChatbotDTO);
}
