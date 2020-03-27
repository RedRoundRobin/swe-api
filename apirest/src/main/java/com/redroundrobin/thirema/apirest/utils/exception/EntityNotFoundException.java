package com.redroundrobin.thirema.apirest.utils.exception;

/**
 * The EntityNotFoundException is used when the entity with entityId furnished doesn't exist.
 */
public class EntityNotFoundException extends Exception {
  public EntityNotFoundException(String reason) {
    super(reason);
  }
}
