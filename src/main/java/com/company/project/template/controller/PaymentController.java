package com.company.project.template.controller;

import com.company.project.template.model.dto.PaymentDto;
import com.company.project.template.model.dto.PaymentResponseDto;
import com.company.project.template.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/template-project/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody PaymentDto paymentDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.createPayment(paymentDto));
    }

    @PostMapping("/execute/{paymentExecutionId}")
    public ResponseEntity<Void> executePayment(@NotBlank @PathVariable String paymentExecutionId) {
        paymentService.executePayment(paymentExecutionId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
