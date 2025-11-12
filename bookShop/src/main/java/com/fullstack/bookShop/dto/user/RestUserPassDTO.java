package com.fullstack.bookShop.dto.user;

import com.fullstack.bookShop.util.valid.PasswordChecked;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PasswordChecked
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestUserPassDTO {

    @Size(min = 6, message = "Password must be at least 6 characters long!")
    private String currentPassword;

    @Size(min = 6, message = "New password must be at least 6 characters long!")
    private String newPassword;

    @Size(min = 6, message = "Confirm password must be at least 6 characters long!")
    private String confirmPassword;
}
