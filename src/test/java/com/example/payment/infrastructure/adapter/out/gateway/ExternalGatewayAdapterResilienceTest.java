package com.example.payment.infrastructure.adapter.out.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class ExternalGatewayAdapterResilienceTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private ExternalGatewayAdapter gatewayAdapter;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("gateway.external.url", wireMockServer::baseUrl);
    }

    @Test
    @DisplayName("Deve executar o Fallback amigável quando a API externa retornar HTTP 500")
    void shouldTriggerFallbackWhenGatewayFails() {
        wireMockServer.stubFor((post(urlEqualTo("/v1/charge")))
                .willReturn(aResponse().withStatus(500)));

        boolean result = gatewayAdapter.processPayment("cli-99", new BigDecimal("100.00"), "BRL");

        assertFalse(result, "O fallback deve retornar false sem lançar exceção não tratada na API");
        wireMockServer.verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/v1/charge")));

    }
}
