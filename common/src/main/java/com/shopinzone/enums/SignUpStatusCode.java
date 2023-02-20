package com.shopinzone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum SignUpStatusCode {
    SIGN_UP_DONE("001", "SignUp is successfully done"), SIGN_UP_FAILED("002", "SignUp is failed"),
    DUPLICATE_EMAIL("003", "User is already registered with mentioned email"),
    DUPLICATE_MOBILE("004", "User is already registered with mentioned mobile");

    private final String code;
    private final String description;
}
