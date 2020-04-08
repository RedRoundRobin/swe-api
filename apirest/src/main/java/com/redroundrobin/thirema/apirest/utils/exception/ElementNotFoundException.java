package com.redroundrobin.thirema.apirest.utils.exception;

public class ElementNotFoundException extends Exception {
  public ElementNotFoundException(String message) {
    super(message);
  }

  public static ElementNotFoundException defaultMessage(String element) {
    return new ElementNotFoundException(element + " with provided id is not found");
  }
}
