package com.fullstack.bookShop.dto.request.auth;

import com.fullstack.bookShop.util.constant.auth.LoginConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = LoginConstant.USERNAME_REQUIRED)
    @Size(min = 4, max = 50, message = LoginConstant.USERNAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[a-zA-Z0-9_.@]+$",
            message = LoginConstant.USERNAME_INVALID
    )
    private String username;

    @NotBlank(message = LoginConstant.PASSWORD_REQUIRED)
    @Size(min = 6, message = LoginConstant.PASSWORD_MAX_LENGTH)
    private String password;
}
