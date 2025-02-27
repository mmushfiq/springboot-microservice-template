package com.company.project.template.controller;

import static com.company.project.template.common.JsonFiles.CREATE_PAYMENT_REQUEST;
import static com.company.project.template.common.TestConstants.BASE_PATH;
import static com.company.project.template.common.TestConstants.paymentDto;
import static com.company.project.template.common.TestConstants.paymentResponseDto;
import static com.company.project.template.common.TestUtil.json;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.project.common.config.OpenTelemetryConfig;
import com.company.project.template.config.CommonLibScannerConfig;
import com.company.project.template.model.dto.PaymentDto;
import com.company.project.template.service.PaymentService;
import io.micrometer.tracing.Tracer;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PaymentController.class)
@Import({CommonLibScannerConfig.class})
@MockBean({Tracer.class, OpenTelemetryConfig.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void createPayment_Should_ReturnSuccess() throws Exception {
        PaymentDto paymentDto = paymentDto();
        given(paymentService.createPayment(paymentDto)).willReturn(paymentResponseDto());

        mockMvc.perform(post(BASE_PATH + "/v1/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(CREATE_PAYMENT_REQUEST)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentExecutionId").isNotEmpty())
                .andExpect(jsonPath("$.expireAt").isNotEmpty());

        then(paymentService).should().createPayment(paymentDto);
    }

    @Test
    void executePayment_Should_ReturnSuccess() throws Exception {
        String paymentExecutionId = UUID.randomUUID().toString();

        mockMvc.perform(post(BASE_PATH + "/v1/payments/execute/{paymentExecutionId}", paymentExecutionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        then(paymentService).should().executePayment(paymentExecutionId);
    }

}