import React from 'react';

function DetalhesModal({ desembolso, onFechar }) {
  if (!desembolso) return null;

  const formatarValor = (valor) => {
    if (!valor && valor !== 0) return '-';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  };

  const campos = [
    { label: 'ID', valor: desembolso.id },
    { label: 'Ano', valor: desembolso.ano },
    { label: 'Mês', valor: desembolso.mes },
    { label: 'Forma de Apoio', valor: desembolso.formaDeApoio },
    { label: 'Produto', valor: desembolso.produto },
    { label: 'Inovação', valor: desembolso.inovacao },
    { label: 'Porte da Empresa', valor: desembolso.porteDaEmpresa },
    { label: 'Região', valor: desembolso.regiao },
    { label: 'UF', valor: desembolso.uf },
    { label: 'Município', valor: desembolso.municipio },
    { label: 'Setor BNDES', valor: desembolso.setorBndes },
    { label: 'Subsetor BNDES', valor: desembolso.subsetorBndes },
    { label: 'Valor do Desembolso', valor: formatarValor(desembolso.desembolsos) },
  ];

  return (
    <div className="modal-overlay" onClick={onFechar}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Detalhes da Operação #{desembolso.id}</h2>
          <button className="modal-close" onClick={onFechar}>&times;</button>
        </div>
        <div className="modal-body">
          <table className="detalhes-tabela">
            <tbody>
              {campos.map(c => (
                <tr key={c.label}>
                  <td className="detalhes-label">{c.label}</td>
                  <td className="detalhes-valor">{c.valor ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="modal-footer">
          <button className="btn btn-primary" onClick={onFechar}>Fechar</button>
        </div>
      </div>
    </div>
  );
}

export default DetalhesModal;
