# 📖 Entendendo o Payment Service: Guia Didático e Arquitetura

Bem-vindo ao mapa conceitual do **Payment Service**! Este documento foi escrito de forma simples, clara e sem "tecniquês" excessivo para explicar o que o sistema faz, como ele funciona por dentro e por que foi construído desta maneira.

---

## 🎯 O que é este projeto? (Visão Geral do Negócio)

Imagine um sistema de caixa de um supermercado de alta demanda. Quando um cliente passa o cartão, o sistema precisa:

* **Validar** se os dados do pagamento fazem sentido (valor correto, moeda válida).
* **Autorizar** a cobrança em um banco/provedor externo.
* **Guardar** o histórico da transação com segurança.
* **Lidar com falhas** (ex: internet caindo ou banco do cliente fora do ar) sem travar a loja inteira.
* **Notificar** outros sistemas da empresa que a compra foi paga.

**O Payment Service é a API (a "central de inteligência")** responsável por fazer exatamente essa gestão de cobranças de forma ultra-segura, rápida e resistente a falhas.

---

## 🏛️ A Ideia Central: Arquitetura Hexagonal (Ports & Adapters)

Para entender a estrutura das pastas, imagine uma tomada de parede:

1. **O centro da tomada (a regra de negócio):** É sempre o mesmo.
2. **O mundo externo:** Não importa se você pluga um liquidificador, uma TV ou um carregador de celular na tomada: o centro funciona do mesmo jeito.

No nosso código:

* **O Núcleo (Domínio):** É onde ficam as regras puras de cobrança. Ele não sabe o que é banco de dados, nem o que é internet ou tela. É protegido contra mudanças do mundo externo.
* **As Portas (Ports):** São os "formatos dos furos da tomada". Definem o que o sistema aceita receber e o que ele precisa enviar para fora.
* **Os Adaptadores (Adapters):** São os "plugs/adaptadores". Conectam o banco de dados (MySQL/PostgreSQL), a API da operadora de cartão ou a tela do usuário às portas do núcleo.

---

## 📂 Navegando Pelas Pastas do Código

Abaixo está o papel de cada pasta e classe no projeto, explicado passo a passo:

```text
payment-service/
├── .github/
│   └── workflows/
│       └── ci-cd.yml                                 # Pipeline de integração e entrega contínua (Build, Test, Deploy)
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties                  # Configuração do Maven Wrapper para padronização de versão
├── docker/
│   ├── grafana/
│   │   ├── dashboards/
│   │   │   └── resilience4j-dashboard.json           # Painel visual pré-configurado para métricas de Circuit Breaker
│   │   └── provisioning/
│   │       └── datasources/
│   │           └── grafana-datasources.yaml          # Conexão automática do Grafana com Loki, Tempo e Prometheus
│   ├── loki/
│   │   └── loki-config.yml                           # Configuração do centralizador de logs do Grafana Stack
│   ├── prometheus/
│   │   └── prometheus.yml                            # Configuração de coleta de métricas (Scrape) da API Spring Boot
│   └── tempo/
│       └── tempo.yaml                                # Configuração do servidor de Distributed Tracing (OpenTelemetry)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── payment/
│   │   │               ├── domain/                   <-- 1. O NÚCLEO (Regras Negociais Puras e Zero Dependências)
│   │   │               │   ├── exception/
│   │   │               │   │   └── PaymentNotFoundException.java     # Exceção de negócio disparada quando pagamento não existe
│   │   │               │   ├── model/
│   │   │               │   │   ├── Money.java        # Value Object que garante validações de valor e moeda
│   │   │               │   │   ├── Payment.java      # Entidade principal do domínio com regras e transições de status
│   │   │               │   │   └── PaymentStatus.java# Enum contendo os estados do pagamento (PENDING, APPROVED, FAILED)
│   │   │               │   └── port/
│   │   │               │       ├── in/
│   │   │               │       │   └── ProcessPaymentUseCase.java       # Interface da porta de entrada para o caso de uso
│   │   │               │       └── out/
│   │   │               │           ├── GatewayPaymentPort.java          # Porta de saída para chamadas a operadoras de cartão
│   │   │               │           ├── PaymentEventPublisherPort.java   # Porta de saída para publicação de eventos
│   │   │               │           ├── PaymentNotificationPort.java     # Porta de saída para notificações de status
│   │   │               │           └── PaymentRepositoryPort.java       # Porta de saída para abstração do banco de dados
│   │   │               │
│   │   │               ├── application/              <-- 2. A ORQUESTRAÇÃO (Casos de Uso da Aplicação)
│   │   │               │   └── service/
│   │   │               │       └── ProcessPaymentService.java           # Orquestrador do fluxo completo de cobrança e gravações
│   │   │               │
│   │   │               ├── infrastructure/           <-- 3. O MUNDO EXTERNO (Adapters, Frameworks e Tecnologias)
│   │   │               │   ├── adapter/
│   │   │               │   │   ├── in/
│   │   │               │   │   │   └── web/
│   │   │               │   │   │       ├── dto/
│   │   │               │   │   │       │   ├── CreatePaymentRequest.java# Record Java DTO para sanitizar o payload de entrada
│   │   │               │   │   │       │   └── PaymentResponse.java     # Record Java DTO de resposta HTTP formatada
│   │   │               │   │   │       ├── exception/
│   │   │               │   │   │       │   └── GlobalExceptionHandler.java # Interceptador global de erros (RFC 7807 Problem Details)
│   │   │               │   │   │       ├── filter/
│   │   │               │   │   │       │   └── CorrelationIdFilter.java # Filtro que injeta o header X-Correlation-ID nos logs
│   │   │               │   │   │       └── PaymentController.java       # Adapter REST com os endpoints HTTP (/api/v1/payments)
│   │   │               │   │   └── out/
│   │   │               │   │       ├── gateway/
│   │   │               │   │       │   └── ExternalGatewayAdapter.java  # Cliente REST externo protegido por Resilience4j
│   │   │               │   │       ├── messaging/
│   │   │               │   │       │   ├── kafka/
│   │   │               │   │       │   │   └── OutboxKafkaScheduler.java# Job agendado que lê a Outbox e envia eventos ao Kafka
│   │   │               │   │       │   └── LogNotificationAdapter.java  # Adapter de notificações via logs estruturados
│   │   │               │   │       ├── nosql/
│   │   │               │   │       │   ├── PaymentAuditDocument.java    # Documento NoSQL mapeado para o MongoDB
│   │   │               │   │       │   ├── PaymentAuditMongoRepository.java # Repositório Spring Data MongoDB para auditoria
│   │   │               │   │       │   └── PaymentCacheService.java     # Serviço de gerenciamento de cache com Redis
│   │   │               │   │       └── persistence/
│   │   │               │   │           ├── outbox/
│   │   │               │   │           │   ├── OutboxAdapter.java       # Implementação do padrão Transactional Outbox
│   │   │               │   │           │   ├── PaymentOutboxEntity.java # Entidade JPA mapeada para a tabela 'payments_outbox'
│   │   │               │   │           │   └── SpringDataOutboxRepository.java # Repositório JPA para registros de outbox
│   │   │               │   │           ├── PaymentEntity.java           # Entidade JPA mapeada para a tabela 'payments'
│   │   │               │   │           ├── PaymentRepositoryAdapter.java# Adapter do banco de dados relacional (PostgreSQL/H2)
│   │   │               │   │           └── SpringDataPaymentRepository.java # Repositório Spring Data JPA principal
│   │   │               │   └── config/
│   │   │               │       ├── DomainConfig.java                    # Configuração de injeção de dependência do domínio puro
│   │   │               │       ├── OpenApiConfig.java                   # Configuração da documentação Swagger / OpenAPI
│   │   │               │       ├── RedisConfig.java                     # Configuração do pool de conexões e serializador do Redis
│   │   │               │       └── SecurityConfig.java                  # Configuração de segurança Spring Security (OAuth2/JWT)
│   │   │               │
│   │   │               └── PaymentServiceApplication.java               # Classe principal de inicialização da aplicação Spring Boot
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__create_tables_payments_and_outbox.sql        # Migration Flyway: Criação das tabelas SQL de pagamentos e outbox
│   │       │       └── V2__create_table_payment_audit.sql               # Migration Flyway: Estrutura para auditoria
│   │       ├── application-dev.yml                   # Propriedades de configuração específicas para o perfil de desenvolvimento
│   │       ├── application-prod.yml                  # Propriedades de configuração para o perfil de produção
│   │       ├── application.yml                       # Propriedades gerais da aplicação Spring Boot
│   │       └── logback-spring.xml                    # Configuração de logs formatados com JSON e traceId para o Loki
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── payment/
│                       ├── application/
│                       │   └── service/
│                       │       └── ProcessPaymentServiceTest.java       # Teste unitário isolado da lógica do serviço de aplicação
│                       ├── infrastructure/
│                       │   └── adapter/
│                       │       └── in/
│                       │           └── web/
│                       │               └── PaymentControllerIntegrationTest.java # Teste de integração do controller REST
│                       ├── PaymentServiceApplicationTests.java           # Teste de carregamento do contexto do Spring Boot
│                       ├── TestcontainersConfiguration.java             # Configuração do Testcontainers para testes de integração
│                       └── TestPaymentServiceApplication.java            # Executável de testes utilizando containers reais
│
├── terraform/
│   ├── aws/
│   │   └── main.tf                                   # Automação de infraestrutura como código para Amazon Web Services
│   ├── azure/
│   │   └── main.tf                                   # Automação de infraestrutura como código para Microsoft Azure
│   ├── gcp/
│   │   └── main.tf                                   # Automação de infraestrutura como código para Google Cloud Platform
│   └── oci/
│       └── main.tf                                   # Automação de infraestrutura como código para Oracle Cloud Infrastructure
├── .dockerignore                                     # Arquivos e pastas ignorados na criação da imagem Docker
├── .gitattributes                                    # Configurações globais de atributos de linhas e codificação do Git
├── .gitignore                                        # Lista de arquivos excluídos do controle de versão Git
├── docker-compose-dev.yml                            # Subida leve de infraestrutura local (Postgres, Mongo, Redis, Kafka, Kafdrop)
├── docker-compose.yml                                # Subida completa com serviços de observabilidade (Grafana, Loki, Tempo, Prometheus)
├── Dockerfile                                        # Script de build multi-stage para containerização da aplicação Java 21
├── HELP.md                                           # Guia inicial padrão de referência gerado pelo Spring Initializr
├── LOCAL_TESTING.md                                  # Guia detalhado de testes locais com Curl, Postman e endpoints
├── mvnw                                              # Script executável do Maven Wrapper em sistemas Unix/Linux
├── mvnw.cmd                                          # Script executável do Maven Wrapper em sistemas Windows
├── OBSERVABILITY_SETUP.md                            # Guia completo de utilização das ferramentas de observabilidade e métricas
├── pom.xml                                           # Gerenciador de dependências e plugins Maven do projeto Java 21
└── PROJECT_ARCHITECTURE_EXPLAINED.md                 # Documentação explicativa da Arquitetura Hexagonal para onboarding e reuniões
```
### 1. Camada de Domínio (`domain`)

> **O Coração da Aplicação:** Esta camada contém exclusivamente as regras de negócio puras. Não depende de nenhum framework ou biblioteca externa.

#### 📁 `domain.model` — *Os Conceitos do Negócio*

* **`Money.java` (Objeto de Valor / Value Object)**
   * **O que faz:** Representa o dinheiro no sistema (um valor numérico e um tipo de moeda, ex: `250.00 BRL`).
   * **Por que faz:** Evita que alguém tente criar pagamentos com valores zerados, negativos ou sem especificar a moeda.

* **`Payment.java` (A Entidade Principal / Aggregate Root)**
   * **O que faz:** É a "ficha do pagamento". Guarda o ID único, cliente, valor, data e o status atual.
   * **Métodos Principais:**
      * `createNew(...)`: Cria uma nova ficha de pagamento com status inicial `"PENDENTE"`.
      * `markAsApproved()`: Altera o status para `"APROVADO"` (se estivesse pendente).
      * `markAsFailed()`: Altera o status para `"FALHOU"`.

* **`PaymentStatus.java` (Opções de Status)**
   * **O que faz:** Lista fixa dos estados possíveis de um pagamento: `PENDING` (Pendente), `APPROVED` (Aprovado) e `FAILED` (Falhou).

---

#### 📁 `domain.port` — *Os Contratos de Comunicação*

* **`in.ProcessPaymentUseCase.java` (Porta de Entrada)**
   * **O que faz:** Contrato que define: *"Quem quiser processar um pagamento neste sistema precisa me passar o ID do cliente e o Dinheiro."*

* **`out.PaymentRepositoryPort.java` (Porta de Saída - Banco de Dados)**
   * **O que faz:** Contrato que define: *"O sistema precisa de um lugar para salvar e buscar pagamentos, independentemente do banco utilizado (Postgres, H2, Mongo, etc.)."*

* **`out.GatewayPaymentPort.java` (Porta de Saída - Cobrança Externa)**
   * **O que faz:** Contrato que define a interface de comunicação com a operadora de cartão de crédito.

* **`out.PaymentNotificationPort.java` (Porta de Saída - Avisos)**
   * **O que faz:** Contrato que define como o sistema notifica outros serviços após o término da cobrança.

---

### 2. Camada de Aplicação (`application`)

> **O Gerente de Operações:** Não cria regras, mas chama as regras do domínio na ordem exata de execução.

#### 📁 `application.service`

* **`ProcessPaymentService.java` (O Gerente do Processo)**
   * **O que faz:** Executa o fluxo completo de cobrança passo a passo:
      1. Cria o pagamento com status `PENDING`.
      2. Salva o registro inicial no banco de dados.
      3. Tenta processar a cobrança no gateway externo.
      4. Atualiza o status para `APPROVED` (sucesso) ou `FAILED` (falha).
      5. Salva a atualização no banco de dados.
      6. Dispara a notificação com o resultado final.

---

### 3. Camada de Infraestrutura (`infrastructure`)

> **A Engenharia Pesada:** Conecta a aplicação com o mundo externo (internet, bancos de dados, mensageria e APIs de terceiros).

#### 📁 `infrastructure.adapter.in.web` — *Recebendo Pedidos do Usuário*

* **`PaymentController.java` (A Porta de Entrada HTTP/REST)**
   * **O que faz:** Recebe as requisições vindo da internet (ex: payloads JSON enviados para `/api/v1/payments`).
   * **Método `createPayment(...)`:** Converte os dados recebidos em objetos do domínio e aciona o `ProcessPaymentService`.

* **`filter.CorrelationIdFilter.java` (O Detetive / Rastreamento)**
   * **O que faz:** Captura ou gera um código único (`X-Correlation-ID`) para cada requisição HTTP.
   * **Por que faz:** Permite rastrear o caminho exato de uma cobrança específica no meio de milhões de linhas de log.

* **`exception.GlobalExceptionHandler.java` (O Tradutor de Erros)**
   * **O que faz:** Intercepta erros de validação (ex: valor negativo) e retorna respostas amigáveis no padrão internacional **RFC 7807 (Problem Details)**.

---

#### 📁 `infrastructure.adapter.out` — *Conectando com o Mundo Externo*

* **`persistence.PaymentRepositoryAdapter.java` (Conector de Banco Relacional)**
   * **O que faz:** Converte a entidade de domínio (`Payment`) para a entidade JPA (`PaymentEntity`) e realiza a gravação no banco de dados.

* **`gateway.ExternalGatewayAdapter.java` (Conector do Provedor de Cartões + Resiliência)**
   * **O que faz:** Executa a chamada de rede para o banco/operadora externa.
   * **Proteção (Resilience4j):** Implementa **Circuit Breaker** (Disjuntor) e **Retry** (Re-tentativas). Se o provedor externo oscilar, ele tenta novamente; se cair, o disjuntor abre e aciona um plano B (*Fallback*) para evitar o travamento da API.

* **`messaging.LogNotificationAdapter.java` (Emissor de Avisos)**
   * **O que faz:** Registra nos sistemas de log e mensageria que o pagamento mudou de estado.

* **`nosql.PaymentCacheService.java` (Guardião de Cache - Redis)**
   * **O que faz:** Armazena consultas frequentes na memória ultra-rápida (Redis) para reduzir a carga sobre o banco de dados principal.

---

#### 📁 `infrastructure.config` — *A Central de Montagem e Segurança*

* **`DomainConfig.java`:** Conecta as dependências mantendo o core de domínio 100% livre de anotações de frameworks.
* **`SecurityConfig.java` (A Portaria / Segurança):** Gerencia a autenticação por Tokens JWT/OAuth2 e as permissões de acesso.
* **`RedisConfig.java`:** Configura os serializadores e conexões do servidor Redis.

---

## 📊 Fluxo Completo de uma Requisição (Resumo Visual)

1. **Cliente HTTP:** Envia a requisição `POST /api/v1/payments`.
2. **`CorrelationIdFilter`:** Injeta a etiqueta `X-Correlation-ID` para auditoria nos logs.
3. **`SecurityConfig`:** Valida as credenciais e permissões de acesso.
4. **`PaymentController`:** Sanitiza e valida o payload da requisição (`Bean Validation`).
5. **`ProcessPaymentService`:** Inicia a orquestração da regra de negócio.
6. **`ExternalGatewayAdapter`:** Processa a cobrança no fornecedor externo com proteção de **Circuit Breaker**.
7. **`PaymentRepositoryAdapter`:** Grava a transação no banco de dados e registra o evento no **Outbox**.
8. **Cliente HTTP:** Recebe o retorno `200 OK` (ou `201 Created`) com o status final (`APPROVED` ou `FAILED`).