# OISM Capital Tech Backend

Backend Spring Boot 3 com Java 17 para autenticação JWT, gestão de usuários, rendimento diário automatizado e integração Pix.

## Requisitos

- Java 17
- Maven 3.8+
- Docker Desktop opcional para subir o PostgreSQL

## Estrutura

- `controller`: endpoints REST
- `service`: regras de negócio
- `repository`: acesso ao PostgreSQL
- `security`: autenticação JWT
- `scheduler`: robô diário de rendimento

## Subindo o banco

```bash
docker compose -f compose.yml up -d
```

## Variáveis de ambiente

```bash
DB_URL=jdbc:postgresql://localhost:5432/oism_capital
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-this-secret-change-this-secret-123456
JWT_EXPIRATION_MS=86400000
PIX_BANK_API_URL=https://api.seu-banco.com/pix
PIX_WEBHOOK_SECRET=webhook-secret
```

## Rodando a aplicação

```bash
mvn spring-boot:run
```

## Endpoints

### Registro

`POST /api/auth/register`

```json
{
  "nome": "Joao",
  "email": "joao@oism.com",
  "senha": "123456",
  "saldo": 1000
}
```

### Login

`POST /api/auth/login`

```json
{
  "email": "joao@oism.com",
  "senha": "123456"
}
```

### Listagem de usuários

`GET /api/users`

Header:

```text
Authorization: Bearer <jwt>
```

### Gerar QR Code Pix

`POST /api/pix/qrcode`

```json
{
  "userId": 1,
  "valor": 250.00,
  "descricao": "Aporte OISM"
}
```

### Webhook de confirmação Pix

`POST /api/pix/webhook`

Header:

```text
X-Webhook-Secret: webhook-secret
```

Body:

```json
{
  "transactionId": "tx-123",
  "userId": 1,
  "valor": 250.00,
  "status": "CONFIRMED"
}
```

## Regras implementadas

- Senha criptografada com BCrypt
- Login com JWT stateless
- Scheduler diário `0 0 0 * * *`
- Multiplicador diário `1.001615`
- Crédito automático no saldo do usuário após webhook Pix confirmado

## Observação

O serviço Pix foi preparado com chamada HTTP para a API do banco, mas hoje retorna um payload simulado enquanto as credenciais reais e o contrato do banco não forem configurados. Se você quiser Firestore além do PostgreSQL, basta adicionar um adaptador na etapa de confirmação do webhook.
