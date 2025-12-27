package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.book.RestBookViewDTO;
import com.fullstack.bookShop.dto.chatbot.RestChatbotDTO;
import com.fullstack.bookShop.service.BookService;
import com.fullstack.bookShop.service.ChatbotService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatClient chatClient;

    private final BookService bookService;

    public ChatbotServiceImpl(
            ChatClient.Builder chatClientBuilder,
            BookService bookService
    ) {
        this.chatClient = chatClientBuilder.build();
        this.bookService = bookService;
    }

    @Override
    public RestChatbotDTO handleChatbot(RestChatbotDTO restChatbotDTO) {
        StringBuilder bookInfo = new StringBuilder();

        List<RestBookViewDTO> allRestBookViewDTOs = bookService.fetchAllRestBookViewDTOsFromBooks();
        for (RestBookViewDTO restBookViewDTO : allRestBookViewDTOs) {
            bookInfo.append(
                    """
                    - ID: %d
                      Name: %s
                      Author: %s
                      Category: %s
                      Price: %s
                      Quantity: %d
                      Author: %s
                      Rating: %s ⭐
                      Introduce text: %s
                      Description: %s
                      Reader: %s
                    """.formatted(
                        restBookViewDTO.getId(),
                        restBookViewDTO.getName(),
                        restBookViewDTO.getAuthor(),
                        restBookViewDTO.getCategory(),
                        restBookViewDTO.getPrice(),
                        restBookViewDTO.getQuantity(),
                        restBookViewDTO.getAuthor(),
                        restBookViewDTO.getAverageStars() != null
                                ? String.format("%.1f", restBookViewDTO.getAverageStars())
                                : "N/A",
                        restBookViewDTO.getIntroduceText(),
                        restBookViewDTO.getDescription(),
                        restBookViewDTO.getReader()
                    )
            );
        }

        SystemMessage systemMessage = new SystemMessage(
                        """
                        You are BookShop AI
                        You should response with a funny voice
                        All books database
                        """ + bookInfo
        );

        UserMessage userMessage = new UserMessage(restChatbotDTO.getMessage());
        Prompt prompt = new Prompt(systemMessage, userMessage);

        String messageResponse = chatClient.prompt(prompt)
                .call()
                .content();

        return new RestChatbotDTO(messageResponse);
    }
}
