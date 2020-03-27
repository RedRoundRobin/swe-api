package com.redroundrobin.thirema.apirest.utils.exception;

/**
 * The KeysNotFoundException is used when there are keys in a set that doesn't exist in the class
 * is trying to edit.
 */
public class KeysNotFoundException extends Exception {
  public KeysNotFoundException(String reason) {
    super(reason);
  }
}
