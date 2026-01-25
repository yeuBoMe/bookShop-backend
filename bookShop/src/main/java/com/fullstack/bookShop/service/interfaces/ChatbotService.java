package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.ChatbotRequest;
import com.fullstack.bookShop.dto.response.chatbot.ChatbotResponse;

public interface ChatbotService {
    ChatbotResponse chatbot(ChatbotRequest chatbotRequest);
}
