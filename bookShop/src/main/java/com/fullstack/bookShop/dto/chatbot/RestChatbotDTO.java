package com.fullstack.bookShop.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestChatbotDTO {

    @NotBlank(message = "Message is required")
    private String message;
}
