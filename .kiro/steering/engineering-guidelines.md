---
inclusion: auto
---

# OISM Capital Tech — Diretrizes de Engenharia (Backend)

## Segurança Financeira
- Use **`BigDecimal`** para todos os cálculos monetários. Nunca use `double` ou `float` para valores financeiros.
- Arredondamentos devem usar `RoundingMode.HALF_UP`.

## Resiliência — Processamento de Juros
- O cálculo diário de juros deve ser executado via **Scheduled Task (robô/cron)**.
- Cada execução deve gerar **logs de auditoria** registrando: data de processamento, plano processado, valor calculado e resultado.

## Concorrência
- Aplique **locks otimistas ou pessimistas** no banco de dados para operações de saque e resgate.
- Evitar que múltiplas requisições simultâneas consumam o mesmo saldo.

## Arquitetura — Service Pattern
- Isole a **lógica de cálculo de juros** da lógica de persistência.
- Estrutura esperada:
  - `Controller` → recebe requisição
  - `Service` → contém regras de negócio e cálculos
  - `Repository` → persistência de dados
- A lógica de accrual (cálculo de juros diários) deve residir em um serviço dedicado (ex: `InterestAccrualService`).

## Operações Atômicas
- A contratação de um plano (débito na Wallet + criação do Investment) deve ser executada dentro de uma **transação única** (`@Transactional`).
- O encerramento de um plano (crédito na Wallet + atualização do status do Investment) também deve ser transacional.

## Tipos e Validações
- Datas: use `LocalDate` / `LocalDateTime` com timezone definido.
- Status do plano: use `enum` (ex: `ACTIVE`, `MATURED`, `WITHDRAWN`).
- Validar saldo disponível antes de qualquer operação de débito na Wallet.
