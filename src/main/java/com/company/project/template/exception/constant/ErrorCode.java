package com.company.project.template.exception.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorCode {

    public static final String HOLDER_NAME_INVALID = "holder_name_invalid";
    public static final String EVENT_DATA_MISMATCHING = "event_data_mismatching";

}
