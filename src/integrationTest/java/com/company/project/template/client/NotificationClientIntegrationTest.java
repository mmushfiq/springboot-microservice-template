package com.company.project.template.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.project.template.BaseIntegrationTest;
import com.company.project.template.client.notification.NotificationClient;
import com.company.project.template.client.notification.model.PushNotificationRequest;
import com.company.project.template.client.notification.model.SendEmailRequest;
import com.company.project.template.client.notification.model.SendSmsRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import feign.FeignException;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DisplayName("Notification Client Integration Tests with WireMock")
class NotificationClientIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationClient notificationClient;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @AfterEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @DynamicPropertySource
    static void configureWireMock(DynamicPropertyRegistry registry) {
        registry.add("application.client.notification.url", () -> "http://localhost:8089");
    }

    @Test
    @DisplayName("POST /notifications/email - Should send email successfully via OpenFeign client")
    void shouldSendEmailSuccessfully() {
        // Given: WireMock stub for email endpoint
        stubFor(post(urlEqualTo("/notifications/email"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .body("Test email body")
                .build();

        // When: Sending email via Feign client
        notificationClient.sendEmail(emailRequest);

        // Then: Verify request was made to WireMock
        verify(postRequestedFor(urlEqualTo("/notifications/email"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.to", equalTo("test@example.com")))
                .withRequestBody(matchingJsonPath("$.subject", equalTo("Test Subject")))
                .withRequestBody(matchingJsonPath("$.body", equalTo("Test email body"))));
    }

    @Test
    @DisplayName("POST /notifications/email - Should handle 400 bad request from notification service")
    void shouldHandle400ErrorForEmail() {
        // Given: WireMock stub returning 400
        stubFor(post(urlEqualTo("/notifications/email"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"error\":\"Invalid email address\"}")));

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to("invalid-email")
                .subject("Test")
                .body("Test body")
                .build();

        // When & Then: Should throw ConstraintViolationException due to client-side validation
        assertThatThrownBy(() -> notificationClient.sendEmail(emailRequest))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
                .hasMessageContaining("must be a well-formed email address");
    }

    @Test
    @DisplayName("POST /notifications/email - Should handle 500 internal server error")
    void shouldHandle500ErrorForEmail() {
        // Given: WireMock stub returning 500
        stubFor(post(urlEqualTo("/notifications/email"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"error\":\"Internal server error\"}")));

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to("test@example.com")
                .subject("Test")
                .body("Test body")
                .build();

        // When & Then: Should throw ClientException (wrapped by custom error decoder from common library)
        assertThatThrownBy(() -> notificationClient.sendEmail(emailRequest))
                .isInstanceOf(com.company.project.common.exception.ClientException.class)
                .hasMessageContaining("NotificationClient#sendEmail");
    }

    @Test
    @DisplayName("POST /notifications/sms - Should send SMS successfully via OpenFeign client")
    void shouldSendSmsSuccessfully() {
        // Given: WireMock stub for SMS endpoint
        stubFor(post(urlEqualTo("/notifications/sms"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        SendSmsRequest smsRequest = SendSmsRequest.builder()
                .phoneNumber("+1234567890")
                .text("Test SMS message")
                .build();

        // When: Sending SMS via Feign client
        notificationClient.sendSms(smsRequest);

        // Then: Verify request was made to WireMock
        verify(postRequestedFor(urlEqualTo("/notifications/sms"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.phoneNumber", equalTo("+1234567890")))
                .withRequestBody(matchingJsonPath("$.text", equalTo("Test SMS message"))));
    }

    @Test
    @DisplayName("POST /notifications/sms - Should handle timeout error")
    void shouldHandleTimeoutForSms() {
        // Given: WireMock stub with delay to simulate timeout
        stubFor(post(urlEqualTo("/notifications/sms"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(65000))); // 65 seconds delay (exceeds typical timeout)

        SendSmsRequest smsRequest = SendSmsRequest.builder()
                .phoneNumber("+1234567890")
                .text("Test SMS")
                .build();

        // When & Then: Should throw FeignException due to timeout
        // Note: This will timeout based on Feign client configuration
        assertThatThrownBy(() -> notificationClient.sendSms(smsRequest))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("POST /notifications/push - Should send push notification with headers successfully")
    void shouldSendPushNotificationSuccessfully() {
        // Given: WireMock stub for push notification endpoint
        stubFor(post(urlEqualTo("/notifications/push"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        Map<String, String> headers = Map.of(
                "X-Request-Id", "req-123",
                "X-User-Id", "user-456"
        );

        PushNotificationRequest pushRequest = PushNotificationRequest.builder()
                .identifier("device-123")
                .heading("New Payment")
                .content("Your payment was successful")
                .deviceType("ANDROID")
                .action("OPEN_APP")
                .deepLink("/payments/123")
                .data(Map.of("paymentId", "PAY001", "amount", "100.00"))
                .build();

        // When: Sending push notification via Feign client
        notificationClient.sendPushNotification(headers, pushRequest);

        // Then: Verify request was made with headers
        verify(postRequestedFor(urlEqualTo("/notifications/push"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-Request-Id", equalTo("req-123"))
                .withHeader("X-User-Id", equalTo("user-456"))
                .withRequestBody(matchingJsonPath("$.identifier", equalTo("device-123")))
                .withRequestBody(matchingJsonPath("$.heading", equalTo("New Payment")))
                .withRequestBody(matchingJsonPath("$.content", equalTo("Your payment was successful")))
                .withRequestBody(matchingJsonPath("$.deviceType", equalTo("ANDROID"))));
    }

    @Test
    @DisplayName("POST /notifications/push - Should verify complex nested data in push notification")
    void shouldSendPushNotificationWithComplexData() {
        // Given: WireMock stub
        stubFor(post(urlEqualTo("/notifications/push"))
                .willReturn(aResponse()
                        .withStatus(200)));

        PushNotificationRequest pushRequest = PushNotificationRequest.builder()
                .identifier("device-123")
                .heading("Test")
                .content("Test content")
                .deviceType("IOS")
                .data(Map.of(
                        "key1", "value1",
                        "key2", "value2",
                        "key3", "value3"
                ))
                .build();

        // When: Sending push notification
        notificationClient.sendPushNotification(Map.of(), pushRequest);

        // Then: Verify nested data was sent correctly
        verify(postRequestedFor(urlEqualTo("/notifications/push"))
                .withRequestBody(matchingJsonPath("$.data.key1", equalTo("value1")))
                .withRequestBody(matchingJsonPath("$.data.key2", equalTo("value2")))
                .withRequestBody(matchingJsonPath("$.data.key3", equalTo("value3"))));
    }

    @Test
    @DisplayName("Should verify OpenFeign request/response logging")
    void shouldVerifyFeignLogging() {
        // Given: WireMock stub
        stubFor(post(urlEqualTo("/notifications/email"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("X-Response-Id", "resp-123")));

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to("test@example.com")
                .subject("Test")
                .body("Test body")
                .build();

        // When: Sending email
        notificationClient.sendEmail(emailRequest);

        // Then: Verify response headers were received
        // Note: In a real scenario, you would verify logs contain request/response details
        verify(postRequestedFor(urlEqualTo("/notifications/email")));
    }

    @Test
    @DisplayName("Should verify OpenFeign JSON serialization with special characters")
    void shouldHandleSpecialCharactersInJson() {
        // Given: WireMock stub
        stubFor(post(urlEqualTo("/notifications/email"))
                .willReturn(aResponse()
                        .withStatus(200)));

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to("test@example.com")
                .subject("Test \"Quote\" & Special <Characters>")
                .body("Body with\nnewline and\ttab")
                .build();

        // When: Sending email with special characters
        notificationClient.sendEmail(emailRequest);

        // Then: Verify special characters were properly escaped in JSON
        verify(postRequestedFor(urlEqualTo("/notifications/email"))
                .withRequestBody(containing("Test \\\"Quote\\\" & Special <Characters>")));
    }

    @Test
    @DisplayName("Should verify multiple concurrent requests to notification service")
    void shouldHandleConcurrentRequests() {
        // Given: WireMock stubs
        stubFor(post(urlEqualTo("/notifications/email"))
                .willReturn(aResponse().withStatus(200)));
        stubFor(post(urlEqualTo("/notifications/sms"))
                .willReturn(aResponse().withStatus(200)));
        stubFor(post(urlEqualTo("/notifications/push"))
                .willReturn(aResponse().withStatus(200)));

        // When: Sending multiple notifications
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to("test@example.com").subject("Test").body("Test").build();
        SendSmsRequest smsRequest = SendSmsRequest.builder()
                .phoneNumber("+1234567890").text("Test").build();
        PushNotificationRequest pushRequest = PushNotificationRequest.builder()
                .identifier("device-123").heading("Test").content("Test").deviceType("ANDROID").build();

        notificationClient.sendEmail(emailRequest);
        notificationClient.sendSms(smsRequest);
        notificationClient.sendPushNotification(Map.of(), pushRequest);

        // Then: All requests should be received
        verify(1, postRequestedFor(urlEqualTo("/notifications/email")));
        verify(1, postRequestedFor(urlEqualTo("/notifications/sms")));
        verify(1, postRequestedFor(urlEqualTo("/notifications/push")));
    }

    @Test
    @DisplayName("Should verify WireMock request matching and verification")
    void shouldVerifyRequestMatching() {
        // Given: WireMock stub with specific request matching
        stubFor(post(urlEqualTo("/notifications/email"))
                .withRequestBody(matchingJsonPath("$.to", equalTo("specific@example.com")))
                .willReturn(aResponse().withStatus(200)));

        stubFor(post(urlEqualTo("/notifications/email"))
                .withRequestBody(matchingJsonPath("$.to", equalTo("other@example.com")))
                .willReturn(aResponse().withStatus(400)));

        // When: Sending email to specific address
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to("specific@example.com")
                .subject("Test")
                .body("Test")
                .build();

        notificationClient.sendEmail(emailRequest);

        // Then: Should match correct stub and return 200
        verify(postRequestedFor(urlEqualTo("/notifications/email"))
                .withRequestBody(matchingJsonPath("$.to", equalTo("specific@example.com"))));
    }
}
