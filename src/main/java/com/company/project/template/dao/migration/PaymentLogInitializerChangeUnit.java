package com.company.project.template.dao.migration;

import com.company.project.template.dao.document.PaymentLogDocument;
import com.company.project.template.dao.repository.PaymentLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.io.InputStream;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

@ChangeUnit(id = "payment-log-initializer", order = "001", author = "Mushfig Mammadov")
public class PaymentLogInitializerChangeUnit {

    @SneakyThrows
    @Execution
    public void loadPaymentLogs(PaymentLogRepository paymentLogRepository, ObjectMapper objectMapper) {
        InputStream resource = new ClassPathResource("mongock/payment-logs.json").getInputStream();
        PaymentLogDocument[] paymentLogs = objectMapper.readValue(resource, PaymentLogDocument[].class);
        paymentLogRepository.saveAll(Arrays.asList(paymentLogs));
    }

    @RollbackExecution
    public void rollbackLoadPaymentLogs(PaymentLogRepository paymentLogRepository) {
        paymentLogRepository.deleteAll();
    }

}
