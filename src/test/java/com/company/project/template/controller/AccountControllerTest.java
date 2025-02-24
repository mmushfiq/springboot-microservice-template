package com.company.project.template.controller;

import static com.company.project.template.common.JsonFiles.CREATE_ACCOUNT_REQUEST;
import static com.company.project.template.common.JsonFiles.CREATE_ACCOUNT_RESPONSE;
import static com.company.project.template.common.JsonFiles.GET_ACCOUNT_RESPONSE;
import static com.company.project.template.common.JsonFiles.GET_ALL_ACCOUNTS_RESPONSE;
import static com.company.project.template.common.JsonFiles.UPDATE_ACCOUNT_REQUEST;
import static com.company.project.template.common.JsonFiles.UPDATE_ACCOUNT_RESPONSE;
import static com.company.project.template.common.TestConstants.BASE_PATH;
import static com.company.project.template.common.TestConstants.ID;
import static com.company.project.template.common.TestConstants.accountDto;
import static com.company.project.template.common.TestConstants.createAccountDto;
import static com.company.project.template.common.TestConstants.createAccountDtoWithWrongHolderName;
import static com.company.project.template.common.TestConstants.updateAccountDto;
import static com.company.project.template.common.TestUtil.json;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.project.common.config.OpenTelemetryConfig;
import com.company.project.template.config.CommonLibScannerConfig;
import com.company.project.template.exception.constant.ErrorCode;
import com.company.project.template.model.dto.AccountDto;
import com.company.project.template.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AccountController.class)
@Import({CommonLibScannerConfig.class})
@MockBean({Tracer.class, OpenTelemetryConfig.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void getAllAccounts_Should_ReturnSuccess() throws Exception {
        given(accountService.getAllAccounts()).willReturn(List.of(accountDto()));

        String expectedResult = json(GET_ALL_ACCOUNTS_RESPONSE);
        mockMvc.perform(get(BASE_PATH + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResult));

        then(accountService).should().getAllAccounts();
    }

    @Test
    void getAccount_Should_ReturnSuccess() throws Exception {
        given(accountService.getAccount(ID)).willReturn(accountDto());

        String expectedResult = json(GET_ACCOUNT_RESPONSE);
        mockMvc.perform(get(BASE_PATH + "/accounts/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResult));

        then(accountService).should().getAccount(ID);
    }

    @Test
    void createAccount_Should_ReturnSuccess() throws Exception {
        given(accountService.createAccount(createAccountDto())).willReturn(accountDto());

        String expectedResult = json(CREATE_ACCOUNT_RESPONSE);
        mockMvc.perform(post(BASE_PATH + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(CREATE_ACCOUNT_REQUEST)))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedResult));

        then(accountService).should().createAccount(any());
    }

    @Test
    void createAccount_Should_ThrowMethodArgumentNotValidException_When_HolderNameIsNotValid() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createAccountDtoWithWrongHolderName())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCode.HOLDER_NAME_INVALID)))
                .andExpect(jsonPath("$.message", is("accountHolderName should be consisted of only letters "
                        + "and allowed length is {min=5, max=25}")));
    }

    @Test
    void updateAccount_Should_ReturnSuccess() throws Exception {
        AccountDto accountDto = updateAccountDto();
        given(accountService.updateAccount(accountDto)).willReturn(accountDto);

        String expectedResult = json(UPDATE_ACCOUNT_RESPONSE);
        mockMvc.perform(put(BASE_PATH + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(UPDATE_ACCOUNT_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(content().json(expectedResult));

        then(accountService).should().updateAccount(accountDto);
    }

    @Test
    void deleteAccount_Should_ReturnSuccess() throws Exception {
        willDoNothing().given(accountService).deleteAccount(ID);

        mockMvc.perform(delete(BASE_PATH + "/accounts/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        then(accountService).should().deleteAccount(ID);
    }

}
