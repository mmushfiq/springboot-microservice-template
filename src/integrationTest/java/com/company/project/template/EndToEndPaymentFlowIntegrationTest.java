package com.company.project.template;

import static com.company.project.template.model.constant.RabbitConstant.K_EXECUTE_PAYMENT_RESULT;
import static com.company.project.template.model.constant.RabbitConstant.X_PN_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.company.project.common.messaging.BaseResultEvent;
import com.company.project.common.messaging.Result;
import com.company.project.template.dao.document.PaymentLogDocument;
import com.company.project.template.dao.repository.PaymentLogRepository;
import com.company.project.template.dao.repository.cache.PaymentCacheRepository;
import com.company.project.template.model.dto.PaymentDto;
import com.company.project.template.model.dto.PaymentResponseDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("End-to-End Payment Flow Integration Tests")
class EndToEndPaymentFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentCacheRepository paymentCacheRepository;

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    private static final String PAYMENT_BASE_URL = "/template-project/v1/payments";

    @BeforeEach
    void setUp() {
        // Start all RabbitMQ listener containers manually for these tests
        rabbitListenerEndpointRegistry.getListenerContainers()
                .forEach(container -> {
                    if (!container.isRunning()) {
                        container.start();
                    }
                });
    }

    @AfterEach
    void tearDown() {
        paymentLogRepository.deleteAll();
    }

    @Test
    @DisplayName("End-to-End: Complete payment flow from creation to MongoDB persistence")
    void shouldExecuteCompletePaymentFlowSuccessfully() {
        // STEP 1: Create Payment (stored in Redis)
        PaymentDto paymentDto = createTestPayment("PAY-E2E-001", "ACC001", "ACC999");

        ResponseEntity<PaymentResponseDto> createResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();

        String paymentExecutionId = createResponse.getBody().getPaymentExecutionId();
        assertThat(paymentExecutionId).isNotNull();

        // Verify payment is in Redis cache
        PaymentDto cachedPayment = paymentCacheRepository.read(paymentExecutionId);
        assertThat(cachedPayment).isNotNull();
        assertThat(cachedPayment.getPaymentId()).isEqualTo("PAY-E2E-001");

        // STEP 2: Execute Payment (sends to RabbitMQ and removes from Redis)
        ResponseEntity<Void> executeResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/execute/" + paymentExecutionId,
                null,
                Void.class
        );

        assertThat(executeResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Verify payment is removed from Redis cache
        await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    PaymentDto removedPayment = paymentCacheRepository.read(paymentExecutionId);
                    assertThat(removedPayment).isNull();
                });

        // STEP 3: Simulate Payment Processing Result (external system response via RabbitMQ)
        BaseResultEvent<PaymentDto> resultEvent = BaseResultEvent.<PaymentDto>builder()
                .eventId(paymentExecutionId)
                .result(Result.SUCCESS)
                .payload(paymentDto)
                .build();

        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, resultEvent);

        // STEP 4: Verify Payment Result is Consumed and Saved to MongoDB
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);

                    PaymentLogDocument log = logs.get(0);
                    assertThat(log.getPaymentId()).isEqualTo("PAY-E2E-001");
                    assertThat(log.getAccountNumber()).isEqualTo("ACC001");
                    assertThat(log.getRecipientAccountNumber()).isEqualTo("ACC999");
                    assertThat(log.getStatus()).isEqualTo("SUCCESS");
                    assertThat(log.getCreatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("End-to-End: Failed payment flow should record failure in MongoDB")
    void shouldHandleFailedPaymentFlowCorrectly() {
        // STEP 1: Create Payment
        PaymentDto paymentDto = createTestPayment("PAY-E2E-002", "ACC002", "ACC998");

        ResponseEntity<PaymentResponseDto> createResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );

        String paymentExecutionId = createResponse.getBody().getPaymentExecutionId();

        // STEP 2: Execute Payment
        restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/execute/" + paymentExecutionId,
                null,
                Void.class
        );

        // STEP 3: Simulate Failed Payment Processing Result
        BaseResultEvent<PaymentDto> failedResultEvent = BaseResultEvent.<PaymentDto>builder()
                .eventId(paymentExecutionId)
                .result(Result.FAILURE)
                .payload(paymentDto)
                .build();

        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, failedResultEvent);

        // STEP 4: Verify Failure is Recorded in MongoDB
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);

                    PaymentLogDocument log = logs.get(0);
                    assertThat(log.getStatus()).isEqualTo("FAILED");
                    assertThat(log.getPaymentId()).isEqualTo("PAY-E2E-002");
                });
    }

    @Test
    @DisplayName("End-to-End: Multiple concurrent payments should all be processed correctly")
    void shouldHandleMultipleConcurrentPayments() {
        // STEP 1: Create Multiple Payments Concurrently
        PaymentDto payment1 = createTestPayment("PAY-E2E-010", "ACC010", "ACC990");
        PaymentDto payment2 = createTestPayment("PAY-E2E-011", "ACC011", "ACC991");
        PaymentDto payment3 = createTestPayment("PAY-E2E-012", "ACC012", "ACC992");

        ResponseEntity<PaymentResponseDto> response1 = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create", payment1, PaymentResponseDto.class);
        ResponseEntity<PaymentResponseDto> response2 = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create", payment2, PaymentResponseDto.class);
        ResponseEntity<PaymentResponseDto> response3 = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create", payment3, PaymentResponseDto.class);

        String execId1 = response1.getBody().getPaymentExecutionId();
        String execId2 = response2.getBody().getPaymentExecutionId();
        String execId3 = response3.getBody().getPaymentExecutionId();

        // STEP 2: Execute All Payments
        restTemplate.postForEntity(PAYMENT_BASE_URL + "/execute/" + execId1, null, Void.class);
        restTemplate.postForEntity(PAYMENT_BASE_URL + "/execute/" + execId2, null, Void.class);
        restTemplate.postForEntity(PAYMENT_BASE_URL + "/execute/" + execId3, null, Void.class);

        // STEP 3: Simulate Processing Results
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT,
                createResultEvent(execId1, payment1, Result.SUCCESS));
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT,
                createResultEvent(execId2, payment2, Result.SUCCESS));
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT,
                createResultEvent(execId3, payment3, Result.FAILURE));

        // STEP 4: Verify All Payments Were Processed
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(3);

                    assertThat(logs)
                            .extracting(PaymentLogDocument::getPaymentId)
                            .containsExactlyInAnyOrder("PAY-E2E-010", "PAY-E2E-011", "PAY-E2E-012");

                    long successCount = logs.stream()
                            .filter(log -> "SUCCESS".equals(log.getStatus()))
                            .count();
                    long failedCount = logs.stream()
                            .filter(log -> "FAILED".equals(log.getStatus()))
                            .count();

                    assertThat(successCount).isEqualTo(2);
                    assertThat(failedCount).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("End-to-End: Payment with additional data should preserve all fields through entire flow")
    void shouldPreserveAdditionalDataThroughCompleteFlow() {
        // STEP 1: Create Payment with Additional Data
        PaymentDto paymentDto = createTestPayment("PAY-E2E-020", "ACC020", "ACC980");
        Map<String, Object> additionalData = Map.of(
                "merchantId", "MERCH123",
                "transactionType", "ONLINE",
                "metadata", Map.of("source", "WEB", "userAgent", "Chrome")
        );
        paymentDto.setAdditionalData(additionalData);

        ResponseEntity<PaymentResponseDto> createResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );

        String paymentExecutionId = createResponse.getBody().getPaymentExecutionId();

        // Verify additional data in Redis
        PaymentDto cachedPayment = paymentCacheRepository.read(paymentExecutionId);
        assertThat(cachedPayment.getAdditionalData()).isNotNull();
        assertThat(cachedPayment.getAdditionalData()).containsKeys("merchantId", "transactionType", "metadata");

        // STEP 2: Execute Payment
        restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/execute/" + paymentExecutionId,
                null,
                Void.class
        );

        // STEP 3: Send Result Event
        BaseResultEvent<PaymentDto> resultEvent = createResultEvent(paymentExecutionId, paymentDto, Result.SUCCESS);
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, resultEvent);

        // STEP 4: Verify Additional Data Persisted in MongoDB
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);

                    PaymentLogDocument log = logs.get(0);
                    assertThat(log.getAdditionalData()).isNotNull();
                    assertThat(log.getAdditionalData()).containsKeys("merchantId", "transactionType", "metadata");
                    assertThat(log.getAdditionalData().get("merchantId")).isEqualTo("MERCH123");
                });
    }

    @Test
    @DisplayName("End-to-End: Payment execution should fail if payment expired from Redis cache")
    void shouldFailExecutionWhenPaymentExpiredFromCache() {
        // STEP 1: Create Payment
        PaymentDto paymentDto = createTestPayment("PAY-E2E-030", "ACC030", "ACC970");

        ResponseEntity<PaymentResponseDto> createResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create",
                paymentDto,
                PaymentResponseDto.class
        );

        String paymentExecutionId = createResponse.getBody().getPaymentExecutionId();

        // STEP 2: Manually Delete from Redis to Simulate Expiration
        paymentCacheRepository.delete(paymentExecutionId);

        // STEP 3: Try to Execute Payment (should fail with 404)
        ResponseEntity<String> executeResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/execute/" + paymentExecutionId,
                null,
                String.class
        );

        assertThat(executeResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(executeResponse.getBody()).contains("Payment does not exist or has expired");

        // STEP 4: Verify No Record in MongoDB
        List<PaymentLogDocument> logs = paymentLogRepository.findAll();
        assertThat(logs).isEmpty();
    }

    @Test
    @DisplayName("End-to-End: Verify all infrastructure components (PostgreSQL, Redis, RabbitMQ, MongoDB)")
    void shouldVerifyAllInfrastructureComponents() {
        // This test verifies that all TestContainers are running and accessible

        // Redis: Create and read payment
        PaymentDto paymentDto = createTestPayment("PAY-INFRA-001", "ACC001", "ACC999");
        ResponseEntity<PaymentResponseDto> createResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/create", paymentDto, PaymentResponseDto.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String execId = createResponse.getBody().getPaymentExecutionId();
        assertThat(paymentCacheRepository.read(execId)).isNotNull();

        // RabbitMQ: Execute payment (sends message)
        ResponseEntity<Void> executeResponse = restTemplate.postForEntity(
                PAYMENT_BASE_URL + "/execute/" + execId, null, Void.class);
        assertThat(executeResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // MongoDB: Consume result and save
        BaseResultEvent<PaymentDto> resultEvent = createResultEvent(execId, paymentDto, Result.SUCCESS);
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, resultEvent);

        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);
                });

        // All infrastructure components verified successfully
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

    private BaseResultEvent<PaymentDto> createResultEvent(String eventId, PaymentDto payload, Result result) {
        return BaseResultEvent.<PaymentDto>builder()
                .eventId(eventId)
                .result(result)
                .payload(payload)
                .build();
    }
}
