package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class CoreController {

  protected JwtUtil jwtUtil;

  protected UserService userService;

  @Autowired
  public void setJwtUtil(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  protected User getUserFromAuthorization(String authorization) {
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
}
