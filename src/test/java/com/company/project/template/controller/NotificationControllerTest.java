package com.company.project.template.controller;

import static com.company.project.template.common.JsonFiles.SEND_EMAIL_REQUEST;
import static com.company.project.template.common.JsonFiles.SEND_PUSH_REQUEST;
import static com.company.project.template.common.JsonFiles.SEND_SMS_REQUEST;
import static com.company.project.template.common.TestConstants.BASE_PATH;
import static com.company.project.template.common.TestUtil.json;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.project.common.config.OpenTelemetryConfig;
import com.company.project.template.config.CommonLibScannerConfig;
import com.company.project.template.service.NotificationService;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationController.class)
@Import({CommonLibScannerConfig.class})
@MockBean({Tracer.class, OpenTelemetryConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void sendMail_Should_ReturnSuccess() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SEND_EMAIL_REQUEST)))
                .andExpect(status().isOk());

        then(notificationService).should().sendEmail(any());
    }

    @Test
    void sendSms_Should_ReturnSuccess() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/v1/notifications/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SEND_SMS_REQUEST)))
                .andExpect(status().isOk());

        then(notificationService).should().sendSms(any());
    }

    @Test
    void sendPushNotification_Should_ReturnSuccess() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/v1/notifications/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SEND_PUSH_REQUEST)))
                .andExpect(status().isAccepted());

        then(notificationService).should().sendPushNotification(any(), any());
    }

}