package com.acme.payroll.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thrown when user input or a business rule is violated. Carries one or more readable messages.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Collections.singletonList(message);
    }

    public ValidationException(List<String> errors) {
        super(String.join("\n", errors));
        this.errors = Collections.unmodifiableList(new ArrayList<String>(errors));
    }

    public List<String> getErrors() {
        return errors;
    }
}
