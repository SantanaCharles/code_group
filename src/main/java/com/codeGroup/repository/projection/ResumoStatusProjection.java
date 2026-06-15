package com.codeGroup.repository.projection;

import com.codeGroup.model.enums.StatusProjeto;

import java.math.BigDecimal;

/** Projecao para agregacao de projetos por status no relatorio. */
public interface ResumoStatusProjection {
    StatusProjeto getStatus();

    long getQuantidade();

    BigDecimal getTotalOrcado();
}
