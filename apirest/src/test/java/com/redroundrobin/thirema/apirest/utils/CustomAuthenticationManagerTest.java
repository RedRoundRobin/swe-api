package com.redroundrobin.thirema.apirest.utils;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CustomAuthenticationManagerTest {

  private CustomAuthenticationManager customAuthenticationManager;

  @MockBean
  private UserService userService;

  @Before
  public void setUp() {
    customAuthenticationManager = new CustomAuthenticationManager(userService);
  }

  @Test
  public void authenticateSuccessfull() {
    User user = new User("name","surname","email","password", User.Role.USER);
    Entity entity = new Entity("name", "loc");
    user.setEntity(entity);
    when(userService.findByEmail(anyString())).thenReturn(user);

    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        user.getEmail(), user.getPassword());

    try {
      Authentication auth = customAuthenticationManager.authenticate(authenticationToken);

      assertEquals(auth.getPrincipal()+"", user.getEmail());
      assertEquals(auth.getCredentials()+"", user.getPassword());
    } catch (BadCredentialsException | DisabledException bce) {
      fail();
    }
  }

  @Test
  public void authenticateThrowBadCredentialsException() {
    User user = new User("name","surname","email","password", User.Role.USER);
    Entity entity = new Entity("name", "loc");
    user.setEntity(entity);
    when(userService.findByEmail(anyString())).thenReturn(user);

    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        user.getEmail(), "pass");

    try {
      customAuthenticationManager.authenticate(authenticationToken);

      fail();
    } catch (BadCredentialsException bce) {
      assertTrue(true);
    } catch (DisabledException de) {
      fail();
    }
  }

  @Test
  public void authenticateThrowDisabledException() {
    User user = new User("name","surname","email","password", User.Role.USER);
    user.setDeleted(true);
    Entity entity = new Entity("name", "loc");
    user.setEntity(entity);
    when(userService.findByEmail(anyString())).thenReturn(user);

    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        user.getEmail(), user.getPassword());

    try {
      customAuthenticationManager.authenticate(authenticationToken);

      fail();
    } catch (BadCredentialsException bce) {
      fail();
    } catch (DisabledException de) {
      assertTrue(true);
    }
  }
}
