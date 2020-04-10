package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import javax.servlet.http.HttpServletRequest;

/*
 * le risorse sono identificabili tramite url
 * le operazioni devono essere implementati tramite metodi appropiati
 * la rappresentazione delle risorse devono essere tramite un formato standard, specificato nel body
 */
public abstract class CoreController {

  protected JwtUtil jwtUtil;

  protected LogService logService;

  protected UserService userService;

  public CoreController(JwtUtil jwtUtil, LogService logService, UserService userService) {
    this.jwtUtil = jwtUtil;
    this.logService = logService;
    this.userService = userService;
  }

  protected final User getUserFromAuthorization(String authorization) {
    String token = authorization.substring(7);
    String username = jwtUtil.extractUsername(token);

    User user;
    if (jwtUtil.extractType(token).equals("telegram")) {
      user = userService.findByTelegramName(username);
    } else {
      user = userService.findByEmail(username);
    }

    return user;
  }

  protected final String getIpAddress(HttpServletRequest http) {
    if (http.getHeader("X-Forwarded-For") == null) {
      return http.getHeader("X-Forwarded-For");
    } else {
      return  http.getRemoteAddr();
    }
  }
}
