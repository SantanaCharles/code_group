package com.codeGroup.service.rules;

import com.codeGroup.exception.BusinessRuleException;
import com.codeGroup.model.enums.StatusProjeto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusTransitionValidatorTest {

    private final StatusTransitionValidator validator = new StatusTransitionValidator();

    @Test
    void devePermitirAvancarUmaEtapaNaSequencia() {
        assertThatCode(() -> validator.validar(
                StatusProjeto.EM_ANALISE, StatusProjeto.ANALISE_REALIZADA))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validar(
                StatusProjeto.EM_ANDAMENTO, StatusProjeto.ENCERRADO))
                .doesNotThrowAnyException();
    }

    @Test
    void deveBloquearPularEtapas() {
        assertThatThrownBy(() -> validator.validar(
                StatusProjeto.EM_ANALISE, StatusProjeto.INICIADO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("invalida");
    }

    @Test
    void deveBloquearRetrocederStatus() {
        assertThatThrownBy(() -> validator.validar(
                StatusProjeto.INICIADO, StatusProjeto.EM_ANALISE))
                .isInstanceOf(BusinessRuleException.class);
    }

    @ParameterizedTest
    @EnumSource(value = StatusProjeto.class,
            names = {"EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA",
                    "INICIADO", "PLANEJADO", "EM_ANDAMENTO"})
    void devePermitirCancelarDeQualquerEstadoNaoTerminal(StatusProjeto origem) {
        assertThatCode(() -> validator.validar(origem, StatusProjeto.CANCELADO))
                .doesNotThrowAnyException();
    }

    @Test
    void naoDevePermitirSairDeEstadoTerminal() {
        assertThatThrownBy(() -> validator.validar(
                StatusProjeto.ENCERRADO, StatusProjeto.CANCELADO))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> validator.validar(
                StatusProjeto.CANCELADO, StatusProjeto.EM_ANALISE))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void deveBloquearTransicaoParaOMesmoStatus() {
        assertThatThrownBy(() -> validator.validar(
                StatusProjeto.INICIADO, StatusProjeto.INICIADO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ja esta no status");
    }
}
