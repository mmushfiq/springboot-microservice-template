package com.company.project.template.client.notification;

import com.company.project.common.config.FeignConfig;
import com.company.project.template.client.notification.model.PushNotificationRequest;
import com.company.project.template.client.notification.model.SendEmailRequest;
import com.company.project.template.client.notification.model.SendSmsRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-notification", url = "${application.client.notification.url}/notifications",
        configuration = FeignConfig.class)
@Validated
public interface NotificationClient {

    @PostMapping("/email")
    void sendEmail(@Valid @RequestBody SendEmailRequest emailRequest);

    @PostMapping("/sms")
    void sendSms(@Valid @RequestBody SendSmsRequest smsRequest);

    @PostMapping("/push")
    void sendPushNotification(@RequestHeader Map<String, String> headers,
                              @Valid @RequestBody PushNotificationRequest pushNotificationRequest);

}
