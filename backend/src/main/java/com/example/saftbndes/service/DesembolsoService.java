package com.example.saftbndes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saftbndes.dto.ResumoDTO;
import com.example.saftbndes.model.DesembolsoMensal;
import com.example.saftbndes.repository.DesembolsoRepository;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

// =============================================
// CAMADA SERVICE
// =============================================
// Aqui fica a LÓGICA DE NEGÓCIO do sistema.
// O Controller chama o Service.
// O Service chama o Repository.
// Nunca o Controller chama o Repository diretamente!
//
// @Service: diz ao Spring que essa classe é um serviço
// e pode ser injetada em outros lugares com @Autowired
// =============================================

@Service
public class DesembolsoService {

    // @Autowired: o Spring injeta automaticamente o Repository aqui
    // Não precisamos escrever "new DesembolsoRepository()" em nenhum lugar
    @Autowired
    private DesembolsoRepository repository;

    // =============================================
    // MÉTODO: CARREGAR CSV VIA UPLOAD (MultipartFile)
    // =============================================
    // Recebe o arquivo enviado pelo navegador (upload real),
    // lê o InputStream e salva no banco H2.
    // Reutiliza toda a lógica de parsing do método carregarCSV.
    // =============================================
    // =============================================
    // MÉTODO: CARREGAR CSV VIA UPLOAD COM LIMITE DE LINHAS
    // =============================================
    // limite = 0 significa "sem limite" (importa tudo)
    // limite = 5000 importa apenas as primeiras 5000 linhas
    // Útil para testes com arquivos grandes (ex: 770MB do BNDES)
    // =============================================
    public String carregarCSVUpload(MultipartFile arquivo, int limite) {
        if (arquivo == null || arquivo.isEmpty()) {
            return "ERRO: Nenhum arquivo recebido. Selecione um arquivo CSV.";
        }

        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || !nomeArquivo.toLowerCase().endsWith(".csv")) {
            return "ERRO: O arquivo enviado não é um CSV. Envie um arquivo com extensão .csv";
        }

        int linhasSalvas = 0;
        int linhasComErro = 0;
        boolean limitado = limite > 0;

        try {
            BufferedReader br = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), Charset.forName("Windows-1252"))
            );

            br.readLine(); // descarta o cabeçalho

            List<DesembolsoMensal> lote = new ArrayList<>();

            String linha;
            while ((linha = br.readLine()) != null) {

                // Para se atingiu o limite
                if (limitado && linhasSalvas >= limite) break;

                if (linha.trim().isEmpty()) continue;

                try {
                    String[] col = linha.split(";", -1);
                    if (col.length < 15) continue;

                    DesembolsoMensal d = new DesembolsoMensal();
                    d.setAno(parseInt(col[0]));
                    d.setMes(parseInt(col[1]));
                    d.setFormaDeApoio(limpar(col[2]));
                    d.setProduto(limpar(col[3]));
                    d.setInovacao(limpar(col[5]));
                    d.setPorteDaEmpresa(limpar(col[6]));
                    d.setRegiao(limpar(col[7]));
                    d.setUf(limpar(col[8]));
                    d.setMunicipio(limpar(col[9]));
                    d.setSetorBndes(limpar(col[13]));
                    if (col.length > 14) d.setSubsetorBndes(limpar(col[14]));
                    if (col.length > 15) d.setDesembolsos(parseDouble(col[15]));

                    // Acumula em lote para salvar de 500 em 500 (muito mais rápido)
                    lote.add(d);
                    linhasSalvas++;

                    if (lote.size() == 500) {
                        repository.saveAll(lote); // salva 500 de uma vez em vez de 1 por vez
                        lote.clear();
                        System.out.println("Upload: salvando lote... " + linhasSalvas + " registros");
                    }

                } catch (Exception e) {
                    if (linhasComErro < 5) {
                        System.out.println("  ERRO na linha " + (linhasSalvas + linhasComErro + 1) + ": " + e.getMessage());
                    }
                    linhasComErro++;
                }
            }

            if (!lote.isEmpty()) {
                repository.saveAll(lote);
            }

            br.close();

            String msg = "Arquivo '" + nomeArquivo + "' importado! " +
                    linhasSalvas + " registros salvos. " +
                    linhasComErro + " linhas ignoradas.";
            if (limitado) {
                msg += " (limitado às primeiras " + limite + " linhas)";
            }
            return msg;

        } catch (Exception e) {
            return "ERRO ao processar o arquivo: " + e.getMessage();
        }
    }

    // =============================================
    // MÉTODO: CARREGAR CSV DO BNDES
    // =============================================
    // Lê o arquivo CSV linha por linha, cria objetos DesembolsoMensal
    // e salva no banco H2 usando repository.save()
    //
    // IMPORTANTE: O CSV do BNDES usa encoding Windows-1252 e separador ";"
    // =============================================
    public String carregarCSV(String caminhoDoArquivo, int limite) {
        int linhasSalvas = 0;
        int linhasComErro = 0;
        boolean limitado = limite > 0;

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(caminhoDoArquivo),
                            Charset.forName("Windows-1252")
                    )
            );

            br.readLine(); // descarta o cabeçalho

            List<DesembolsoMensal> lote = new ArrayList<>();

            String linha;
            while ((linha = br.readLine()) != null) {

                if (limitado && linhasSalvas >= limite) break;
                if (linha.trim().isEmpty()) continue;

                try {
                    String[] col = linha.split(";", -1);
                    if (col.length < 15) continue;

                    DesembolsoMensal d = new DesembolsoMensal();
                    d.setAno(parseInt(col[0]));
                    d.setMes(parseInt(col[1]));
                    d.setFormaDeApoio(limpar(col[2]));
                    d.setProduto(limpar(col[3]));
                    d.setInovacao(limpar(col[5]));
                    d.setPorteDaEmpresa(limpar(col[6]));
                    d.setRegiao(limpar(col[7]));
                    d.setUf(limpar(col[8]));
                    d.setMunicipio(limpar(col[9]));
                    d.setSetorBndes(limpar(col[13]));
                    if (col.length > 14) d.setSubsetorBndes(limpar(col[14]));
                    if (col.length > 15) d.setDesembolsos(parseDouble(col[15]));

                    lote.add(d);
                    linhasSalvas++;

                    if (lote.size() == 500) {
                        repository.saveAll(lote);
                        lote.clear();
                        System.out.println("Salvando lote... " + linhasSalvas + " registros");
                    }

                } catch (Exception e) {
                    if (linhasComErro < 5) {
                        System.out.println("  ERRO na linha " + (linhasSalvas + linhasComErro + 1) + ": " + e.getMessage());
                    }
                    linhasComErro++;
                }
            }

            if (!lote.isEmpty()) {
                repository.saveAll(lote);
            }
                    linhasComErro++;
                }
            }

            if (!lote.isEmpty()) {
                repository.saveAll(lote);
            }

            br.close();

            String msg = "Carga concluída! " + linhasSalvas + " registros salvos. " +
                    linhasComErro + " linhas com erro ignoradas.";
            if (limitado) {
                msg += " (limitado às primeiras " + limite + " linhas)";
            }
            return msg;

        } catch (FileNotFoundException e) {
            return "ERRO: Arquivo não encontrado em: " + caminhoDoArquivo +
                    ". Verifique se o arquivo existe no caminho informado.";
        } catch (Exception e) {
            return "ERRO ao carregar CSV: " + e.getMessage();
        }
    }

    // =============================================
    // MÉTODO: CARREGAR DADOS DE EXEMPLO
    // =============================================
    // Carrega dados fictícios para demonstração quando não há CSV disponível
    // Útil para testar o sistema sem precisar do arquivo real
    // =============================================
    public String carregarDadosExemplo() {
        // Limpa dados anteriores
        repository.deleteAll();

        List<DesembolsoMensal> exemplos = new ArrayList<>();

        // Dados fictícios baseados em valores reais do BNDES
        exemplos.add(new DesembolsoMensal(2023, 1, "Indireto", "BNDES Automático", "Não", "Micro", "Sudeste", "SP", "São Paulo", "Indústria", "Tecnologia da Informação", 150000.0));
        exemplos.add(new DesembolsoMensal(2023, 1, "Indireto", "BNDES Automático", "Sim", "Pequeno", "Sudeste", "SP", "Campinas", "Comércio e Serviços", "Software", 280000.0));
        exemplos.add(new DesembolsoMensal(2023, 2, "Direto", "BNDES Finem", "Sim", "Médio", "Sudeste", "RJ", "Rio de Janeiro", "Indústria", "Tecnologia da Informação", 1200000.0));
        exemplos.add(new DesembolsoMensal(2023, 2, "Indireto", "BNDES Automático", "Não", "Micro", "Sul", "RS", "Porto Alegre", "Comércio e Serviços", "Comércio Varejista", 95000.0));
        exemplos.add(new DesembolsoMensal(2023, 3, "Indireto", "BNDES Automático", "Sim", "Pequeno", "Sul", "SC", "Florianópolis", "Indústria", "Software", 340000.0));
        exemplos.add(new DesembolsoMensal(2023, 3, "Indireto", "BNDES Automático", "Não", "Micro", "Nordeste", "BA", "Salvador", "Comércio e Serviços", "Serviços de TI", 75000.0));
        exemplos.add(new DesembolsoMensal(2023, 4, "Direto", "BNDES Finem", "Sim", "Grande", "Sudeste", "MG", "Belo Horizonte", "Indústria", "Tecnologia da Informação", 5000000.0));
        exemplos.add(new DesembolsoMensal(2023, 4, "Indireto", "BNDES Automático", "Não", "Pequeno", "Centro-Oeste", "GO", "Goiânia", "Agropecuária", "Agricultura", 420000.0));
        exemplos.add(new DesembolsoMensal(2023, 5, "Indireto", "BNDES Automático", "Sim", "Micro", "Nordeste", "PE", "Recife", "Comércio e Serviços", "Software", 110000.0));
        exemplos.add(new DesembolsoMensal(2023, 5, "Indireto", "BNDES Automático", "Não", "Médio", "Sul", "PR", "Curitiba", "Indústria", "Fabricação de Equipamentos", 890000.0));
        exemplos.add(new DesembolsoMensal(2023, 6, "Direto", "BNDES Finem", "Sim", "Grande", "Sudeste", "SP", "São Paulo", "Infraestrutura", "Energia Elétrica", 12000000.0));
        exemplos.add(new DesembolsoMensal(2023, 6, "Indireto", "BNDES Automático", "Não", "Micro", "Norte", "AM", "Manaus", "Indústria", "Fabricação de Eletrônicos", 200000.0));
        exemplos.add(new DesembolsoMensal(2023, 7, "Indireto", "BNDES Automático", "Sim", "Pequeno", "Sudeste", "SP", "Santos", "Comércio e Serviços", "Tecnologia da Informação", 175000.0));
        exemplos.add(new DesembolsoMensal(2023, 7, "Indireto", "BNDES Automático", "Não", "Médio", "Nordeste", "CE", "Fortaleza", "Comércio e Serviços", "Serviços Financeiros", 650000.0));
        exemplos.add(new DesembolsoMensal(2023, 8, "Direto", "BNDES Finem", "Sim", "Grande", "Sul", "RS", "Porto Alegre", "Infraestrutura", "Saneamento", 8500000.0));
        exemplos.add(new DesembolsoMensal(2024, 1, "Indireto", "BNDES Automático", "Sim", "Micro", "Sudeste", "SP", "São Paulo", "Indústria", "Tecnologia da Informação", 180000.0));
        exemplos.add(new DesembolsoMensal(2024, 1, "Indireto", "BNDES Automático", "Não", "Pequeno", "Sudeste", "RJ", "Niterói", "Comércio e Serviços", "Software", 320000.0));
        exemplos.add(new DesembolsoMensal(2024, 2, "Direto", "BNDES Finem", "Sim", "Médio", "Sul", "PR", "Curitiba", "Indústria", "Tecnologia da Informação", 1500000.0));
        exemplos.add(new DesembolsoMensal(2024, 2, "Indireto", "BNDES Automático", "Não", "Micro", "Nordeste", "RN", "Natal", "Agropecuária", "Fruticultura", 85000.0));
        exemplos.add(new DesembolsoMensal(2024, 3, "Indireto", "BNDES Automático", "Sim", "Pequeno", "Centro-Oeste", "MT", "Cuiabá", "Agropecuária", "Pecuária", 460000.0));

        repository.saveAll(exemplos);
        return "20 registros de exemplo carregados com sucesso!";
    }

    // =============================================
    // MÉTODOS DE CONSULTA
    // =============================================

    // Retorna todos os registros
    public List<DesembolsoMensal> listarTodos() {
        return repository.findAll();
    }

    // Busca por ID — retorna Optional para evitar NullPointerException
    // Optional significa: "pode ter um valor ou pode estar vazio"
    public Optional<DesembolsoMensal> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Filtros individuais
    public List<DesembolsoMensal> filtrarPorUf(String uf) {
        return repository.findByUf(uf);
    }

    public List<DesembolsoMensal> filtrarPorSetor(String setor) {
        return repository.findBySetorBndes(setor);
    }

    public List<DesembolsoMensal> filtrarPorUfESetor(String uf, String setor) {
        return repository.findByUfAndSetorBndes(uf, setor);
    }

    // Retorna listas para popular os filtros no frontend
    public List<String> listarUfs() {
        return repository.findDistinctUfs();
    }

    public List<String> listarSetores() {
        return repository.findDistinctSetores();
    }

    public List<Integer> listarAnos() {
        return repository.findDistinctAnos();
    }

    // Resumo por estado (para o gráfico de barras)
    public List<ResumoDTO> resumoPorUf() {
        List<Object[]> resultado = repository.sumDesembolsosByUf();
        List<ResumoDTO> resumos = new ArrayList<>();
        for (Object[] linha : resultado) {
            String nome = (String) linha[0];
            Double total = (Double) linha[1];
            resumos.add(new ResumoDTO(nome, total));
        }
        return resumos;
    }

    // Resumo por setor (para o gráfico de pizza)
    public List<ResumoDTO> resumoPorSetor() {
        List<Object[]> resultado = repository.sumDesembolsosBySetor();
        List<ResumoDTO> resumos = new ArrayList<>();
        for (Object[] linha : resultado) {
            String nome = (String) linha[0];
            Double total = (Double) linha[1];
            resumos.add(new ResumoDTO(nome, total));
        }
        return resumos;
    }

    // Salva um novo desembolso (usado pelo formulário do frontend)
    public DesembolsoMensal salvar(DesembolsoMensal desembolso) {
        return repository.save(desembolso);
    }

    // Deleta por ID
    public void deletar(Long id) {
        repository.deleteById(id);
    }

    // =============================================
    // MÉTODOS AUXILIARES PRIVADOS
    // =============================================

    // Limpa espaços e aspas dos valores do CSV
    private String limpar(String valor) {
        if (valor == null) return "";
        return valor.trim().replace("\"", "");
    }

    // Converte String para Integer com segurança
    private Integer parseInt(String valor) {
        try {
            return Integer.parseInt(limpar(valor));
        } catch (Exception e) {
            return null;
        }
    }

    // Converte String para Double com segurança
    // O CSV do BNDES usa vírgula como separador decimal
    private Double parseDouble(String valor) {
        try {
            String limpo = limpar(valor).replace(".", "").replace(",", ".");
            return Double.parseDouble(limpo);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
