package com.fullstack.bookShop.dto.response.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalResponse<T> {
    private int statusCode;
    private Object message;
    private String error;
    private T data;
}
