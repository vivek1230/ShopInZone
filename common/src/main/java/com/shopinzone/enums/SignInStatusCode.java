package com.shopinzone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum SignInStatusCode {
    AUTHENTICATION_DONE("001", "User authentication is successfully done"),
    AUTHENTICATION_FAILED("002", "User authentication is failed"),
    NOT_REGISTERED_WITH_EMAIL("003", "User is not registered with given email yet"),
    NOT_REGISTERED_WITH_MOBILE("004", "User is not registered with given mobile yet");

    private final String code;
    private final String description;
}
