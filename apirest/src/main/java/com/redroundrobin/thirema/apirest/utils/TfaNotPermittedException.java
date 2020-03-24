package com.redroundrobin.thirema.apirest.utils;

public class TfaNotPermittedException extends Exception {
  public TfaNotPermittedException(String reason) {
    super(reason);
  }
}
