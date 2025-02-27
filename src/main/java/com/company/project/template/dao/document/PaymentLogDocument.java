package com.company.project.template.dao.document;

import com.company.project.template.model.constant.CollectionName;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"paymentId"})
@Getter
@Setter
@Document(CollectionName.PAYMENT_LOG)
public class PaymentLogDocument {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String paymentId;

    @NotBlank
    private String accountNumber;

    private BigDecimal amount;

    private String currency;

    private String paymentMethod;

    private String recipientName;

    @NotBlank
    private String recipientAccountNumber;

    private Map<String, Object> additionalData;

    private String status;

    @CreatedDate
    private LocalDateTime createdAt;

}
