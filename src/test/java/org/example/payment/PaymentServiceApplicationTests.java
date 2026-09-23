package org.example.payment;

import com.example.payment.PaymentServiceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = PaymentServiceApplication.class)
class PaymentServiceApplicationTests {

	@Test
	@DisplayName("Deve carregar o contexto da aplicação Spring Boot com sucesso")
	void contextLoads() {
		// Valida se todas as dependências, Beans e configurações do Spring Boot estão corretas
	}

}
