package com.finsecure.pay.model;

import java.util.List;

public record ValidationResponse(String transactionId, String status, List<String> errors) {

    public static ValidationResponse of(String transactionId, List<String> errors) {
        return new ValidationResponse(transactionId, errors.isEmpty() ? "VALID" : "INVALID", errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
