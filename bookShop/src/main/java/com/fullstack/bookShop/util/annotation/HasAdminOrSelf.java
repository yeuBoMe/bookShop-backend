package com.fullstack.bookShop.util.annotation;

import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
        "hasRole('" + AuthorityConstant.ADMIN + "') " +
        "or #id == authentication.principal.claims['user']['id']"
)
public @interface HasAdminOrSelf {
}
