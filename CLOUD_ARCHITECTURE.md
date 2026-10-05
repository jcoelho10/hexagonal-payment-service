# ☁️ Arquitetura Cloud Multi-Cloud — Payment Service

Para implantar e gerenciar o **Payment Service** com qualidade enterprise em qualquer uma das quatro principais nuvens (**AWS, Azure, GCP ou OCI**), os serviços necessários seguem uma sequência lógica de infraestrutura:

1. **Rede e Segurança do Perímetro** (ISOLAMENTO)
2. **Cluster de Gerenciamento do Container** (COMPUTAÇÃO)
3. **Bancos de Dados Relacional e NoSQL** (PERSISTÊNCIA & AUDITORIA)
4. **Cache em Memória** (PERFORMANCE)
5. **Mensageria e Streaming de Eventos** (INTEGRAÇÃO & OUTBOX)
6. **Segurança de Credenciais e Identidade** (GOVERNANÇA)
7. **Observabilidade e Monitoramento** (MÉTRICAS & LOGS)

---

A pasta terraform/ com os arquivos .tf traz as configurações de Infraestrutura como Código (IaC - Infrastructure as Code) do projeto usando o Terraform.

Enquanto os arquivos do docker-compose-dev.yml servem para subir os containers e os bancos de dados localmente na sua máquina, as configurações do Terraform servem para provisionar toda a infraestrutura real equivalente nas provedoras de nuvem de forma automatizada e padronizada.

🗺️ Como a pasta terraform/ está organizada no projeto?

No seu projeto, a pasta foi dividida por provedor cloud:

```text
terraform/
├── aws/
│   └── main.tf    # Configura os recursos na Amazon Web Services (EKS, RDS, MSK, ElastiCache, etc.)
├── azure/
│   └── main.tf    # Configura os recursos na Microsoft Azure (AKS, Azure Postgres, Event Hubs, etc.)
├── gcp/
│   └── main.tf    # Configura os recursos no Google Cloud (GKE, Cloud SQL, Memorystore, etc.)
└── oci/
    └── main.tf    # Configura os recursos na Oracle Cloud (OKE, OCI Postgres, OCI Streaming, etc.)
```    
💡 Para que serve isso no contexto da sua aplicação?
Automação Multi-Cloud: Se amanhã a empresa decidir implantar o payment-service na AWS (ou Azure, GCP, OCI), ninguém precisa entrar no painel web da nuvem e criar recursos manualmente clicando em botões.

Reprodutibilidade: Com um único comando (terraform apply), o Terraform lê o arquivo main.tf correspondente e cria o cluster Kubernetes, os bancos de dados PostgreSQL e MongoDB, os clusters de Kafka e os caches de Redis na nuvem exatamente como configurados.

Versionamento de Infraestrutura: As alterações na infraestrutura (como aumentar o tamanho do banco de dados ou criar um novo tópico no Kafka) passam a ser versionadas no mesmo histórico do Git do projeto.

Portfólio / Nível Enterprise: No LinkedIn e no GitHub, ter a pasta terraform/ estruturada demonstra maturidade arquitetural (Cloud-Native / DevOps Ready), mostrando que o projeto não foi pensado apenas para rodar na máquina local, mas sim para ser implantado em ambiente produtivo em qualquer uma das quatro grandes nuvens do mercado.

Abaixo está o detalhamento sequencial de cada serviço para cada provedor cloud.

---

# 1. ☁️ Amazon Web Services (AWS)

```text
[VPC/Route53] -> [ALB/WAF] -> [EKS (Spring Boot)] -> [RDS Postgres + DocumentDB + ElastiCache] -> [MSK (Kafka)]
```

1. **AWS VPC (Virtual Private Cloud) & Internet Gateway:**
   Cria a rede virtual isolada com sub-redes públicas e privadas. O Spring Boot e os bancos rodam estritamente em sub-redes privadas.

2. **AWS WAF & Application Load Balancer (ALB):**
   O WAF protege contra ataques OWASP (ex: SQL Injection), e o ALB distribui o tráfego HTTP/HTTPS para o cluster Kubernetes na porta `8081`.

3. **Amazon EKS (Elastic Kubernetes Service):**
   Orquestra os Pods do `payment-service` em containers com auto-scaling automático (HPA), Liveness/Readiness probes e controle de CPU/Memória.

4. **Amazon RDS for PostgreSQL:**
   Banco de dados relacional gerenciado e multi-AZ que armazena a tabela `payments` e a tabela `payments_outbox` com suporte a transações ACID.

5. **Amazon DocumentDB (compatível com MongoDB):**
   Armazena os documentos NoSQL de auditoria (`payment_audit`) gerados pelo serviço de auditoria.

6. **Amazon ElastiCache for Redis:**
   Cache distribuído em memória para acelerar consultas e reduzir a carga sobre o banco relacional.

7. **Amazon MSK (Managed Streaming for Apache Kafka):**
   Cluster Kafka gerenciado para onde o worker do *Transactional Outbox Pattern* publica os eventos de pagamento.

8. **AWS KMS & Secrets Manager:**
   Criptografa dados em repouso e gerencia senhas do banco de dados e chaves OAuth2 sem expor credenciais no código/YAML.

9. **Amazon CloudWatch & AWS X-Ray:**
   Coleta métricas de CPU/RAM, centraliza logs e rastreia o fluxo das requisições (*distributed tracing*).

---

# 2. ☁️ Microsoft Azure

```text
[VNet/DNS] -> [App Gateway/WAF] -> [AKS (Spring Boot)] -> [Flexible Postgres + Cosmos DB + Azure Cache for Redis] -> [Event Hubs for Kafka]
```

1. **Azure Virtual Network (VNet):**
   Define o isolamento de rede corporativo com regras de grupo de segurança (NSG).

2. **Azure Application Gateway + WAF:**
   Atua como balanceador de carga de camada 7 e firewall de aplicação web, fazendo a terminação TLS.

3. **Azure Kubernetes Service (AKS):**
   Gerencia a execução escalável dos containers Docker da aplicação Spring Boot em Java 21.

4. **Azure Database for PostgreSQL (Flexible Server):**
   Persistência relacional de alta performance para os pagamentos e mensagens de outbox.

5. **Azure Cosmos DB (com API para MongoDB):**
   Armazenamento NoSQL de documentos de auditoria com baixíssima latência.

6. **Azure Cache for Redis:**
   Guarda em memória dados de consulta rápida e sessões da API.

7. **Azure Event Hubs (com API para Kafka):**
   Funciona como a malha de mensageria para o Outbox Pattern, aceitando produtores/consumidores Kafka nativos sem necessidade de alterar o código do Java.

8. **Azure Key Vault:**
   Armazena certificados, senhas e tokens de forma segura e centralizada.

9. **Azure Monitor & Application Insights:**
   Ingestão de telemetria, logs centralizados (equivalente ao Loki) e rastreamento APM em tempo real.

---

# 3. ☁️ Google Cloud Platform (GCP)

```text
[VPC Network] -> [Cloud Load Balancing/Armor] -> [GKE (Spring Boot)] -> [Cloud SQL + MongoDB Atlas / Firestore] -> [Memorystore] -> [Pub/Sub ou Managed Kafka]
```

1. **GCP VPC Network:**
   Estrutura de rede global simplificada e privada para tráfego seguro entre o microsserviço e as bases de dados.

2. **Google Cloud Armor & Cloud Load Balancing:**
   Protege contra ataques DDoS/OWASP e distribui requisições HTTP para a aplicação.

3. **Google Kubernetes Engine (GKE):**
   Considerado o orquestrador Kubernetes gerenciado mais maduro do mercado para rodar os Pods da aplicação Java.

4. **Cloud SQL for PostgreSQL:**
   Instância gerenciada do PostgreSQL com backup automático e replicação para suportar as transações do pagamento.

5. **MongoDB Atlas no GCP (ou Firestore em modo Datastore):**
   Banco NoSQL ideal para registrar os documentos de auditoria de pagamento.

6. **Memorystore for Redis:**
   Instância Redis em memória fully-managed para caching de alta performance.

7. **Managed Service for Apache Kafka (ou Cloud Pub/Sub com Kafka Connector):**
   Broker de streaming de mensagens para desacoplamento e envio de eventos assíncronos via Outbox.

8. **Secret Manager & Cloud KMS:**
   Gerenciamento seguro de chaves de criptografia e credenciais corporativas.

9. **Google Cloud Observability (Cloud Logging, Monitoring e Trace):**
   Rastreia os `X-Correlation-ID`, exibe métricas do Spring Actuator e métricas da JVM.

---

# 4. ☁️ Oracle Cloud Infrastructure (OCI)

```text
[VCN] -> [Flexible Load Balancer / OCI WAF] -> [OKE (Spring Boot)] -> [OCI PostgreSQL + OCI NoSQL] -> [OCI Cache for Redis] -> [OCI Streaming (Kafka API)]
```

1. **OCI Virtual Cloud Network (VCN):**
   Sub-redes públicas e privadas configuradas com Security Lists rígidas.

2. **OCI Load Balancer & WAF:**
   Ponto de entrada público para a API com inspeção de segurança e regras de balanceamento de tráfego HTTP.

3. **Oracle Container Engine for Kubernetes (OKE):**
   Cluster Kubernetes nativo para execução e auto-scaling da imagem Docker do `payment-service`.

4. **OCI Database for PostgreSQL:**
   Serviço de banco de dados relacional totalmente gerenciado pela Oracle para persistência ACID dos pagamentos.

5. **OCI NoSQL Database (ou MongoDB Cloud no OCI):**
   Serviço NoSQL leve e rápido para gravação dos logs de auditoria dos pagamentos.

6. **OCI Cache with Redis:**
   Serviço gerenciado de Redis para acelerar a camada de leitura e cache.

7. **OCI Streaming Service:**
   Possui compatibilidade nativa com a API do Apache Kafka, permitindo que a aplicação publique eventos do *Transactional Outbox* sem mudar as dependências do `pom.xml`.

8. **OCI Vault:**
   Serviço de gerenciamento de chaves (KMS) e armazenamento de segredos e senhas de acesso.

9. **OCI Logging & Application Performance Monitoring (APM):**
   Captura logs distribuídos do Logback e exibe rastreamento de chamadas por transação.

---

# 📊 Resumo Comparativo dos Serviços

| **Etapa na Sequência**  | **AWS**             | **Azure**                    | **GCP**                     | **OCI**                   |
| ----------------------- | ------------------- | ---------------------------- | --------------------------- | ------------------------- |
| **1. Entrada e WAF**    | ALB + WAF           | App Gateway + WAF            | Cloud Load Balancer + Armor | OCI Load Balancer + WAF   |
| **2. Computação (K8s)** | Amazon EKS          | Azure AKS                    | Google GKE                  | Oracle OKE                |
| **3. Banco Relacional** | RDS PostgreSQL      | Azure Database for Postgres  | Cloud SQL for Postgres      | OCI Database for Postgres |
| **4. Banco NoSQL**      | Amazon DocumentDB   | Azure Cosmos DB              | MongoDB Atlas / Firestore   | OCI NoSQL Database        |
| **5. Cache**            | ElastiCache Redis   | Azure Cache for Redis        | Memorystore for Redis       | OCI Cache with Redis      |
| **6. Eventos/Kafka**    | Amazon MSK          | Event Hubs (Kafka API)       | Managed Kafka / Pub/Sub     | OCI Streaming (Kafka API) |
| **7. Segredos**         | AWS Secrets Manager | Azure Key Vault              | Secret Manager              | OCI Vault                 |
| **8. Tracing & Logs**   | CloudWatch + X-Ray  | Azure Monitor + App Insights | GCP Cloud Observability     | OCI Logging + APM         |
