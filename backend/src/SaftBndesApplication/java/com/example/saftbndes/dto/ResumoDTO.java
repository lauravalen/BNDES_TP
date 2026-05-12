package com.example.saftbndes.dto;

// =============================================
// DTO - Data Transfer Object
// =============================================
// Um DTO é um objeto simples usado para TRANSFERIR dados
// entre o backend e o frontend de forma organizada.
//
// Em vez de retornar a entidade inteira (com todos os campos),
// criamos um DTO só com o que o frontend precisa ver.
//
// Aqui usamos para retornar o resumo:
// "Sudeste" → R$ 50.000.000,00
// "SP" → R$ 30.000.000,00
// =============================================

public class ResumoDTO {

    // Nome do grupo (pode ser estado, setor, região, etc.)
    private String nome;

    // Total de desembolsos desse grupo em Reais
    private Double totalDesembolsos;

    // Construtor vazio
    public ResumoDTO() {}

    // Construtor com campos (usado no Service para criar os objetos)
    public ResumoDTO(String nome, Double totalDesembolsos) {
        this.nome = nome;
        this.totalDesembolsos = totalDesembolsos;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getTotalDesembolsos() { return totalDesembolsos; }
    public void setTotalDesembolsos(Double totalDesembolsos) { this.totalDesembolsos = totalDesembolsos; }
}
