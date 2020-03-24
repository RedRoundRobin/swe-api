package com.redroundrobin.thirema.apirest.utils;

public class EntityNotFoundException extends Exception {
  public EntityNotFoundException(String reason) {
    super(reason);
  }
}
