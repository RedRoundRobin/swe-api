package com.redroundrobin.thirema.apirest.utils.exception;

import java.lang.Exception;

public class ViewNotFoundException extends Exception {
  public ViewNotFoundException(String reason) {
    super(reason);
  }
}
