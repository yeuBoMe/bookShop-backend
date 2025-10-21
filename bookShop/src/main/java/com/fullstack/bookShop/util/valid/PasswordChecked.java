package com.fullstack.bookShop.util.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordChecked {
    String message() default "User password validation failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
