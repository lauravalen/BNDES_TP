# SAFT-BNDES
### Sistema de Apoio ao Financiamento Tecnológico

Aplicação Full Stack que consulta, filtra e visualiza dados reais de desembolsos do BNDES, auxiliando pequenas empresas de tecnologia na tomada de decisão.

---

## Integrantes

- Laura Valentim
- Rebeca Gomes
- Thiago Farias

---

## Objetivo

Transformar dados brutos de financiamentos do BNDES em informações estratégicas. O sistema permite consultar, filtrar, cadastrar e visualizar graficamente os desembolsos realizados pelo banco.

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 17 + Spring Boot 3 |
| Banco de Dados | H2 (modo arquivo) + Spring Data JPA |
| Frontend | React 19 + Recharts |

---

## Dicionário de Dados

Entidade `DesembolsoMensal` — baseada no CSV oficial do BNDES:

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | Long | Chave primária gerada automaticamente |
| `ano` | Integer | Ano do desembolso |
| `mes` | Integer | Mês do desembolso |
| `formaDeApoio` | String | Direto (empresa → BNDES) ou Indireto (via banco parceiro) |
| `produto` | String | Produto financeiro (ex: BNDES Automático) |
| `inovacao` | String | Se o financiamento é voltado a inovação (Sim/Não) |
| `porteDaEmpresa` | String | Micro, Pequeno, Médio ou Grande |
| `regiao` | String | Região do Brasil |
| `uf` | String | Estado (ex: SP, RJ) |
| `municipio` | String | Município do investimento |
| `setorBndes` | String | Setor econômico (ex: Indústria) |
| `subsetorBndes` | String | Subsetor (ex: Tecnologia da Informação) |
| `desembolsos` | Double | Valor em Reais liberado |

**Fonte:** [Portal de Dados Abertos do BNDES](https://dadosabertos.bndes.gov.br/dataset/desembolsos-mensais)

---

## Rotas da API

| Método | Rota | Descrição | Status |
|--------|------|-----------|--------|
| GET | `/api/desembolsos` | Lista todos (paginado) | 200 OK |
| GET | `/api/desembolsos?uf=SP&setor=Indústria&page=0&size=50` | Filtra e pagina | 200 OK |
| GET | `/api/desembolsos/{id}` | Busca por ID | 200 / 404 |
| POST | `/api/desembolsos` | Cadastra novo | 201 Created |
| PUT | `/api/desembolsos/{id}` | Atualiza | 200 / 404 |
| DELETE | `/api/desembolsos/{id}` | Remove | 204 / 404 |
| POST | `/api/carga/exemplo` | Carrega dados de exemplo | 200 OK |
| POST | `/api/carga?caminho=...` | Importa CSV do BNDES | 200 OK |
| GET | `/api/filtros/ufs` | Lista UFs disponíveis | 200 OK |
| GET | `/api/filtros/setores` | Lista setores disponíveis | 200 OK |
| GET | `/api/resumo/por-uf` | Total por estado | 200 OK |
| GET | `/api/resumo/por-setor` | Total por setor | 200 OK |

---

## Arquitetura

```
Navegador (React - porta 3000)
         ↕  HTTP / JSON
Controller (@RestController - porta 8080)
         ↓
Service (@Service) ←── lê CSV
         ↓
Repository (JpaRepository)
         ↓
Banco H2 (modo arquivo)
```

---

## Justificativas Técnicas

**Por que `Optional`?**
Usado no `buscarPorId()` para evitar `NullPointerException`. Se o ID não existir no banco, o Optional retorna vazio e o sistema responde com **404 Not Found** em vez de travar.

**Por que `ResponseEntity`?**
Para controlar o código HTTP retornado em cada situação: 200 (OK), 201 (criado), 204 (deletado) e 404 (não encontrado).

---

## Pré-requisitos

- **Java 17+** — [baixar](https://adoptium.net/)
- **Maven** — incluso no projeto via `mvnw`
- **Node.js 18+** — [baixar](https://nodejs.org/)

## Como rodar o backend

**Via terminal:**
```bash
cd projeto/backend
.\mvnw.cmd spring-boot:run
```

**Via IntelliJ IDEA:**
Abra a pasta `projeto/backend` no IntelliJ e clique em Run.

A API inicia em `http://localhost:8080`.

### Carga de dados

Ao iniciar pela primeira vez, o sistema carrega automaticamente o CSV reduzido que está em `src/main/resources/`. Os dados persistem em disco (`data/`) mesmo após reiniciar o backend.

Para resetar o banco, pare o servidor e apague a pasta `backend/data/`.

### Console H2

Acesse `http://localhost:8080/h2-console` com:

- **JDBC URL:** `jdbc:h2:file:./data/saftbndes`
- **Usuário:** `sa`
- **Senha:** *(vazio)*

## Como rodar o frontend

**Via terminal (VS Code):**
```bash
cd projeto/frontend
npm install
npm start
```

A página abre em `http://localhost:3000`.

## Prints do Sistema

*(Adicione prints do frontend e do Postman aqui antes de entregar)*

---

## Desafios Encontrados

1. **Encoding do CSV:** O arquivo do BNDES usa Windows-1252, não UTF-8. Resolvido especificando o charset na leitura.
2. **CORS:** React (porta 3000) e Spring (porta 8080) são origens diferentes. Resolvido com `@CrossOrigin` no Controller.
3. **Formato numérico:** O CSV usa vírgula como separador decimal. Resolvido convertendo com `.replace(",", ".")` antes do parse.
4. **Desempenho:** A base cheia tem centenas de milhares de registros. Resolvido com paginação server-side (50 por página).
