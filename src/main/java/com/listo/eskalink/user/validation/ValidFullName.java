package com.listo.eskalink.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FullNameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFullName {
    String message() default "Name must contain only alphabets and one space between first name and last name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
