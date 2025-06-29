package com.epam.rd.autocode.spring.project.validation;

import com.epam.rd.autocode.spring.project.annotations.BookTitle;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BookTitleValidator implements ConstraintValidator<BookTitle, String> {
    private boolean required;

    @Override
    public void initialize(BookTitle constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        if (title == null)return !required;
        int TITLE_LENGTH = 255;
        return !title.isBlank() && title.trim().length() <= TITLE_LENGTH;
    }
}
