package com.finsecure.pay.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finsecure.pay.service.TransactionValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
@Import(TransactionValidator.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void healthReturnsUp() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void validTransactionReturns200() throws Exception {
        String body = """
                {"transactionId":"T1","fromAccount":"A100","toAccount":"B200","amount":250.00,"currency":"USD"}
                """;
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALID"));
    }

    @Test
    void invalidTransactionReturns400() throws Exception {
        String body = """
                {"transactionId":"T2","fromAccount":"A100","toAccount":"B200","amount":-10,"currency":"USD"}
                """;
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("INVALID"))
                .andExpect(jsonPath("$.errors[0]").value("amount must be greater than zero"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }
}
