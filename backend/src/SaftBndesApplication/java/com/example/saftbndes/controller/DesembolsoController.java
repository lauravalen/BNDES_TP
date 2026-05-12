package com.example.saftbndes.controller;

import com.example.saftbndes.dto.ResumoDTO;
import com.example.saftbndes.model.DesembolsoMensal;
import com.example.saftbndes.service.DesembolsoService;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// =============================================
// CAMADA CONTROLLER
// =============================================
// O Controller é a "porta de entrada" da API.
// Ele recebe as requisições HTTP do frontend (React)
// e chama o Service para processar.
//
// @RestController: combina @Controller + @ResponseBody
//   Faz com que os retornos dos métodos virem JSON automaticamente
//
// @RequestMapping("/api"): todos os endpoints começam com /api
//   Ex: GET http://localhost:8080/api/desembolsos
//
// @CrossOrigin: permite que o React (porta 3000) acesse a API (porta 8080)
//   Sem isso, o navegador bloquearia as requisições por segurança
// =============================================

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // permite qualquer origem (bom para desenvolvimento)
public class DesembolsoController {

    // Spring injeta o Service automaticamente
    @Autowired
    private DesembolsoService service;

    // =============================================
    // GET /api/desembolsos
    // Retorna TODOS os desembolsos ou filtra por UF e/ou Setor
    //
    // Exemplos de uso:
    //   GET /api/desembolsos             → todos
    //   GET /api/desembolsos?uf=SP       → só São Paulo
    //   GET /api/desembolsos?setor=Indústria
    //   GET /api/desembolsos?uf=SP&setor=Indústria
    // =============================================
    @GetMapping("/desembolsos")
    public ResponseEntity<List<DesembolsoMensal>> listar(
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String setor) {

        List<DesembolsoMensal> resultado;

        // Decide qual filtro aplicar baseado nos parâmetros recebidos
        if (uf != null && setor != null) {
            resultado = service.filtrarPorUfESetor(uf, setor);
        } else if (uf != null) {
            resultado = service.filtrarPorUf(uf);
        } else if (setor != null) {
            resultado = service.filtrarPorSetor(setor);
        } else {
            resultado = service.listarTodos();
        }

        // ResponseEntity.ok() retorna HTTP 200 OK com os dados
        return ResponseEntity.ok(resultado);
    }

    // =============================================
    // GET /api/desembolsos/{id}
    // Retorna UM desembolso específico pelo ID
    //
    // Se encontrar → 200 OK com o objeto
    // Se não encontrar → 404 Not Found
    // =============================================
    @GetMapping("/desembolsos/{id}")
    public ResponseEntity<DesembolsoMensal> buscarPorId(@PathVariable Long id) {
        // Optional: pode ter valor ou pode estar vazio (sem NullPointerException)
        Optional<DesembolsoMensal> desembolso = service.buscarPorId(id);

        if (desembolso.isPresent()) {
            return ResponseEntity.ok(desembolso.get()); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // =============================================
    // POST /api/desembolsos
    // Cadastra um NOVO desembolso (formulário do frontend)
    //
    // O frontend envia um JSON no corpo da requisição:
    // { "ano": 2024, "uf": "SP", "setor": "Indústria", ... }
    //
    // @RequestBody: o Spring converte o JSON em objeto Java automaticamente
    // =============================================
    @PostMapping("/desembolsos")
    public ResponseEntity<DesembolsoMensal> criar(@RequestBody DesembolsoMensal desembolso) {
        DesembolsoMensal salvo = service.salvar(desembolso);
        // ResponseEntity.status(201) retorna HTTP 201 Created
        return ResponseEntity.status(201).body(salvo);
    }

    // =============================================
    // PUT /api/desembolsos/{id}
    // Atualiza um desembolso existente
    // =============================================
    @PutMapping("/desembolsos/{id}")
    public ResponseEntity<DesembolsoMensal> atualizar(
            @PathVariable Long id,
            @RequestBody DesembolsoMensal dadosNovos) {

        Optional<DesembolsoMensal> existente = service.buscarPorId(id);

        if (existente.isPresent()) {
            dadosNovos.setId(id); // garante que vai atualizar e não criar novo
            DesembolsoMensal atualizado = service.salvar(dadosNovos);
            return ResponseEntity.ok(atualizado);
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // =============================================
    // DELETE /api/desembolsos/{id}
    // Remove um desembolso pelo ID
    // =============================================
    @DeleteMapping("/desembolsos/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Optional<DesembolsoMensal> existente = service.buscarPorId(id);

        if (existente.isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // =============================================
    // POST /api/carga
    // Lê o arquivo CSV e popula o banco H2
    //
    // Recebe o caminho do arquivo como parâmetro:
    // POST /api/carga?caminho=C:/Downloads/desembolsos-mensais.csv
    // =============================================
    @PostMapping("/carga")
    public ResponseEntity<String> carregarCSV(@RequestParam String caminho) {
        String resultado = service.carregarCSV(caminho);
        return ResponseEntity.ok(resultado);
    }

    // =============================================
    // POST /api/carga/exemplo
    // Carrega dados de exemplo para demonstração
    // Útil para testar sem precisar do CSV real
    // =============================================
    @PostMapping("/carga/exemplo")
    public ResponseEntity<String> carregarExemplo() {
        String resultado = service.carregarDadosExemplo();
        return ResponseEntity.ok(resultado);
    }

    // =============================================
    // GET /api/filtros/ufs
    // Retorna lista de estados disponíveis (para o dropdown de filtro)
    // =============================================
    @GetMapping("/filtros/ufs")
    public ResponseEntity<List<String>> listarUfs() {
        return ResponseEntity.ok(service.listarUfs());
    }

    // =============================================
    // GET /api/filtros/setores
    // Retorna lista de setores disponíveis
    // =============================================
    @GetMapping("/filtros/setores")
    public ResponseEntity<List<String>> listarSetores() {
        return ResponseEntity.ok(service.listarSetores());
    }

    // =============================================
    // GET /api/resumo/por-uf
    // Retorna total de desembolsos agrupado por estado (para gráfico)
    // =============================================
    @GetMapping("/resumo/por-uf")
    public ResponseEntity<List<ResumoDTO>> resumoPorUf() {
        return ResponseEntity.ok(service.resumoPorUf());
    }

    // =============================================
    // GET /api/resumo/por-setor
    // Retorna total de desembolsos agrupado por setor (para gráfico)
    // =============================================
    @GetMapping("/resumo/por-setor")
    public ResponseEntity<List<ResumoDTO>> resumoPorSetor() {
        return ResponseEntity.ok(service.resumoPorSetor());
    }
}
