package com.carhub.carrental.user.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PersonNameValidator implements ConstraintValidator<ValidPersonName, String> {
    private int min;
    private int max;

    @Override
    public void initialize(ValidPersonName annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String normalized = value.strip();
        return normalized.length() >= min && normalized.length() <= max;
    }
}