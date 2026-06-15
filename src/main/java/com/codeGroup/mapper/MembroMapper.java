package com.codeGroup.mapper;

import com.codeGroup.dto.membro.MembroRequest;
import com.codeGroup.dto.membro.MembroResponse;
import com.codeGroup.model.Membro;
import org.springframework.stereotype.Component;

@Component
public class MembroMapper {

    public Membro toEntity(MembroRequest request) {
        return Membro.builder()
                .nome(request.nome())
                .atribuicao(request.atribuicao())
                .build();
    }

    public MembroResponse toResponse(Membro membro) {
        return new MembroResponse(membro.getId(), membro.getNome(), membro.getAtribuicao());
    }
}
