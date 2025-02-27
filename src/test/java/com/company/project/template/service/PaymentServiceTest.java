package com.company.project.template.service;

import static com.company.project.common.exception.constant.CommonErrorCode.DATA_NOT_FOUND;
import static com.company.project.common.model.constant.CommonConstants.HttpHeader.PN_CUSTOMER_ID;
import static com.company.project.template.common.TestConstants.paymentDto;
import static com.company.project.template.common.TestConstants.paymentDtoBaseResultEvent;
import static com.company.project.template.exception.constant.ErrorCode.EVENT_DATA_MISMATCHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.company.project.common.exception.DataNotFoundException;
import com.company.project.common.messaging.BaseResultEvent;
import com.company.project.common.util.WebUtil;
import com.company.project.template.config.properties.ApplicationProperties;
import com.company.project.template.dao.document.PaymentLogDocument;
import com.company.project.template.dao.repository.PaymentLogRepository;
import com.company.project.template.dao.repository.cache.PaymentCacheRepository;
import com.company.project.template.exception.EventDataMismatchingException;
import com.company.project.template.messaging.MessageProducer;
import com.company.project.template.model.dto.PaymentDto;
import com.company.project.template.model.dto.PaymentResponseDto;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private PaymentCacheRepository paymentCacheRepository;

    @Mock
    private PaymentLogRepository paymentLogRepository;

    @Mock
    private ApplicationProperties props;

    @Mock
    private WebUtil webUtil;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_Should_ReturnSuccess() {
        PaymentDto paymentDto = paymentDto();
        given(props.getCachePaymentTtl()).willReturn(5L);

        PaymentResponseDto actualDto = paymentService.createPayment(paymentDto);
        assertNotNull(actualDto);
        assertThat(actualDto.getExpireAt()).isAfter(LocalDateTime.now().plusMinutes(4));
        assertThat(actualDto.getExpireAt()).isBeforeOrEqualTo(LocalDateTime.now().plusMinutes(5));
        assertThatCode(() -> UUID.fromString(actualDto.getPaymentExecutionId()))
                .doesNotThrowAnyException();

        then(paymentCacheRepository).should().save(anyString(), eq(paymentDto));
    }

    @Test
    void executePayment_Should_ReturnSuccess() {
        String paymentExecutionId = UUID.randomUUID().toString();
        given(paymentCacheRepository.read(paymentExecutionId)).willReturn(paymentDto());
        given(webUtil.getProjectBasedHeaders()).willReturn(Map.of(PN_CUSTOMER_ID, "111"));

        paymentService.executePayment(paymentExecutionId);

        then(paymentCacheRepository).should().read(paymentExecutionId);
        then(messageProducer).should().sendPaymentExecutionEvent(any());
        then(paymentCacheRepository).should().delete(paymentExecutionId);
    }

    @Test
    void executePayment_Should_ThrowDataNotFoundException_When_GivenIdNotExistOrExpired() {
        String paymentExecutionId = UUID.randomUUID().toString();

        DataNotFoundException ex = assertThrows(DataNotFoundException.class,
                () -> paymentService.executePayment(paymentExecutionId));
        assertThat(ex.getErrorCode()).isEqualTo(DATA_NOT_FOUND);
        assertThat(ex.getMessage()).contains("Payment does not exist or has expired");

        then(paymentCacheRepository).should().read(paymentExecutionId);
        then(messageProducer).should(never()).sendPaymentExecutionEvent(any());
        then(paymentCacheRepository).should(never()).delete(any());
    }

    @Test
    void processPaymentExecutionResult_Should_ReturnSuccess() {
        paymentService.processPaymentExecutionResult(paymentDtoBaseResultEvent());
        then(paymentLogRepository).should().insert(any(PaymentLogDocument.class));
    }

    @Test
    void processPaymentExecutionResult_Should_ThrowEventDataMismatchingException_When_EventIdIsBlank() {
        BaseResultEvent<PaymentDto> baseResultEvent = paymentDtoBaseResultEvent();
        baseResultEvent.setEventId(null);

        EventDataMismatchingException ex = assertThrows(EventDataMismatchingException.class,
                () -> paymentService.processPaymentExecutionResult(baseResultEvent));
        assertThat(ex.getErrorCode()).isEqualTo(EVENT_DATA_MISMATCHING);
        assertThat(ex.getMessage()).contains("eventId can not be blank");

        then(paymentLogRepository).should(never()).insert(any(PaymentLogDocument.class));
    }

}