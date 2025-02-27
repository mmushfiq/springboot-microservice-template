package com.company.project.template.messaging;

import static com.company.project.template.model.constant.RabbitConstant.Q_EXECUTE_PAYMENT_RESULT;

import com.company.project.common.messaging.BaseResultEvent;
import com.company.project.template.model.dto.PaymentDto;
import com.company.project.template.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = Q_EXECUTE_PAYMENT_RESULT)
    public void receivePaymentExecutionResultEvent(BaseResultEvent<PaymentDto> baseResultEvent) {
        paymentService.processPaymentExecutionResult(baseResultEvent);
    }

}
