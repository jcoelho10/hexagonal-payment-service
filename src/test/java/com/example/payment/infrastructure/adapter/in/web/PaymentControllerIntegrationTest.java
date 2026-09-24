package com.example.payment.infrastructure.adapter.in.web;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // 1. Sobe um banco PostgreSQL real no Docker para os testes
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("paymentdb")
            .withUsername("test")
            .withPassword("test");

    static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        // 2. Sobe o servidor WireMock na porta 8089 para simular o Gateway Externo
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    // Sobrescreve as propriedades de configuração em tempo de execução
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("payment.gateway.url", () -> "http://localhost:8089/gateway/process");
    }

    @Test
    @DisplayName("Deve processar o pagamento com SUCESSO quando o Gateway responder HTTP 200")
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Arrange (Stub do WireMock usando WireMock.post)
        wireMockServer.stubFor(WireMock.post(urlEqualTo("/gateway/process"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"SUCCESS\"}")));

        String requestJson = """
                {
                    "customerId": "cli-777",
                    "amount": 500.00,
                    "currency": "BRL"
                }
                """;

        // Act & Assert (MockMvc usando MockMvcRequestBuilders.post)
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.customerId").value("cli-777"));
    }

    @Test
    @DisplayName("Deve acionar o FALLBACK do Circuit Breaker quando o Gateway responder HTTP 500")
    void shouldTriggerFallbackWhenGatewayFails() throws Exception {
        // Arrange (Stub do WireMock usando WireMock.post, simulando erro 500)
        wireMockServer.stubFor(WireMock.post(urlEqualTo("/gateway/process"))
                .willReturn(aResponse()
                        .withStatus(500)));

        String requestJson = """
                    {
                        "customerId": "cli-999",
                        "amount": 100.00,
                        "currency": "BRL"
                    }
                """;

        // Act & Assert (usando MockMvcRequestBuilders.post)
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

}
