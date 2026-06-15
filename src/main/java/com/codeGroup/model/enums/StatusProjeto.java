package com.codeGroup.model.enums;

import java.util.Set;

/**
 * Status fixos do ciclo de vida de um projeto.
 *
 * <p>A ordem de declaracao representa a sequencia logica obrigatoria:
 * EM_ANALISE -> ANALISE_REALIZADA -> ANALISE_APROVADA -> INICIADO ->
 * PLANEJADO -> EM_ANDAMENTO -> ENCERRADO.</p>
 *
 * <p>CANCELADO e um estado terminal que pode ser atingido a partir de
 * qualquer estado nao-terminal, quebrando a sequencia.</p>
 */
public enum StatusProjeto {
    EM_ANALISE("Em analise"),
    ANALISE_REALIZADA("Analise realizada"),
    ANALISE_APROVADA("Analise aprovada"),
    INICIADO("Iniciado"),
    PLANEJADO("Planejado"),
    EM_ANDAMENTO("Em andamento"),
    ENCERRADO("Encerrado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusProjeto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Estados que impedem a exclusao do projeto. */
    public static final Set<StatusProjeto> BLOQUEIAM_EXCLUSAO =
            Set.of(INICIADO, EM_ANDAMENTO, ENCERRADO);

    /** Estados terminais (sem transicao de saida). */
    public boolean isTerminal() {
        return this == ENCERRADO || this == CANCELADO;
    }

    public boolean impedeExclusao() {
        return BLOQUEIAM_EXCLUSAO.contains(this);
    }
}
