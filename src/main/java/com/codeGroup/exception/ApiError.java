package com.codeGroup.exception;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Formato consistente de erro retornado por toda a API.
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> validationErrors) {

    public record FieldValidationError(String field, String message) {
    }

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path,
                              List<FieldValidationError> validationErrors) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, validationErrors);
    }
}
