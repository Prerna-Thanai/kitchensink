package com.kitchensink.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidUserRoleValidatorTest {

    private ValidUserRoleValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidUserRoleValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
    }

    @Test
    void testIsValid_WhenRolesIsNull_ShouldReturnFalse() {
        // Arrange
        List<String> roles = null;

        // Act
        boolean result = validator.isValid(roles, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_WhenRolesIsEmpty_ShouldReturnFalse() {
        // Arrange
        List<String> roles = Collections.emptyList();

        // Act
        boolean result = validator.isValid(roles, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_WhenRolesHasMoreThanOneRole_ShouldReturnFalse() {
        // Arrange
        List<String> roles = Arrays.asList("USER", "ADMIN");

        // Act
        boolean result = validator.isValid(roles, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_WhenRolesContainsUserRole_ShouldReturnTrue() {
        // Arrange
        List<String> roles = Collections.singletonList("USER");

        // Act
        boolean result = validator.isValid(roles, context);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsValid_WhenRolesContainsDifferentRole_ShouldReturnFalse() {
        // Arrange
        List<String> roles = Collections.singletonList("ADMIN");

        // Act
        boolean result = validator.isValid(roles, context);

        // Assert
        assertFalse(result);
    }
}
