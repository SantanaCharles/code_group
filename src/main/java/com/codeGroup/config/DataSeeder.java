package com.codeGroup.config;

import com.codeGroup.model.Membro;
import com.codeGroup.repository.MembroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Popula alguns membros na API externa (mockada) para facilitar a
 * demonstracao. Nao executa no perfil de testes.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final MembroRepository membroRepository;

    public DataSeeder(MembroRepository membroRepository) {
        this.membroRepository = membroRepository;
    }

    @Override
    public void run(String... args) {
        if (membroRepository.count() > 0) {
            return;
        }
        membroRepository.saveAll(List.of(
                Membro.builder().nome("Ana Lima").atribuicao("gerente").build(),
                Membro.builder().nome("Bruno Costa").atribuicao("funcionário").build(),
                Membro.builder().nome("Carla Dias").atribuicao("funcionário").build(),
                Membro.builder().nome("Diego Reis").atribuicao("funcionário").build(),
                Membro.builder().nome("Eva Martins").atribuicao("estagiário").build()
        ));
    }
}
