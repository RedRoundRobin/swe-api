package com.redroundrobin.thirema.apirest.utils;
import java.lang.Exception;

public class NotAllowedToEditFields extends Exception {
  public NotAllowedToEditFields(String reason) {
    super(reason);
  }
}
