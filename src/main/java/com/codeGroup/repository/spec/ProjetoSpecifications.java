package com.codeGroup.repository.spec;

import com.codeGroup.model.Projeto;
import com.codeGroup.model.enums.StatusProjeto;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros componiveis para a listagem paginada de projetos.
 */
public final class ProjetoSpecifications {

    private ProjetoSpecifications() {
    }

    public static Specification<Projeto> nomeContem(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String padrao = "%" + nome.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), padrao);
    }

    public static Specification<Projeto> comStatus(StatusProjeto status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Projeto> comGerente(Long gerenteId) {
        if (gerenteId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("gerenteResponsavel").get("id"), gerenteId);
    }
}
