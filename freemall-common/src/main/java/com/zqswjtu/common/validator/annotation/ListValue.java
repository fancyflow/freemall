package com.zqswjtu.common.validator.annotation;

import com.zqswjtu.common.validator.ListValueConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ListValue {
    String message() default "{com.zqswjtu.common.validator.annotation.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int[] value() default {};
}
