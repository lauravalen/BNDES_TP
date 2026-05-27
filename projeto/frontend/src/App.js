import React, { useState, useEffect } from 'react';
import Tabela from './components/Tabela';
import Formulario from './components/Formulario';
import Graficos from './components/Graficos';
import DetalhesModal from './components/DetalhesModal';
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
  const [carregandoMais, setCarregandoMais] = useState(false);

  // Controle de paginação (server-side)
  const [paginaAtual, setPaginaAtual] = useState(0);
  const [totalRegistros, setTotalRegistros] = useState(0);
  const TAMANHO_PAGINA = 50;

  // Detalhes da operação (modal)
  const [detalheSelecionado, setDetalheSelecionado] = useState(null);

  // ---- EFEITO INICIAL ----
  useEffect(() => {
    carregarFiltros();
    buscarDesembolsos(0, false);
  }, []);

  // ---- FUNÇÕES DE COMUNICAÇÃO COM A API ----

  // Busca uma página de desembolsos
  // page: número da página (0-based)
  // acumular: se true, adiciona ao final dos dados existentes
  async function buscarDesembolsos(page = 0, acumular = false, uf = filtroUf, setor = filtroSetor) {
    if (acumular) setCarregandoMais(true);
    else setCarregando(true);

    try {
      const params = [`page=${page}`, `size=${TAMANHO_PAGINA}`];
      if (uf) params.push(`uf=${encodeURIComponent(uf)}`);
      if (setor) params.push(`setor=${encodeURIComponent(setor)}`);
      let url = `${API_URL}/desembolsos?${params.join('&')}`;

      const resposta = await fetch(url);
      const body = await resposta.json();

      if (acumular) {
        setDesembolsos(prev => [...prev, ...body.content]);
      } else {
        setDesembolsos(body.content);
      }
      setTotalRegistros(body.totalElements);
      setPaginaAtual(page);
    } catch (erro) {
      setMensagem('Erro ao conectar com o servidor. Verifique se o Java está rodando na porta 8080.');
    }

    if (acumular) setCarregandoMais(false);
    else setCarregando(false);
  }

  // Carrega a próxima página e acumula na tabela
  async function carregarMais() {
    await buscarDesembolsos(paginaAtual + 1, true);
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
        setFiltroUf('');
        setFiltroSetor('');
        setDesembolsos([]);
        buscarDesembolsos(0, false, '', '');
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
        buscarDesembolsos(0, false);
      } else {
        setMensagem('Registro não encontrado.');
      }
    } catch (erro) {
      setMensagem('Erro ao excluir: ' + erro.message);
    }
  }

  // Busca detalhes de uma operação pelo ID e abre o modal
  async function verDetalhes(id) {
    try {
      const resposta = await fetch(`${API_URL}/desembolsos/${id}`);
      if (resposta.ok) {
        setDetalheSelecionado(await resposta.json());
      }
    } catch (erro) {
      setMensagem('Erro ao buscar detalhes: ' + erro.message);
    }
  }

  // Aplica os filtros quando o usuário muda o dropdown
  function aplicarFiltros(novaUf, novoSetor) {
    setFiltroUf(novaUf);
    setFiltroSetor(novoSetor);
    setDesembolsos([]);
    buscarDesembolsos(0, false, novaUf, novoSetor);
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
        </div>
      </header>

      {/* MENSAGEM DE FEEDBACK */}
      {mensagem && (
        <div className={`mensagem ${mensagem.toLowerCase().includes('erro') ? 'erro' : 'sucesso'}`}>
          {mensagem}
          <button className="fechar-msg" onClick={() => setMensagem('')}>×</button>
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
                : `${totalRegistros} registro(s) encontrado(s)`}
            </div>

            {/* TABELA DE DADOS */}
            <Tabela
              dados={desembolsos}
              onDeletar={deletarDesembolso}
              onVerDetalhes={verDetalhes}
              carregando={carregando}
              carregandoMais={carregandoMais}
              onCarregarMais={carregarMais}
              temMais={desembolsos.length < totalRegistros}
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

      <DetalhesModal
        desembolso={detalheSelecionado}
        onFechar={() => setDetalheSelecionado(null)}
      />
    </div>
  );
}

export default App;
