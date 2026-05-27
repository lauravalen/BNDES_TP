# SAFT-BNDES

Sistema de Apoio ao Financiamento Tecnológico — projeto acadêmico.

## Pré-requisitos

- **Java 17+** — [baixar](https://adoptium.net/)
- **Maven** — incluso no projeto via `mvnw` (Windows) / `mvnw` (Linux/Mac)
- **Node.js 18+** — [baixar](https://nodejs.org/)

## Como rodar o backend

```bash
cd projeto/backend
.\mvnw.cmd spring-boot:run
```

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

```bash
cd projeto/frontend
npm install
npm start
```

A página abre em `http://localhost:3000`.

## Endpoints da API

| Método | Rota                    | Descrição                         |
|--------|-------------------------|-----------------------------------|
| GET    | `/api/desembolsos`      | Lista desembolsos (paginado)      |
| GET    | `/api/desembolsos/{id}` | Busca um desembolso pelo ID       |
| POST   | `/api/desembolsos`      | Cadastra um novo desembolso       |
| DELETE | `/api/desembolsos/{id}` | Exclui um desembolso              |
| POST   | `/api/carga`            | Carrega CSV do disco              |
| POST   | `/api/carga/exemplo`    | Carrega dados de exemplo          |
| GET    | `/api/filtros/ufs`      | Lista UFs disponíveis             |
| GET    | `/api/filtros/setores`  | Lista setores disponíveis         |
| GET    | `/api/resumo/por-uf`    | Soma de desembolsos por estado    |
| GET    | `/api/resumo/por-setor` | Soma de desembolsos por setor     |

## Parâmetros de consulta

`GET /api/desembolsos?uf=SP&setor=Ind%C3%BAstria&page=0&size=50`
