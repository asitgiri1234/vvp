package com.example.paymentprocessor;

import com.example.paymentprocessor.dto.PaymentRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processesSuccessfulPayment() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("100.00", "payment-success")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Payment successful")))
                .andExpect(jsonPath("$.transactionId").isNumber());
    }

    @Test
    void rejectsInsufficientBalance() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("600.00", "payment-insufficient")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("FAILED")))
                .andExpect(jsonPath("$.message", is("Insufficient balance")));
    }

    @Test
    void returnsExistingPaymentForDuplicateIdempotencyKey() throws Exception {
        String payment = paymentJson("100.00", "payment-duplicate");

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payment))
                .andExpect(status().isOk());

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Payment already processed")));
    }

    @Test
    void rejectsValidationFailure() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("0.00", "payment-invalid")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("FAILED")))
                .andExpect(jsonPath("$.message", is("Amount must be greater than zero")));
    }

    @Test
    void retrievesExistingTransaction() throws Exception {
        String response = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("100.00", "payment-retrieve")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long transactionId = objectMapper.readTree(response).get("transactionId").asLong();

        mockMvc.perform(get("/payments/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is((int) transactionId)))
                .andExpect(jsonPath("$.senderId", is("user-1")))
                .andExpect(jsonPath("$.receiverId", is("user-2")))
                .andExpect(jsonPath("$.status", is("SUCCESS")));
    }

    @Test
    void returnsNotFoundForMissingTransaction() throws Exception {
        mockMvc.perform(get("/payments/999"))
                .andExpect(status().isNotFound());
    }

    private String paymentJson(String amount, String idempotencyKey) throws Exception {
        return objectMapper.writeValueAsString(
                new PaymentRequest("user-1", "user-2",
                        new java.math.BigDecimal(amount), idempotencyKey));
    }
}
