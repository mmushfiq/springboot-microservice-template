package com.company.project.template.config;

import static com.company.project.template.model.constant.RabbitConstant.DLQ_EXECUTE_PAYMENT_RESULT;
import static com.company.project.template.model.constant.RabbitConstant.DLX_PN;
import static com.company.project.template.model.constant.RabbitConstant.K_EXECUTE_PAYMENT_RESULT;
import static com.company.project.template.model.constant.RabbitConstant.Q_EXECUTE_PAYMENT_RESULT;
import static com.company.project.template.model.constant.RabbitConstant.X_PN_PAYMENT;
import static org.springframework.amqp.core.BindingBuilder.bind;

import com.company.project.common.messaging.RetryLogic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;

@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq.listener.simple.retry")
@Getter
@Setter
public class RabbitConfig {

    private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    private int maxAttempts;
    private List<String> nonRetryableExceptions;

    @Bean
    public Queue executePaymentResultQueue() {
        return QueueBuilder.durable(Q_EXECUTE_PAYMENT_RESULT)
                .withArgument(X_DEAD_LETTER_EXCHANGE, DLX_PN)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, K_EXECUTE_PAYMENT_RESULT)
                .build();
    }

    @Bean
    public Queue executePaymentResultDeadLetterQueue() {
        return new Queue(DLQ_EXECUTE_PAYMENT_RESULT);
    }

    @Bean
    public DirectExchange pnDeadLetterExchange() {
        return new DirectExchange(DLX_PN);
    }

    @Bean
    public DirectExchange pnPaymentExchange() {
        return new DirectExchange(X_PN_PAYMENT);
    }

    @Bean
    public Declarables bindings() {
        return new Declarables(
                bind(executePaymentResultQueue())
                        .to(pnPaymentExchange()).with(K_EXECUTE_PAYMENT_RESULT),
                bind(executePaymentResultDeadLetterQueue())
                        .to(pnDeadLetterExchange()).with(K_EXECUTE_PAYMENT_RESULT)
        );
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setObservationEnabled(true);
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitRetryTemplateCustomizer rabbitRetryTemplateCustomizer() {
        var allNonRetryableExceptions = RetryLogic.addAndGetNonRetryableExceptions(nonRetryableExceptions);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, allNonRetryableExceptions, true, true);
        return (target, retryTemplate) -> retryTemplate.setRetryPolicy(retryPolicy);
    }

}
