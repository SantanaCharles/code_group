package com.codeGroup.dto.membro;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Payload de criacao de membro na API externa (mockada). */
public record MembroRequest(

        @Schema(example = "Maria Souza")
        @NotBlank(message = "O nome e obrigatorio.")
        String nome,

        @Schema(example = "funcionário", description = "Cargo / atribuicao do membro")
        @NotBlank(message = "A atribuicao e obrigatoria.")
        String atribuicao) {
}
