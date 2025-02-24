package com.company.project.template.service;

import static com.company.project.common.exception.constant.CommonErrorCode.REQUEST_INVALID;
import static com.company.project.template.exception.constant.ErrorMessage.ACCOUNT_NOT_FOUND_MESSAGE;
import static com.company.project.template.exception.constant.ErrorMessage.REQUIRED_NONNULL_MESSAGE;
import static com.company.project.template.exception.constant.ErrorMessage.REQUIRED_NULL_MESSAGE;

import com.company.project.common.exception.DataNotFoundException;
import com.company.project.common.exception.InvalidInputException;
import com.company.project.template.dao.entity.AccountEntity;
import com.company.project.template.dao.repository.AccountRepository;
import com.company.project.template.mapper.AccountMapper;
import com.company.project.template.model.dto.AccountDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public List<AccountDto> getAllAccounts() {
        List<AccountEntity> accountEntities = accountRepository.findAll();
        return AccountMapper.INSTANCE.toDto(accountEntities);
    }

    public AccountDto getAccount(Long id) {
        AccountEntity accountEntity = findAccountById(id);
        return AccountMapper.INSTANCE.toDto(accountEntity);
    }

    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        if (accountDto.getId() != null) {
            throw InvalidInputException.of(REQUEST_INVALID, REQUIRED_NULL_MESSAGE, "id");
        }

        AccountEntity accountEntity = accountRepository.save(AccountMapper.INSTANCE.toEntity(accountDto));
        log.info("Account created, id: {}", accountEntity.getId());

        return AccountMapper.INSTANCE.toDto(accountEntity);
    }

    @Transactional
    public AccountDto updateAccount(AccountDto accountDto) {
        if (accountDto.getId() == null) {
            throw InvalidInputException.of(REQUEST_INVALID, REQUIRED_NONNULL_MESSAGE, "id");
        }

        AccountEntity accountEntity = findAccountById(accountDto.getId());
        AccountMapper.INSTANCE.updateAccountEntity(accountDto, accountEntity);
        accountEntity = accountRepository.save(accountEntity);
        log.info("Account updated, id: {}", accountEntity.getId());

        return AccountMapper.INSTANCE.toDto(accountEntity);
    }

    @Transactional
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
        log.info("Account deleted, id: {}", id);
    }

    private AccountEntity findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> DataNotFoundException.of(ACCOUNT_NOT_FOUND_MESSAGE, id));
    }

}
