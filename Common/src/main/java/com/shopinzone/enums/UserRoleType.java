package com.shopinzone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum UserRoleType {
    ADMIN("1", "ADMIN"), CUSTOMER("2", "CUSTOMER"), RETAILER("3", "RETAILER");

    private final String code;
    private final String value;
}
