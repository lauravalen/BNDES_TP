package com.example.saftbndes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.saftbndes.model.DesembolsoMensal;

import java.util.List;

// =============================================
// CAMADA REPOSITORY
// =============================================
// Essa interface é responsável por TODA comunicação com o banco.
// Ao estender JpaRepository, o Spring cria automaticamente os métodos:
//   - findAll()       → busca todos
//   - findById(id)    → busca por ID (retorna Optional)
//   - save(objeto)    → salva ou atualiza
//   - deleteById(id)  → deleta por ID
//   - count()         → conta registros
//
// Os métodos abaixo são CUSTOMIZADOS.
// O Spring lê o nome do método e monta o SQL automaticamente!
// Ex: findByUf("SP") → SELECT * FROM desembolsos WHERE uf = 'SP'
// =============================================

@Repository
public interface DesembolsoRepository extends JpaRepository<DesembolsoMensal, Long> {

    // Busca todos os desembolsos de um estado específico
    // Ex: findByUf("SP") → todos os desembolsos de São Paulo
    List<DesembolsoMensal> findByUf(String uf);

    // Busca por setor do BNDES
    // Ex: findBySetorBndes("Indústria")
    List<DesembolsoMensal> findBySetorBndes(String setorBndes);

    // Busca por região
    // Ex: findByRegiao("Sudeste")
    List<DesembolsoMensal> findByRegiao(String regiao);

    // Busca por porte da empresa
    // Ex: findByPorteDaEmpresa("Micro")
    List<DesembolsoMensal> findByPorteDaEmpresa(String porteDaEmpresa);

    // Busca por ano
    List<DesembolsoMensal> findByAno(Integer ano);

    // Busca combinada: estado + setor ao mesmo tempo
    List<DesembolsoMensal> findByUfAndSetorBndes(String uf, String setorBndes);

    // Busca desembolsos com valor acima de um mínimo
    // Ex: findByDesembolsosGreaterThan(1000000.0) → acima de R$1 milhão
    List<DesembolsoMensal> findByDesembolsosGreaterThan(Double valor);

    // =============================================
    // QUERIES CUSTOMIZADAS COM @Query
    // =============================================
    // Quando o nome do método não é suficiente, usamos SQL direto com @Query

    // Retorna lista de todos os estados (sem repetição) para popular o filtro
    @Query("SELECT DISTINCT d.uf FROM DesembolsoMensal d WHERE d.uf IS NOT NULL ORDER BY d.uf")
    List<String> findDistinctUfs();

    // Retorna lista de todos os setores (sem repetição) para popular o filtro
    @Query("SELECT DISTINCT d.setorBndes FROM DesembolsoMensal d WHERE d.setorBndes IS NOT NULL ORDER BY d.setorBndes")
    List<String> findDistinctSetores();

    // Retorna lista de anos disponíveis
    @Query("SELECT DISTINCT d.ano FROM DesembolsoMensal d WHERE d.ano IS NOT NULL ORDER BY d.ano DESC")
    List<Integer> findDistinctAnos();

    // Soma total de desembolsos agrupado por UF (para o gráfico de barras)
    // Retorna lista de arrays: [estado, total]
    @Query("SELECT d.uf, SUM(d.desembolsos) FROM DesembolsoMensal d GROUP BY d.uf ORDER BY SUM(d.desembolsos) DESC")
    List<Object[]> sumDesembolsosByUf();

    // Soma total agrupado por setor (para o gráfico de pizza)
    @Query("SELECT d.setorBndes, SUM(d.desembolsos) FROM DesembolsoMensal d GROUP BY d.setorBndes ORDER BY SUM(d.desembolsos) DESC")
    List<Object[]> sumDesembolsosBySetor();
}
