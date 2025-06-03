package com.preeti.sansarcart.exception.custom;

import java.util.List;

import static com.preeti.sansarcart.common.I18n.i18n;

public class ListValidationException extends RuntimeException {
    private final List<String> errors;

    public ListValidationException(List<String> errors) {
        super(i18n("exception.validation.error"));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}

