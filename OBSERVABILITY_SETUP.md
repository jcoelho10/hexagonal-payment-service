# 📊 Guia Prático da Stack de Observabilidade

Este documento explica como a infraestrutura de telemetria do **Payment Service** funciona e como você pode usá-la no seu dia a dia para monitorar, debugar e analisar a performance da aplicação.

---

## 🎯 O que é essa Stack e para que serve?

A observabilidade do projeto é sustentada pelos **3 Pilares da Telemetria**, centralizados na interface do **Grafana**:

1. **Prometheus (Métricas):** Mede a "saúde" quantitativa. Responde a perguntas como: *Quantas requisições por segundo estamos recebendo? O Circuit Breaker está aberto ou fechado?*
2. **Grafana Loki (Logs):** Centraliza todos os logs da aplicação em um só lugar. Responde a: *Qual foi a exceção lançada na tentativa de pagamento às 14:00?*
3. **Grafana Tempo (Traces/Rastreamento):** Faz o "raio-x" do caminho de uma requisição. Responde a: *Dos 3 segundos que a API demorou para responder, quanto tempo foi gasto no Banco de Dados e quanto foi no Gateway Externo?*

---

Endereços de Acesso Rápido:
Grafana (Dashboards e Consultas): http://localhost:3000 (Login: admin / Senha: admin)

Prometheus (Métricas Brutas): http://localhost:9090

Spring Actuator (Métricas da Aplicação): http://localhost:8080/actuator/prometheus

🔍 Como usar as ferramentas no dia a dia
1. Consultar Logs no Grafana Loki
   Acesse o Grafana (http://localhost:3000).

No menu lateral esquerdo, clique em Explore (ícone de bússola).

No seletor superior de Data Source, escolha Loki.

No campo de busca (LogQL), digite a consulta abaixo para ver os logs da aplicação em tempo real:

Snippet de código
{app="payment-service"}
Para filtrar apenas por erros:

Snippet de código
{app="payment-service"} |= "ERROR"
2. Rastrear uma Requisição Lenta (TraceID -> Tempo)
   Toda requisição processada pelo Spring cria automaticamente um código único chamado traceId (exemplo: 4bf92f3577b34da6a3ce929d0e0e4736).

Quando você visualizar um log no Loki, o traceId aparecerá destacado.

O botão com o link para o Tempo estará associado ao traceId.

Ao clicar nele, o Grafana abre a linha do tempo detalhada mostrando o tempo exato gasto em cada etapa do código (Controller, Persistência, Chama de Gateway, Evento Kafka).

3. Monitorar a Resiliência (Resilience4j + Prometheus)
   Para acompanhar se o seu Circuit Breaker abriu por instabilidade do fornecedor externo:

No Grafana, acesse Explore e selecione a fonte de dados Prometheus.

Busque pelas métricas do Resilience4j:

resilience4j_circuitbreaker_state: Indica o estado atual do Circuit Breaker (0 para Closed, 1 para Open, 2 para Half-Open).

resilience4j_retry_calls_total: Mostra quantas tentativas de retry foram acionadas.

🛠️ Verificação Rápida de Saúde (Healthcheck)
Você pode verificar se a aplicação e seus adaptadores estão funcionais via terminal:

Bash
curl http://localhost:8080/actuator/health

---

## 🚀 Como Iniciar a Infraestrutura

Com o Docker Desktop em execução, rode o comando na raiz do projeto:

```bash
docker-compose up -d
