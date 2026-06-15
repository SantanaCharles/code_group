package com.codeGroup.controller;

import com.codeGroup.dto.membro.MembroRequest;
import com.codeGroup.dto.membro.MembroResponse;
import com.codeGroup.service.MembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * API REST externa (mockada) para criar e consultar membros.
 * Simula um servico de terceiros; o dominio de projetos consome estes
 * endpoints atraves de um cliente HTTP.
 */
@RestController
@RequestMapping("/api/external/membros")
@Tag(name = "Membros (API externa mockada)",
        description = "Criacao e consulta de membros enviando nome e atribuicao (cargo)")
public class MembroExternalController {

    private final MembroService membroService;

    public MembroExternalController(MembroService membroService) {
        this.membroService = membroService;
    }

    @PostMapping
    @Operation(summary = "Cria um membro (nome e atribuicao)")
    public ResponseEntity<MembroResponse> criar(@Valid @RequestBody MembroRequest request) {
        MembroResponse criado = membroService.criar(request);
        return ResponseEntity.created(URI.create("/api/external/membros/" + criado.id())).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista todos os membros")
    public List<MembroResponse> listar() {
        return membroService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um membro por id")
    public MembroResponse buscar(@PathVariable Long id) {
        return membroService.buscarPorId(id);
    }
}
