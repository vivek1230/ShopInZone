package com.shopinzone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Slf4j
public enum PaymentOptionType {
    CASH("1", "CASH"), WALLET("2", "WALLET"), UPI("3", "UPI"), DEBIT_CARD("4", "DEBIT_CARD"),
    CREDIT_CARD("5", "CREDIT_CARD");

    private final String code;
    private final String value;
}
