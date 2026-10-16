package com.carhub.carrental.user.web.validation;

import jakarta.validation.*;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Documented
@Constraint(validatedBy = PersonNameValidator.class)
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPersonName {
    String message() default "Name must contain between {min} and {max} characters after trimming leading and trailing whitespace.";
    int min() default 2;
    int max() default 150;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
