package com.example.user;

/**
 * Custom exception for validation errors.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Specific exception thrown when attempting to create a user with a duplicate email.
 */
class DuplicateEmailException extends ValidationException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
