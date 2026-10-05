# 📊 Matriz de Compatibilidade — Payment Service vs. Banco de Perguntas de Entrevista

Este documento relaciona os principais **cenários do banco de perguntas de entrevista** com as decisões arquiteturais e implementações presentes no projeto **`payment-service`**.

O objetivo é utilizar o projeto como **laboratório prático de preparação para entrevistas técnicas**, permitindo demonstrar conceitos de arquitetura, desenvolvimento backend, resiliência, mensageria, observabilidade, segurança e cloud-native por meio de código real.

---

## 📋 Índice

* [1. Matriz de Compatibilidade](#1-matriz-de-compatibilidade)
* [2. Defesa dos Cenários Críticos](#2-defesa-dos-cenários-críticos)

    * [Resiliência](#21-resiliência-e-prevenção-de-cascata)
    * [Transactional Outbox](#22-consistência-e-entrega-de-eventos)
    * [Observabilidade](#23-observabilidade-e-rastreamento-distribuído)
    * [Arquitetura Hexagonal e DDD](#24-arquitetura-hexagonal-e-ddd)
* [3. Mapa de Tecnologias](#3-mapa-de-tecnologias)
* [4. Estratégia para Entrevistas](#4-estratégia-para-entrevistas)
* [5. Checklist de Preparação](#5-checklist-de-preparação)

---

# 1. 📊 Matriz de Compatibilidade

| Cenários             | Tema da entrevista                                    | O que o entrevistador busca avaliar                                                                                   | Implementação no `payment-service`                                                                                                                  |
| -------------------- | ----------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Casos 1, 3 e 14**  | 🏗️ Arquitetura Hexagonal, DDD e isolamento de regras | Separação de responsabilidades, baixo acoplamento, domínio independente de frameworks e aplicação de Ports & Adapters | **Cobertura:** `domain` com `Payment` e `Money`, `application` com casos de uso e `infrastructure` com adaptadores HTTP, persistência e integrações |
| **Casos 7 e 10**     | 🛡️ Resiliência e prevenção de falhas em cascata      | Circuit Breaker, Retry, timeout, fallback e proteção contra indisponibilidade de serviços externos                    | **Cobertura:** Resilience4j configurado em `application.yml` e aplicado no `ExternalGatewayAdapter`                                                 |
| **Casos 11 e 25**    | 📨 Integridade de dados e Event-Driven Architecture   | Consistência entre banco e mensageria, desacoplamento e confiabilidade na publicação de eventos                       | **Cobertura:** Transactional Outbox com `payments_outbox` e publicação assíncrona via `OutboxKafkaScheduler`                                        |
| **Casos 7, 23 e 27** | 📊 Observabilidade e Distributed Tracing              | Logs centralizados, métricas, tracing, correlação de requisições e diagnóstico de incidentes                          | **Cobertura:** Prometheus, Grafana, Loki, Tempo, Micrometer, W3C Trace Context e `X-Correlation-ID`                                                 |
| **Casos 20 e 24**    | 🔐 Segurança, DTOs e sanitização REST                 | Imutabilidade, Bean Validation, autenticação stateless e tratamento consistente de erros                              | **Cobertura:** Java 21 `record`, `@Valid`, Spring Security, `SessionCreationPolicy.STATELESS` e tratamento global de exceções                       |
| **Casos 3 e 8**      | ☁️ Cloud-Native e múltiplos ambientes                 | Containerização, configuração por ambiente, portabilidade e isolamento de infraestrutura                              | **Cobertura:** Dockerfile multi-stage, Docker Compose, profiles e configurações separadas para execução local e containerizada                      |

---

# 2. 🛡️ Defesa dos Cenários Críticos

Esta seção transforma a implementação técnica em **respostas que podem ser utilizadas durante uma entrevista**.

A ideia não é apenas citar uma tecnologia, mas explicar:

1. **Qual problema existe;**
2. **Qual decisão arquitetural foi tomada;**
3. **Como foi implementada;**
4. **Qual benefício foi obtido;**
5. **Quais trade-offs existem.**

---

## 2.1 🛡️ Resiliência e Prevenção de Falhas em Cascata

### ❓ Pergunta do entrevistador

> **"Como você garante resiliência quando um parceiro externo apresenta instabilidade?"**

### 🎯 Conceitos envolvidos

* Circuit Breaker
* Retry
* Exponential Backoff
* Timeout
* Fallback
* Resilience4j
* Prevenção de cascading failure

### 💬 Como defender o projeto

> **"No `payment-service`, evitamos realizar chamadas síncronas desprotegidas para o provedor externo. A comunicação é encapsulada no `ExternalGatewayAdapter` e protegida utilizando Resilience4j."**
>
> **"Configuramos um Circuit Breaker com janela deslizante de 10 chamadas e limite de 50% de falhas. Também utilizamos Retry com Exponential Backoff, limitado a 3 tentativas."**
>
> **"Quando o serviço externo apresenta falhas persistentes ou ultrapassa os limites configurados, o Circuit Breaker pode abrir o circuito, evitando novas chamadas desnecessárias. O fluxo então utiliza o mecanismo de fallback definido pela aplicação."**

### 🔎 Pontos para demonstrar no código

```text
ExternalGatewayAdapter
        │
        ├── Retry
        │
        ├── Circuit Breaker
        │
        ├── Timeout
        │
        └── Fallback
```

### 🧠 Pergunta de aprofundamento

**"Por que não fazer Retry indefinidamente?"**

Resposta:

> "Retries excessivos podem aumentar a carga sobre um serviço que já está degradado e provocar uma falha em cascata. Por isso, o número de tentativas precisa ser limitado e combinado com backoff e Circuit Breaker."

---

# 2.2 📨 Consistência e Entrega de Eventos

### ❓ Pergunta do entrevistador

> **"Como garantir consistência e evitar a perda de eventos caso o broker de mensageria fique indisponível?"**

### 🎯 Conceitos envolvidos

* Transactional Outbox
* Apache Kafka
* ACID
* Event-Driven Architecture
* Consistência eventual
* Processamento assíncrono
* Retry de publicação

### 💬 Como defender o projeto

> **"Utilizamos o Transactional Outbox Pattern para evitar o problema de dual write entre o banco de dados e o broker."**
>
> **"Quando o `ProcessPaymentService` processa o pagamento, a alteração da transação e o registro da intenção do evento são persistidos dentro da mesma transação ACID."**
>
> **"O evento é armazenado na tabela `payments_outbox`. Posteriormente, o `OutboxKafkaScheduler` consulta os eventos pendentes e realiza a publicação no Apache Kafka."**
>
> **"Dessa forma, se o Kafka estiver indisponível no momento do processamento, o evento não é perdido. Ele permanece persistido no Outbox e pode ser processado posteriormente."**

### 🔎 Fluxo arquitetural

```text
                ┌─────────────────────┐
                │ ProcessPaymentService│
                └──────────┬──────────┘
                           │
                    Transaction ACID
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
       Payment Table             payments_outbox
                                        │
                                        │ Scheduler
                                        ▼
                                   Apache Kafka
```

### 🧠 Pergunta de aprofundamento

**"O Outbox garante exatamente uma publicação?"**

Resposta:

> "Não necessariamente. O padrão normalmente trabalha com entrega pelo menos uma vez, então consumidores precisam ser preparados para possíveis duplicidades. Por isso, idempotência e uma estratégia adequada de processamento de mensagens são importantes."

---

# 2.3 📊 Observabilidade e Rastreamento Distribuído

### ❓ Pergunta do entrevistador

> **"Como você rastrearia um erro em um ambiente distribuído com vários microsserviços?"**

### 🎯 Conceitos envolvidos

* Observabilidade
* Logs
* Metrics
* Traces
* OpenTelemetry
* Micrometer
* Prometheus
* Grafana
* Loki
* Tempo
* `traceId`
* `spanId`
* `X-Correlation-ID`
* W3C Trace Context

### 💬 Como defender o projeto

> **"Utilizamos os três pilares de observabilidade: logs, métricas e traces."**
>
> **"Para correlação de negócio, utilizamos o `X-Correlation-ID`. Para o rastreamento técnico distribuído, utilizamos `traceId` e `spanId` através do contexto de tracing."**
>
> **"As métricas são coletadas utilizando Micrometer e disponibilizadas para o Prometheus. Os logs são centralizados no Loki e visualizados pelo Grafana, enquanto os traces são enviados para o Tempo."**
>
> **"Assim, diante de um incidente, conseguimos partir de uma requisição, identificar o Correlation ID ou trace ID e correlacionar logs, métricas e traces para identificar onde ocorreu a falha."**

### 🔎 Stack

```text
                    Payment Service
                          │
            ┌─────────────┼─────────────┐
            │             │             │
            ▼             ▼             ▼
          Logs         Metrics        Traces
            │             │             │
            ▼             ▼             ▼
          Loki        Prometheus       Tempo
            │             │             │
            └─────────────┼─────────────┘
                          ▼
                       Grafana
```

### 🔍 Consulta de exemplo no Loki

```logql
{app="payment-service"} |= "transacao-dev-123"
```

---

# 2.4 🏗️ Arquitetura Hexagonal e DDD

### ❓ Pergunta do entrevistador

> **"Como você estruturaria uma aplicação para ser testável e reduzir a dependência de frameworks?"**

### 🎯 Conceitos envolvidos

* Hexagonal Architecture
* Ports & Adapters
* Domain-Driven Design
* Separation of Concerns
* Dependency Inversion
* Testabilidade
* Baixo acoplamento

### 💬 Como defender o projeto

> **"Aplicamos Arquitetura Hexagonal combinada com princípios de DDD para manter as regras de negócio isoladas dos detalhes de infraestrutura."**
>
> **"O núcleo da aplicação fica na camada `domain`, onde estão entidades e objetos de valor, como `Payment` e `Money`. A camada `application` contém os casos de uso e orquestra o fluxo da aplicação."**
>
> **"As integrações externas ficam atrás de Ports, enquanto as implementações concretas ficam nos Adapters da infraestrutura."**
>
> **"Dessa forma, o domínio não precisa conhecer detalhes de HTTP, banco de dados, Kafka ou frameworks específicos."**

### 🔎 Estrutura conceitual

```text
┌─────────────────────────────────────────────┐
│                Infrastructure               │
│                                             │
│ REST Adapter ─────┐                         │
│ Database Adapter ─┤                         │
│ Kafka Adapter ────┤                         │
│ External API ─────┤                         │
│                   ▼                         │
│              Application                    │
│                   │                         │
│              Use Cases                      │
│                   │                         │
│                   ▼                         │
│                Domain                       │
│                                             │
│     Payment │ Money │ Business Rules        │
└─────────────────────────────────────────────┘
```

### 📁 Estrutura esperada

```text
src/main/java/
└── com.example.payment
    ├── domain
    │   ├── model
    │   │   ├── Payment.java
    │   │   └── Money.java
    │   └── port
    │       ├── PaymentRepositoryPort.java
    │       └── PaymentGatewayPort.java
    │
    ├── application
    │   └── service
    │       └── ProcessPaymentService.java
    │
    └── infrastructure
        ├── web
        ├── persistence
        ├── messaging
        └── gateway
```

---

# 3. 🧰 Mapa de Tecnologias

A matriz abaixo facilita a associação entre **pergunta → conceito → tecnologia → implementação**.

| Conceito           | Tecnologia / Recurso         | Implementação                                |
| ------------------ | ---------------------------- | -------------------------------------------- |
| Linguagem          | Java 21                      | Records, API moderna e recursos da linguagem |
| Framework          | Spring Boot                  | Runtime e configuração da aplicação          |
| Arquitetura        | Hexagonal / Ports & Adapters | `domain`, `application`, `infrastructure`    |
| Modelagem          | DDD                          | Entidades, Value Objects e regras de domínio |
| API                | REST                         | Endpoints `/api/v1/payments`                 |
| Validação          | Jakarta Bean Validation      | `@Valid` e constraints                       |
| Segurança          | Spring Security              | OAuth2 / JWT / Stateless                     |
| Resiliência        | Resilience4j                 | Retry / Circuit Breaker / Fallback           |
| Persistência       | H2 / PostgreSQL              | Persistência relacional                      |
| Mensageria         | Apache Kafka                 | Eventos de integração                        |
| Consistência       | Transactional Outbox         | `payments_outbox`                            |
| Métricas           | Micrometer / Prometheus      | Métricas da aplicação                        |
| Logs               | Loki                         | Centralização de logs                        |
| Dashboards         | Grafana                      | Visualização de observabilidade              |
| Tracing            | Tempo / OpenTelemetry        | Distributed tracing                          |
| Containers         | Docker                       | Containerização                              |
| Orquestração local | Docker Compose               | Stack de desenvolvimento                     |
| Build              | Maven                        | Build e gerenciamento de dependências        |
| Testes             | JUnit / Spring Test          | Testes unitários e integração                |

---

# 4. 🎯 Estratégia para Entrevistas

O diferencial de utilizar esse projeto durante uma entrevista é conseguir conectar **teoria, decisão arquitetural e implementação**.

Em vez de responder apenas:

> "Conheço Circuit Breaker."

A resposta pode ser estruturada assim:

```text
CONCEITO
   ↓
PROBLEMA
   ↓
DECISÃO ARQUITETURAL
   ↓
IMPLEMENTAÇÃO
   ↓
TRADE-OFF
   ↓
RESULTADO ESPERADO
```

### Exemplo

**Pergunta:**

> "Você já trabalhou com Circuit Breaker?"

**Resposta baseada no projeto:**

> "Sim. No meu projeto de Payment Service utilizei Resilience4j para proteger a integração com um gateway externo. A chamada fica encapsulada no `ExternalGatewayAdapter`, onde aplicamos Retry, Circuit Breaker e fallback. O objetivo é evitar que uma indisponibilidade do parceiro provoque falhas em cascata dentro do nosso serviço."

Essa estrutura demonstra não apenas conhecimento da ferramenta, mas também **entendimento do problema arquitetural que ela resolve**.

---

# 5. 🧠 Checklist de Preparação para Entrevista

Antes da entrevista, revise cada tópico abaixo e consiga explicar **sem consultar o código**:

### 🏗️ Arquitetura

* [ ] Arquitetura Hexagonal
* [ ] Ports & Adapters
* [ ] DDD
* [ ] Dependency Inversion
* [ ] Separation of Concerns
* [ ] SOLID
* [ ] Domain vs. Application vs. Infrastructure

### 🛡️ Resiliência

* [ ] Circuit Breaker
* [ ] Retry
* [ ] Exponential Backoff
* [ ] Timeout
* [ ] Fallback
* [ ] Cascading Failure
* [ ] Bulkhead
* [ ] Idempotência

### 📨 Mensageria

* [ ] Apache Kafka
* [ ] Transactional Outbox
* [ ] Event-Driven Architecture
* [ ] Consistência eventual
* [ ] At-least-once delivery
* [ ] Idempotência de consumidores
* [ ] Dead Letter Queue

### 📊 Observabilidade

* [ ] Logs estruturados
* [ ] Métricas
* [ ] Traces
* [ ] `traceId`
* [ ] `spanId`
* [ ] `X-Correlation-ID`
* [ ] OpenTelemetry
* [ ] Micrometer
* [ ] Prometheus
* [ ] Grafana
* [ ] Loki
* [ ] Tempo

### 🔐 Segurança

* [ ] OAuth2
* [ ] JWT
* [ ] Resource Server
* [ ] Stateless
* [ ] Bean Validation
* [ ] DTOs
* [ ] Tratamento global de exceções
* [ ] RFC 7807 / ProblemDetail

### ☁️ Cloud-Native

* [ ] Docker
* [ ] Dockerfile multi-stage
* [ ] Docker Compose
* [ ] Profiles
* [ ] Configuração por ambiente
* [ ] Health Checks
* [ ] Actuator

---

# 🏁 Conclusão

O `payment-service` pode ser utilizado como um **projeto prático de referência para discutir arquitetura backend moderna** durante entrevistas técnicas.

A principal vantagem é conseguir conectar cada conceito a uma implementação concreta:

```text
┌──────────────────────────────────────────────────────┐
│                  PAYMENT SERVICE                     │
├──────────────────────────────────────────────────────┤
│                                                      │
│  Java 21 ────────────────► Backend moderno           │
│  Spring Boot ────────────► Application Framework     │
│  Hexagonal + DDD ────────► Arquitetura               │
│  Resilience4j ───────────► Resiliência               │
│  Kafka + Outbox ─────────► Event-Driven              │
│  OAuth2 + JWT ───────────► Segurança                 │
│  Prometheus + Grafana ───► Métricas                  │
│  Loki ───────────────────► Logs                      │
│  Tempo + OTel ───────────► Distributed Tracing      │
│  Docker ─────────────────► Cloud-Native              │
│  JUnit ──────────────────► Testabilidade             │
│                                                      │
└──────────────────────────────────────────────────────┘
```

## 💬 Frase de abertura para entrevista

> **"Eu gosto de demonstrar arquitetura através de implementação. No meu projeto de Payment Service, estruturei um microsserviço utilizando Java 21 e Spring Boot, com Arquitetura Hexagonal e princípios de DDD, Resilience4j para resiliência, Kafka com Transactional Outbox para eventos, OAuth2/JWT para segurança e uma stack completa de observabilidade com Prometheus, Grafana, Loki e Tempo."**

A partir dessa introdução, cada tecnologia pode ser explorada pelo entrevistador e você consegue direcionar a conversa para **decisões arquiteturais, implementação e trade-offs**.
