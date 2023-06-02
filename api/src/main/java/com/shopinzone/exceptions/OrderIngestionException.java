package com.shopinzone.exceptions;

public class OrderIngestionException extends Exception {

  private String code;

  public OrderIngestionException(String message) {
    super(message);
  }

  public OrderIngestionException(String message, String code) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return this.code;
  }
}
