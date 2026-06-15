package com.codeGroup.exception;

/** Violacao de uma regra de negocio. Mapeada para HTTP 422. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
