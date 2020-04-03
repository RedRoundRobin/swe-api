package com.redroundrobin.thirema.apirest.utils.exception;

public class ValuesNotAllowedException extends Exception {
  public ValuesNotAllowedException(String reason) {
    super(reason);
  }
  public ValuesNotAllowedException() {
  }
}
