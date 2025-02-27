package com.company.project.template.dao.repository.cache;

import com.company.project.template.config.properties.ApplicationProperties;
import com.company.project.template.model.dto.PaymentDto;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentCacheRepository {

    private final RedisTemplate<String, PaymentDto> redisTemplate;
    private final ApplicationProperties props;

    public PaymentCacheRepository(@Qualifier("payment") RedisTemplate<String, PaymentDto> redisTemplate,
                                  ApplicationProperties props) {
        this.redisTemplate = redisTemplate;
        this.props = props;
    }

    public void save(String identifier, PaymentDto paymentDto) {
        String key = key(identifier);
        redisTemplate.opsForValue().set(key, paymentDto, props.getCachePaymentTtl(), TimeUnit.MINUTES);
    }

    public PaymentDto read(@NonNull String identifier) {
        return redisTemplate.opsForValue().get(key(identifier));
    }

    public void delete(String identifier) {
        redisTemplate.delete(key(identifier));
    }

    private String key(String identifier) {
        return props.getCachePaymentPrefix().concat(identifier);
    }

}