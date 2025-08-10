package com.listo.eskalink.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class FullNameValidator implements ConstraintValidator<ValidFullName, String> {

    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[A-Za-z]+ [A-Za-z]+$");

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return FULL_NAME_PATTERN.matcher(name.trim()).matches();
    }
}

