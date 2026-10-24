package com.finsecure.pay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.finsecure.pay.model.TransactionRequest;
import com.finsecure.pay.model.ValidationResponse;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TransactionValidatorTest {

    private final TransactionValidator validator =
            new TransactionValidator(new BigDecimal("10000"), Set.of("USD", "EUR", "GBP", "INR"));

    private TransactionRequest tx(String from, String to, String amount, String currency) {
        return new TransactionRequest("T1", from, to, amount == null ? null : new BigDecimal(amount), currency);
    }

    @Test
    void validTransactionPasses() {
        ValidationResponse r = validator.validate(tx("A100", "B200", "250.50", "USD"));
        assertEquals("VALID", r.status());
        assertTrue(r.errors().isEmpty());
    }

    @Test
    void currencyIsCaseInsensitive() {
        assertEquals("VALID", validator.validate(tx("A100", "B200", "10", "eur")).status());
    }

    @Test
    void amountAtLimitPasses() {
        assertEquals("VALID", validator.validate(tx("A100", "B200", "10000", "USD")).status());
    }

    @Test
    void zeroAmountFails() {
        ValidationResponse r = validator.validate(tx("A100", "B200", "0", "USD"));
        assertEquals("INVALID", r.status());
        assertTrue(r.errors().contains("amount must be greater than zero"));
    }

    @Test
    void negativeAmountFails() {
        assertEquals("INVALID", validator.validate(tx("A100", "B200", "-5", "USD")).status());
    }

    @Test
    void amountAboveLimitFails() {
        ValidationResponse r = validator.validate(tx("A100", "B200", "10000.01", "USD"));
        assertEquals("INVALID", r.status());
        assertTrue(r.errors().get(0).contains("exceeds"));
    }

    @Test
    void missingAmountFails() {
        assertTrue(validator.validate(tx("A100", "B200", null, "USD")).errors().contains("amount is required"));
    }

    @Test
    void unsupportedCurrencyFails() {
        ValidationResponse r = validator.validate(tx("A100", "B200", "10", "XYZ"));
        assertEquals("INVALID", r.status());
        assertTrue(r.errors().get(0).contains("not supported"));
    }

    @Test
    void sameSenderAndReceiverFails() {
        ValidationResponse r = validator.validate(tx("A100", "A100", "10", "USD"));
        assertTrue(r.errors().contains("fromAccount and toAccount must be different"));
    }

    @Test
    void missingAccountsFail() {
        ValidationResponse r = validator.validate(tx(" ", null, "10", "USD"));
        assertTrue(r.errors().contains("fromAccount is required"));
        assertTrue(r.errors().contains("toAccount is required"));
    }

    @Test
    void multipleErrorsAreReported() {
        ValidationResponse r = validator.validate(tx("A100", "A100", "-1", "XYZ"));
        assertEquals(3, r.errors().size());
    }

    @Test
    void nullRequestFails() {
        assertEquals("INVALID", validator.validate(null).status());
    }
}
