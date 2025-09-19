package com.fullstack.bookShop.domain.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestResponse<T> {
    private int statusCode;
    private Object message;
    private String error;
    private T data;
}
