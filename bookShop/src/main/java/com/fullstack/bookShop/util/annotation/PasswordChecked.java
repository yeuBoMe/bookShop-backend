package com.fullstack.bookShop.util.annotation;

import com.fullstack.bookShop.util.valid.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface PasswordChecked {

    String message() default "User password validation failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
