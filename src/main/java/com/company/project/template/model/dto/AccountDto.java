package com.company.project.template.model.dto;

import com.company.project.template.exception.validation.constraint.Currency;
import com.company.project.template.exception.validation.constraint.HolderName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private Long id;

    @NotBlank
    private String customerId;

    @NotBlank
    private String accountNumber;

    @NotBlank
    @HolderName
    private String accountHolderName;

    @NotBlank
    private String accountType;

    @NotNull
    private BigDecimal balance;

    @Currency
    private String currency;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountDto that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(customerId, that.customerId)
                && Objects.equals(accountNumber, that.accountNumber)
                && Objects.equals(accountHolderName, that.accountHolderName)
                && Objects.equals(accountType, that.accountType)
                && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, accountNumber, accountHolderName, accountType, currency);
    }

}
