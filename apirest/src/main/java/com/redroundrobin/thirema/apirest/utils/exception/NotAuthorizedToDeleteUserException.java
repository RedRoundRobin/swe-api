package com.redroundrobin.thirema.apirest.utils.exception;

import java.lang.Exception;

public class NotAuthorizedToDeleteUserException extends Exception {
  public NotAuthorizedToDeleteUserException(String reason) {
    super(reason);
  }
}

