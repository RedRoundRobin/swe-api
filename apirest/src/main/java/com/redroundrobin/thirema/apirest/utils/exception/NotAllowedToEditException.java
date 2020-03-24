package com.redroundrobin.thirema.apirest.utils.exception;

import java.lang.Exception;

/**
 * The NotAllowedToEditFieldsException is used when someone is trying to edit one or more fields
 * without permission.
 */
public class NotAllowedToEditException extends Exception {
  public NotAllowedToEditException(String reason) {
    super(reason);
  }
}
