package com.company.project.template.config;

import com.company.project.template.model.dto.PaymentDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean("payment")
    public RedisTemplate<String, PaymentDto> paymentRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, PaymentDto> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(PaymentDto.class));
        template.setConnectionFactory(connectionFactory);
        return template;
    }

}
