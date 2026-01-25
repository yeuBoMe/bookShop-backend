package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.ChatbotRequest;
import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.chatbot.ChatbotResponse;
import com.fullstack.bookShop.service.interfaces.BookService;
import com.fullstack.bookShop.service.interfaces.ChatbotService;
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
    public ChatbotResponse chatbot(ChatbotRequest chatbotRequest) {
        StringBuilder bookInfo = new StringBuilder();

        List<BookResponse> bookResponseList = bookService.fetchAllBookResponsesFromBooks();
        for (BookResponse bookResponse : bookResponseList) {
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
                            bookResponse.getId(),
                            bookResponse.getName(),
                            bookResponse.getAuthor(),
                            bookResponse.getCategory(),
                            bookResponse.getPrice(),
                            bookResponse.getQuantity(),
                            bookResponse.getAuthor(),
                            bookResponse.getAverageStars() != null
                                    ? String.format("%.1f", bookResponse.getAverageStars())
                                    : "N/A",
                            bookResponse.getIntroduceText(),
                            bookResponse.getDescription(),
                            bookResponse.getReader()
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

        UserMessage userMessage = new UserMessage(chatbotRequest.getMessage());
        Prompt prompt = new Prompt(systemMessage, userMessage);

        String botMessage = chatClient.prompt(prompt)
                .call()
                .content();

        return new ChatbotResponse(botMessage);
    }
}
