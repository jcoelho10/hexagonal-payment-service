# 🧪 Guia Prático de Testes Locais — Payment Service

Este guia apresenta os procedimentos para **executar, testar e validar localmente o Payment Service**, cobrindo o fluxo completo de criação de pagamentos, validação de entrada, resiliência, segurança, rastreabilidade e observabilidade.

---

## 📋 Índice

* [🛠️ Pré-requisitos](#️-pré-requisitos)
* [🚀 1. Inicializando a aplicação](#-1-inicializando-a-aplicação)
* [🔐 2. Autenticação OAuth2 em ambiente local](#-2-autenticação-oauth2-em-ambiente-local)
* [🧪 3. Testes da API](#-3-testes-da-api)

    * [Teste A — Criação de pagamento](#teste-a--criação-de-pagamento)
    * [Teste B — Resiliência e fallback](#teste-b--resiliência-e-fallback)
    * [Teste C — Validação de entrada](#teste-c--validação-de-entrada)
    * [Teste D — Health Check](#teste-d--health-check)
* [📊 4. Observabilidade com Docker Compose](#-4-observabilidade-com-docker-compose)
* [🧪 5. Testes automatizados](#-5-testes-automatizados)
* [✅ Checklist de validação](#-checklist-de-validação)

---

## 🛠️ Pré-requisitos

Antes de iniciar, certifique-se de possuir os seguintes componentes instalados:

| Componente        | Versão mínima | Verificação              |
| ----------------- | ------------: | ------------------------ |
| ☕ Java            |            21 | `java -version`          |
| 📦 Apache Maven   |          3.9+ | `mvn -version`           |
| 🐳 Docker         |         Atual | `docker --version`       |
| 🐳 Docker Compose |         Atual | `docker compose version` |

> **Nota:** Docker é opcional para executar a aplicação e os testes básicos. Ele é necessário apenas para executar a stack de observabilidade.

### Verificando o ambiente

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

# 🚀 1. Inicializando a aplicação

O Payment Service está configurado para execução local utilizando:

* **Java 21**
* **Spring Boot**
* **H2 Database em memória**
* **Porta HTTP `8081`**
* Configuração específica para desenvolvimento local

### ▶️ Iniciando com Maven

Na raiz do projeto, execute:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em:

```text
http://localhost:8081
```

### ✅ Aplicação inicializada

Aguarde no console uma mensagem semelhante a:

```text
Started PaymentServiceApplication in X.XXX seconds
(process running for X.XXX)
```

A partir desse momento, a API estará disponível para os testes.

---

# 🔐 2. Autenticação OAuth2 em ambiente local

O projeto possui integração com:

* Spring Security
* OAuth2
* JWT
* Resource Server

Para facilitar os testes locais, a configuração de segurança disponibiliza os endpoints de desenvolvimento conforme definido em `SecurityConfig.java`.

### 🔓 Execução sem servidor OAuth2 externo

Quando a configuração de desenvolvimento estiver habilitada, os testes podem ser executados sem depender de um servidor externo como:

* Keycloak
* Auth0
* Azure AD
* Outro Identity Provider

### 🔑 Execução utilizando JWT

Caso o Resource Server esteja configurado para exigir autenticação, adicione um token JWT às requisições:

```bash
-H "Authorization: Bearer <SEU_TOKEN>"
```

Exemplo:

```bash
curl -i -X POST http://localhost:8081/api/v1/payments \
-H "Content-Type: application/json" \
-H "Authorization: Bearer <SEU_TOKEN>" \
-H "X-Correlation-ID: transacao-dev-123" \
-d '{
  "customerId": "cli-8899",
  "amount": 250.00,
  "currency": "BRL"
}'
```

---

# 🧪 3. Testes da API

A API está disponível localmente através da porta:

```text
8081
```

Base URL:

```text
http://localhost:8081
```

---

## Teste A — Criação de pagamento

### 🎯 Objetivo

Validar:

* Criação de um pagamento;
* Execução do caso de uso;
* Comunicação com o gateway de pagamento;
* Propagação do `X-Correlation-ID`;
* Rastreabilidade da requisição;
* Retorno HTTP esperado.

### 🐧 Linux / macOS / Git Bash

```bash
curl -i -X POST http://localhost:8081/api/v1/payments \
-H "Content-Type: application/json" \
-H "X-Correlation-ID: transacao-dev-123" \
-d '{
  "customerId": "cli-8899",
  "amount": 250.00,
  "currency": "BRL"
}'
```

### 🪟 Windows PowerShell

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8081/api/v1/payments" `
  -Method Post `
  -Headers @{
    "Content-Type" = "application/json"
    "X-Correlation-ID" = "transacao-dev-123"
  } `
  -Body '{
    "customerId": "cli-8899",
    "amount": 250.00,
    "currency": "BRL"
  }'
```

### ✅ Validar no retorno

**HTTP Status:**

```text
200 OK
```

**Response Header:**

```text
X-Correlation-ID: transacao-dev-123
```

**Logs da aplicação:**

Verifique no console do Spring Boot se os logs relacionados à requisição apresentam o identificador:

```text
[transacao-dev-123]
```

Isso permite validar a rastreabilidade da transação através das diferentes camadas da aplicação.

---

# 🛡️ Teste B — Resiliência e Fallback

### 🎯 Objetivo

Validar o comportamento da aplicação diante de falhas ou instabilidade do gateway externo.

O adaptador de gateway possui uma simulação de instabilidade para permitir a validação dos mecanismos de resiliência.

### 🔄 Como executar

Execute o Teste A várias vezes consecutivamente:

```bash
curl -i -X POST http://localhost:8081/api/v1/payments \
-H "Content-Type: application/json" \
-H "X-Correlation-ID: transacao-dev-123" \
-d '{
  "customerId": "cli-8899",
  "amount": 250.00,
  "currency": "BRL"
}'
```

Execute aproximadamente **5 a 10 requisições**.

### 🔎 Comportamento esperado

Em condições normais:

```json
{
  "status": "APPROVED"
}
```

Quando ocorrer uma falha simulada no gateway:

```json
{
  "status": "FAILED"
}
```

O objetivo é verificar que a falha seja tratada pelo mecanismo de resiliência configurado, como:

* Retry;
* Circuit Breaker;
* Fallback.

A aplicação deve tratar a indisponibilidade do serviço externo sem propagar uma exceção não tratada ao cliente.

> **Observação:** o comportamento exato depende das políticas configuradas no `Resilience4j`, incluindo número de tentativas, condições de abertura do Circuit Breaker e estratégia de fallback.

---

# 🛡️ Teste C — Validação de entrada

### 🎯 Objetivo

Validar o tratamento de dados inválidos através de:

* Bean Validation;
* Validação dos DTOs;
* Validação de regras de negócio;
* RFC 7807 / `ProblemDetail`;
* Retorno HTTP `400 Bad Request`.

### ❌ Payload inválido

Neste cenário serão enviados:

* `customerId` vazio;
* `amount` negativo;
* moeda fora do padrão ISO-4217.

```bash
curl -i -X POST http://localhost:8081/api/v1/payments \
-H "Content-Type: application/json" \
-d '{
  "customerId": "",
  "amount": -50.00,
  "currency": "REAL"
}'
```

### ✅ Retorno esperado

```text
HTTP/1.1 400 Bad Request
```

Exemplo de resposta:

```json
{
  "type": "https://api.payment-service.com/errors/invalid-input",
  "title": "Erro de Validação de Entrada",
  "status": 400,
  "detail": "A requisição possui atributos inválidos ou malformados",
  "invalidFields": {
    "customerId": "O ID do cliente não pode estar em branco",
    "amount": "O valor do pagamento deve ser estritamente maior que zero",
    "currency": "A moeda deve seguir o padrão ISO-4217 de 3 letras maiúsculas (ex: BRL, USD)"
  }
}
```

### 🔎 O que validar

| Campo        | Valor enviado | Resultado esperado          |
| ------------ | ------------- | --------------------------- |
| `customerId` | `""`          | ❌ Inválido                  |
| `amount`     | `-50.00`      | ❌ Inválido                  |
| `currency`   | `REAL`        | ❌ Inválido                  |
| HTTP Status  | —             | `400 Bad Request`           |
| Content-Type | —             | `application/problem+json`* |

> * Caso esteja configurado dessa forma no `ProblemDetail`/Spring Boot.

---

# ❤️ Teste D — Health Check

O Spring Boot Actuator disponibiliza informações sobre a saúde da aplicação e de seus componentes monitorados.

Execute:

```bash
curl -i http://localhost:8081/actuator/health
```

### ✅ Exemplo de resposta

```json
{
  "status": "UP"
}
```

Quando houver componentes monitorados:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

### 🎯 O que validar

* Aplicação disponível;
* Conectividade com o banco;
* Status dos componentes monitorados;
* Endpoint Actuator acessível.

---

# 📊 4. Observabilidade com Docker Compose

O projeto possui uma stack de observabilidade para permitir a análise de:

* 📈 Métricas — Prometheus;
* 📊 Dashboards — Grafana;
* 📝 Logs — Loki;
* 🔎 Rastreamento distribuído — Tempo.

## 🚀 Subindo a infraestrutura

Na raiz do projeto:

```bash
docker compose up -d
```

Verifique os containers:

```bash
docker compose ps
```

Para acompanhar os logs:

```bash
docker compose logs -f
```

---

## 📊 Grafana

Acesse:

```text
http://localhost:3000
```

Credenciais padrão, caso não tenham sido alteradas:

```text
Usuário: admin
Senha: admin
```

> **Segurança:** em ambientes compartilhados ou de produção, altere as credenciais padrão.

---

## 📈 Prometheus

Acesse:

```text
http://localhost:9090
```

O Prometheus pode ser utilizado para consultar as métricas expostas pela aplicação e pelos demais componentes da infraestrutura.

---

## 📝 Loki — Consulta de logs

No Grafana:

```text
Explore
   ↓
Loki
```

Utilize o `X-Correlation-ID` para localizar os logs relacionados à transação:

```logql
{app="payment-service"} |= "transacao-dev-123"
```

Isso permite acompanhar os eventos associados a uma requisição específica.

---

## 🔎 Tempo — Distributed Tracing

Caso o tracing esteja habilitado, utilize o Grafana para consultar os traces relacionados à requisição.

O objetivo é conseguir correlacionar:

```text
HTTP Request
     │
     ├── Payment Controller
     │
     ├── Payment Use Case
     │
     ├── Payment Gateway
     │
     └── Database
```

com os respectivos logs e métricas.

---

# 🧪 5. Testes automatizados

Para executar a suíte completa de testes:

```bash
mvn clean test
```

O comando executará os testes configurados no projeto, incluindo, conforme a implementação:

* Testes unitários;
* Testes de domínio;
* Testes de aplicação;
* Testes de integração;
* Testes de controllers;
* Testes de infraestrutura.

### 🔎 Resultado esperado

Ao final da execução:

```text
BUILD SUCCESS
```

Caso algum teste falhe:

```text
BUILD FAILURE
```

Consulte os relatórios gerados em:

```text
target/surefire-reports/
```

e, quando aplicável:

```text
target/failsafe-reports/
```

---

# ✅ Checklist de Validação

Use este checklist para confirmar que o ambiente está funcionando corretamente.

### Aplicação

* [ ] Java 21 configurado
* [ ] Maven 3.9+ configurado
* [ ] Aplicação iniciada na porta `8081`
* [ ] H2 disponível
* [ ] Spring Boot iniciado sem erros

### API

* [ ] Criação de pagamento funcionando
* [ ] HTTP `200 OK`
* [ ] `X-Correlation-ID` retornado
* [ ] Logs contendo o Correlation ID
* [ ] Validação de DTO funcionando
* [ ] HTTP `400 Bad Request` para payload inválido
* [ ] `ProblemDetail` retornado corretamente

### Resiliência

* [ ] Retry funcionando
* [ ] Circuit Breaker funcionando
* [ ] Fallback funcionando
* [ ] Falhas do gateway não provocam erro HTTP 500 não tratado

### Segurança

* [ ] Configuração OAuth2 validada
* [ ] JWT validado quando Resource Server estiver habilitado
* [ ] Endpoints de desenvolvimento configurados corretamente

### Observabilidade

* [ ] Docker Compose iniciado
* [ ] Grafana acessível
* [ ] Prometheus acessível
* [ ] Loki recebendo logs
* [ ] Logs pesquisáveis pelo `X-Correlation-ID`
* [ ] Tempo recebendo traces, quando habilitado

### Testes automatizados

* [ ] `mvn clean test` executado
* [ ] Todos os testes passando
* [ ] Relatórios de testes gerados

---

# 🏁 Execução Rápida

Para quem já possui o ambiente configurado, o fluxo básico é:

```bash
# 1. Iniciar a aplicação
mvn spring-boot:run

# 2. Em outro terminal, testar a API
curl -i -X POST http://localhost:8081/api/v1/payments \
-H "Content-Type: application/json" \
-H "X-Correlation-ID: transacao-dev-123" \
-d '{
  "customerId": "cli-8899",
  "amount": 250.00,
  "currency": "BRL"
}'

# 3. Verificar health check
curl http://localhost:8081/actuator/health

# 4. Executar testes automatizados
mvn clean test

# 5. Subir observabilidade, se necessário
docker compose up -d
```

---

## 📚 Tecnologias envolvidas

| Categoria          | Tecnologia                     |
| ------------------ | ------------------------------ |
| Linguagem          | Java 21                        |
| Framework          | Spring Boot                    |
| Segurança          | Spring Security / OAuth2 / JWT |
| Validação          | Jakarta Bean Validation        |
| API Error Handling | RFC 7807 / ProblemDetail       |
| Resiliência        | Resilience4j                   |
| Banco local        | H2                             |
| Build              | Apache Maven                   |
| Containerização    | Docker / Docker Compose        |
| Métricas           | Prometheus                     |
| Dashboards         | Grafana                        |
| Logs               | Loki                           |
| Tracing            | Tempo                          |
| Testes             | JUnit / Spring Test            |
| API                | REST                           |

---

> **💡 Objetivo do guia:** ao concluir todos os testes, o ambiente local deverá permitir validar não apenas o funcionamento funcional do Payment Service, mas também aspectos importantes de uma aplicação backend moderna, incluindo **segurança, resiliência, observabilidade, rastreabilidade, validação de contratos e testes automatizados**.

```
Para executar o teste Testes de Mutação (PITest Plugin) de mutação via terminal:
mvn pitest:mutationCoverage

# Caso tenha o K6 instalado localmente:
Para executar o teste Teste de Carga e Vazão de Virtual Threads (Script K6):
k6 run k6-load-test.js
```