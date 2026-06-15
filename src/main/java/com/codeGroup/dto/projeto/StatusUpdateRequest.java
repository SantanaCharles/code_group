package com.codeGroup.dto.projeto;

import com.codeGroup.model.enums.StatusProjeto;
import jakarta.validation.constraints.NotNull;

/** Payload de transicao de status. */
public record StatusUpdateRequest(
        @NotNull(message = "O novo status e obrigatorio.")
        StatusProjeto novoStatus) {
}
