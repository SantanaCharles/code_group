package com.codeGroup.controller;

import com.codeGroup.dto.relatorio.RelatorioPortfolio;
import com.codeGroup.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatorios", description = "Relatorio resumido do portfolio")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Relatorio: projetos por status, total orcado, duracao media e membros unicos")
    public RelatorioPortfolio portfolio() {
        return relatorioService.gerar();
    }
}
