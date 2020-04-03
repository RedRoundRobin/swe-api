package com.redroundrobin.thirema.apirest.utils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

final class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
  // holds custom header and value mapping
  private final Map<String, String> customHeaders;

  public CustomHttpServletRequestWrapper(HttpServletRequest request) {
    super(request);
    this.customHeaders = new HashMap<>();
  }

  public void putHeader(String name, String value) {
    this.customHeaders.put(name, value);
  }

  @Override
  public String getHeader(String name) {
    String headerValue = super.getHeader(name);
    if (customHeaders.containsKey(name)) {
      headerValue = customHeaders.get(name);
    }
    return headerValue;
  }

  /**
   * get the Header names.
   */
  @Override
  public Enumeration<String> getHeaderNames() {
    List<String> names = Collections.list(super.getHeaderNames());
    for (String name : customHeaders.keySet()) {
      names.add(name);
    }
    return Collections.enumeration(names);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    List<String> values = Collections.list(super.getHeaders(name));
    if (customHeaders.containsKey(name)) {
      values.add(customHeaders.get(name));
    }
    return Collections.enumeration(values);
  }
}