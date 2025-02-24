package com.company.project.template.exception.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessage {

    // CHECKSTYLE:OFF

    public static final String REQUIRED_NULL_MESSAGE = "{0} must be null";
    public static final String REQUIRED_NONNULL_MESSAGE = "{0} must not be null";
    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found with given id {0}";

    // CHECKSTYLE:ON

}
