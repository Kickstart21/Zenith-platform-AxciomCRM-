package com.acxiom.crm.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IndianPhoneValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIndianPhone {
    String message() default "Enter a valid phone number.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
