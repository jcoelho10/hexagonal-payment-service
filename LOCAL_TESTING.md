🧪 Guia Prático de Testes Locais - Payment Service
Este guia descreve os passos necessários para executar e validar localmente o Payment Service, cobrindo o fluxo de criação de pagamentos, resiliência, validação estrita de DTOs e rastreabilidade por headers.

🛠️ Prerequisitos
Java 21 instalado e configurado (java -version)

Apache Maven 3.9+ (mvn -version)

Docker / Docker Desktop (opcional, necessário apenas para a stack de observabilidade)

🚀 1. Inicializando a Aplicação
A aplicação está configurada para rodar localmente usando banco de dados H2 em memória e porta 8081.

No terminal, execute o comando na raiz do projeto:

Bash
mvn spring-boot:run
Aguarde até visualizar a mensagem no console:
Started PaymentServiceApplication in X.XXX seconds (process running for X.XXX)

⚙️ 2. Nota sobre Autenticação OAuth2 em Dev Local
O projeto possui integração com Spring Security e OAuth2/JWT Resource Server.

Para rodar os testes abaixo localmente sem depender de um servidor Keycloak/Auth0 externo ativo:

A classe SecurityConfig.java permite a execução dos endpoints de dev.

Se estiver com o Resource Server ativo exigindo token, basta incluir o header -H "Authorization: Bearer <SEU_TOKEN>" nas requisições cURL.

🧪 3. Executando os Testes da API (PORTA 8081)
Teste A: Criar Pagamento com Sucesso e Rastreabilidade (X-Correlation-ID)
Este teste valida a criação do pagamento, o acionamento do caso de uso e a injeção do header de correlação para auditoria distribuída.

Linux / macOS / Git Bash:
Bash
curl -i -X POST http://localhost:8081/api/v1/payments \
-H "Content-Type: application/json" \
-H "X-Correlation-ID: transacao-dev-123" \
-d '{
"customerId": "cli-8899",
"amount": 250.00,
"currency": "BRL"
}'
Windows (PowerShell):
PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/payments" `
-Method Post `
-Headers @{"Content-Type"="application/json"; "X-Correlation-ID"="transacao-dev-123"} `
-Body '{"customerId": "cli-8899", "amount": 250.00, "currency": "BRL"}'
O que validar no retorno:

HTTP Status 200 OK

Response Header: X-Correlation-ID: transacao-dev-123

Console do Spring Boot: Verifique que todas as linhas de log geradas para a requisição possuem a tag [transacao-dev-123].

Teste B: Resiliência & Fallback (Resilience4j)
O adaptador de gateway externo possui uma simulação de instabilidade.

Como testar:
Execute o comando do Teste A sucessivamente por 5 a 10 vezes.

Algumas requisições retornarão com "status": "APPROVED".

As requisições que sofrerem instabilidade acionarão o Circuit Breaker / Retry com fallback automático, retornando "status": "FAILED" sem estourar erro HTTP 500 no cliente.

Teste C: Sanitização e Validação de Entrada (Bean Validation + RFC 7807)
Este teste valida se a API rejeita dados malformados (valor negativo, moeda inválida e cliente em branco) retornando o padrão corporativo ProblemDetail.

Bash
curl -i -X POST http://localhost:8081/api/v1/payments \
-H "Content-Type: application/json" \
-d '{
"customerId": "",
"amount": -50.00,
"currency": "REAL"
}'
Retorno Esperado (HTTP 400 Bad Request):

JSON
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
Teste D: Healthcheck do Actuator
Verifique a saúde da aplicação e dos adaptadores:

Bash
curl http://localhost:8081/actuator/health
📊 4. Testes com a Stack de Telemetria (Docker Compose)
Caso queira analisar os gráficos no Grafana, consultar os logs centralizados no Loki e rastrear chamadas no Tempo:

Suba a infraestrutura de telemetria:

Bash
docker compose up -d
Acesse as interfaces:

Grafana: http://localhost:3000 (Login: admin / Senha: admin)

Prometheus UI: http://localhost:9090

No Grafana, acesse a aba Explore -> Loki e filtre pelos logs informando o ID da transação:

Snippet de código
{app="payment-service"} |= "transacao-dev-123"
🧪 5. Executando a Suíte de Testes Automatizados
Para rodar todos os testes unitários da camada de domínio e testes de integração:

Bash
mvn clean test