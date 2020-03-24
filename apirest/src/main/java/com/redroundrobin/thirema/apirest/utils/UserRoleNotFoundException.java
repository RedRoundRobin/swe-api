package com.redroundrobin.thirema.apirest.utils;

public class UserRoleNotFoundException extends Exception {
  public UserRoleNotFoundException(String reason) {
    super(reason);
  }
}
