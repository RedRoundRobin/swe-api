package com.redroundrobin.thirema.apirest.utils.exception;

/**
 * The UserRoleNotFoundException is used when is tried to convert a not managed User Role contained
 * in the enum models.postgres.User.role.
 */
public class UserRoleNotFoundException extends Exception {
  public UserRoleNotFoundException(String reason) {
    super(reason);
  }
}
