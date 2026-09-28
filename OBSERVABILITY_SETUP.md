```markdown
# 📊 Guia Prático da Stack de Observabilidade

Este documento explica como a infraestrutura de telemetria do **Payment Service** funciona e como utilizá-la no dia a dia para monitorar, debugar e analisar a performance e a resiliência da aplicação.

---

## 🎯 Os 3 Pilares da Telemetria

A observabilidade do projeto é centralizada na interface do **Grafana**:

1. **Prometheus (Métricas):** Coleta quantitativa da saúde da aplicação e estado dos componentes (ex: taxa de erro, tempo de resposta, estado do Circuit Breaker).
2. **Grafana Loki (Logs):** Centraliza todos os logs do serviço com suporte a rastreabilidade via `X-Correlation-ID` e `traceId`.
3. **Grafana Tempo (Traces/Rastreamento):** Exibe a linha do tempo detalhada da requisição através dos adaptadores (Controller -> Servico -> Banco de Dados -> Gateway -> Outbox/Kafka).

---

## 🔗 Endereços de Acesso Rápido

| Serviço | URL | Descrição |
| :--- | :--- | :--- |
| **Aplicação Spring Boot** | `http://localhost:8081` | API REST de Pagamentos |
| **Spring Actuator Prometheus** | `http://localhost:8081/actuator/prometheus` | Exposição de métricas brutas |
| **Healthcheck** | `http://localhost:8081/actuator/health` | Status de saúde da aplicação |
| **Grafana** | `http://localhost:3000` | Dashboards, Logs e Traces (User: `admin` / Pass: `admin`) |
| **Prometheus UI** | `http://localhost:9090` | Consultas PromQL brutas |
| **H2 Console** | `http://localhost:8081/h2-console` | Banco de dados em memória (`jdbc:h2:mem:paymentdb`) |

---

## 🔍 Como usar as ferramentas no dia a dia

### 1. Consultar Logs no Grafana Loki
1. Acesse o Grafana (`http://localhost:3000`).
2. No menu lateral esquerdo, clique em **Explore** (ícone de bússola).
3. Selecione a fonte de dados **Loki**.
4. No campo LogQL, busque por logs da aplicação:
   ```logql
   {app="payment-service"}
Para filtrar logs por erro ou por um Correlation-ID específico:

Snippet de código
{app="payment-service"} |= "ERROR"
{app="payment-service"} |= "123e4567-e89b-12d3-a456-426614174000"
2. Rastrear uma Requisição (TraceID & Correlation-ID -> Tempo)
Toda requisição processada gera um traceId e insere o correlationId nos logs através do filtro HTTP.

Ao visualizar o log no Loki, clique no campo traceId para abrir a visão do Grafana Tempo.

O Tempo exibirá o tempo exato consumido em cada camada (Controller, Banco JPA, Gateway Externo e evento de Outbox).

3. Monitorar a Resiliência (Resilience4j + Prometheus)
No Grafana (Explore -> Prometheus), consulte as métricas do Resilience4j:

resilience4j_circuitbreaker_state: Estado atual do Circuit Breaker (0 = Closed/Saudável, 1 = Open/Aberto, 2 = Half-Open).

resilience4j_retry_calls_total: Quantidade total de re-tentativas executadas pelo adaptador do gateway.

🚀 Como Iniciar a Infraestrutura Docker
Certifique-se de que o Docker Desktop está rodando e execute na raiz do projeto:

Bash
docker compose up -d --build