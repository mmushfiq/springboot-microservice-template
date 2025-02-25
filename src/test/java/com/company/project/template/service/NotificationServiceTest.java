package com.company.project.template.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

import com.company.project.template.client.notification.NotificationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendEmail_Should_ReturnSuccess() {
        notificationService.sendEmail(any());
        then(notificationClient).should().sendEmail(any());
    }

    @Test
    void sendSms_Should_ReturnSuccess() {
        notificationService.sendSms(any());
        then(notificationClient).should().sendSms(any());
    }

    @Test
    void sendPushNotification_Should_ReturnSuccess() {
        notificationService.sendPushNotification(any(), any());
        then(notificationClient).should().sendPushNotification(any(), any());
    }

}