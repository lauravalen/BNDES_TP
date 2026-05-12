import React, { useState, useEffect } from 'react';
import Tabela from './components/Tabela';
import Formulario from './components/Formulario';
import Graficos from './components/Graficos';
import './App.css';

// =============================================
// CONSTANTE: URL BASE DA API
// =============================================
// Aqui definimos o endereço do backend Java.
// Se mudar a porta, basta alterar aqui.
const API_URL = 'http://localhost:8080/api';

// =============================================
// COMPONENTE PRINCIPAL: App
// =============================================
// Em React, tudo é dividido em "componentes".
// O App é o componente raiz — ele controla quais
// outros componentes são mostrados na tela.
//
// useState: guarda valores que, quando mudam,
//   fazem a tela re-renderizar automaticamente.
//
// useEffect: executa código quando o componente
//   é carregado na tela (similar ao onLoad do HTML).
// =============================================

function App() {
  // ---- ESTADOS (dados que a tela usa) ----

  // Lista de desembolsos que aparece na tabela
  const [desembolsos, setDesembolsos] = useState([]);

  // Listas para popular os dropdowns de filtro
  const [ufs, setUfs] = useState([]);
  const [setores, setSetores] = useState([]);

  // Filtros selecionados pelo usuário
  const [filtroUf, setFiltroUf] = useState('');
  const [filtroSetor, setFiltroSetor] = useState('');

  // Qual aba está ativa: 'tabela', 'formulario' ou 'graficos'
  const [abaAtiva, setAbaAtiva] = useState('tabela');

  // Mensagens de feedback para o usuário
  const [mensagem, setMensagem] = useState('');
  const [carregando, setCarregando] = useState(false);

  // Modal de carregamento de CSV
  const [modalCSVAberto, setModalCSVAberto] = useState(false);
  const [caminhoCSV, setCaminhoCSV] = useState('');

  // ---- EFEITO INICIAL ----
  // Roda UMA VEZ quando a página carrega
  useEffect(() => {
    carregarFiltros();
    buscarDesembolsos();
  }, []); // [] = sem dependências = roda só uma vez

  // ---- FUNÇÕES DE COMUNICAÇÃO COM A API ----

  // Busca os desembolsos aplicando os filtros ativos
  async function buscarDesembolsos(uf = filtroUf, setor = filtroSetor) {
    setCarregando(true);
    try {
      // Monta a URL com os filtros (se existirem)
      let url = `${API_URL}/desembolsos`;
      const params = [];
      if (uf) params.push(`uf=${encodeURIComponent(uf)}`);
      if (setor) params.push(`setor=${encodeURIComponent(setor)}`);
      if (params.length > 0) url += '?' + params.join('&');

      // fetch() faz a requisição HTTP para o Java
      const resposta = await fetch(url);
      const dados = await resposta.json(); // converte JSON em objeto JS
      setDesembolsos(dados);
    } catch (erro) {
      setMensagem('Erro ao conectar com o servidor. Verifique se o Java está rodando na porta 8080.');
    }
    setCarregando(false);
  }

  // Busca os valores disponíveis para os filtros
  async function carregarFiltros() {
    try {
      const [resUfs, resSetores] = await Promise.all([
        fetch(`${API_URL}/filtros/ufs`),
        fetch(`${API_URL}/filtros/setores`)
      ]);
      setUfs(await resUfs.json());
      setSetores(await resSetores.json());
    } catch (erro) {
      console.log('Aguardando servidor...');
    }
  }

  // Carrega dados de exemplo (sem precisar do CSV)
  async function carregarExemplo() {
    setCarregando(true);
    setMensagem('Carregando dados de exemplo...');
    try {
      const resposta = await fetch(`${API_URL}/carga/exemplo`, { method: 'POST' });
      const texto = await resposta.text();
      setMensagem('Sucesso: ' + texto);
      await carregarFiltros();
      await buscarDesembolsos('', '');
    } catch (erro) {
      setMensagem('Erro: ' + erro.message);
    }
    setCarregando(false);
  }

  // Carrega o CSV real do BNDES
  async function carregarCSV() {
    setModalCSVAberto(true);
    setCaminhoCSV('');
  }

  // Finaliza o carregamento do CSV após o usuário digitar o caminho
  async function finalizarCarregamentoCSV() {
    if (!caminhoCSV.trim()) {
      setMensagem('Por favor, insira um caminho de arquivo válido.');
      return;
    }

    setModalCSVAberto(false);
    setCarregando(true);
    setMensagem('Carregando CSV... (pode demorar alguns minutos para arquivos grandes)');
    try {
      const resposta = await fetch(
        `${API_URL}/carga?caminho=${encodeURIComponent(caminhoCSV)}`,
        { method: 'POST' }
      );
      const texto = await resposta.text();
      setMensagem('Sucesso: ' + texto);
      await carregarFiltros();
      await buscarDesembolsos('', '');
    } catch (erro) {
      setMensagem('Erro: ' + erro.message);
    }
    setCarregando(false);
    setCaminhoCSV('');
  }

  // Fecha o modal sem carregar
  function fecharModalCSV() {
    setModalCSVAberto(false);
    setCaminhoCSV('');
  }

  // Salva um novo desembolso vindo do formulário
  async function salvarDesembolso(dados) {
    try {
      const resposta = await fetch(`${API_URL}/desembolsos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }, // diz que o corpo é JSON
        body: JSON.stringify(dados) // converte objeto JS em JSON
      });

      if (resposta.status === 201) {
        setMensagem('Desembolso cadastrado com sucesso!');
        setAbaAtiva('tabela');
        buscarDesembolsos();
      }
    } catch (erro) {
      setMensagem('Erro ao salvar: ' + erro.message);
    }
  }

  // Deleta um desembolso pelo ID
  async function deletarDesembolso(id) {
    if (!window.confirm('Tem certeza que deseja excluir este registro?')) return;

    try {
      const resposta = await fetch(`${API_URL}/desembolsos/${id}`, {
        method: 'DELETE'
      });

      if (resposta.status === 204) {
        setMensagem('Registro excluído!');
        buscarDesembolsos(); // atualiza a tabela
      } else {
        setMensagem('Registro não encontrado.');
      }
    } catch (erro) {
      setMensagem('Erro ao excluir: ' + erro.message);
    }
  }

  // Aplica os filtros quando o usuário muda o dropdown
  function aplicarFiltros(novaUf, novoSetor) {
    setFiltroUf(novaUf);
    setFiltroSetor(novoSetor);
    buscarDesembolsos(novaUf, novoSetor);
  }

  // ---- RENDERIZAÇÃO (o que aparece na tela) ----
  return (
    <div className="app">

      {/* CABEÇALHO */}
      <header className="header">
        <div className="header-content">
          <div className="header-logo">
            <div>
              <h1>SAFT-BNDES</h1>
              <p>Sistema de Apoio ao Financiamento Tecnológico</p>
            </div>
          </div>
          <div className="header-acoes">
            <button
              className="btn btn-outline"
              onClick={carregarExemplo}
              disabled={carregando}
              title="Carrega dados de demonstração para testar o sistema"
            >
              Carregar exemplo
            </button>
            <button
              className="btn btn-primary"
              onClick={carregarCSV}
              disabled={carregando}
              title="Carrega o CSV real baixado do portal do BNDES"
            >
              Importar CSV
            </button>
          </div>
        </div>
      </header>

      {/* MENSAGEM DE FEEDBACK */}
      {mensagem && (
        <div className={`mensagem ${mensagem.toLowerCase().includes('erro') ? 'erro' : 'sucesso'}`}>
          {mensagem}
          <button className="fechar-msg" onClick={() => setMensagem('')}>×</button>
        </div>
      )}

      {/* MODAL DE CARREGAMENTO DE CSV */}
      {modalCSVAberto && (
        <div className="modal-overlay" onClick={fecharModalCSV}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Importar arquivo CSV</h2>
              <button className="modal-close" onClick={fecharModalCSV}>×</button>
            </div>
            <div className="modal-body">
              <p className="modal-descricao">
                Insira o caminho completo do arquivo CSV que você baixou do portal do BNDES:
              </p>
              <input
                type="text"
                className="modal-input"
                placeholder="Ex: C:\Users\Seu Nome\Downloads\desembolsos-mensais.csv"
                value={caminhoCSV}
                onChange={e => setCaminhoCSV(e.target.value)}
                onKeyPress={e => e.key === 'Enter' && finalizarCarregamentoCSV()}
                autoFocus
              />
              <div className="modal-exemplos">
                <p className="exemplos-titulo">Exemplos de caminho:</p>
                <div className="exemplo">
                  <strong>Windows:</strong>
                  <code>C:\Users\SeuNome\Downloads\desembolsos.csv</code>
                </div>
                <div className="exemplo">
                  <strong>Mac/Linux:</strong>
                  <code>/home/seunome/Downloads/desembolsos.csv</code>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={fecharModalCSV}>Cancelar</button>
              <button className="btn btn-primary" onClick={finalizarCarregamentoCSV}>Carregar</button>
            </div>
          </div>
        </div>
      )}

      {/* ABAS DE NAVEGAÇÃO */}
      <nav className="abas">
        <button
          className={`aba ${abaAtiva === 'tabela' ? 'ativa' : ''}`}
          onClick={() => setAbaAtiva('tabela')}
        >
          Consultar dados
        </button>
        <button
          className={`aba ${abaAtiva === 'graficos' ? 'ativa' : ''}`}
          onClick={() => setAbaAtiva('graficos')}
        >
          Gráficos
        </button>
        <button
          className={`aba ${abaAtiva === 'formulario' ? 'ativa' : ''}`}
          onClick={() => setAbaAtiva('formulario')}
        >
          Novo registro
        </button>
      </nav>

      {/* CONTEÚDO PRINCIPAL */}
      <main className="conteudo">

        {/* ABA: TABELA COM FILTROS */}
        {abaAtiva === 'tabela' && (
          <div>
            {/* FILTROS */}
            <div className="filtros">
              <h3>Filtros</h3>
              <div className="filtros-linha">
                <div className="filtro-grupo">
                  <label>Estado (UF):</label>
                  <select
                    value={filtroUf}
                    onChange={e => aplicarFiltros(e.target.value, filtroSetor)}
                  >
                    <option value="">Todos os estados</option>
                    {ufs.map(uf => (
                      <option key={uf} value={uf}>{uf}</option>
                    ))}
                  </select>
                </div>

                <div className="filtro-grupo">
                  <label>Setor:</label>
                  <select
                    value={filtroSetor}
                    onChange={e => aplicarFiltros(filtroUf, e.target.value)}
                  >
                    <option value="">Todos os setores</option>
                    {setores.map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </div>

                <button
                  className="btn btn-outline"
                  onClick={() => aplicarFiltros('', '')}
                >
                  Limpar filtros
                </button>
              </div>
            </div>

            {/* CONTADOR DE RESULTADOS */}
            <div className="contador">
              {carregando
                ? 'Carregando...'
                : `${desembolsos.length} registro(s) encontrado(s)`}
            </div>

            {/* TABELA DE DADOS */}
            <Tabela
              dados={desembolsos}
              onDeletar={deletarDesembolso}
              carregando={carregando}
            />
          </div>
        )}

        {/* ABA: GRÁFICOS */}
        {abaAtiva === 'graficos' && (
          <Graficos apiUrl={API_URL} />
        )}

        {/* ABA: FORMULÁRIO */}
        {abaAtiva === 'formulario' && (
          <Formulario
            onSalvar={salvarDesembolso}
            onCancelar={() => setAbaAtiva('tabela')}
          />
        )}
      </main>
    </div>
  );
}

export default App;
