package com.company.project.template.model.dto;

import com.company.project.template.exception.validation.constraint.Currency;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto implements Serializable {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String accountNumber;

    @DecimalMin(value = "1")
    @DecimalMax(value = "1000")
    private BigDecimal amount;

    @Currency
    private String currency;

    private String paymentMethod;

    private String recipientName;

    @NotBlank
    private String recipientAccountNumber;

    private Map<String, Object> additionalData;

}
