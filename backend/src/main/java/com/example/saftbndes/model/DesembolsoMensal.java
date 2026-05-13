package com.example.saftbndes.model;

import jakarta.persistence.*;

// =============================================
// CAMADA MODEL (Entidade)
// =============================================
// @Entity: diz ao JPA que essa classe vira uma tabela no banco
// Cada atributo abaixo corresponde a uma coluna do CSV do BNDES
// =============================================

@Entity
@Table(name = "desembolsos")
public class DesembolsoMensal {

    // @Id: chave primária da tabela
    // @GeneratedValue: o banco gera o número automaticamente (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ano da liberação do desembolso (ex: 2023)
    private Integer ano;

    // Mês da liberação (ex: 1 = Janeiro, 12 = Dezembro)
    private Integer mes;

    // Forma de apoio: "Direto" (empresa vai direto ao BNDES)
    // ou "Indireto" (empresa vai a um banco parceiro)
    @Column(name = "forma_de_apoio")
    private String formaDeApoio;

    // Nome do produto financeiro do BNDES (ex: BNDES Finem, BNDES Automático)
    private String produto;

    // Indica se o financiamento é voltado a inovação ("Sim" ou "Não")
    private String inovacao;

    // Porte da empresa que recebeu o financiamento
    // Ex: "Micro", "Pequeno", "Médio", "Grande"
    @Column(name = "porte_da_empresa")
    private String porteDaEmpresa;

    // Região do Brasil (ex: "Sudeste", "Sul", "Nordeste")
    private String regiao;

    // Unidade Federativa / Estado (ex: "SP", "RJ", "MG")
    private String uf;

    // Município onde o investimento foi realizado
    private String municipio;

    // Setor econômico segundo classificação do BNDES
    // Ex: "Indústria", "Comércio e Serviços", "Agropecuária"
    @Column(name = "setor_bndes")
    private String setorBndes;

    // Subsetor mais detalhado (ex: "Tecnologia da Informação")
    @Column(name = "subsetor_bndes")
    private String subsetorBndes;

    // Valor em Reais liberado naquele mês para aquele grupo
    // Ex: 1500000.00 = R$ 1.500.000,00
    @Column(name = "desembolsos_reais")
    private Double desembolsos;

    // =============================================
    // CONSTRUTORES
    // =============================================

    // Construtor vazio: obrigatório para o JPA funcionar
    public DesembolsoMensal() {}

    // Construtor com todos os campos (útil para criar objetos rapidamente)
    public DesembolsoMensal(Integer ano, Integer mes, String formaDeApoio,
                            String produto, String inovacao, String porteDaEmpresa,
                            String regiao, String uf, String municipio,
                            String setorBndes, String subsetorBndes, Double desembolsos) {
        this.ano = ano;
        this.mes = mes;
        this.formaDeApoio = formaDeApoio;
        this.produto = produto;
        this.inovacao = inovacao;
        this.porteDaEmpresa = porteDaEmpresa;
        this.regiao = regiao;
        this.uf = uf;
        this.municipio = municipio;
        this.setorBndes = setorBndes;
        this.subsetorBndes = subsetorBndes;
        this.desembolsos = desembolsos;
    }

    // =============================================
    // GETTERS E SETTERS
    // =============================================
    // Métodos para acessar e modificar os atributos privados

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public String getFormaDeApoio() { return formaDeApoio; }
    public void setFormaDeApoio(String formaDeApoio) { this.formaDeApoio = formaDeApoio; }

    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }

    public String getInovacao() { return inovacao; }
    public void setInovacao(String inovacao) { this.inovacao = inovacao; }

    public String getPorteDaEmpresa() { return porteDaEmpresa; }
    public void setPorteDaEmpresa(String porteDaEmpresa) { this.porteDaEmpresa = porteDaEmpresa; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getSetorBndes() { return setorBndes; }
    public void setSetorBndes(String setorBndes) { this.setorBndes = setorBndes; }

    public String getSubsetorBndes() { return subsetorBndes; }
    public void setSubsetorBndes(String subsetorBndes) { this.subsetorBndes = subsetorBndes; }

    public Double getDesembolsos() { return desembolsos; }
    public void setDesembolsos(Double desembolsos) { this.desembolsos = desembolsos; }
}
