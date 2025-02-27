package com.company.project.template.service;

import static com.company.project.template.model.constant.RabbitConstant.RQI_EXECUTE_PAYMENT_RESULT;

import com.company.project.common.exception.DataNotFoundException;
import com.company.project.common.messaging.BaseEvent;
import com.company.project.common.messaging.BaseResultEvent;
import com.company.project.common.messaging.Result;
import com.company.project.common.util.WebUtil;
import com.company.project.template.config.properties.ApplicationProperties;
import com.company.project.template.dao.document.PaymentLogDocument;
import com.company.project.template.dao.repository.PaymentLogRepository;
import com.company.project.template.dao.repository.cache.PaymentCacheRepository;
import com.company.project.template.exception.EventDataMismatchingException;
import com.company.project.template.mapper.PaymentMapper;
import com.company.project.template.messaging.MessageProducer;
import com.company.project.template.model.dto.PaymentDto;
import com.company.project.template.model.dto.PaymentResponseDto;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final MessageProducer messageProducer;
    private final PaymentCacheRepository paymentCacheRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final ApplicationProperties props;
    private final WebUtil webUtil;

    public PaymentResponseDto createPayment(PaymentDto paymentDto) {
        String paymentExecutionId = UUID.randomUUID().toString();
        paymentCacheRepository.save(paymentExecutionId, paymentDto);

        return PaymentResponseDto.builder()
                .paymentExecutionId(paymentExecutionId)
                .expireAt(LocalDateTime.now().plusMinutes(props.getCachePaymentTtl()))
                .build();
    }

    public void executePayment(String paymentExecutionId) {
        PaymentDto paymentDto = Optional.ofNullable(paymentCacheRepository.read(paymentExecutionId))
                .orElseThrow(() -> DataNotFoundException.of("Payment does not exist or has expired"));

        BaseEvent<PaymentDto> paymentDtoBaseEvent = BaseEvent.<PaymentDto>builder()
                .eventId(paymentExecutionId)
                .headers(webUtil.getProjectBasedHeaders())
                .payload(paymentDto)
                .responseQueueInfo(RQI_EXECUTE_PAYMENT_RESULT)
                .build();
        messageProducer.sendPaymentExecutionEvent(paymentDtoBaseEvent);
        paymentCacheRepository.delete(paymentExecutionId);
        log.info("Payment execution started, paymentExecutionId: {}", paymentExecutionId);
    }

    public void processPaymentExecutionResult(BaseResultEvent<PaymentDto> baseResultEvent) {
        if (StringUtils.isBlank(baseResultEvent.getEventId())) {
            throw new EventDataMismatchingException("eventId can not be blank");
        }

        String status  = baseResultEvent.getResult() == Result.SUCCESS ? "SUCCESS" : "FAILED";
        PaymentLogDocument paymentLogDocument =
                PaymentMapper.INSTANCE.toDocument(baseResultEvent.getPayload(), status);
        paymentLogRepository.insert(paymentLogDocument);
        log.info("Payment execution was completed and data saved to database, paymentExecutionId: {}",
                baseResultEvent.getEventId());
    }

}
