package com.fullstack.bookShop.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Annotation available at runtime
@Target(ElementType.METHOD) // Annotation can be applied to methods
public @interface ApiMessage {
    String value(); // Example attribute
}
