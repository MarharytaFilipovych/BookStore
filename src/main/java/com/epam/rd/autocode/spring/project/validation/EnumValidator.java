package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.CorrectEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<CorrectEnum, Enum<?>> {
    private Class<? extends Enum<?>> enumClass;
    boolean required;

    @Override
    public void initialize(CorrectEnum constraintAnnotation) {
        enumClass = constraintAnnotation.enumClass();
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if(value == null)return !required;
        return value.getClass() == enumClass;
    }
}
