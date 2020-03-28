package com.redroundrobin.thirema.apirest.utils.exception;
import java.lang.Exception;

public class UserNotFoundException extends Exception {
  public UserNotFoundException(String reason) {
    super(reason);
  }
}
