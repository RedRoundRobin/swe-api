package com.redroundrobin.thirema.apirest.config;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class CustomAuthenticationManager implements AuthenticationManager {

  @Autowired
  UserService userService;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String email = authentication.getPrincipal() + "";
    String password = authentication.getCredentials() + "";

    User user = userService.findByEmail(email);

    if (user == null || !password.equals(user.getPassword())) {
      throw new BadCredentialsException("401");
    } else if (user.isDeleted() || (user.getType() != 2 && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new DisabledException("403");
    }

    return new UsernamePasswordAuthenticationToken(email, password);
  }
}
