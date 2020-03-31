package com.redroundrobin.thirema.apirest.utils.exception;

import java.lang.Exception;

/**
 * The MissingFieldsException is used when there aren't all necessary keys to complete the
 * operation. The operation could be edit or create of elements;
 */
public class MissingFieldsException extends Exception {
  public MissingFieldsException(String reason) {
    super(reason);
  }
}
