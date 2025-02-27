package com.company.project.template.messaging;

import static com.company.project.template.model.constant.RabbitConstant.K_EXECUTE_PAYMENT;
import static com.company.project.template.model.constant.RabbitConstant.X_PN_PAYMENT;

import com.company.project.common.messaging.BaseEvent;
import com.company.project.template.model.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendPaymentExecutionEvent(BaseEvent<PaymentDto> baseEvent) {
        publish(X_PN_PAYMENT, K_EXECUTE_PAYMENT, baseEvent);
    }

    private <T> void publish(String exchange, String routingKey, T event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

}
