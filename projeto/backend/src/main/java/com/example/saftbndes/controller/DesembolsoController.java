package com.example.saftbndes.controller;

import com.example.saftbndes.dto.ResumoDTO;
import com.example.saftbndes.model.DesembolsoMensal;
import com.example.saftbndes.service.DesembolsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    // Retorna desembolsos com paginação e filtros opcionais
    //
    // Parâmetros:
    //   page  = número da página (0-based, padrão: 0)
    //   size  = itens por página (padrão: 50)
    //   uf    = filtro por estado (opcional)
    //   setor = filtro por setor (opcional)
    //
    // Exemplos:
    //   GET /api/desembolsos                     → página 0, 50 itens
    //   GET /api/desembolsos?page=1&size=20      → página 1, 20 itens
    //   GET /api/desembolsos?uf=SP               → filtrado por SP
    //   GET /api/desembolsos?uf=SP&setor=Indústria
    // =============================================
    @GetMapping("/desembolsos")
    public ResponseEntity<Page<DesembolsoMensal>> listar(
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String setor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<DesembolsoMensal> resultado;

        if (uf != null && setor != null) {
            resultado = service.filtrarPorUfESetorPaginado(uf, setor, pageable);
        } else if (uf != null) {
            resultado = service.filtrarPorUfPaginado(uf, pageable);
        } else if (setor != null) {
            resultado = service.filtrarPorSetorPaginado(setor, pageable);
        } else {
            resultado = service.listarPaginado(pageable);
        }

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
    // POST /api/carga/upload
    // Recebe um arquivo CSV enviado pelo navegador (upload real)
    //
    // O frontend envia o arquivo como multipart/form-data:
    //   <input type="file"> → FormData → fetch POST
    //
    // @RequestParam("arquivo") MultipartFile: Spring captura o arquivo
    // =============================================
    // =============================================
    // POST /api/carga/upload
    // Parâmetros:
    //   arquivo  = o arquivo CSV (obrigatório)
    //   limite   = quantas linhas importar (opcional, padrão 5000)
    //              use limite=0 para importar TUDO (cuidado com arquivos grandes!)
    //
    // Exemplos:
    //   POST /api/carga/upload?limite=1000   → só as 1000 primeiras linhas
    //   POST /api/carga/upload?limite=0      → arquivo inteiro
    // =============================================
    @PostMapping("/carga/upload")
    public ResponseEntity<String> uploadCSV(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "limite", defaultValue = "5000") int limite) {

        String resultado = service.carregarCSVUpload(arquivo, limite);
        if (resultado.startsWith("ERRO")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    // =============================================
    // POST /api/carga
    // Lê o arquivo CSV diretamente do disco e popula o banco H2
    //
    // Recebe o caminho do arquivo como parâmetro:
    // POST /api/carga?caminho=C:/Downloads/desembolsos-mensais.csv
    //
    // Opcional: limite (ex: &limite=10000) para importar apenas N linhas
    // Use limite=0 para importar tudo (padrão: 50000)
    //
    // Vantagem sobre o upload: não precisa enviar o arquivo pela internet,
    // ideal para arquivos grandes (700MB+)
    // =============================================
    @PostMapping("/carga")
    public ResponseEntity<String> carregarCSV(
            @RequestParam String caminho,
            @RequestParam(value = "limite", defaultValue = "50000") int limite) {
        String resultado = service.carregarCSV(caminho, limite);
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
