package com.fullstack.bookShop.dto.request.user;

import com.fullstack.bookShop.util.annotation.PasswordChecked;
import com.fullstack.bookShop.util.constant.PasswordConstant;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PasswordChecked
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordRequest {

    @Size(min = 6, message = PasswordConstant.CURRENT_PASS_WORD_MIN_LENGTH)
    private String currentPassword;

    @Size(min = 6, message = PasswordConstant.NEW_PASSWORD_MIN_LENGTH)
    private String newPassword;

    @Size(min = 6, message = PasswordConstant.CONFIRM_PASSWORD_MIN_LENGTH)
    private String confirmPassword;
}
