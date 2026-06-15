package com.codeGroup.model;

import com.codeGroup.model.enums.StatusProjeto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projetos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "previsao_termino", nullable = false)
    private LocalDate previsaoTermino;

    @Column(name = "data_real_termino")
    private LocalDate dataRealTermino;

    @Column(name = "orcamento_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal orcamentoTotal;

    @Column(length = 2000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gerente_responsavel_id", nullable = false)
    private Membro gerenteResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusProjeto status = StatusProjeto.EM_ANALISE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "projeto_membros",
            joinColumns = @JoinColumn(name = "projeto_id"),
            inverseJoinColumns = @JoinColumn(name = "membro_id"))
    @Builder.Default
    private Set<Membro> membros = new HashSet<>();
}
