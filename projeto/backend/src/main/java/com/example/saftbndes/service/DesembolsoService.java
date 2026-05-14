package com.example.saftbndes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                    d.setFormaDeApoio(normalizarFormaApoio(col[2]));
                    d.setProduto(normalizar(col[3]));
                    d.setInovacao(normalizarInovacao(col[5]));
                    d.setPorteDaEmpresa(normalizar(col[6]));
                    d.setRegiao(normalizar(col[7]));
                    d.setUf(converterUf(col[8]));
                    d.setMunicipio(normalizar(col[9]));
                    d.setSetorBndes(normalizar(col[13]));
                    if (col.length > 14) d.setSubsetorBndes(normalizar(col[14]));
                    if (col.length > 15) d.setDesembolsos(parseDouble(col[15]));

                    lote.add(d);
                    linhasSalvas++;

                    if (lote.size() == 500) {
                        repository.saveAll(lote);
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
                    d.setFormaDeApoio(normalizarFormaApoio(col[2]));
                    d.setProduto(normalizar(col[3]));
                    d.setInovacao(normalizarInovacao(col[5]));
                    d.setPorteDaEmpresa(normalizar(col[6]));
                    d.setRegiao(normalizar(col[7]));
                    d.setUf(converterUf(col[8]));
                    d.setMunicipio(normalizar(col[9]));
                    d.setSetorBndes(normalizar(col[13]));
                    if (col.length > 14) d.setSubsetorBndes(normalizar(col[14]));
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

    // =============================================
    // MÉTODOS DE CONSULTA (NÃO-PAGINADOS)
    // =============================================

    public List<DesembolsoMensal> listarTodos() {
        return repository.findAll();
    }

    public Optional<DesembolsoMensal> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<DesembolsoMensal> filtrarPorUf(String uf) {
        return repository.findByUf(uf);
    }

    public List<DesembolsoMensal> filtrarPorSetor(String setor) {
        return repository.findBySetorBndes(setor);
    }

    public List<DesembolsoMensal> filtrarPorUfESetor(String uf, String setor) {
        return repository.findByUfAndSetorBndes(uf, setor);
    }

    // =============================================
    // MÉTODOS DE CONSULTA (PAGINADOS)
    // =============================================

    public Page<DesembolsoMensal> listarPaginado(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<DesembolsoMensal> filtrarPorUfPaginado(String uf, Pageable pageable) {
        return repository.findByUf(uf, pageable);
    }

    public Page<DesembolsoMensal> filtrarPorSetorPaginado(String setor, Pageable pageable) {
        return repository.findBySetorBndes(setor, pageable);
    }

    public Page<DesembolsoMensal> filtrarPorUfESetorPaginado(String uf, String setor, Pageable pageable) {
        return repository.findByUfAndSetorBndes(uf, setor, pageable);
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

    // =============================================
    // NORMALIZAÇÃO DOS DADOS DO CSV
    // =============================================
    // Converte texto para Title Case (primeira letra maiúscula)
    // Ex: "RIO DE JANEIRO" → "Rio De Janeiro"
    private String normalizar(String valor) {
        String limpo = limpar(valor);
        if (limpo.isEmpty()) return limpo;

        // Palavras que ficam em minúsculo (preposições)
        var preposicoes = java.util.Set.of("de", "da", "do", "das", "dos", "em", "na", "no");

        String[] palavras = limpo.toLowerCase().split(" ");
        var resultado = new StringBuilder();

        for (int i = 0; i < palavras.length; i++) {
            String p = palavras[i];
            if (p.isEmpty()) continue;
            if (i > 0 && preposicoes.contains(p)) {
                resultado.append(p);
            } else {
                resultado.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) resultado.append(p.substring(1));
            }
            if (i < palavras.length - 1) resultado.append(" ");
        }
        return resultado.toString();
    }

    // Normaliza forma de apoio: "DIRETA" → "Direto", "INDIRETA" → "Indireto"
    private String normalizarFormaApoio(String valor) {
        String limpo = limpar(valor).toUpperCase();
        if (limpo.equals("DIRETA")) return "Direto";
        if (limpo.equals("INDIRETA")) return "Indireto";
        return normalizar(valor);
    }

    // Normaliza inovação: "SIM" → "Sim", "NÃO" → "Não"
    private String normalizarInovacao(String valor) {
        String limpo = limpar(valor).toUpperCase();
        if (limpo.equals("SIM")) return "Sim";
        if (limpo.equals("NÃO") || limpo.equals("NAO")) return "Não";
        return normalizar(valor);
    }

    // Converte nome completo do estado para sigla (UF)
    // Ex: "RIO DE JANEIRO" → "RJ"
    private String converterUf(String valor) {
        String limpo = limpar(valor).toUpperCase().trim();

        // Mapa de nomes de estados para códigos UF
        var mapa = new java.util.LinkedHashMap<String, String>();
        mapa.put("ACRE", "AC");
        mapa.put("ALAGOAS", "AL");
        mapa.put("AMAPÁ", "AP");
        mapa.put("AMAZONAS", "AM");
        mapa.put("BAHIA", "BA");
        mapa.put("CEARÁ", "CE");
        mapa.put("DISTRITO FEDERAL", "DF");
        mapa.put("ESPÍRITO SANTO", "ES");
        mapa.put("GOIÁS", "GO");
        mapa.put("MARANHÃO", "MA");
        mapa.put("MATO GROSSO", "MT");
        mapa.put("MATO GROSSO DO SUL", "MS");
        mapa.put("MINAS GERAIS", "MG");
        mapa.put("PARÁ", "PA");
        mapa.put("PARAÍBA", "PB");
        mapa.put("PARANÁ", "PR");
        mapa.put("PERNAMBUCO", "PE");
        mapa.put("PIAUÍ", "PI");
        mapa.put("RIO DE JANEIRO", "RJ");
        mapa.put("RIO GRANDE DO NORTE", "RN");
        mapa.put("RIO GRANDE DO SUL", "RS");
        mapa.put("RONDÔNIA", "RO");
        mapa.put("RORAIMA", "RR");
        mapa.put("SANTA CATARINA", "SC");
        mapa.put("SÃO PAULO", "SP");
        mapa.put("SERGIPE", "SE");
        mapa.put("TOCANTINS", "TO");

        // Se for uma sigla (2 letras), retorna direto
        if (limpo.length() == 2) return limpo;

        // Procura no mapa pelo nome completo
        for (var entry : mapa.entrySet()) {
            if (limpo.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Se não encontrar, retorna como estava
        return normalizar(valor);
    }
}
