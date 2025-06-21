package com.epam.rd.autocode.spring.project.annotations;

import com.epam.rd.autocode.spring.project.validation.BookTitleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookTitleValidator.class)
public @interface BookTitle {
    boolean required() default true;
    String message() default "Book name is required and must be between 1 and 255 characters!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

