package com.shopinzone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum Gender {
    MALE("1", "MALE"), FEMALE("2", "FEMALE"), OTHER("3", "OTHER"),;

    private final String code;
    private final String value;
}
