package com.kitchensink.validation;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidUserRoleValidator implements ConstraintValidator<ValidUserRole, List<String>> {

    @Override
    public boolean isValid(List<String> roles, ConstraintValidatorContext context) {
        return roles != null && roles.size() == 1 && "USER".equals(roles.get(0));
    }
}
