package com.codeGroup.service;

import com.codeGroup.dto.relatorio.RelatorioPortfolio;
import com.codeGroup.model.enums.StatusProjeto;
import com.codeGroup.repository.ProjetoRepository;
import com.codeGroup.repository.projection.ResumoStatusProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private RelatorioService service;

    @Test
    void deveGerarRelatorioComMediaDeDuracao() {
        when(projetoRepository.resumirPorStatus()).thenReturn(List.of(
                resumo(StatusProjeto.ENCERRADO, 2, "300000"),
                resumo(StatusProjeto.EM_ANDAMENTO, 1, "150000")));
        when(projetoRepository.buscarDuracoesEncerrados(any())).thenReturn(List.of(
                duracao(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 11)),  // 10 dias
                duracao(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 21)))); // 20 dias
        when(projetoRepository.contarMembrosUnicosAlocados()).thenReturn(7L);

        RelatorioPortfolio rel = service.gerar();

        assertThat(rel.resumoPorStatus()).hasSize(2);
        assertThat(rel.mediaDuracaoDiasProjetosEncerrados()).isEqualTo(15.0);
        assertThat(rel.totalMembrosUnicosAlocados()).isEqualTo(7L);
    }

    @Test
    void deveRetornarMediaNulaQuandoNaoHaProjetosEncerrados() {
        when(projetoRepository.resumirPorStatus()).thenReturn(List.of());
        when(projetoRepository.buscarDuracoesEncerrados(any())).thenReturn(List.of());
        when(projetoRepository.contarMembrosUnicosAlocados()).thenReturn(0L);

        RelatorioPortfolio rel = service.gerar();

        assertThat(rel.mediaDuracaoDiasProjetosEncerrados()).isNull();
        assertThat(rel.resumoPorStatus()).isEmpty();
    }

    private ResumoStatusProjection resumo(StatusProjeto status, long qtd, String total) {
        return new ResumoStatusProjection() {
            public StatusProjeto getStatus() {
                return status;
            }

            public long getQuantidade() {
                return qtd;
            }

            public BigDecimal getTotalOrcado() {
                return new BigDecimal(total);
            }
        };
    }

    private ProjetoRepository.DuracaoProjection duracao(LocalDate inicio, LocalDate fim) {
        return new ProjetoRepository.DuracaoProjection() {
            public LocalDate getDataInicio() {
                return inicio;
            }

            public LocalDate getDataRealTermino() {
                return fim;
            }
        };
    }
}
