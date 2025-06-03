package com.preeti.sansarcart.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class I18n {

    private static MessageSource messageSource;

    @Autowired
    public I18n(MessageSource messageSource) {
        I18n.messageSource = messageSource;
    }

    public static String i18n(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}

