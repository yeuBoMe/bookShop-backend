package com.fullstack.bookShop.dto.request;

import com.fullstack.bookShop.util.constant.ChatbotConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {

    @NotBlank(message = ChatbotConstant.MESSAGE_REQUIRED)
    private String message;
}
