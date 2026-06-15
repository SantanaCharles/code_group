package com.codeGroup.mapper;

import com.codeGroup.dto.membro.MembroResponse;
import com.codeGroup.dto.projeto.ProjetoResponse;
import com.codeGroup.model.Membro;
import com.codeGroup.model.Projeto;
import com.codeGroup.service.rules.RiscoCalculator;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProjetoMapper {

    private final RiscoCalculator riscoCalculator;
    private final MembroMapper membroMapper;

    public ProjetoMapper(RiscoCalculator riscoCalculator, MembroMapper membroMapper) {
        this.riscoCalculator = riscoCalculator;
        this.membroMapper = membroMapper;
    }

    public ProjetoResponse toResponse(Projeto projeto) {
        List<MembroResponse> membros = projeto.getMembros().stream()
                .sorted(Comparator.comparing(Membro::getId))
                .map(membroMapper::toResponse)
                .toList();

        return new ProjetoResponse(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDataInicio(),
                projeto.getPrevisaoTermino(),
                projeto.getDataRealTermino(),
                projeto.getOrcamentoTotal(),
                projeto.getDescricao(),
                membroMapper.toResponse(projeto.getGerenteResponsavel()),
                projeto.getStatus(),
                riscoCalculator.calcular(projeto),
                membros);
    }
}
