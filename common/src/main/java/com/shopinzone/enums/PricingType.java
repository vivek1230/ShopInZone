package com.shopinzone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum PricingType {
    PIECE("1", "PIECE"), WEIGHT("2", "WEIGHT");

    private final String code;
    private final String value;
}
