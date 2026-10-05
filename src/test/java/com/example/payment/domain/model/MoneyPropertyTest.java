package com.example.payment.domain.model;

import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;

/**
 * - Testes Baseados em Propriedades (jqwik)
 */
public class MoneyPropertyTest {

    @Property
    void moneyShouldRejectZeroOrNegativeAmounts(@ForAll("invalidAmounts")BigDecimal invalidAmount) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Money(invalidAmount, "BRL"));
    }

    @Property
    void moneyShouldRejectInvalidCurrencies(@ForAll("invalidCurrencies") String invalidCurrency) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new Money(new BigDecimal("100.00"), invalidCurrency));
    }

    @Provide
    Arbitrary<BigDecimal> invalidAmounts() {
        return Arbitraries.bigDecimals()
                .lessOrEqual(BigDecimal.ZERO);
    }

    @Provide
    Arbitrary<String> invalidCurrencies() {
        return Arbitraries.of(null, "", " ", "US", "REAL", "123");
    }

}
