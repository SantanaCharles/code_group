package com.codeGroup.service;

import com.codeGroup.dto.membro.MembroRequest;
import com.codeGroup.dto.membro.MembroResponse;
import com.codeGroup.exception.ResourceNotFoundException;
import com.codeGroup.mapper.MembroMapper;
import com.codeGroup.model.Membro;
import com.codeGroup.repository.MembroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras da API externa (mockada) de membros: criacao e consulta.
 * O cadastro de membros nao e feito diretamente pelo dominio de projetos.
 */
@Service
public class MembroService {

    private final MembroRepository membroRepository;
    private final MembroMapper membroMapper;

    public MembroService(MembroRepository membroRepository, MembroMapper membroMapper) {
        this.membroRepository = membroRepository;
        this.membroMapper = membroMapper;
    }

    @Transactional
    public MembroResponse criar(MembroRequest request) {
        Membro membro = membroRepository.save(membroMapper.toEntity(request));
        return membroMapper.toResponse(membro);
    }

    @Transactional(readOnly = true)
    public List<MembroResponse> listar() {
        return membroRepository.findAll().stream().map(membroMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MembroResponse buscarPorId(Long id) {
        return membroRepository.findById(id)
                .map(membroMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Membro", id));
    }
}
