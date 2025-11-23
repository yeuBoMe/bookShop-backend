package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.chatbot.RestChatbotDTO;
import com.fullstack.bookShop.service.ChatbotService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatClient chatClient;

    public ChatbotServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public RestChatbotDTO handleChatbot(RestChatbotDTO restChatbotDTO) {
        SystemMessage systemMessage = new SystemMessage(
                """
                        You are BookShop AI
                        You should response with a funny voice
        """);

        UserMessage userMessage = new UserMessage(restChatbotDTO.getMessage());

        Prompt prompt = new Prompt(systemMessage, userMessage);

        String messageResponse = chatClient.prompt(prompt)
                .call()
                .content();
        return new RestChatbotDTO(messageResponse);
    }
}
