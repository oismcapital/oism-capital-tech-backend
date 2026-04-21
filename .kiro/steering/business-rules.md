---
inclusion: auto
---

# OISM Capital Tech — Regras de Negócio

## 1. Gestão de Saldos e Contas

- **Wallet (Saldo em Conta):** Cada usuário possui um saldo base (`balance`). Esse valor é estático e não sofre reajustes automáticos.
- **Saldo Alocado (Invested):** Ao contratar um plano, o valor é transferido da Wallet para uma entidade `Investment`. Essa operação deve ser **atômica (transacional)**.
- **Saque da Wallet:** O saldo na Wallet pode ser sacado via PIX a qualquer momento, respeitando validação de saldo disponível.

## 2. Planos de Investimento

### Grade de Planos (valores fixos nominais)
- R$ 25,00
- R$ 50,00
- R$ 100,00
- R$ 500,00
- R$ 1.000,00

### Multiplicidade
- O usuário pode manter múltiplos planos ativos simultaneamente.
- Cada instância de plano é um objeto único com seu próprio ciclo de vida.

### Cálculo de Rendimento (Accrual)
- Taxa: até **10% ao mês** sobre o valor nominal do plano.
- Frequência: **diária** → `Taxa Mensal / 30`
- Período de rentabilidade: juros calculados estritamente nos **primeiros 30 dias**.

### Ciclo de Vida do Plano
- Duração total: **35 dias corridos**.
- No **35º dia**: contrato encerrado automaticamente → Principal + Juros Acumulados são liquidados e estornados para a Wallet do usuário.

## 3. Regras de Liquidez e Resgate

- **Carência de Lucros:** Juros acumulados tornam-se elegíveis para resgate somente após o **15º dia** da contratação.
- **Bloqueio do Principal:** O valor nominal investido permanece bloqueado até o encerramento dos 35 dias.
- **Operação de Resgate de Lucro:** Transfere os juros já calculados (que cumpriram a carência de 15 dias) para a Wallet.

## 4. Dados Obrigatórios por Contrato (UI/UX)

Cada contrato ativo ou finalizado deve expor:

| Campo | Descrição |
|---|---|
| Timestamp de Contratação | Data e hora do início |
| Janela de Resgate | D+15 — data a partir da qual o lucro pode ser movido para a conta |
| Data de Maturação | D+35 — data de encerramento do plano |
| Valor Nominal | Montante alocado inicialmente |
| Projeção de Juros | Juros acumulados até o momento vs. total previsto ao fim do ciclo |
