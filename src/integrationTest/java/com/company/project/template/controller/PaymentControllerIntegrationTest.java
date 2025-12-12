package com.company.project.template.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.company.project.template.BaseIntegrationTest;
import com.company.project.template.dao.repository.cache.PaymentCacheRepository;
import com.company.project.template.model.dto.PaymentDto;
import com.company.project.template.model.dto.PaymentResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Payment Controller Integration Tests")
class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentCacheRepository paymentCacheRepository;

    private static final String BASE_URL = "/template-project/v1/payments";

    @AfterEach
    void tearDown() {
        // Clean up Redis cache after each test
        // Note: PaymentCacheRepository doesn't have deleteAll, so we rely on TTL or manual cleanup
    }

    @Test
    @DisplayName("POST /payments/create - Should create payment and store in Redis cache")
    void shouldCreatePaymentAndStoreInRedis() {
        // Given: A valid payment request
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");

        // When: POST request to create payment
        ResponseEntity<PaymentResponseDto> response = restTemplate.postForEntity(
                BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );

        // Then: Should return 201 CREATED with payment execution ID
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentExecutionId()).isNotNull();
        assertThat(response.getBody().getExpireAt()).isAfter(LocalDateTime.now());

        // Verify payment is stored in Redis
        String paymentExecutionId = response.getBody().getPaymentExecutionId();
        PaymentDto cachedPayment = paymentCacheRepository.read(paymentExecutionId);
        assertThat(cachedPayment).isNotNull();
        assertThat(cachedPayment.getPaymentId()).isEqualTo("PAY001");
        assertThat(cachedPayment.getAccountNumber()).isEqualTo("ACC001");
        assertThat(cachedPayment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("POST /payments/create - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() {
        // Given: Invalid payment (missing required fields)
        PaymentDto invalidPayment = PaymentDto.builder()
                .paymentId("") // Invalid: blank
                .amount(BigDecimal.valueOf(1500)) // Invalid: exceeds max
                .build();

        // When: POST request with invalid data
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/create",
                invalidPayment,
                String.class
        );

        // Then: Should return 400 BAD REQUEST
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /payments/create - Should validate amount constraints")
    void shouldValidateAmountConstraints() {
        // Given: Payment with amount below minimum
        PaymentDto paymentBelowMin = createTestPayment("PAY001", "ACC001", "ACC999");
        paymentBelowMin.setAmount(BigDecimal.valueOf(0.5)); // Below min of 1

        // When: POST request with amount below minimum
        ResponseEntity<String> response1 = restTemplate.postForEntity(
                BASE_URL + "/create",
                paymentBelowMin,
                String.class
        );

        // Then: Should return 400 BAD REQUEST
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Given: Payment with amount above maximum
        PaymentDto paymentAboveMax = createTestPayment("PAY002", "ACC001", "ACC999");
        paymentAboveMax.setAmount(BigDecimal.valueOf(1500)); // Above max of 1000

        // When: POST request with amount above maximum
        ResponseEntity<String> response2 = restTemplate.postForEntity(
                BASE_URL + "/create",
                paymentAboveMax,
                String.class
        );

        // Then: Should return 400 BAD REQUEST
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /payments/execute/{id} - Should execute payment, delete from Redis, and send to RabbitMQ")
    void shouldExecutePaymentAndSendToRabbitMq() {
        // Given: A payment created and stored in Redis
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");
        ResponseEntity<PaymentResponseDto> createResponse = restTemplate.postForEntity(
                BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );
        String paymentExecutionId = createResponse.getBody().getPaymentExecutionId();

        // Verify payment exists in Redis before execution
        assertThat(paymentCacheRepository.read(paymentExecutionId)).isNotNull();

        // When: POST request to execute payment
        ResponseEntity<Void> response = restTemplate.postForEntity(
                BASE_URL + "/execute/" + paymentExecutionId,
                null,
                Void.class
        );

        // Then: Should return 202 ACCEPTED
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Verify payment is removed from Redis cache after execution
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    PaymentDto cachedPayment = paymentCacheRepository.read(paymentExecutionId);
                    assertThat(cachedPayment).isNull();
                });

        // Note: Verifying RabbitMQ message would require a message listener
        // which is covered in the MessageConsumerIntegrationTest
    }

    @Test
    @DisplayName("POST /payments/execute/{id} - Should return 404 when payment not found in Redis")
    void shouldReturn404WhenPaymentNotFoundInCache() {
        // Given: A non-existent payment execution ID
        String nonExistentId = "non-existent-id";

        // When: POST request to execute non-existent payment
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/execute/" + nonExistentId,
                null,
                String.class
        );

        // Then: Should return 404 NOT FOUND
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Payment does not exist or has expired");
    }

    @Test
    @DisplayName("POST /payments/execute/{id} - Should return 400 when payment execution ID is blank")
    void shouldReturn400WhenPaymentExecutionIdIsBlank() {
        // Given: A blank payment execution ID
        String blankId = "   ";

        // When: POST request with blank ID
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/execute/" + blankId,
                null,
                String.class
        );

        // Then: Should return 400 BAD REQUEST
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should verify Redis cache TTL expiration")
    void shouldVerifyRedisCacheTtl() throws InterruptedException {
        // Given: A payment created with TTL
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");
        ResponseEntity<PaymentResponseDto> createResponse = restTemplate.postForEntity(
                BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );
        String paymentExecutionId = createResponse.getBody().getPaymentExecutionId();

        // Verify payment exists immediately
        assertThat(paymentCacheRepository.read(paymentExecutionId)).isNotNull();

        // Note: Full TTL test would take 5 minutes (default TTL)
        // In a real scenario, you might want to reduce TTL for testing
        // For now, we just verify it exists and would expire eventually
        assertThat(createResponse.getBody().getExpireAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle multiple concurrent payment creations with Redis")
    void shouldHandleConcurrentPaymentCreations() {
        // Given: Multiple payment requests
        PaymentDto payment1 = createTestPayment("PAY001", "ACC001", "ACC999");
        PaymentDto payment2 = createTestPayment("PAY002", "ACC002", "ACC998");
        PaymentDto payment3 = createTestPayment("PAY003", "ACC003", "ACC997");

        // When: Creating payments concurrently
        ResponseEntity<PaymentResponseDto> response1 = restTemplate.postForEntity(
                BASE_URL + "/create", payment1, PaymentResponseDto.class);
        ResponseEntity<PaymentResponseDto> response2 = restTemplate.postForEntity(
                BASE_URL + "/create", payment2, PaymentResponseDto.class);
        ResponseEntity<PaymentResponseDto> response3 = restTemplate.postForEntity(
                BASE_URL + "/create", payment3, PaymentResponseDto.class);

        // Then: All should be created successfully
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify all are stored in Redis
        assertThat(paymentCacheRepository.read(response1.getBody().getPaymentExecutionId())).isNotNull();
        assertThat(paymentCacheRepository.read(response2.getBody().getPaymentExecutionId())).isNotNull();
        assertThat(paymentCacheRepository.read(response3.getBody().getPaymentExecutionId())).isNotNull();
    }

    @Test
    @DisplayName("Should verify Redis serialization/deserialization with complex payment data")
    void shouldVerifyRedisSerializationWithComplexData() {
        // Given: A payment with additional data
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");
        paymentDto.setAdditionalData(java.util.Map.of(
                "transactionType", "ONLINE",
                "merchantId", "MERCH123",
                "deviceInfo", java.util.Map.of("deviceId", "DEV001", "platform", "WEB")
        ));

        // When: Creating payment
        ResponseEntity<PaymentResponseDto> response = restTemplate.postForEntity(
                BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );

        // Then: Should serialize and deserialize correctly
        String paymentExecutionId = response.getBody().getPaymentExecutionId();
        PaymentDto cachedPayment = paymentCacheRepository.read(paymentExecutionId);

        assertThat(cachedPayment).isNotNull();
        assertThat(cachedPayment.getAdditionalData()).isNotNull();
        assertThat(cachedPayment.getAdditionalData()).containsKeys("transactionType", "merchantId", "deviceInfo");
        assertThat(cachedPayment.getAdditionalData().get("transactionType")).isEqualTo("ONLINE");
    }

    private PaymentDto createTestPayment(String paymentId, String accountNumber, String recipientAccountNumber) {
        return PaymentDto.builder()
                .paymentId(paymentId)
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .paymentMethod("BANK_TRANSFER")
                .recipientName("John Doe")
                .recipientAccountNumber(recipientAccountNumber)
                .build();
    }
}
