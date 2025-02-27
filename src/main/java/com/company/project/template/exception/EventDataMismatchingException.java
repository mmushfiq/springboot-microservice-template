package com.company.project.template.exception;

import static com.company.project.template.exception.constant.ErrorCode.EVENT_DATA_MISMATCHING;

import lombok.Getter;

@Getter
public class EventDataMismatchingException extends RuntimeException {

    private final String errorCode;
    private final String message;

    public EventDataMismatchingException(String message) {
        this.message = message;
        this.errorCode = EVENT_DATA_MISMATCHING;
    }

}
