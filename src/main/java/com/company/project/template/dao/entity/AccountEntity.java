package com.company.project.template.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "account")
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AccountEntity extends BaseEntity {

    @NotNull
    @Column(name = "customer_id")
    private String customerId;

    @NotNull
    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @NotNull
    @Column(name = "account_holder_name")
    private String accountHolderName;

    @NotNull
    @Column(name = "account_type")
    private String accountType;

    @NotNull
    @Column(name = "balance")
    private BigDecimal balance;

    @NotNull
    @Column(name = "currency")
    private String currency;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountEntity that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(customerId, that.customerId)
                && Objects.equals(accountNumber, that.accountNumber)
                && Objects.equals(accountHolderName, that.accountHolderName)
                && Objects.equals(accountType, that.accountType)
                && Objects.equals(balance, that.balance)
                && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), customerId, accountNumber, accountHolderName, accountType, balance,
                currency);
    }

}
