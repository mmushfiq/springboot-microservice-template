package com.company.project.template.exception.validation.constraint;

import com.company.project.template.exception.validation.CurrencyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Currency {

    String message() default "validation.currencyInvalid.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}