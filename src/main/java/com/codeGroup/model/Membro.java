package com.codeGroup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Membro da empresa. O cadastro nao e feito diretamente: os registros sao
 * criados/consultados pela API REST externa (mockada) e consumidos pelo
 * dominio de projetos atraves de um cliente HTTP.
 */
@Entity
@Table(name = "membros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membro {

    /** Valor de atribuicao que habilita a alocacao em projetos. */
    public static final String ATRIBUICAO_FUNCIONARIO = "funcionário";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /** Cargo / atribuicao (ex.: "funcionário", "gerente"). */
    @Column(nullable = false)
    private String atribuicao;

    /** Apenas membros com atribuicao "funcionário" podem ser alocados a projetos. */
    public boolean isFuncionario() {
        return atribuicao != null && ATRIBUICAO_FUNCIONARIO.equalsIgnoreCase(atribuicao.trim());
    }
}
