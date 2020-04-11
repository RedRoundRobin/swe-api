package com.redroundrobin.thirema.apirest.utils;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class CustomAuthenticationManager implements AuthenticationManager {

  UserService userService;

  public CustomAuthenticationManager(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) {
    String email = authentication.getPrincipal() + "";
    String password = authentication.getCredentials() + "";

    User user = userService.findByEmail(email);

    if (user == null || !password.equals(user.getPassword())) {
      throw new BadCredentialsException("User not found or password not match");
    } else if (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new DisabledException("User is deleted or not have an entity");
    }

    return new UsernamePasswordAuthenticationToken(email, password);
  }
}
