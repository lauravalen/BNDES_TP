import React, { useState, useEffect } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';

// =============================================
// COMPONENTE: Graficos
// =============================================
// Busca os resumos da API e exibe:
//   1. Gráfico de barras: Top estados por desembolso
//   2. Gráfico de pizza: Distribuição por setor
// =============================================

// Cores para o gráfico de pizza
const CORES = ['#2563eb', '#16a34a', '#dc2626', '#d97706', '#7c3aed',
               '#0891b2', '#be185d', '#65a30d', '#9f1239', '#1d4ed8'];

function Graficos({ apiUrl }) {
  const [dadosUf, setDadosUf] = useState([]);
  const [dadosSetor, setDadosSetor] = useState([]);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    async function buscar() {
      try {
        const [resUf, resSetor] = await Promise.all([
          fetch(`${apiUrl}/resumo/por-uf`),
          fetch(`${apiUrl}/resumo/por-setor`)
        ]);
        const uf = await resUf.json();
        const setor = await resSetor.json();

        // Pega só os top 10 para não poluir o gráfico
        setDadosUf(uf.slice(0, 10));
        setDadosSetor(setor.slice(0, 8));
      } catch (e) {
        console.error(e);
      }
      setCarregando(false);
    }
    buscar();
  }, [apiUrl]);

  // Formata valores grandes em formato legível
  // Ex: 1500000 → "R$ 1,5M"
  function formatarEixo(valor) {
    if (valor >= 1_000_000_000) return `R$ ${(valor / 1_000_000_000).toFixed(1)}B`;
    if (valor >= 1_000_000) return `R$ ${(valor / 1_000_000).toFixed(1)}M`;
    if (valor >= 1_000) return `R$ ${(valor / 1_000).toFixed(0)}K`;
    return `R$ ${valor}`;
  }

  function formatarTooltip(valor) {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  if (carregando) {
    return <div className="loading">Carregando gráficos...</div>;
  }

  if (dadosUf.length === 0) {
    return (
      <div className="vazio">
        <p>Sem dados para exibir nos gráficos.</p>
        <p>Carregue dados na aba <strong>Consultar Dados</strong> primeiro.</p>
      </div>
    );
  }

  return (
    <div className="graficos">

      {/* GRÁFICO 1: Barras por Estado */}
      <div className="grafico-card">
        <h3>Top Estados por Volume de Desembolsos</h3>
        <p className="grafico-descricao">Soma total de desembolsos por Unidade Federativa</p>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={dadosUf} margin={{ top: 10, right: 20, left: 20, bottom: 5 }}>
            <XAxis dataKey="nome" />
            <YAxis tickFormatter={formatarEixo} width={80} />
            <Tooltip formatter={(v) => formatarTooltip(v)} labelFormatter={(l) => `Estado: ${l}`} />
            <Bar dataKey="totalDesembolsos" fill="#2563eb" radius={[4, 4, 0, 0]} name="Total" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* GRÁFICO 2: Pizza por Setor */}
      <div className="grafico-card">
        <h3>Distribuição por Setor BNDES</h3>
        <p className="grafico-descricao">Proporção do volume financiado por setor econômico</p>
        <ResponsiveContainer width="100%" height={340}>
          <PieChart>
            <Pie
              data={dadosSetor}
              dataKey="totalDesembolsos"
              nameKey="nome"
              cx="50%"
              cy="45%"
              outerRadius={120}
              label={({ nome, percent }) =>
                `${nome}: ${(percent * 100).toFixed(1)}%`
              }
              labelLine={false}
            >
              {dadosSetor.map((_, index) => (
                <Cell key={index} fill={CORES[index % CORES.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(v) => formatarTooltip(v)} />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>

    </div>
  );
}

export default Graficos;
