package com.codeGroup.service.rules;

import com.codeGroup.model.enums.NivelRisco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RiscoCalculatorTest {

    private final RiscoCalculator calculator = new RiscoCalculator();
    private static final LocalDate INICIO = LocalDate.of(2025, 1, 1);

    @ParameterizedTest(name = "orcamento={0}, termino={1} -> {2}")
    @CsvSource({
            // Baixo: orcamento <= 100.000 E prazo <= 3 meses
            "100000, 2025-04-01, BAIXO",
            "50000,  2025-03-01, BAIXO",
            // Medio: orcamento 100.001..500.000 OU prazo 4..6 meses
            "100001, 2025-04-01, MEDIO",
            "50000,  2025-05-01, MEDIO",
            "500000, 2025-07-01, MEDIO",
            // Alto: orcamento > 500.000 OU prazo > 6 meses
            "500001, 2025-02-01, ALTO",
            "50000,  2025-08-01, ALTO"
    })
    @DisplayName("Classifica o risco conforme orcamento e prazo")
    void deveClassificarRisco(String orcamento, String termino, NivelRisco esperado) {
        NivelRisco risco = calculator.calcular(
                new BigDecimal(orcamento), INICIO, LocalDate.parse(termino));
        assertThat(risco).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({"99999, BAIXO", "100000, BAIXO", "100001, MEDIO", "500000, MEDIO", "500001, ALTO"})
    @DisplayName("Limites de orcamento nas fronteiras exatas")
    void deveRespeitarFronteirasDeOrcamento(String orcamento, NivelRisco esperado) {
        NivelRisco risco = calculator.calcular(
                new BigDecimal(orcamento), INICIO, INICIO.plusMonths(1));
        assertThat(risco).isEqualTo(esperado);
    }
}
