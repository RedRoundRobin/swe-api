package com.redroundrobin.thirema.apirest.utils.exception;

/**
 * The TfaNotPermittedException is used when models.postgres.User two factor authentication can't
 * be activated.
 */
public class TfaNotPermittedException extends Exception {
  public TfaNotPermittedException(String reason) {
    super(reason);
  }
}
