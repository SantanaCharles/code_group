package com.codeGroup.dto.projeto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload de criacao/atualizacao de projeto. Status e risco nao sao
 * informados pelo cliente: o status segue a maquina de estados e o risco
 * e calculado dinamicamente.
 */
public record ProjetoRequest(

        @Schema(example = "Migracao de ERP")
        @NotBlank(message = "O nome e obrigatorio.")
        String nome,

        @NotNull(message = "A data de inicio e obrigatoria.")
        LocalDate dataInicio,

        @NotNull(message = "A previsao de termino e obrigatoria.")
        LocalDate previsaoTermino,

        @Schema(description = "Data real de termino (opcional)")
        LocalDate dataRealTermino,

        @Schema(example = "250000.00")
        @NotNull(message = "O orcamento total e obrigatorio.")
        @DecimalMin(value = "0.0", inclusive = false, message = "O orcamento deve ser positivo.")
        BigDecimal orcamentoTotal,

        String descricao,

        @Schema(description = "Id do membro gerente responsavel")
        @NotNull(message = "O gerente responsavel e obrigatorio.")
        @Positive(message = "O id do gerente deve ser positivo.")
        Long gerenteResponsavelId) {
}
