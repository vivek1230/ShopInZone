package com.shopinzone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum AddressType {
    PRIMARY("1", "PRIMARY"), SECONDARY("2", "SECONDARY");

    private final String code;
    private final String value;
}
