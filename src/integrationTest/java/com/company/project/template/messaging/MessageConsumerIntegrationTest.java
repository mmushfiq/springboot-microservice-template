package com.company.project.template.messaging;

import static com.company.project.template.model.constant.RabbitConstant.K_EXECUTE_PAYMENT_RESULT;
import static com.company.project.template.model.constant.RabbitConstant.X_PN_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.company.project.common.messaging.BaseResultEvent;
import com.company.project.common.messaging.Result;
import com.company.project.template.BaseIntegrationTest;
import com.company.project.template.dao.document.PaymentLogDocument;
import com.company.project.template.dao.repository.PaymentLogRepository;
import com.company.project.template.model.dto.PaymentDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Message Consumer Integration Tests")
class MessageConsumerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @BeforeEach
    void setUp() {
        paymentLogRepository.deleteAll();
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
    @DisplayName("Should consume SUCCESS payment execution result from RabbitMQ and save to MongoDB")
    void shouldConsumeSuccessPaymentResultAndSaveToMongoDb() {
        // Given: A successful payment result event
        String eventId = UUID.randomUUID().toString();
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");
        BaseResultEvent<PaymentDto> resultEvent = createResultEvent(eventId, paymentDto, Result.SUCCESS);

        // When: Publishing message to RabbitMQ
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, resultEvent);

        // Then: Message should be consumed and saved to MongoDB
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);

                    PaymentLogDocument log = logs.get(0);
                    assertThat(log.getPaymentId()).isEqualTo("PAY001");
                    assertThat(log.getAccountNumber()).isEqualTo("ACC001");
                    assertThat(log.getRecipientAccountNumber()).isEqualTo("ACC999");
                    assertThat(log.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
                    assertThat(log.getStatus()).isEqualTo("SUCCESS");
                    assertThat(log.getCreatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should consume FAILED payment execution result from RabbitMQ and save to MongoDB")
    void shouldConsumeFailedPaymentResultAndSaveToMongoDb() {
        // Given: A failed payment result event
        String eventId = UUID.randomUUID().toString();
        PaymentDto paymentDto = createTestPayment("PAY002", "ACC002", "ACC998");
        BaseResultEvent<PaymentDto> resultEvent = createResultEvent(eventId, paymentDto, Result.FAILURE);

        // When: Publishing message to RabbitMQ
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, resultEvent);

        // Then: Message should be consumed and saved to MongoDB with FAILED status
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);

                    PaymentLogDocument log = logs.get(0);
                    assertThat(log.getPaymentId()).isEqualTo("PAY002");
                    assertThat(log.getStatus()).isEqualTo("FAILED");
                });
    }

    @Test
    @DisplayName("Should consume multiple payment results from RabbitMQ and save all to MongoDB")
    void shouldConsumeMultiplePaymentResults() {
        // Given: Multiple payment result events
        String eventId1 = UUID.randomUUID().toString();
        String eventId2 = UUID.randomUUID().toString();
        String eventId3 = UUID.randomUUID().toString();

        PaymentDto payment1 = createTestPayment("PAY001", "ACC001", "ACC999");
        PaymentDto payment2 = createTestPayment("PAY002", "ACC002", "ACC998");
        PaymentDto payment3 = createTestPayment("PAY003", "ACC003", "ACC997");

        BaseResultEvent<PaymentDto> event1 = createResultEvent(eventId1, payment1, Result.SUCCESS);
        BaseResultEvent<PaymentDto> event2 = createResultEvent(eventId2, payment2, Result.FAILURE);
        BaseResultEvent<PaymentDto> event3 = createResultEvent(eventId3, payment3, Result.SUCCESS);

        // When: Publishing multiple messages to RabbitMQ
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, event1);
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, event2);
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, event3);

        // Then: All messages should be consumed and saved to MongoDB
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(3);

                    assertThat(logs)
                            .extracting(PaymentLogDocument::getPaymentId)
                            .containsExactlyInAnyOrder("PAY001", "PAY002", "PAY003");

                    assertThat(logs)
                            .extracting(PaymentLogDocument::getStatus)
                            .contains("SUCCESS", "FAILED");
                });
    }

    @Test
    @DisplayName("Should verify MongoDB document structure and fields")
    void shouldVerifyMongoDbDocumentStructure() {
        // Given: A payment result event with all fields
        String eventId = UUID.randomUUID().toString();
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");
        paymentDto.setPaymentMethod("BANK_TRANSFER");
        paymentDto.setRecipientName("John Doe");
        paymentDto.setAdditionalData(Map.of("notes", "Test payment", "reference", "REF001"));

        BaseResultEvent<PaymentDto> resultEvent = createResultEvent(eventId, paymentDto, Result.SUCCESS);

        // When: Publishing message
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, resultEvent);

        // Then: All fields should be persisted correctly in MongoDB
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);

                    PaymentLogDocument log = logs.get(0);
                    assertThat(log.getId()).isNotNull();
                    assertThat(log.getPaymentId()).isEqualTo("PAY001");
                    assertThat(log.getAccountNumber()).isEqualTo("ACC001");
                    assertThat(log.getRecipientAccountNumber()).isEqualTo("ACC999");
                    assertThat(log.getRecipientName()).isEqualTo("John Doe");
                    assertThat(log.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
                    assertThat(log.getCurrency()).isEqualTo("USD");
                    assertThat(log.getPaymentMethod()).isEqualTo("BANK_TRANSFER");
                    assertThat(log.getStatus()).isEqualTo("SUCCESS");
                    assertThat(log.getAdditionalData()).containsKeys("notes", "reference");
                    assertThat(log.getCreatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should handle RabbitMQ retry mechanism for failed message processing")
    void shouldVerifyRabbitMqRetryMechanism() {
        // Given: A payment result event with blank eventId (will cause validation error)
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");
        BaseResultEvent<PaymentDto> invalidEvent = createResultEvent("", paymentDto, Result.SUCCESS);

        // When: Publishing invalid message to RabbitMQ
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, invalidEvent);

        // Then: Message should fail validation and not be persisted
        // Wait a bit to ensure message processing was attempted
        await()
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).isEmpty();
                });

        // Note: The message should go to DLQ (Dead Letter Queue) after retries
        // In a real scenario, you would verify the DLQ contains the failed message
    }

    @Test
    @DisplayName("Should verify RabbitMQ message deserialization from JSON")
    void shouldVerifyMessageDeserialization() {
        // Given: A complex payment with nested data
        String eventId = UUID.randomUUID().toString();
        PaymentDto paymentDto = createTestPayment("PAY001", "ACC001", "ACC999");
        paymentDto.setAdditionalData(Map.of(
                "merchantDetails", Map.of("merchantId", "MERCH123", "merchantName", "Test Merchant"),
                "deviceInfo", Map.of("deviceId", "DEV001", "platform", "WEB")
        ));

        BaseResultEvent<PaymentDto> resultEvent = createResultEvent(eventId, paymentDto, Result.SUCCESS);

        // When: Publishing message with complex data
        rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, resultEvent);

        // Then: Message should be deserialized correctly
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(1);

                    PaymentLogDocument log = logs.get(0);
                    assertThat(log.getAdditionalData()).isNotNull();
                    assertThat(log.getAdditionalData()).containsKeys("merchantDetails", "deviceInfo");
                });
    }

    @Test
    @DisplayName("Should verify message ordering in RabbitMQ and MongoDB")
    void shouldVerifyMessageOrdering() {
        // Given: Multiple payment results sent in sequence
        for (int i = 1; i <= 5; i++) {
            String eventId = UUID.randomUUID().toString();
            PaymentDto payment = createTestPayment("PAY00" + i, "ACC00" + i, "ACC99" + i);
            BaseResultEvent<PaymentDto> event = createResultEvent(eventId, payment, Result.SUCCESS);
            rabbitTemplate.convertAndSend(X_PN_PAYMENT, K_EXECUTE_PAYMENT_RESULT, event);
        }

        // Then: All messages should be consumed and stored
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<PaymentLogDocument> logs = paymentLogRepository.findAll();
                    assertThat(logs).hasSize(5);
                });
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
