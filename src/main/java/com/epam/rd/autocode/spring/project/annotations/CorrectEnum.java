package com.epam.rd.autocode.spring.project.annotations;

import com.epam.rd.autocode.spring.project.validation.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface CorrectEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "Incorrect enum value! Must be any of {enumClass}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
