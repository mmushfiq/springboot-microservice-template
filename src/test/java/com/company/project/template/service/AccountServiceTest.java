package com.company.project.template.service;

import static com.company.project.common.exception.constant.CommonErrorCode.DATA_NOT_FOUND;
import static com.company.project.template.common.TestConstants.ID;
import static com.company.project.template.common.TestConstants.accountDto;
import static com.company.project.template.common.TestConstants.accountEntity;
import static com.company.project.template.common.TestConstants.createAccountDto;
import static com.company.project.template.common.TestConstants.updateAccountDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import com.company.project.common.exception.DataNotFoundException;
import com.company.project.template.dao.repository.AccountRepository;
import com.company.project.template.model.dto.AccountDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getAll_Accounts_Should_ReturnSuccess() {
        given(accountRepository.findAll()).willReturn(List.of(accountEntity()));

        List<AccountDto> actualDtoList = accountService.getAllAccounts();
        assertNotNull(actualDtoList);
        assertThat(actualDtoList).isEqualTo(List.of(accountDto()));

        then(accountRepository).should().findAll();
    }

    @Test
    void getAccount_Should_ReturnSuccess() {
        given(accountRepository.findById(ID)).willReturn(Optional.of(accountEntity()));

        AccountDto actualDto = accountService.getAccount(ID);
        assertNotNull(actualDto);
        assertThat(actualDto).usingRecursiveComparison().isEqualTo(accountDto());

        then(accountRepository).should().findById(ID);
    }

    @Test
    void getAccount_Should_ThrowDataNotFoundException_When_GivenIdNotExist() {
        given(accountRepository.findById(ID)).willReturn(Optional.empty());

        DataNotFoundException ex = assertThrows(DataNotFoundException.class,
                () -> accountService.getAccount(ID));
        assertThat(ex.getErrorCode()).isEqualTo(DATA_NOT_FOUND);
        assertThat(ex.getMessage()).contains("Account not found with given id 1");

        then(accountRepository).should().findById(ID);
    }

    @Test
    void createAccount_Should_ReturnSuccess() {
        given(accountRepository.save(any())).willReturn(accountEntity());

        AccountDto actualDto = accountService.createAccount(createAccountDto());
        assertNotNull(actualDto);
        assertThat(actualDto).usingRecursiveComparison().isEqualTo(accountDto());

        then(accountRepository).should().save(any());
    }

    @Test
    void updateAccount_Should_ReturnSuccess() {
        given(accountRepository.findById(ID)).willReturn(Optional.of(accountEntity()));
        given(accountRepository.save(any())).willReturn(accountEntity());

        AccountDto actualDto = accountService.updateAccount(updateAccountDto());
        assertNotNull(actualDto);
        assertThat(actualDto).usingRecursiveComparison().isEqualTo(accountDto());

        then(accountRepository).should().save(any());
    }

    @Test
    void delete_Account_Should_ReturnSuccess() {
        willDoNothing().given(accountRepository).deleteById(ID);

        accountService.deleteAccount(ID);

        then(accountRepository).should().deleteById(ID);
    }

}
