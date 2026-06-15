package com.codeGroup.service;

import com.codeGroup.client.MembroClient;
import com.codeGroup.dto.membro.MembroResponse;
import com.codeGroup.dto.projeto.ProjetoRequest;
import com.codeGroup.dto.projeto.ProjetoResponse;
import com.codeGroup.exception.BusinessRuleException;
import com.codeGroup.exception.ResourceNotFoundException;
import com.codeGroup.mapper.ProjetoMapper;
import com.codeGroup.model.Membro;
import com.codeGroup.model.Projeto;
import com.codeGroup.model.enums.StatusProjeto;
import com.codeGroup.repository.MembroRepository;
import com.codeGroup.repository.ProjetoRepository;
import com.codeGroup.service.rules.StatusTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;
    @Mock
    private MembroRepository membroRepository;
    @Mock
    private MembroClient membroClient;
    @Mock
    private ProjetoMapper projetoMapper;
    @Mock
    private StatusTransitionValidator statusTransitionValidator;

    @InjectMocks
    private ProjetoService service;

    private ProjetoResponse dummyResponse;

    @BeforeEach
    void setup() {
        dummyResponse = new ProjetoResponse(1L, "P", LocalDate.now(), LocalDate.now(),
                null, BigDecimal.TEN, null, null, StatusProjeto.EM_ANALISE, null, java.util.List.of());
        lenient().when(projetoMapper.toResponse(any())).thenReturn(dummyResponse);
    }

    // ---------- criar ----------

    @Test
    void deveCriarProjetoQuandoGerenteExiste() {
        ProjetoRequest req = request(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 1), 1L);
        when(membroClient.buscarPorId(1L)).thenReturn(Optional.of(membroResponse(1L, "gerente")));
        when(membroRepository.getReferenceById(1L)).thenReturn(membro(1L, "gerente"));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjetoResponse out = service.criar(req);

        assertThat(out).isEqualTo(dummyResponse);
        verify(projetoRepository).save(any(Projeto.class));
    }

    @Test
    void deveFalharAoCriarComPrevisaoAntesDoInicio() {
        ProjetoRequest req = request(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 1, 1), 1L);

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("previsao de termino");
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void deveFalharAoCriarQuandoGerenteNaoExiste() {
        ProjetoRequest req = request(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 1), 99L);
        when(membroClient.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- buscar / excluir ----------

    @Test
    void deveLancarNotFoundAoBuscarProjetoInexistente() {
        when(projetoRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBloquearExclusaoDeProjetoIniciado() {
        Projeto p = projeto(5L, StatusProjeto.INICIADO);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.excluir(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nao pode ser excluido");
        verify(projetoRepository, never()).delete(any(Projeto.class));
    }

    @Test
    void devePermitirExclusaoDeProjetoEmAnalise() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));

        service.excluir(5L);

        verify(projetoRepository).delete(p);
    }

    // ---------- status ----------

    @Test
    void deveAlterarStatusDelegandoAoValidador() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.alterarStatus(5L, StatusProjeto.ANALISE_REALIZADA);

        verify(statusTransitionValidator).validar(StatusProjeto.EM_ANALISE, StatusProjeto.ANALISE_REALIZADA);
        assertThat(p.getStatus()).isEqualTo(StatusProjeto.ANALISE_REALIZADA);
    }

    // ---------- alocacao ----------

    @Test
    void deveBloquearAlocacaoDeMembroNaoFuncionario() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(membroClient.buscarPorId(2L)).thenReturn(Optional.of(membroResponse(2L, "gerente")));

        assertThatThrownBy(() -> service.alocarMembros(5L, Set.of(2L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("funcionário");
    }

    @Test
    void deveBloquearAlocacaoAcimaDoLimiteDeProjetosSimultaneos() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(membroClient.buscarPorId(2L)).thenReturn(Optional.of(membroResponse(2L, "funcionário")));
        when(projetoRepository.countProjetosAtivosDoMembro(eq(2L), any())).thenReturn(3L);

        assertThatThrownBy(() -> service.alocarMembros(5L, Set.of(2L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("simultaneamente");
    }

    @Test
    void deveBloquearAlocacaoAcimaDeDezMembros() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        Set<Membro> dez = IntStream.rangeClosed(100, 109)
                .mapToObj(i -> membro((long) i, "funcionário"))
                .collect(Collectors.toCollection(HashSet::new));
        p.setMembros(dez);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(membroClient.buscarPorId(200L)).thenReturn(Optional.of(membroResponse(200L, "funcionário")));
        when(projetoRepository.countProjetosAtivosDoMembro(eq(200L), any())).thenReturn(0L);
        when(membroRepository.getReferenceById(200L)).thenReturn(membro(200L, "funcionário"));

        assertThatThrownBy(() -> service.alocarMembros(5L, Set.of(200L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no maximo");
    }

    @Test
    void deveAlocarMembroFuncionarioComSucesso() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(membroClient.buscarPorId(2L)).thenReturn(Optional.of(membroResponse(2L, "funcionário")));
        when(projetoRepository.countProjetosAtivosDoMembro(eq(2L), any())).thenReturn(0L);
        when(membroRepository.getReferenceById(2L)).thenReturn(membro(2L, "funcionário"));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.alocarMembros(5L, Set.of(2L));

        assertThat(p.getMembros()).extracting(Membro::getId).contains(2L);
    }

    @Test
    void deveSerIdempotenteAoAlocarMembroJaAlocado() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        p.setMembros(new HashSet<>(Set.of(membro(2L, "funcionário"))));
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.alocarMembros(5L, Set.of(2L));

        verify(membroClient, never()).buscarPorId(2L);
        assertThat(p.getMembros()).hasSize(1);
    }

    // ---------- desalocacao ----------

    @Test
    void deveDesalocarMembroMantendoMinimo() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        p.setMembros(new HashSet<>(Set.of(membro(2L, "funcionário"), membro(3L, "funcionário"))));
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));
        when(projetoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.desalocarMembro(5L, 2L);

        assertThat(p.getMembros()).extracting(Membro::getId).containsExactly(3L);
    }

    @Test
    void deveFalharAoDesalocarMembroNaoAlocado() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        p.setMembros(new HashSet<>(Set.of(membro(2L, "funcionário"))));
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.desalocarMembro(5L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBloquearDesalocacaoAbaixoDoMinimo() {
        Projeto p = projeto(5L, StatusProjeto.EM_ANALISE);
        p.setMembros(new HashSet<>(Set.of(membro(2L, "funcionário"))));
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.desalocarMembro(5L, 2L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no minimo");
    }

    // ---------- helpers ----------

    private ProjetoRequest request(LocalDate inicio, LocalDate previsao, Long gerenteId) {
        return new ProjetoRequest("Projeto X", inicio, previsao, null,
                new BigDecimal("100000"), "desc", gerenteId);
    }

    private Projeto projeto(Long id, StatusProjeto status) {
        return Projeto.builder()
                .id(id)
                .nome("Projeto X")
                .dataInicio(LocalDate.of(2025, 1, 1))
                .previsaoTermino(LocalDate.of(2025, 3, 1))
                .orcamentoTotal(new BigDecimal("100000"))
                .status(status)
                .membros(new HashSet<>())
                .build();
    }

    private Membro membro(Long id, String atribuicao) {
        return Membro.builder().id(id).nome("M" + id).atribuicao(atribuicao).build();
    }

    private MembroResponse membroResponse(Long id, String atribuicao) {
        return new MembroResponse(id, "M" + id, atribuicao);
    }
}
