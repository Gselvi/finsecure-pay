package com.finsecure.pay.controller;

import com.finsecure.pay.model.TransactionRequest;
import com.finsecure.pay.model.ValidationResponse;
import com.finsecure.pay.service.TransactionValidator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionValidator validator;

    public TransactionController(TransactionValidator validator) {
        this.validator = validator;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "finsecure-pay");
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validate(@RequestBody TransactionRequest request) {
        ValidationResponse response = validator.validate(request);
        log.info("Validated transaction id={} status={} errors={}",
                response.transactionId(), response.status(), response.errors());
        HttpStatus status = response.isValid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}
