package com.codeGroup.exception;

/** Recurso inexistente. Mapeada para HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String recurso, Object id) {
        return new ResourceNotFoundException(recurso + " nao encontrado(a) para o id: " + id);
    }
}
