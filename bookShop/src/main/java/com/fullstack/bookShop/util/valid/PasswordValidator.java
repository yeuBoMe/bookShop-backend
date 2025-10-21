package com.fullstack.bookShop.util.valid;

import com.fullstack.bookShop.domain.dto.user.RestUserPassDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidator implements ConstraintValidator<PasswordChecked, RestUserPassDTO> {

    @Override
    public boolean isValid(RestUserPassDTO restUserPassDTO, ConstraintValidatorContext constraintValidatorContext) {
        boolean valid = true;

        if (!restUserPassDTO.getNewPassword().equals(restUserPassDTO.getConfirmPassword())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Passwords do not match")
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
