package com.acxiom.crm.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IndianPhoneValidator implements ConstraintValidator<ValidIndianPhone, String> {

    // 10-digit mobile number starting with 6, 7, 8, or 9 (optionally with +91 or 0 prefix)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+91|0)?[6-9]\\d{9}$");

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim().replaceAll("[\\s-]", "")).matches();
    }
}
