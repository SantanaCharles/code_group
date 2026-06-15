package com.codeGroup.model.enums;

/**
 * Classificacao de risco calculada dinamicamente (nunca persistida).
 */
public enum NivelRisco {
    BAIXO("Baixo risco"),
    MEDIO("Medio risco"),
    ALTO("Alto risco");

    private final String descricao;

    NivelRisco(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
