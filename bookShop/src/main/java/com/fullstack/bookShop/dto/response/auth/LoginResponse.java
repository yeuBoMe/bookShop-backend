package com.fullstack.bookShop.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fullstack.bookShop.util.constant.auth.TokenConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @JsonProperty(TokenConstant.ACCESS_TOKEN)
    private String accessToken;

    private UserLoginResponse user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGetAccountResponse {
        private UserLoginResponse user;
    }
}
