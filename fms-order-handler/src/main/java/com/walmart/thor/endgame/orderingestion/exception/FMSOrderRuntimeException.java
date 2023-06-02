package com.walmart.thor.endgame.orderingestion.exception;

import org.apache.commons.lang3.StringUtils;

public class FMSOrderRuntimeException  extends RuntimeException {

    private final String code;

    public FMSOrderRuntimeException(String message) {
        super(message);
        this.code= StringUtils.EMPTY;

    }

    public FMSOrderRuntimeException(String message, String code) {
        super(message);
        this.code = code;
    }

    public FMSOrderRuntimeException(String message, Throwable e) {
        super(message, e);
        this.code= StringUtils.EMPTY;
    }

    public String getCode() {
        return this.code;
    }
}

