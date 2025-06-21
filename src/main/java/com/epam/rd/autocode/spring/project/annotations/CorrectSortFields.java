package com.epam.rd.autocode.spring.project.annotations;

import com.epam.rd.autocode.spring.project.model.enums.SortableEntity;
import com.epam.rd.autocode.spring.project.validation.CorrectSortFieldsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CorrectSortFieldsValidator.class)
public @interface CorrectSortFields {
    String message() default "One or more of your sort fields was (were) incorrect!";
    Class<?>[] groups() default {};
    SortableEntity entityType();
    Class<? extends Payload>[] payload() default {};
    SortMapping[] sortMappings() default {};

    @interface SortMapping{
        String from();
        String to();
    }
}
