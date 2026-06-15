package com.codeGroup.service;

import com.codeGroup.dto.relatorio.RelatorioPortfolio;
import com.codeGroup.model.enums.StatusProjeto;
import com.codeGroup.repository.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Gera o relatorio resumido do portfolio.
 */
@Service
public class RelatorioService {

    private final ProjetoRepository projetoRepository;

    public RelatorioService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioPortfolio gerar() {
        List<RelatorioPortfolio.ResumoStatus> resumo = projetoRepository.resumirPorStatus().stream()
                .map(p -> new RelatorioPortfolio.ResumoStatus(
                        p.getStatus(), p.getQuantidade(), p.getTotalOrcado()))
                .toList();

        Double mediaDuracao = calcularMediaDuracaoEncerrados();
        long membrosUnicos = projetoRepository.contarMembrosUnicosAlocados();

        return new RelatorioPortfolio(resumo, mediaDuracao, membrosUnicos);
    }

    private Double calcularMediaDuracaoEncerrados() {
        List<ProjetoRepository.DuracaoProjection> duracoes =
                projetoRepository.buscarDuracoesEncerrados(StatusProjeto.ENCERRADO);
        if (duracoes.isEmpty()) {
            return null;
        }
        return duracoes.stream()
                .mapToLong(d -> ChronoUnit.DAYS.between(d.getDataInicio(), d.getDataRealTermino()))
                .average()
                .orElse(0.0);
    }
}
