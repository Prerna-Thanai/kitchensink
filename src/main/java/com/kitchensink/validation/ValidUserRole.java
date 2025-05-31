package com.kitchensink.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * The Interface ValidUserRole for custom validation.
 *
 * @author prerna
 */
@Documented
@Constraint(validatedBy = ValidUserRoleValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserRole {

    /** The message */
    String message() default "Only 'USER' role allowed for registration";

    /** The groups */
    Class<?>[] groups() default {};

    /** The payload */
    Class<? extends Payload>[] payload() default {};
}
