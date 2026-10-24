package com.finsecure.pay.service;

import com.finsecure.pay.model.TransactionRequest;
import com.finsecure.pay.model.ValidationResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransactionValidator {

    private final BigDecimal maxAmount;
    private final Set<String> allowedCurrencies;

    public TransactionValidator(
            @Value("${finsecure.max-amount:10000}") BigDecimal maxAmount,
            @Value("${finsecure.allowed-currencies:USD,EUR,GBP,INR}") Set<String> allowedCurrencies) {
        this.maxAmount = maxAmount;
        this.allowedCurrencies = allowedCurrencies;
    }

    public ValidationResponse validate(TransactionRequest tx) {
        List<String> errors = new ArrayList<>();

        if (tx == null) {
            errors.add("Request body is required");
            return ValidationResponse.of(null, errors);
        }

        if (isBlank(tx.transactionId())) {
            errors.add("transactionId is required");
        }
        if (isBlank(tx.fromAccount())) {
            errors.add("fromAccount is required");
        }
        if (isBlank(tx.toAccount())) {
            errors.add("toAccount is required");
        }
        if (!isBlank(tx.fromAccount()) && tx.fromAccount().equals(tx.toAccount())) {
            errors.add("fromAccount and toAccount must be different");
        }

        if (tx.amount() == null) {
            errors.add("amount is required");
        } else if (tx.amount().signum() <= 0) {
            errors.add("amount must be greater than zero");
        } else if (tx.amount().compareTo(maxAmount) > 0) {
            errors.add("amount exceeds the maximum allowed limit of " + maxAmount);
        }

        if (isBlank(tx.currency())) {
            errors.add("currency is required");
        } else if (!allowedCurrencies.contains(tx.currency().toUpperCase())) {
            errors.add("currency '" + tx.currency() + "' is not supported");
        }

        return ValidationResponse.of(tx.transactionId(), errors);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
