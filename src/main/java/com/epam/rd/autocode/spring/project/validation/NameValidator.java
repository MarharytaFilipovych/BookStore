package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.CorrectName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<CorrectName, String> {
    private Pattern pattern;
    private boolean required;

    @Override
    public void initialize(CorrectName constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        pattern = Pattern.compile( "^[\\p{L}\\s\\-.']+$");
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)return !required;
        if(value.isBlank())return false;
        String trimmedValue = value.trim();
        int NAME_LENGTH = 100;
        if(trimmedValue.length() > NAME_LENGTH || trimmedValue.length() < 2)return false;
        return pattern.matcher(trimmedValue).matches();
    }
}
