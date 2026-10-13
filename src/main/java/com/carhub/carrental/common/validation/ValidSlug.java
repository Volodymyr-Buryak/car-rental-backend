package com.carhub.carrental.common.validation;

import java.lang.annotation.*;
import jakarta.validation.Payload;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.Pattern;

@Pattern(
        regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
        message = "{carfilter.validation.slug.invalid}"
)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidSlug {
    String message() default "{carfilter.validation.slug.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
