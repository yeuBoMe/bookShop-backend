package com.fullstack.bookShop.util.valid;

import com.fullstack.bookShop.dto.user.RestUserPassDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidator implements ConstraintValidator<PasswordChecked, RestUserPassDTO> {

    @Override
    public boolean isValid(RestUserPassDTO restUserPassDTO, ConstraintValidatorContext constraintValidatorContext) {
        boolean valid = true;

        if (!restUserPassDTO.getNewPassword().equals(restUserPassDTO.getConfirmPassword())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Confirm password does not match the new password.")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            valid = false;
        }

        if (restUserPassDTO.getNewPassword().equals(restUserPassDTO.getCurrentPassword())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("New password must not be the same as the current password.")
                    .addPropertyNode("newPassword")
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
