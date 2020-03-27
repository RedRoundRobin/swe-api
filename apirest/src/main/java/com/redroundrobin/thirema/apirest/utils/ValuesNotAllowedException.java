package com.redroundrobin.thirema.apirest.utils;

import java.lang.Exception;

public class ValuesNotAllowedException extends Exception {
  public ValuesNotAllowedException(String reason) {
    super(reason);
  }
}
