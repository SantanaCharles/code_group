package com.codeGroup.service.rules;

import com.codeGroup.exception.BusinessRuleException;
import com.codeGroup.model.enums.StatusProjeto;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Garante que a mudanca de status respeite a sequencia logica do ciclo de
 * vida. Nao e permitido pular etapas; CANCELADO pode ser atingido a partir
 * de qualquer estado nao-terminal.
 */
@Component
public class StatusTransitionValidator {

    private static final Map<StatusProjeto, Set<StatusProjeto>> TRANSICOES_PERMITIDAS =
            construirGrafoDeTransicoes();

    public void validar(StatusProjeto atual, StatusProjeto novo) {
        if (atual == novo) {
            throw new BusinessRuleException(
                    "O projeto ja esta no status '" + novo.getDescricao() + "'.");
        }
        Set<StatusProjeto> permitidos = TRANSICOES_PERMITIDAS.getOrDefault(atual, Set.of());
        if (!permitidos.contains(novo)) {
            throw new BusinessRuleException(String.format(
                    "Transicao de status invalida: '%s' -> '%s'. Nao e permitido pular etapas.",
                    atual.getDescricao(), novo.getDescricao()));
        }
    }

    private static Map<StatusProjeto, Set<StatusProjeto>> construirGrafoDeTransicoes() {
        Map<StatusProjeto, Set<StatusProjeto>> grafo = new EnumMap<>(StatusProjeto.class);
        StatusProjeto[] sequencia = {
                StatusProjeto.EM_ANALISE,
                StatusProjeto.ANALISE_REALIZADA,
                StatusProjeto.ANALISE_APROVADA,
                StatusProjeto.INICIADO,
                StatusProjeto.PLANEJADO,
                StatusProjeto.EM_ANDAMENTO,
                StatusProjeto.ENCERRADO
        };

        for (int i = 0; i < sequencia.length; i++) {
            StatusProjeto atual = sequencia[i];
            if (atual == StatusProjeto.ENCERRADO) {
                // Estado terminal: nenhuma saida permitida.
                grafo.put(atual, Set.of());
                continue;
            }
            // Proximo da sequencia + CANCELADO a qualquer momento.
            grafo.put(atual, Set.of(sequencia[i + 1], StatusProjeto.CANCELADO));
        }
        grafo.put(StatusProjeto.CANCELADO, Set.of());
        return grafo;
    }
}
