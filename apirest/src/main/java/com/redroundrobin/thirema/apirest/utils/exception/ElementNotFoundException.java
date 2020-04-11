package com.redroundrobin.thirema.apirest.utils.exception;

public class ElementNotFoundException extends Exception {
  public ElementNotFoundException(String message) {
    super(message);
  }

  public static ElementNotFoundException notFoundMessage(String element) {
    return new ElementNotFoundException(element + " with provided id is not found");
  }

  public static ElementNotFoundException notFoundOrNotAuthorizedMessage(String element) {
    return new ElementNotFoundException(element + " with provided id is not found or not "
        + "authorized");
  }
}
