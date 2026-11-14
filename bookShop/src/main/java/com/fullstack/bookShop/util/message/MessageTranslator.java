package com.fullstack.bookShop.util.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Dich message key sang ngon ngu cua request hien tai.
 * Moi text hien ra cho nguoi dung deu di qua day, khong hardcode o tang nao.
 */
@Component
@RequiredArgsConstructor
public class MessageTranslator {

    private final ResourceBundleMessageSource messageSource;

    public String toLocale(String messageKey) {
        return toLocale(messageKey, new Object[0]);
    }

    /** Tham so dien vao cho {0}, {1}... trong messages.properties. */
    public String toLocale(String messageKey, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageKey, args, locale);
    }
}
