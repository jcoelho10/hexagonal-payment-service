# 📊 Guia Prático da Stack de Observabilidade

Este documento explica como a infraestrutura de telemetria do **Payment Service** funciona e como você pode usá-la no seu dia a dia para monitorar, debugar e analisar a performance da aplicação.

---

## 🎯 O que é essa Stack e para que serve?

A observabilidade do projeto é sustentada pelos **3 Pilares da Telemetria**, centralizados na interface do **Grafana**:

1. **Prometheus (Métricas):** Mede a "saúde" quantitativa. Responde a perguntas como: *Quantas requisições por segundo estamos recebendo? O Circuit Breaker está aberto ou fechado?*
2. **Grafana Loki (Logs):** Centraliza todos os logs da aplicação em um só lugar. Responde a: *Qual foi a exceção lançada na tentativa de pagamento às 14:00?*
3. **Grafana Tempo (Traces/Rastreamento):** Faz o "raio-x" do caminho de uma requisição. Responde a: *Dos 3 segundos que a API demorou para responder, quanto tempo foi gasto no Banco de Dados e quanto foi no Gateway Externo?*

---

## 🚀 Como Iniciar a Infraestrutura

Com o Docker Desktop em execução, rode o comando na raiz do projeto:

```bash
docker-compose up -d