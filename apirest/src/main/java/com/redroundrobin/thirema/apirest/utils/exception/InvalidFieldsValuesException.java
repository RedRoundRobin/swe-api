package com.redroundrobin.thirema.apirest.utils.exception;

/**
 * The InvalidFieldsException is used when there are field values that are not valid.
 */
public class InvalidFieldsValuesException extends Exception {
  public InvalidFieldsValuesException(String reason) {
    super(reason);
  }
}