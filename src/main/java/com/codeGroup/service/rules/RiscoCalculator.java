package com.codeGroup.service.rules;

import com.codeGroup.model.Projeto;
import com.codeGroup.model.enums.NivelRisco;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calcula a classificacao de risco de um projeto dinamicamente.
 *
 * <pre>
 * Baixo : orcamento <= 100.000      E  prazo <= 3 meses
 * Medio : orcamento 100.001..500.000 OU prazo 4..6 meses
 * Alto  : orcamento  > 500.000      OU prazo > 6 meses
 * </pre>
 *
 * O prazo e medido em meses completos entre a data de inicio e a previsao
 * de termino. A avaliacao segue a ordem decrescente de severidade para
 * resolver as sobreposicoes descritas nas regras de negocio.
 */
@Component
public class RiscoCalculator {

    private static final BigDecimal LIMITE_BAIXO = new BigDecimal("100000");
    private static final BigDecimal LIMITE_MEDIO = new BigDecimal("500000");
    private static final long PRAZO_BAIXO_MESES = 3;
    private static final long PRAZO_MEDIO_MESES = 6;

    public NivelRisco calcular(Projeto projeto) {
        return calcular(projeto.getOrcamentoTotal(), projeto.getDataInicio(), projeto.getPrevisaoTermino());
    }

    public NivelRisco calcular(BigDecimal orcamento, LocalDate inicio, LocalDate previsaoTermino) {
        long meses = mesesDeDuracao(inicio, previsaoTermino);

        boolean alto = orcamento.compareTo(LIMITE_MEDIO) > 0 || meses > PRAZO_MEDIO_MESES;
        if (alto) {
            return NivelRisco.ALTO;
        }

        boolean medio = orcamento.compareTo(LIMITE_BAIXO) > 0 || meses > PRAZO_BAIXO_MESES;
        if (medio) {
            return NivelRisco.MEDIO;
        }

        return NivelRisco.BAIXO;
    }

    private long mesesDeDuracao(LocalDate inicio, LocalDate previsaoTermino) {
        if (inicio == null || previsaoTermino == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(inicio, previsaoTermino);
    }
}
