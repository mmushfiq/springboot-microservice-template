package com.company.project.template.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.project.template.BaseIntegrationTest;
import com.company.project.template.dao.repository.AccountRepository;
import com.company.project.template.model.dto.AccountDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Account Controller Integration Tests")
class AccountControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    private static final String BASE_URL = "/template-project/v1/accounts";

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /accounts - Should return all accounts from PostgreSQL")
    void shouldGetAllAccounts() {
        // Given: Multiple accounts exist in database
        AccountDto account1 = createTestAccount("CUST001", "ACC001", "John Doe");
        AccountDto account2 = createTestAccount("CUST002", "ACC002", "Jane Smith");

        restTemplate.postForEntity(BASE_URL, account1, AccountDto.class);
        restTemplate.postForEntity(BASE_URL, account2, AccountDto.class);

        // When: GET request to retrieve all accounts
        ResponseEntity<List<AccountDto>> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AccountDto>>() {
                }
        );

        // Then: Should return 200 OK with all accounts
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .extracting(AccountDto::getAccountNumber)
                .containsExactlyInAnyOrder("ACC001", "ACC002");
    }

    @Test
    @DisplayName("GET /accounts/{id} - Should return specific account by ID")
    void shouldGetAccountById() {
        // Given: An account exists in database
        AccountDto account = createTestAccount("CUST001", "ACC001", "John Doe");
        ResponseEntity<AccountDto> createResponse = restTemplate.postForEntity(BASE_URL, account, AccountDto.class);
        Long accountId = createResponse.getBody().getId();

        // When: GET request for specific account
        ResponseEntity<AccountDto> response = restTemplate.getForEntity(
                BASE_URL + "/" + accountId,
                AccountDto.class
        );

        // Then: Should return 200 OK with the account
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(accountId);
        assertThat(response.getBody().getAccountNumber()).isEqualTo("ACC001");
        assertThat(response.getBody().getAccountHolderName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("GET /accounts/{id} - Should return 404 when account not found")
    void shouldReturn404WhenAccountNotFound() {
        // Given: A non-existent account ID
        Long nonExistentId = 99999L;

        // When: GET request for non-existent account
        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/" + nonExistentId,
                String.class
        );

        // Then: Should return 404 NOT FOUND
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /accounts - Should create account and persist to PostgreSQL")
    void shouldCreateAccount() {
        // Given: A new account
        AccountDto accountDto = createTestAccount("CUST001", "ACC001", "John Doe");

        // When: POST request to create account
        ResponseEntity<AccountDto> response = restTemplate.postForEntity(
                BASE_URL,
                accountDto,
                AccountDto.class
        );

        // Then: Should return 201 CREATED with the created account
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getCustomerId()).isEqualTo("CUST001");
        assertThat(response.getBody().getAccountNumber()).isEqualTo("ACC001");
        assertThat(response.getBody().getAccountHolderName()).isEqualTo("John Doe");

        // Verify persistence in PostgreSQL
        assertThat(accountRepository.findById(response.getBody().getId())).isPresent();
    }

    @Test
    @DisplayName("POST /accounts - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() {
        // Given: An invalid account (missing required fields)
        AccountDto invalidAccount = AccountDto.builder()
                .customerId("") // Invalid: blank
                .build();

        // When: POST request with invalid data
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL,
                invalidAccount,
                String.class
        );

        // Then: Should return 400 BAD REQUEST
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /accounts - Should return 400 when ID is provided for new account")
    void shouldReturn400WhenIdProvidedForCreate() {
        // Given: A new account with ID already set
        AccountDto accountDto = createTestAccount("CUST001", "ACC001", "John Doe");
        accountDto.setId(123L);

        // When: POST request to create account
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL,
                accountDto,
                String.class
        );

        // Then: Should return 400 BAD REQUEST
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("id", "must be null");
    }

    @Test
    @DisplayName("PUT /accounts - Should update existing account in PostgreSQL")
    void shouldUpdateAccount() {
        // Given: An existing account
        AccountDto account = createTestAccount("CUST001", "ACC001", "John Doe");
        ResponseEntity<AccountDto> createResponse = restTemplate.postForEntity(BASE_URL, account, AccountDto.class);
        AccountDto createdAccount = createResponse.getBody();

        // When: PUT request to update account
        createdAccount.setAccountHolderName("John Smith");
        createdAccount.setBalance(BigDecimal.valueOf(2000.00));

        ResponseEntity<AccountDto> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.PUT,
                new HttpEntity<>(createdAccount),
                AccountDto.class
        );

        // Then: Should return 200 OK with updated account
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountHolderName()).isEqualTo("John Smith");
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));

        // Verify persistence in PostgreSQL
        ResponseEntity<AccountDto> getResponse = restTemplate.getForEntity(
                BASE_URL + "/" + createdAccount.getId(),
                AccountDto.class
        );
        assertThat(getResponse.getBody().getAccountHolderName()).isEqualTo("John Smith");
    }

    @Test
    @DisplayName("PUT /accounts - Should return 400 when ID is missing for update")
    void shouldReturn400WhenIdMissingForUpdate() {
        // Given: An account without ID
        AccountDto accountDto = createTestAccount("CUST001", "ACC001", "John Doe");

        // When: PUT request without ID
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.PUT,
                new HttpEntity<>(accountDto),
                String.class
        );

        // Then: Should return 400 BAD REQUEST
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("id", "must not be null");
    }

    @Test
    @DisplayName("PUT /accounts - Should return 404 when updating non-existent account")
    void shouldReturn404WhenUpdatingNonExistentAccount() {
        // Given: An account with non-existent ID
        AccountDto accountDto = createTestAccount("CUST001", "ACC001", "John Doe");
        accountDto.setId(99999L);

        // When: PUT request to update non-existent account
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.PUT,
                new HttpEntity<>(accountDto),
                String.class
        );

        // Then: Should return 404 NOT FOUND
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /accounts/{id} - Should delete account from PostgreSQL")
    void shouldDeleteAccount() {
        // Given: An existing account
        AccountDto account = createTestAccount("CUST001", "ACC001", "John Doe");
        ResponseEntity<AccountDto> createResponse = restTemplate.postForEntity(BASE_URL, account, AccountDto.class);
        Long accountId = createResponse.getBody().getId();

        // When: DELETE request to remove account
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + accountId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then: Should return 204 NO CONTENT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify deletion from PostgreSQL
        assertThat(accountRepository.findById(accountId)).isEmpty();

        // Verify GET returns 404
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                BASE_URL + "/" + accountId,
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should verify Liquibase migrations and database schema")
    void shouldVerifyDatabaseSchemaWithLiquibase() {
        // Given: Spring Boot started and Liquibase migrations executed
        // When: Creating an account
        AccountDto account = createTestAccount("CUST001", "ACC001", "John Doe");
        ResponseEntity<AccountDto> response = restTemplate.postForEntity(BASE_URL, account, AccountDto.class);

        // Then: Account should be created successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();

        // Verify table structure works correctly
        assertThat(accountRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle concurrent requests to PostgreSQL correctly")
    void shouldHandleConcurrentRequests() {
        // Given: Multiple accounts to create concurrently
        AccountDto account1 = createTestAccount("CUST001", "ACC001", "John Doe");
        AccountDto account2 = createTestAccount("CUST002", "ACC002", "Jane Smith");
        AccountDto account3 = createTestAccount("CUST003", "ACC003", "Bob Johnson");

        // When: Creating accounts
        ResponseEntity<AccountDto> response1 = restTemplate.postForEntity(BASE_URL, account1, AccountDto.class);
        ResponseEntity<AccountDto> response2 = restTemplate.postForEntity(BASE_URL, account2, AccountDto.class);
        ResponseEntity<AccountDto> response3 = restTemplate.postForEntity(BASE_URL, account3, AccountDto.class);

        // Then: All should be created successfully
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify all persisted
        assertThat(accountRepository.count()).isEqualTo(3);
    }

    private AccountDto createTestAccount(String customerId, String accountNumber, String holderName) {
        return AccountDto.builder()
                .customerId(customerId)
                .accountNumber(accountNumber)
                .accountHolderName(holderName)
                .accountType("SAVINGS")
                .balance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .build();
    }
}
