package com.kitchensink.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * The Class ValidUserRoleValidator.
 *
 * @author prerna
 */
public class ValidUserRoleValidator implements ConstraintValidator<ValidUserRole, List<String>> {

    /**
     * Validate roles for registration
     *
     * @param roles
     *            the roles list
     * @param context
     *            the context
     * @return boolean
     */
    @Override
    public boolean isValid(List<String> roles, ConstraintValidatorContext context) {
        return roles != null && roles.size() == 1 && "USER".equals(roles.getFirst());
    }
}
