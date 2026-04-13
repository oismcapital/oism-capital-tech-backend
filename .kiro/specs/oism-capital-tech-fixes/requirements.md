# Requirements Document

## Introduction

Este documento descreve os requisitos para corrigir e completar a plataforma **Oism Capital Tech** — um sistema de investimentos automatizado composto por um backend Spring Boot 3.3.4 (Java 17, PostgreSQL, JWT) e um frontend Flutter (Provider, Dio, fl_chart). Os problemas identificados abrangem endpoints ausentes, falhas de autenticação, persistência de token, formato de dados incorreto, configuração de CORS, typos em entidades e funcionalidades de UI incompletas.

---

## Glossary

- **Backend**: Aplicação Spring Boot rodando na porta 8080.
- **Frontend**: Aplicação Flutter que consome a API do Backend.
- **FinanceController**: Controller Spring Boot responsável pelos endpoints `/api/finance/*`.
- **FinanceSummary**: Objeto de resposta contendo `investedBalance`, `dailyProfit` e `performancePoints`.
- **AuthScreen**: Tela Flutter de login e registro de usuários.
- **MainShell**: Widget Flutter que gerencia a navegação por abas da aplicação.
- **TokenHolder**: Classe Flutter responsável por armazenar e recuperar o token JWT.
- **SecureStorage**: Mecanismo de armazenamento seguro e persistente no dispositivo (flutter_secure_storage).
- **RobotScheduler**: Componente Spring Boot que aplica o rendimento diário automaticamente via cron.
- **YieldEntry**: Entrada do histórico de rendimento com campos `data` (ISO-8601) e `valor` (novo saldo).
- **User**: Entidade JPA que representa um usuário no banco de dados PostgreSQL.
- **WalletService**: Serviço Spring Boot que gerencia operações de carteira do usuário autenticado.
- **DepositFlow**: Fluxo de navegação iniciado pelo botão "Depositar" na HomePage.
- **IndicarPage**: Tela Flutter da aba "Indicar" do menu inferior.

---

## Requirements

### Requirement 1: Endpoint `/api/finance/summary` no Backend

**User Story:** Como usuário do aplicativo, quero que a HomePage carregue meu saldo investido, lucro do dia e histórico de performance, para que eu possa acompanhar meus investimentos em tempo real.

#### Acceptance Criteria

1. WHEN uma requisição `GET /api/finance/summary` autenticada é recebida, THE FinanceController SHALL retornar um objeto JSON contendo os campos `investedBalance` (BigDecimal), `dailyProfit` (BigDecimal) e `performancePoints` (lista de doubles).
2. WHEN o usuário autenticado não possui histórico de rendimento, THE FinanceController SHALL retornar `performancePoints` como lista vazia (`[]`).
3. WHEN o usuário autenticado possui histórico de rendimento, THE FinanceController SHALL popular `performancePoints` com os valores de saldo de cada entrada do histórico em ordem cronológica.
4. IF o token JWT estiver ausente ou inválido na requisição, THEN THE FinanceController SHALL retornar HTTP 401.
5. THE FinanceController SHALL derivar `investedBalance` do saldo atual do usuário autenticado e `dailyProfit` do campo `lucroHoje` da entidade User.

---

### Requirement 2: Tela de Login e Registro no Flutter

**User Story:** Como usuário novo ou retornante, quero ver uma tela de autenticação ao abrir o aplicativo, para que eu possa fazer login ou criar uma conta antes de acessar o conteúdo protegido.

#### Acceptance Criteria

1. WHEN o aplicativo é iniciado sem um token JWT válido armazenado, THE AuthScreen SHALL ser exibida como tela inicial em vez do MainShell.
2. WHEN o aplicativo é iniciado com um token JWT válido armazenado, THE Frontend SHALL navegar diretamente para o MainShell sem exibir a AuthScreen.
3. WHEN o usuário preenche email e senha válidos e confirma o login, THE AuthScreen SHALL enviar as credenciais para `POST /api/auth/login` e armazenar o token retornado via TokenHolder.
4. WHEN o login é bem-sucedido, THE AuthScreen SHALL navegar para o MainShell substituindo a rota atual.
5. IF a requisição de login retornar erro HTTP 401, THEN THE AuthScreen SHALL exibir a mensagem "Email ou senha inválidos".
6. IF a requisição de login retornar erro de rede, THEN THE AuthScreen SHALL exibir a mensagem "Sem conexão com o servidor".
7. WHEN o usuário preenche nome, email e senha válidos e confirma o registro, THE AuthScreen SHALL enviar os dados para `POST /api/auth/register` e, em caso de sucesso, realizar login automático.
8. IF o registro retornar erro indicando email já cadastrado, THEN THE AuthScreen SHALL exibir a mensagem "Este email já está em uso".
9. THE AuthScreen SHALL validar que o campo de email contém formato válido antes de submeter o formulário.
10. THE AuthScreen SHALL validar que o campo de senha contém no mínimo 6 caracteres antes de submeter o formulário.

---

### Requirement 3: Persistência do Token JWT com SecureStorage

**User Story:** Como usuário, quero que minha sessão seja mantida após fechar e reabrir o aplicativo, para que eu não precise fazer login toda vez.

#### Acceptance Criteria

1. WHEN um token JWT é recebido após login bem-sucedido, THE TokenHolder SHALL persistir o token no SecureStorage do dispositivo.
2. WHEN o aplicativo é iniciado, THE TokenHolder SHALL tentar recuperar o token do SecureStorage antes de exibir qualquer tela.
3. WHEN o usuário realiza logout, THE TokenHolder SHALL remover o token do SecureStorage e limpar o valor em memória.
4. THE TokenHolder SHALL manter o token em memória após leitura do SecureStorage para evitar leituras repetidas durante a sessão.
5. IF o SecureStorage retornar erro ao ler ou gravar o token, THEN THE TokenHolder SHALL tratar a exceção e retornar `null` sem lançar erro para o chamador.
6. THE Frontend SHALL adicionar `flutter_secure_storage` como dependência no `pubspec.yaml`.

---

### Requirement 4: Formato Correto do Histórico de Rendimento

**User Story:** Como desenvolvedor e usuário, quero que cada entrada do histórico de rendimento contenha a data e o novo saldo, para que o gráfico de performance exiba dados temporais precisos.

#### Acceptance Criteria

1. WHEN o RobotScheduler aplica o rendimento diário, THE Backend SHALL gravar cada entrada do histórico no formato `{"data": "<ISO_DATE>", "valor": <NOVO_SALDO>}` onde `<ISO_DATE>` é a data atual no formato `yyyy-MM-dd` e `<NOVO_SALDO>` é o saldo após aplicação do rendimento.
2. THE Backend SHALL substituir o formato legado `{"lucro": "<valor>"}` pelo novo formato em todas as novas gravações.
3. WHEN o FinanceController monta `performancePoints`, THE FinanceController SHALL extrair o campo `valor` de cada entrada do histórico para popular a lista.
4. FOR ALL entradas do histórico gravadas pelo RobotScheduler, o campo `data` SHALL ser uma string no formato ISO-8601 (`yyyy-MM-dd`) representando a data de aplicação do rendimento.

---

### Requirement 5: Precisão do Multiplicador Diário

**User Story:** Como investidor, quero que o rendimento diário seja calculado com o multiplicador correto de 1.0016158, para que os ganhos reflitam a taxa especificada na plataforma.

#### Acceptance Criteria

1. THE RobotScheduler SHALL utilizar o multiplicador diário `1.0016158` (sete casas decimais) para o cálculo do rendimento.
2. WHEN o rendimento diário é aplicado a um saldo, THE Backend SHALL calcular o novo saldo como `saldo * 1.0016158` arredondado para 4 casas decimais usando `HALF_UP`.
3. THE Backend SHALL substituir o valor `1.001615` atualmente definido em `RobotScheduler.DAILY_MULTIPLIER` pelo valor `1.0016158`.

---

### Requirement 6: Configuração de CORS no Backend

**User Story:** Como desenvolvedor, quero que o backend aceite requisições do aplicativo Flutter (incluindo emuladores e dispositivos físicos), para que o frontend possa se comunicar com a API sem bloqueios de CORS.

#### Acceptance Criteria

1. THE Backend SHALL configurar CORS para permitir requisições de qualquer origem (`*`) nos endpoints `/api/**`.
2. WHEN uma requisição OPTIONS (preflight) é recebida em qualquer endpoint `/api/**`, THE Backend SHALL responder com HTTP 200 e os headers CORS apropriados.
3. THE Backend SHALL permitir os métodos HTTP `GET`, `POST`, `PUT`, `DELETE` e `OPTIONS` nas configurações de CORS.
4. THE Backend SHALL permitir o header `Authorization` nas configurações de CORS para suportar o envio do token JWT.
5. THE SecurityConfig SHALL registrar a configuração de CORS antes do filtro JWT para que requisições preflight não sejam bloqueadas por autenticação.

---

### Requirement 7: Correção do Typo na Entidade User

**User Story:** Como desenvolvedor, quero que o campo do histórico de rendimento tenha o nome correto `historicoRendimentoJSONB`, para que o código seja legível e consistente com a nomenclatura da spec.

#### Acceptance Criteria

1. THE User SHALL renomear o campo `historicoRendimentoJOSNB` para `historicoRendimentoJSONB` na entidade JPA.
2. THE Backend SHALL atualizar todos os getters, setters e referências ao campo renomeado em `UserService`, `UserResponse` e demais classes que o referenciam.
3. WHEN a aplicação iniciar com `ddl-auto: update`, THE Backend SHALL aplicar a renomeação da coluna no banco de dados sem perda de dados existentes.

---

### Requirement 8: Aba "Indicar" no Menu Inferior

**User Story:** Como usuário, quero ver a aba "Indicar" no menu inferior do aplicativo, para que eu possa acessar o programa de indicações conforme especificado.

#### Acceptance Criteria

1. THE MainShell SHALL exibir 5 abas no menu inferior na seguinte ordem: Home, Investir, Ganhos, Indicar, Perfil.
2. WHEN o usuário seleciona a aba "Indicar", THE MainShell SHALL exibir a IndicarPage.
3. THE MainShell SHALL utilizar o ícone `Icons.people_outline` (não selecionado) e `Icons.people` (selecionado) para a aba "Indicar".
4. THE IndicarPage SHALL exibir ao menos um placeholder com o texto "Programa de Indicações" enquanto a funcionalidade completa não está implementada.

---

### Requirement 9: Botão "Depositar" com Ação na HomePage

**User Story:** Como usuário, quero que o botão "Depositar" na HomePage navegue para o fluxo de depósito, para que eu possa adicionar saldo à minha conta.

#### Acceptance Criteria

1. WHEN o usuário pressiona o botão "Depositar" na HomePage, THE Frontend SHALL navegar para a tela de depósito (InvestPage ou tela dedicada de depósito).
2. THE HomePage SHALL substituir o `onPressed: () {}` vazio do botão "Depositar" por uma ação de navegação válida.

---

### Requirement 10: Persistência do Campo `valor_escondido` no Backend

**User Story:** Como usuário, quero que a preferência de ocultar meu saldo seja salva no servidor, para que a configuração seja mantida entre sessões e dispositivos.

#### Acceptance Criteria

1. THE User SHALL incluir um campo booleano `valorEscondido` (coluna `valor_escondido`) com valor padrão `false` na entidade JPA.
2. WHEN o usuário alterna a visibilidade do saldo na HomePage, THE Frontend SHALL enviar uma requisição `PATCH /api/wallet/preferences` com o novo valor de `valorEscondido` para o Backend.
3. WHEN o Backend recebe a requisição `PATCH /api/wallet/preferences`, THE WalletController SHALL persistir o valor de `valorEscondido` no banco de dados para o usuário autenticado.
4. WHEN o Frontend carrega o resumo financeiro via `GET /api/finance/summary`, THE FinanceController SHALL incluir o campo `valorEscondido` na resposta para que o estado inicial da UI seja restaurado.
5. THE Frontend SHALL inicializar o estado `_hideBalance` da HomePage com o valor de `valorEscondido` retornado pelo endpoint `/api/finance/summary`.
