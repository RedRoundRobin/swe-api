package com.redroundrobin.thirema.apirest.utils.exception;

import java.lang.Exception;

public class NotAllowedToEditFieldsException extends Exception {
  public NotAllowedToEditFieldsException(String reason) {
    super(reason);
  }
}
