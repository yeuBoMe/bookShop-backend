package com.fullstack.bookShop.util.valid;

import com.fullstack.bookShop.dto.request.user.UserPasswordRequest;
import com.fullstack.bookShop.util.annotation.PasswordChecked;
import com.fullstack.bookShop.util.constant.PasswordConstant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidator implements ConstraintValidator<PasswordChecked, UserPasswordRequest> {

    @Override
    public boolean isValid(
            UserPasswordRequest userPasswordRequest,
            ConstraintValidatorContext constraintValidatorContext
    ) {
        boolean valid = true;

        if (userPasswordRequest.getNewPassword().equals(userPasswordRequest.getCurrentPassword())) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(PasswordConstant.CURRENT_NEW_PASSWORD_DUPLICATE)
                    .addPropertyNode("newPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            valid = false;
        }

        if (!userPasswordRequest.getNewPassword().equals(userPasswordRequest.getConfirmPassword())) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(PasswordConstant.NEW_CONFIRM_PASSWORD_NOT_MATCH)
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            valid = false;
        }

        return valid;
    }

    @Override
    public void initialize(PasswordChecked constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
}
