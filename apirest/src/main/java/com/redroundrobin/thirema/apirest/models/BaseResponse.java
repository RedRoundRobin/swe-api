package com.redroundrobin.thirema.apirest.models;

public class BaseResponse {

  private final Object data;

  public BaseResponse(Object data) {
    this.data = data;
  }

  public Object getData() {
    return data;
  }
}
