package com.fullstack.bookShop.dto.websocket;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestWebSocketDTO {
    private String sender;
    private String receiver;
    private String content;
    private String from;
    private String timestamp;
}
