package com.epam.rd.autocode.spring.project.annotations;

import com.epam.rd.autocode.spring.project.validation.MyPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MyPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password does not meet our security requirements :)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}