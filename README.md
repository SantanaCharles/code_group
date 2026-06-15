# Portfolio Manager

Sistema para gerenciar o portfólio de projetos de uma empresa, cobrindo todo o
ciclo de vida do projeto — da análise de viabilidade à finalização — incluindo
gestão de equipe, orçamento e classificação dinâmica de risco.

## Stack

- **Java 21** + **Spring Boot 3.3**
- **Spring Web** (REST, arquitetura MVC)
- **Spring Data JPA + Hibernate** (persistência)
- **PostgreSQL** (produção) / **H2** (testes)
- **Spring Security** (HTTP Basic, usuário em memória)
- **springdoc-openapi** (Swagger UI)
- **JUnit 5 + Mockito + AssertJ** (testes)
- **JaCoCo** (cobertura, gate de 70% nas regras de negócio)

## Arquitetura

Camadas bem separadas, aplicando SOLID e Clean Code:

```
controller  ->  service (+ service.rules)  ->  repository  ->  banco
                     |
                  mapper / dto / client (API externa)
```

- **controller**: somente orquestração HTTP e validação de entrada (`@Valid`).
- **service**: regras de negócio. Regras isoladas e testáveis em
  `service/rules` (`RiscoCalculator`, `StatusTransitionValidator`).
- **repository**: acesso a dados (Spring Data JPA + Specifications para filtros).
- **dto + mapper**: contratos de entrada/saída desacoplados das entidades.
- **client**: consumo da API REST externa (mockada) de membros via `RestClient`.
- **exception**: tratamento global e centralizado (`@RestControllerAdvice`).

```
src/main/java/com/codeGroup
├── config        # Security, OpenAPI, DataSeeder
├── controller    # Projetos, Membros (API externa mock), Relatórios
├── service       # ProjetoService, MembroService, RelatorioService
│   └── rules     # RiscoCalculator, StatusTransitionValidator
├── repository    # JPA repositories + Specifications + projections
├── model         # Entidades JPA + enums (Status, Risco)
├── dto           # Records de request/response
├── mapper        # Entidade <-> DTO
├── client        # MembroClient (HTTP)
└── exception     # ApiError + GlobalExceptionHandler
```

## Como executar

### Opção 1 — Docker Compose (app + banco)

```bash
docker compose up --build
```

App em `http://localhost:8080`, PostgreSQL em `localhost:5432`.

### Opção 2 — Banco em Docker + app local

```bash
docker compose up -d db
mvn spring-boot:run
```

### Opção 3 — Tudo local

Requer PostgreSQL em `localhost:5432` (db/usuário/senha = `portfolio`) ou
ajuste as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

```bash
mvn spring-boot:run
```

## Documentação (Swagger)

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Segurança

HTTP Basic com usuário em memória (senha com hash BCrypt):

| Usuário | Senha    |
|---------|----------|
| `admin` | `admin123` |

Configurável via `APP_USER` / `APP_PASSWORD`. O `GET` da API externa de membros
é público (para permitir o consumo pelo cliente HTTP interno); o restante exige
autenticação.

## Principais endpoints

| Método | Rota                                | Descrição                                  |
|--------|-------------------------------------|--------------------------------------------|
| POST   | `/api/external/membros`             | Cria membro (API externa mockada)          |
| GET    | `/api/external/membros`             | Lista membros                              |
| GET    | `/api/external/membros/{id}`        | Consulta membro                            |
| POST   | `/api/projetos`                     | Cria projeto                               |
| GET    | `/api/projetos`                     | Lista (paginação + filtros)                |
| GET    | `/api/projetos/{id}`                | Consulta projeto (com risco calculado)     |
| PUT    | `/api/projetos/{id}`                | Atualiza projeto                           |
| DELETE | `/api/projetos/{id}`                | Exclui projeto (respeitando regras)        |
| PATCH  | `/api/projetos/{id}/status`         | Transição de status                        |
| POST   | `/api/projetos/{id}/membros`        | Aloca membros                              |
| DELETE | `/api/projetos/{id}/membros/{mid}`  | Remove alocação                            |
| GET    | `/api/relatorios/portfolio`         | Relatório resumido do portfólio            |

Listagem com paginação e filtros:
`GET /api/projetos?nome=erp&status=EM_ANDAMENTO&gerenteId=1&page=0&size=10&sort=nome,asc`

## Regras de negócio implementadas

- **CRUD de projetos** com todos os campos exigidos (orçamento em `BigDecimal`).
- **Classificação de risco dinâmica** (nunca persistida) — `RiscoCalculator`:
  - Baixo: orçamento ≤ R$ 100.000 **e** prazo ≤ 3 meses
  - Médio: orçamento R$ 100.001–500.000 **ou** prazo 4–6 meses
  - Alto: orçamento > R$ 500.000 **ou** prazo > 6 meses
- **Status fixos** com sequência obrigatória e sem pular etapas —
  `StatusTransitionValidator`. `CANCELADO` pode ser aplicado a qualquer estado
  não terminal.
- **Exclusão bloqueada** para status `iniciado`, `em andamento` e `encerrado`.
- **Membros via API externa mockada** (não cadastrados diretamente).
- **Alocação**: apenas `funcionário`; mínimo 1 e máximo 10 por projeto; um membro
  não pode estar em mais de 3 projetos ativos (status ≠ encerrado/cancelado).
- **Relatório do portfólio**: quantidade e total orçado por status, duração média
  dos projetos encerrados e total de membros únicos alocados.

## Testes e cobertura

```bash
mvn test          # executa os testes + gera relatório JaCoCo
mvn verify        # também valida o gate de cobertura (>= 70% nas regras)
```

Relatório de cobertura: `target/site/jacoco/index.html`.

Os testes focam nas regras de negócio (pacotes `service` e `service.rules`):
cálculo de risco, máquina de estados, regras de alocação/exclusão e geração do
relatório.

## Exemplo de uso rápido

```bash
# 1. Criar um gerente e um funcionário (API externa)
curl -u admin:admin123 -X POST http://localhost:8080/api/external/membros \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Lima","atribuicao":"gerente"}'

curl -u admin:admin123 -X POST http://localhost:8080/api/external/membros \
  -H "Content-Type: application/json" \
  -d '{"nome":"Bruno Costa","atribuicao":"funcionário"}'

# 2. Criar um projeto
curl -u admin:admin123 -X POST http://localhost:8080/api/projetos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Migração de ERP","dataInicio":"2025-01-01","previsaoTermino":"2025-05-01","orcamentoTotal":250000.00,"descricao":"...","gerenteResponsavelId":1}'

# 3. Alocar funcionário
curl -u admin:admin123 -X POST http://localhost:8080/api/projetos/1/membros \
  -H "Content-Type: application/json" -d '{"membroIds":[2]}'

# 4. Avançar status (uma etapa por vez)
curl -u admin:admin123 -X PATCH http://localhost:8080/api/projetos/1/status \
  -H "Content-Type: application/json" -d '{"novoStatus":"ANALISE_REALIZADA"}'

# 5. Relatório
curl -u admin:admin123 http://localhost:8080/api/relatorios/portfolio
```
