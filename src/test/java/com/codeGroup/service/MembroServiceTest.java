package com.codeGroup.service;

import com.codeGroup.dto.membro.MembroRequest;
import com.codeGroup.dto.membro.MembroResponse;
import com.codeGroup.exception.ResourceNotFoundException;
import com.codeGroup.mapper.MembroMapper;
import com.codeGroup.model.Membro;
import com.codeGroup.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroServiceTest {

    @Mock
    private MembroRepository membroRepository;

    private final MembroMapper membroMapper = new MembroMapper();

    private MembroService service;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        service = new MembroService(membroRepository, membroMapper);
    }

    @Test
    void deveCriarMembro() {
        when(membroRepository.save(any())).thenReturn(
                Membro.builder().id(1L).nome("Ana").atribuicao("funcionário").build());

        MembroResponse out = service.criar(new MembroRequest("Ana", "funcionário"));

        assertThat(out.id()).isEqualTo(1L);
        assertThat(out.atribuicao()).isEqualTo("funcionário");
    }

    @Test
    void deveListarMembros() {
        when(membroRepository.findAll()).thenReturn(List.of(
                Membro.builder().id(1L).nome("Ana").atribuicao("gerente").build(),
                Membro.builder().id(2L).nome("Bia").atribuicao("funcionário").build()));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    void deveBuscarMembroPorId() {
        when(membroRepository.findById(1L)).thenReturn(
                Optional.of(Membro.builder().id(1L).nome("Ana").atribuicao("funcionário").build()));

        assertThat(service.buscarPorId(1L).nome()).isEqualTo("Ana");
    }

    @Test
    void deveLancarNotFoundQuandoMembroInexistente() {
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
