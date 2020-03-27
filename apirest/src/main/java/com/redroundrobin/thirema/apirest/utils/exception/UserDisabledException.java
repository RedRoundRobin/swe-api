package com.redroundrobin.thirema.apirest.utils.exception;

public class UserDisabledException extends Exception {
  public UserDisabledException(String reason) {
    super(reason);
  }
}
