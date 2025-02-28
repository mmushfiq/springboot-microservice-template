package com.company.project.template.scheduler;

import com.company.project.template.service.PaymentService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationScheduler {

    private final PaymentService paymentService;

    @Scheduled(cron = "${application.scheduler.payment-report-cron}")
    @SchedulerLock(name = "ApplicationScheduler.processHourlyPaymentReport")
    public void processHourlyPaymentReport() {
        log.trace("ApplicationScheduler.processHourlyPaymentReport scheduler started in time {}", LocalDateTime.now());

        paymentService.sendHourlyPaymentReport();

        log.trace("ApplicationScheduler.processHourlyPaymentReport scheduler ended in time {}", LocalDateTime.now());
    }

}
