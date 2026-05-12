import React from 'react';

// =============================================
// COMPONENTE: Tabela
// =============================================
// Recebe os dados via "props" (propriedades) e
// renderiza uma tabela HTML com os desembolsos.
//
// Props recebidas:
//   - dados: array de objetos desembolso
//   - onDeletar: função chamada ao clicar em Excluir
//   - carregando: boolean para mostrar loading
// =============================================

function Tabela({ dados, onDeletar, carregando }) {

  // Formata valor em Reais brasileiros
  // Ex: 1500000 → R$ 1.500.000,00
  function formatarValor(valor) {
    if (!valor && valor !== 0) return '-';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  }

  // Traduz número do mês para nome
  function nomeMes(numero) {
    const meses = ['', 'Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun',
                   'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
    return meses[numero] || numero;
  }

  if (carregando) {
    return <div className="loading">Carregando dados...</div>;
  }

  if (dados.length === 0) {
    return (
      <div className="vazio">
        <p>Nenhum dado encontrado.</p>
        <p>Clique em <strong>"Importar CSV"</strong> para carregar dados reais do BNDES.</p>
      </div>
    );
  }

  return (
    <div className="tabela-container">
      <table className="tabela">
        <thead>
          <tr>
            <th>ID</th>
            <th>Período</th>
            <th>UF</th>
            <th>Município</th>
            <th>Setor</th>
            <th>Porte</th>
            <th>Inovação</th>
            <th>Valor (R$)</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {/* map() percorre o array e cria uma <tr> para cada item */}
          {dados.map(item => (
            <tr key={item.id}>
              <td className="td-id">{item.id}</td>
              <td>{nomeMes(item.mes)}/{item.ano}</td>
              <td><span className="badge badge-uf">{item.uf || '-'}</span></td>
              <td>{item.municipio || '-'}</td>
              <td>{item.setorBndes || '-'}</td>
              <td>
                <span className={`badge badge-porte badge-${(item.porteDaEmpresa || '').toLowerCase()}`}>
                  {item.porteDaEmpresa || '-'}
                </span>
              </td>
              <td>
                <span className={`badge ${item.inovacao === 'Sim' ? 'badge-sim' : 'badge-nao'}`}>
                  {item.inovacao === 'Sim' ? 'Sim' : 'Não'}
                </span>
              </td>
              <td className="td-valor">{formatarValor(item.desembolsos)}</td>
              <td>
                <button
                  className="btn-deletar"
                  onClick={() => onDeletar(item.id)}
                  title="Excluir registro"
                >
                  Excluir
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Tabela;
