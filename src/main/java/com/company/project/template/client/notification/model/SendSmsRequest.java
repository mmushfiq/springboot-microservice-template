package com.company.project.template.client.notification.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendSmsRequest {

    @NotBlank
    private String text;

    @NotBlank
    private String phoneNumber;

}
