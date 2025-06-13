package com.epam.rd.autocode.spring.project.annotations;

import com.epam.rd.autocode.spring.project.validation.NameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface CorrectName {
    boolean required() default true;
    String message() default "Incorrect name format!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
