package com.redroundrobin.thirema.apirest.utils.exception;

public class NotAuthorizedException extends Exception {
  public NotAuthorizedException(String message) {
    super(message);
  }

  public static NotAuthorizedException notAuthorizedMessage(String element) {
    return new NotAuthorizedException(element + " with provided id is not authorized");
  }
}
