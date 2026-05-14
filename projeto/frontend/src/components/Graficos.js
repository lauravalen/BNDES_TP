import React, { useState, useEffect } from 'react';
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

const CORES = ['#2563eb', '#16a34a', '#dc2626', '#d97706', '#7c3aed'];

function Graficos({ apiUrl }) {
  const [dados, setDados] = useState([]);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    async function buscar() {
      try {
        const resposta = await fetch(`${apiUrl}/resumo/por-setor`);
        const json = await resposta.json();
        setDados(json.slice(0, 5));
      } catch (e) {
        console.error(e);
      }
      setCarregando(false);
    }
    buscar();
  }, [apiUrl]);

  function formatarTooltip(valor) {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  if (carregando) {
    return <div className="loading">Carregando gráficos...</div>;
  }

  if (dados.length === 0) {
    return (
      <div className="vazio">
        <p>Sem dados para exibir nos gráficos.</p>
        <p>Carregue dados na aba <strong>Consultar Dados</strong> primeiro.</p>
      </div>
    );
  }

  return (
    <div className="graficos">
      <div className="grafico-card">
        <h3>Top 5 Setores mais Financiados</h3>
        <p className="grafico-descricao">Proporção do volume financiado por setor econômico</p>
        <ResponsiveContainer width="100%" height={340}>
          <PieChart>
            <Pie
              data={dados}
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
              {dados.map((_, index) => (
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
