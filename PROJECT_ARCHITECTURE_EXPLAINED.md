📖 Entendendo o Payment Service: Guia Didático e Arquitetura
Bem-vindo ao mapa conceitual do Payment Service! Este documento foi escrito de forma simples, clara e sem "tecniquês" excessivo para explicar o que o sistema faz, como ele funciona por dentro e por que foi construído desta maneira.

🎯 O que é este projeto? (Visão Geral do Negócio)
Imagine um sistema de caixa de um supermercado de alta demanda. Quando um cliente passa o cartão, o sistema precisa:

Validar se os dados do pagamento fazem sentido (valor correto, moeda válida).

Tentar autorizar a cobrança em um banco/provedor externo.

Guardar o histórico da transação com segurança.

Lidar com falhas (ex: internet caindo ou banco do cliente fora do ar) sem travar a loja inteira.

Avisar outros sistemas da empresa que a compra foi paga.

O Payment Service é a API (a "central de inteligência") responsável por fazer exatamente essa gestão de cobranças de forma ultra-segura, rápida e resistente a falhas.

🏛️ A Ideia Central: Arquitetura Hexagonal (Ports & Adapters)
Para entender a estrutura das pastas, imagine uma tomada de parede:

O centro da tomada (a regra de negócio) é sempre o mesmo.

Não importa se você pluga um liquidificador, uma TV ou um carregador de celular na tomada (mundo externo): o centro funciona do mesmo jeito.

No nosso código:

O Núcleo (Domínio): É onde ficam as regras puras de cobrança. Ele não sabe o que é banco de dados, nem o que é internet ou tela. É protegido contra mudanças do mundo externo.

As Portas (Ports): São os "formatos dos furos da tomada". Definem o que o sistema aceita receber e o que ele precisa enviar para fora.

Os Adaptadores (Adapters): São os "plugs/adaptadores". Conectam o banco de dados (MySQL/PostgreSQL), a API da operadora de cartão ou a tela do usuário às portas do núcleo.

📂 Navegando Pelas Pastas do Código
Abaixo está o papel de cada pasta e classe no projeto, explicado passo a passo:

Plaintext
src/main/java/com/example/payment/
├── domain/                         <-- 1. O NÚCLEO (Regras Negociais Puras)
├── application/                    <-- 2. A ORQUESTRAÇÃO (Casos de Uso)
└── infrastructure/                 <-- 3. O MUNDO EXTERNO (Conexões e Tecnologias)
1. Camada de Domínio (domain)
   É o coração da aplicação. Não depende de nenhum framework ou biblioteca externa.

📁 domain.model (Os Conceitos do Negócio)
Money.java (Objeto de Valor / Value Object):

O que faz: Representa o dinheiro no sistema (um valor numérico e um tipo de moeda, ex: 250.00 BRL).

Por que faz: Evita que alguém tente criar pagamentos com valores zerados, negativos ou sem especificar se são Reais ou Dólares.

Payment.java (A Entidade Principal / Aggregate Root):

O que faz: É o "ficha do pagamento". Guarda o ID único, o cliente, o dinheiro, a data e o status atual.

Métodos Principais:

createNew(...): Cria uma nova ficha de pagamento com o status inicial "PENDENTE".

markAsApproved(): Muda o status para "APROVADO" (apenas se estava pendente).

markAsFailed(): Muda o status para "FALHOU".

PaymentStatus.java (Opções de Status):

O que faz: Uma lista fixa dos estados possíveis de um pagamento: PENDING (Pendente), APPROVED (Aprovado) e FAILED (Falhou).

📁 domain.port (Os Contratos de Comunicação)
in.ProcessPaymentUseCase.java (Porta de Entrada):

O que faz: Contrato que diz: "Quem quiser processar um pagamento neste sistema precisa me passar o ID do cliente e o Dinheiro."

out.PaymentRepositoryPort.java (Porta de Saída - Banco de Dados):

O que faz: Contrato que diz: "O sistema precisa de um lugar para salvar e buscar pagamentos, mas não me importa se será em arquivo, Postgres ou MongoDB."

out.GatewayPaymentPort.java (Porta de Saída - Cobrança Externa):

O que faz: Contrato que define a chamada para a operadora de cartão de crédito.

out.PaymentNotificationPort.java (Porta de Saída - Avisos):

O que faz: Contrato que define como o sistema avisa outros serviços após o término da cobrança.

2. Camada de Aplicação (application)
   É o "gerente de operações". Ele não cria regras, mas chama as regras do domínio na ordem certa.

📁 application.service
ProcessPaymentService.java (O Gerente do Processo):

O que faz: Executa a receita completa de cobrança passo a passo:

Cria o pagamento como PENDING.

Salva no banco de dados.

Tenta cobrar no gateway externo.

Se a cobrança funcionar, marca como APPROVED. Se falhar, marca como FAILED.

Salva a atualização no banco.

Dispara uma notificação do resultado.

3. Camada de Infraestrutura (infrastructure)
   É a "engenharia pesada". Aqui conectamos o código com a internet, bancos de dados e ferramentas de terceiros.

📁 infrastructure.adapter.in.web (Recebendo Pedidos do Usuário)
PaymentController.java (A Porta de Entrada HTTP/REST):

O que faz: Recebe o pedido vindo da internet (ex: quando um aplicativo envia um JSON para /api/v1/payments).

Método createPayment(...): Converte os dados do pedido em objetos do domínio e aciona o gerente de operações (ProcessPaymentService).

filter.CorrelationIdFilter.java (O Detetive / Rastreamento):

O que faz: Captura ou gera um código único (X-Correlation-ID) para cada requisição HTTP.

Por que faz: Permite rastrear o caminho exato de uma cobrança específica no meio de milhões de linhas de log.

exception.GlobalExceptionHandler.java (O Tradutor de Erros):

O que faz: Se o usuário enviar dados errados (ex: valor negativo), esta classe intercepta o erro e devolve uma resposta amigável e padronizada (padrão internacional RFC 7807) explicando exatamente o que precisa ser corrigido.

📁 infrastructure.adapter.out (Conectando com o Mundo Externo)
persistence.PaymentRepositoryAdapter.java (O Conector do Banco de Dados Relacional):

O que faz: Pega a ficha de pagamento do domínio e a converte para a tabela do banco de dados (PaymentEntity.java), salvando as informações.

gateway.ExternalGatewayAdapter.java (O Conector do Provedor de Cartões + Resiliência):

O que faz: Faz a chamada de rede para o banco externo.

Proteção (Resilience4j): Possui um "Disjuntor" (Circuit Breaker) e "Tentativas Automáticas" (Retry). Se o banco externo oscilar, o sistema tenta novamente com intervalos inteligentes. Se o banco externo cair de vez, o disjuntor "desarma" e aciona um plano B (Fallback) para evitar que a aplicação fique travada esperando resposta.

messaging.LogNotificationAdapter.java (O Emissor de Avisos):

O que faz: Registra nos sistemas de log e eventos que o pagamento mudou de estado.

nosql.PaymentCacheService.java (O Guardião de Cache - Redis):

O que faz: Salva consultas frequentes na memória ultra-rápida (Redis) para evitar sobrecarregar o banco principal.

📁 infrastructure.config (A Central de Montagem e Segurança)
DomainConfig.java: Ensina a aplicação a juntar as peças (injetar as dependências) mantendo a camada de domínio 100% isolada de anotações da tecnologia.

SecurityConfig.java (A Portaria / Segurança): Exige que as chamadas venham acompanhadas de um passe digital (Token JWT / OAuth2), bloqueando acessos não autorizados.

RedisConfig.java: Configura a conversão de objetos para salvamento no servidor de memória Redis.

📊 Fluxo Completo de uma Requisição (Resumo Visual)
Cliente HTTP: Envia um pedido POST /api/v1/payments.

CorrelationIdFilter: Etiqueta a requisição com uma ID única para auditoria.

SecurityConfig: Valida se a requisição possui permissão de segurança.

PaymentController: Valida se o formato do pedido está correto (Bean Validation).

ProcessPaymentService: Inicia a orquestração do negócio.

ExternalGatewayAdapter: Tenta realizar a cobrança com proteção de disjuntor (Resilience4j).

PaymentRepositoryAdapter: Grava o resultado final no banco de dados.

Cliente HTTP: Recebe a resposta 200 OK com o status final (APPROVED ou FAILED).