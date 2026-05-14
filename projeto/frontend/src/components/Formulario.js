import React, { useState } from 'react';

// =============================================
// COMPONENTE: Formulario
// =============================================
// Permite cadastrar um novo desembolso.
// Quando o usuário clica em "Salvar", chama
// a função onSalvar passada pelo App.js
// =============================================

const UF_PARA_REGIAO = {
  AC: 'Norte', AL: 'Nordeste', AM: 'Norte', AP: 'Norte',
  BA: 'Nordeste', CE: 'Nordeste', DF: 'Centro-Oeste', ES: 'Sudeste',
  GO: 'Centro-Oeste', MA: 'Nordeste', MG: 'Sudeste', MS: 'Centro-Oeste',
  MT: 'Centro-Oeste', PA: 'Norte', PB: 'Nordeste', PE: 'Nordeste',
  PI: 'Nordeste', PR: 'Sul', RJ: 'Sudeste', RN: 'Nordeste',
  RO: 'Norte', RR: 'Norte', RS: 'Sul', SC: 'Sul',
  SE: 'Nordeste', SP: 'Sudeste', TO: 'Norte'
};

function Formulario({ onSalvar, onCancelar }) {

  const [form, setForm] = useState({
    ano: new Date().getFullYear(),
    mes: new Date().getMonth() + 1,
    formaDeApoio: 'Indireto',
    produto: 'BNDES Automático',
    inovacao: 'Não',
    porteDaEmpresa: 'Micro',
    regiao: '',
    uf: '',
    municipio: '',
    setorBndes: '',
    subsetorBndes: '',
    desembolsos: ''
  });

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(anterior => ({
      ...anterior,
      [name]: value,
      ...(name === 'uf' ? { regiao: UF_PARA_REGIAO[value] || '' } : {})
    }));
  }

  // Valida e envia o formulário
  function handleSubmit(e) {
    e.preventDefault(); // impede o reload da página

    // Validação básica
    if (!form.uf || !form.setorBndes || !form.desembolsos) {
      alert('Preencha pelo menos: Estado, Setor e Valor.');
      return;
    }

    // Converte o valor para número antes de enviar
    const dados = {
      ...form,
      desembolsos: parseFloat(form.desembolsos)
    };

    onSalvar(dados);
  }

  return (
    <div className="formulario-container">
      <h2>Novo Registro de Desembolso</h2>
      <p className="form-descricao">
        Simule o cadastro de uma nova solicitação de financiamento.
      </p>

      <form onSubmit={handleSubmit} className="formulario">

        {/* LINHA 1: Ano e Mês */}
        <div className="form-linha">
          <div className="form-grupo">
            <label>Ano *</label>
            <input
              type="number"
              name="ano"
              value={form.ano}
              onChange={handleChange}
              min="2000"
              max="2030"
              required
            />
          </div>
          <div className="form-grupo">
            <label>Mês *</label>
            <select name="mes" value={form.mes} onChange={handleChange}>
              {['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
                'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro']
                .map((m, i) => <option key={i+1} value={i+1}>{m}</option>)}
            </select>
          </div>
        </div>

        {/* LINHA 2: Estado, Região e Município */}
        <div className="form-linha">
          <div className="form-grupo">
            <label>Estado (UF) *</label>
            <select name="uf" value={form.uf} onChange={handleChange} required>
              <option value="">Selecione...</option>
              {['AC','AL','AM','AP','BA','CE','DF','ES','GO','MA','MG','MS',
                'MT','PA','PB','PE','PI','PR','RJ','RN','RO','RR','RS',
                'SC','SE','SP','TO'].map(uf => (
                <option key={uf} value={uf}>{uf}</option>
              ))}
            </select>
          </div>
          <div className="form-grupo">
            <label>Região</label>
            <input
              type="text"
              name="regiao"
              value={form.regiao}
              readOnly
              placeholder="Preenchido automaticamente"
              style={{ background: '#f0f4f8', cursor: 'default' }}
            />
          </div>
          <div className="form-grupo">
            <label>Município</label>
            <input
              type="text"
              name="municipio"
              value={form.municipio}
              onChange={handleChange}
              placeholder="Ex: São Paulo"
            />
          </div>
        </div>

        {/* LINHA 3: Setor e Subsetor */}
        <div className="form-linha">
          <div className="form-grupo">
            <label>Setor BNDES *</label>
            <select name="setorBndes" value={form.setorBndes} onChange={handleChange} required>
              <option value="">Selecione...</option>
              <option>Indústria</option>
              <option>Comércio e Serviços</option>
              <option>Agropecuária</option>
              <option>Infraestrutura</option>
            </select>
          </div>
          <div className="form-grupo">
            <label>Subsetor</label>
            <input
              type="text"
              name="subsetorBndes"
              value={form.subsetorBndes}
              onChange={handleChange}
              placeholder="Ex: Tecnologia da Informação"
            />
          </div>
        </div>

        {/* LINHA 4: Porte e Inovação */}
        <div className="form-linha">
          <div className="form-grupo">
            <label>Porte da Empresa *</label>
            <select name="porteDaEmpresa" value={form.porteDaEmpresa} onChange={handleChange}>
              <option>Micro</option>
              <option>Pequeno</option>
              <option>Médio</option>
              <option>Grande</option>
            </select>
          </div>
          <div className="form-grupo">
            <label>Inovação?</label>
            <select name="inovacao" value={form.inovacao} onChange={handleChange}>
              <option>Não</option>
              <option>Sim</option>
            </select>
          </div>
        </div>

        {/* LINHA 5: Forma de Apoio e Produto */}
        <div className="form-linha">
          <div className="form-grupo">
            <label>Forma de Apoio</label>
            <select name="formaDeApoio" value={form.formaDeApoio} onChange={handleChange}>
              <option>Indireto</option>
              <option>Direto</option>
            </select>
          </div>
          <div className="form-grupo">
            <label>Produto</label>
            <select name="produto" value={form.produto} onChange={handleChange}>
              <option>BNDES Automático</option>
              <option>BNDES Finem</option>
              <option>BNDES Finame</option>
              <option>BNDES Crédito</option>
            </select>
          </div>
        </div>

        {/* LINHA 6: Valor */}
        <div className="form-linha">
          <div className="form-grupo form-grupo-full">
            <label>Valor do Desembolso (R$) *</label>
            <input
              type="number"
              name="desembolsos"
              value={form.desembolsos}
              onChange={handleChange}
              min="0"
              step="0.01"
              placeholder="Ex: 150000.00"
              required
            />
          </div>
        </div>

        {/* BOTÕES */}
        <div className="form-botoes">
          <button type="button" className="btn btn-outline" onClick={onCancelar}>
            Cancelar
          </button>
          <button type="submit" className="btn btn-primary">
            Salvar registro
          </button>
        </div>

      </form>
    </div>
  );
}

export default Formulario;
