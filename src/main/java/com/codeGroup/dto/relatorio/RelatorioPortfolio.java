package com.codeGroup.dto.relatorio;

import com.codeGroup.model.enums.StatusProjeto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Relatorio resumido do portfolio.
 */
public record RelatorioPortfolio(
        List<ResumoStatus> resumoPorStatus,
        Double mediaDuracaoDiasProjetosEncerrados,
        long totalMembrosUnicosAlocados) {

    /** Quantidade e total orcado agrupados por status. */
    public record ResumoStatus(
            StatusProjeto status,
            long quantidadeProjetos,
            BigDecimal totalOrcado) {
    }
}
