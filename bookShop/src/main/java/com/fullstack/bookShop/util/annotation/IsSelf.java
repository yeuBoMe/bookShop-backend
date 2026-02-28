package com.fullstack.bookShop.util.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("#id == authentication.principal.claims['user']['id']")
public @interface IsSelf {
}
