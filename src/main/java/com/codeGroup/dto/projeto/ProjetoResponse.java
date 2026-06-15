package com.codeGroup.dto.projeto;

import com.codeGroup.dto.membro.MembroResponse;
import com.codeGroup.model.enums.NivelRisco;
import com.codeGroup.model.enums.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Representacao completa de um projeto, com risco calculado dinamicamente. */
public record ProjetoResponse(
        Long id,
        String nome,
        LocalDate dataInicio,
        LocalDate previsaoTermino,
        LocalDate dataRealTermino,
        BigDecimal orcamentoTotal,
        String descricao,
        MembroResponse gerenteResponsavel,
        StatusProjeto status,
        NivelRisco risco,
        List<MembroResponse> membros) {
}
