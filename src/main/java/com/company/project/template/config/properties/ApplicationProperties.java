package com.company.project.template.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class ApplicationProperties {

    @Value("${application.cache.prefix.payment}")
    private String cachePaymentPrefix;

    @Value("${application.cache.ttl.payment}")
    private long cachePaymentTtl;

}
