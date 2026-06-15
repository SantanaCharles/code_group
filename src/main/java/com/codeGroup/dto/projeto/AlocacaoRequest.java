package com.codeGroup.dto.projeto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/** Payload de alocacao de membros a um projeto. */
public record AlocacaoRequest(
        @NotEmpty(message = "Informe ao menos um membro para alocar.")
        Set<Long> membroIds) {
}
