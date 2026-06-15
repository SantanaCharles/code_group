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
import com.codeGroup.repository.spec.ProjetoSpecifications;
import com.codeGroup.service.rules.StatusTransitionValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Orquestra as regras de negocio do ciclo de vida de projetos: CRUD,
 * transicao de status, alocacao de membros e regras de exclusao.
 */
@Service
public class ProjetoService {

    static final int MIN_MEMBROS = 1;
    static final int MAX_MEMBROS = 10;
    static final int MAX_PROJETOS_SIMULTANEOS_POR_MEMBRO = 3;
    static final Set<StatusProjeto> STATUS_NAO_ATIVOS =
            Set.of(StatusProjeto.ENCERRADO, StatusProjeto.CANCELADO);

    private final ProjetoRepository projetoRepository;
    private final MembroRepository membroRepository;
    private final MembroClient membroClient;
    private final ProjetoMapper projetoMapper;
    private final StatusTransitionValidator statusTransitionValidator;

    public ProjetoService(ProjetoRepository projetoRepository,
                          MembroRepository membroRepository,
                          MembroClient membroClient,
                          ProjetoMapper projetoMapper,
                          StatusTransitionValidator statusTransitionValidator) {
        this.projetoRepository = projetoRepository;
        this.membroRepository = membroRepository;
        this.membroClient = membroClient;
        this.projetoMapper = projetoMapper;
        this.statusTransitionValidator = statusTransitionValidator;
    }

    @Transactional
    public ProjetoResponse criar(ProjetoRequest request) {
        validarDatas(request);
        Membro gerente = carregarGerente(request.gerenteResponsavelId());

        Projeto projeto = Projeto.builder()
                .nome(request.nome())
                .dataInicio(request.dataInicio())
                .previsaoTermino(request.previsaoTermino())
                .dataRealTermino(request.dataRealTermino())
                .orcamentoTotal(request.orcamentoTotal())
                .descricao(request.descricao())
                .gerenteResponsavel(gerente)
                .status(StatusProjeto.EM_ANALISE)
                .build();

        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    @Transactional
    public ProjetoResponse atualizar(Long id, ProjetoRequest request) {
        validarDatas(request);
        Projeto projeto = obterEntidade(id);
        Membro gerente = carregarGerente(request.gerenteResponsavelId());

        projeto.setNome(request.nome());
        projeto.setDataInicio(request.dataInicio());
        projeto.setPrevisaoTermino(request.previsaoTermino());
        projeto.setDataRealTermino(request.dataRealTermino());
        projeto.setOrcamentoTotal(request.orcamentoTotal());
        projeto.setDescricao(request.descricao());
        projeto.setGerenteResponsavel(gerente);

        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscarPorId(Long id) {
        return projetoMapper.toResponse(obterEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ProjetoResponse> listar(String nome, StatusProjeto status, Long gerenteId, Pageable pageable) {
        Specification<Projeto> spec = Specification
                .where(ProjetoSpecifications.nomeContem(nome))
                .and(ProjetoSpecifications.comStatus(status))
                .and(ProjetoSpecifications.comGerente(gerenteId));
        return projetoRepository.findAll(spec, pageable).map(projetoMapper::toResponse);
    }

    @Transactional
    public void excluir(Long id) {
        Projeto projeto = obterEntidade(id);
        if (projeto.getStatus().impedeExclusao()) {
            throw new BusinessRuleException(
                    "Projeto com status '" + projeto.getStatus().getDescricao()
                            + "' nao pode ser excluido.");
        }
        projetoRepository.delete(projeto);
    }

    @Transactional
    public ProjetoResponse alterarStatus(Long id, StatusProjeto novoStatus) {
        Projeto projeto = obterEntidade(id);
        statusTransitionValidator.validar(projeto.getStatus(), novoStatus);
        projeto.setStatus(novoStatus);
        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    @Transactional
    public ProjetoResponse alocarMembros(Long id, Set<Long> membroIds) {
        Projeto projeto = obterEntidade(id);
        boolean projetoAtivo = !STATUS_NAO_ATIVOS.contains(projeto.getStatus());

        for (Long membroId : membroIds) {
            if (projeto.getMembros().stream().anyMatch(m -> m.getId().equals(membroId))) {
                continue; // ja alocado: idempotente
            }
            validarMembroParaAlocacao(membroId, projetoAtivo);
            projeto.getMembros().add(membroRepository.getReferenceById(membroId));
        }

        if (projeto.getMembros().size() > MAX_MEMBROS) {
            throw new BusinessRuleException(
                    "Um projeto pode ter no maximo " + MAX_MEMBROS + " membros alocados.");
        }
        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    @Transactional
    public ProjetoResponse desalocarMembro(Long id, Long membroId) {
        Projeto projeto = obterEntidade(id);
        boolean removido = projeto.getMembros().removeIf(m -> m.getId().equals(membroId));
        if (!removido) {
            throw ResourceNotFoundException.of("Membro alocado", membroId);
        }
        if (projeto.getMembros().size() < MIN_MEMBROS) {
            throw new BusinessRuleException(
                    "Um projeto deve manter no minimo " + MIN_MEMBROS + " membro alocado.");
        }
        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    private void validarMembroParaAlocacao(Long membroId, boolean projetoAtivo) {
        MembroResponse membro = membroClient.buscarPorId(membroId)
                .orElseThrow(() -> ResourceNotFoundException.of("Membro", membroId));

        if (!Membro.ATRIBUICAO_FUNCIONARIO.equalsIgnoreCase(safeTrim(membro.atribuicao()))) {
            throw new BusinessRuleException("Apenas membros com atribuicao '"
                    + Membro.ATRIBUICAO_FUNCIONARIO + "' podem ser alocados (membro id " + membroId + ").");
        }

        if (projetoAtivo) {
            long ativos = projetoRepository.countProjetosAtivosDoMembro(membroId, STATUS_NAO_ATIVOS);
            if (ativos >= MAX_PROJETOS_SIMULTANEOS_POR_MEMBRO) {
                throw new BusinessRuleException("O membro id " + membroId + " ja esta alocado em "
                        + MAX_PROJETOS_SIMULTANEOS_POR_MEMBRO + " projetos ativos simultaneamente.");
            }
        }
    }

    private Membro carregarGerente(Long gerenteId) {
        membroClient.buscarPorId(gerenteId)
                .orElseThrow(() -> ResourceNotFoundException.of("Gerente (membro)", gerenteId));
        return membroRepository.getReferenceById(gerenteId);
    }

    private void validarDatas(ProjetoRequest request) {
        if (request.previsaoTermino().isBefore(request.dataInicio())) {
            throw new BusinessRuleException("A previsao de termino nao pode ser anterior a data de inicio.");
        }
        if (request.dataRealTermino() != null
                && request.dataRealTermino().isBefore(request.dataInicio())) {
            throw new BusinessRuleException("A data real de termino nao pode ser anterior a data de inicio.");
        }
    }

    private Projeto obterEntidade(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Projeto", id));
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }
}
