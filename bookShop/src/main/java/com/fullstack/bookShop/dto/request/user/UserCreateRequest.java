package com.fullstack.bookShop.dto.request.user;

import com.fullstack.bookShop.util.constant.entity.UserConstant;
import jakarta.validation.constraints.Email;
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
public class UserCreateRequest {

    @NotBlank(message = UserConstant.FULL_NAME_REQUIRED)
    @Size(max = 100, message = UserConstant.FULL_NAME_MAX_LENGTH)
    @Pattern(regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\s()]+$", message = UserConstant.FULL_NAME_INVALID)
    private String fullName;

    @NotBlank(message = UserConstant.EMAIL_REQUIRED)
    @Email(message = UserConstant.EMAIL_INVALID)
    @Size(max = 150, message = UserConstant.EMAIL_MAX_LENGTH)
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$", message = UserConstant.EMAIL_INVALID)
    private String email;

    @NotBlank(message = UserConstant.PHONE_REQUIRED)
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = UserConstant.PHONE_INVALID)
    private String phone;
}
