package com.company.project.template.service;

import com.company.project.template.client.notification.NotificationClient;
import com.company.project.template.client.notification.model.PushNotificationRequest;
import com.company.project.template.client.notification.model.SendEmailRequest;
import com.company.project.template.client.notification.model.SendSmsRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationClient notificationClient;

    public void sendEmail(SendEmailRequest emailRequest) {
        notificationClient.sendEmail(emailRequest);
    }

    public void sendSms(SendSmsRequest smsRequest) {
        notificationClient.sendSms(smsRequest);
    }

    @Async
    public void sendPushNotification(Map<String, String> projectBasedHeaders,
                                     PushNotificationRequest pushNotificationRequest) {
        notificationClient.sendPushNotification(projectBasedHeaders, pushNotificationRequest);
    }

}
