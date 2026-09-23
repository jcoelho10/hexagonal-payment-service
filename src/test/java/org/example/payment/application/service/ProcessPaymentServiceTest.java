package org.example.payment.application.service;

import com.example.payment.application.service.ProcessPaymentService;
import com.example.payment.domain.model.Money;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.model.PaymentStatus;
import com.example.payment.domain.port.out.GatewayPaymentPort;
import com.example.payment.domain.port.out.PaymentNotificationPort;
import com.example.payment.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessPaymentServiceTest {

    @Mock
    private PaymentRepositoryPort repositoryPort;

    @Mock
    private GatewayPaymentPort gatewayPort;

    @Mock
    private PaymentNotificationPort notificationPort;

    @InjectMocks
    private ProcessPaymentService service;

    @Test
    @DisplayName("Deve processar e aprovar um pagamento com sucesso")
    void shouldProcessAndApprovePaymentSuccessfully() {
        // Arrange
        String customerId = "cust-123";
        Money money = new Money(new BigDecimal("150.00"), "BRL");

        when(repositoryPort.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gatewayPort.process(any(Payment.class))).thenReturn(true);

        // Act
        Payment result = service.execute(customerId, money);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        verify(repositoryPort, times(2)).save(any(Payment.class));
        verify(notificationPort, times(1)).notifyPaymentProcessed(any(Payment.class));


    }
}
