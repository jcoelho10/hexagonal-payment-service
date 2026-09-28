package com.example.payment.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank(message = "O ID do cliente é obrigatório. Não pode estar em branco")
        @Pattern(regexp =  "^[a-zA-Z0-9_-]+$", message = "O ID do cliente contém caracteres inválidos")
        String customerId,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser estritamente maior quer zero")
        BigDecimal amount,

        @NotBlank(message = "A moeda é obrigatória")
        @Pattern(regexp = "^[A-Z]{3}$", message = "A  moeda deve seguir o padrão ISO-4217 de 3 letras maiúsculas (ex: BRL, USD)")
        String currency
) {}
