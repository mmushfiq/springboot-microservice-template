package com.company.project.template.exception.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Pattern(
        regexp = "^[a-zA-Z ]{5,25}$",
        message = "validation.holderNameInvalid.message"
)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface HolderName {

    String message() default "should be consisted of only letters and allowed length is {min=5, max=25}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
