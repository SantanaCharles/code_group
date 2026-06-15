package com.codeGroup.controller;

import com.codeGroup.dto.common.PageResponse;
import com.codeGroup.dto.projeto.AlocacaoRequest;
import com.codeGroup.dto.projeto.ProjetoRequest;
import com.codeGroup.dto.projeto.ProjetoResponse;
import com.codeGroup.dto.projeto.StatusUpdateRequest;
import com.codeGroup.model.enums.StatusProjeto;
import com.codeGroup.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.function.Function;

@RestController
@RequestMapping("/api/projetos")
@Tag(name = "Projetos", description = "CRUD e ciclo de vida do portfolio de projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PostMapping
    @Operation(summary = "Cria um novo projeto")
    public ResponseEntity<ProjetoResponse> criar(@Valid @RequestBody ProjetoRequest request) {
        ProjetoResponse criado = projetoService.criar(request);
        return ResponseEntity.created(URI.create("/api/projetos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um projeto existente")
    public ProjetoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProjetoRequest request) {
        return projetoService.atualizar(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um projeto por id")
    public ProjetoResponse buscar(@PathVariable Long id) {
        return projetoService.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Lista projetos com paginacao e filtros (nome, status, gerente)")
    public PageResponse<ProjetoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) StatusProjeto status,
            @RequestParam(required = false) Long gerenteId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<ProjetoResponse> page = projetoService.listar(nome, status, gerenteId, pageable);
        return PageResponse.from(page, Function.identity());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um projeto (bloqueado para iniciado/em andamento/encerrado)")
    public void excluir(@PathVariable Long id) {
        projetoService.excluir(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transiciona o status respeitando a sequencia logica")
    public ProjetoResponse alterarStatus(@PathVariable Long id,
                                         @Valid @RequestBody StatusUpdateRequest request) {
        return projetoService.alterarStatus(id, request.novoStatus());
    }

    @PostMapping("/{id}/membros")
    @Operation(summary = "Aloca membros (apenas 'funcionário') ao projeto")
    public ProjetoResponse alocarMembros(@PathVariable Long id,
                                         @Valid @RequestBody AlocacaoRequest request) {
        return projetoService.alocarMembros(id, request.membroIds());
    }

    @DeleteMapping("/{id}/membros/{membroId}")
    @Operation(summary = "Remove a alocacao de um membro do projeto")
    public ProjetoResponse desalocarMembro(@PathVariable Long id, @PathVariable Long membroId) {
        return projetoService.desalocarMembro(id, membroId);
    }
}
