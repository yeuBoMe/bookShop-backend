package com.fullstack.bookShop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;

@Configuration
public class MessageConfig {

    private static final String MESSAGE_BUNDLE = "messages";

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames(MESSAGE_BUNDLE);
        source.setDefaultEncoding("UTF-8");
        source.setDefaultLocale(Locale.ENGLISH);
        // Thieu key thi tra ve chinh key thay vi nem loi - khong de mot key sot lam sap request
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    /** Cho phep @NotBlank(message = "{book.name.required}") doc thang tu messages.properties. */
    @Bean
    public LocalValidatorFactoryBean defaultValidator(ResourceBundleMessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
