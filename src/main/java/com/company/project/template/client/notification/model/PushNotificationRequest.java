package com.company.project.template.client.notification.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationRequest {

    @NotBlank
    private String identifier;

    @NotBlank
    private String heading;

    @NotBlank
    private String content;

    @NotBlank
    private String deviceType;

    private String action;

    private String deepLink;

    private Map<String, String> data;

}
