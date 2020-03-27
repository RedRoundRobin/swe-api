package com.redroundrobin.thirema.apirest.utils.exception;

import java.lang.Exception;

public class MissingFieldsException extends Exception {
  public MissingFieldsException(String reason) {
    super(reason);
  }
}
