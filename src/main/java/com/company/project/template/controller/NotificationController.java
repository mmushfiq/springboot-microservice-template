package com.company.project.template.controller;

import com.company.project.common.util.WebUtil;
import com.company.project.template.client.notification.model.PushNotificationRequest;
import com.company.project.template.client.notification.model.SendEmailRequest;
import com.company.project.template.client.notification.model.SendSmsRequest;
import com.company.project.template.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/template-project/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final WebUtil webUtil;

    @PostMapping("/email")
    public ResponseEntity<Void> sendMail(@Valid @RequestBody SendEmailRequest emailRequest) {
        notificationService.sendEmail(emailRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sms")
    public ResponseEntity<Void> sendSms(@Valid @RequestBody SendSmsRequest smsRequest) {
        notificationService.sendSms(smsRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/push")
    public ResponseEntity<Void> sendPushNotification(@Valid @RequestBody PushNotificationRequest pushRequest) {
        notificationService.sendPushNotification(webUtil.getProjectBasedHeaders(), pushRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
