package com.codeGroup.client;

import com.codeGroup.dto.membro.MembroResponse;

import java.util.Optional;

/**
 * Abstracao do consumo da API REST externa (mockada) de membros.
 * O dominio de projetos depende desta interface, nao da implementacao HTTP,
 * favorecendo testabilidade e a inversao de dependencia (DIP).
 */
public interface MembroClient {

    Optional<MembroResponse> buscarPorId(Long id);
}
