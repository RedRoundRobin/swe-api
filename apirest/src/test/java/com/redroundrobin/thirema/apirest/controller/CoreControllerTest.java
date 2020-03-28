package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CoreControllerTest {

  private CoreController coreController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private UserService userService;

  private User user;

  private String userTelegramTokenWithBearer = "Bearer userTelegramToken";
  private String userWebappTokenWithBearer = "Bearer userEmailToken";
  private String userTelegramToken = "userTelegramToken";
  private String userWebappToken = "userEmailToken";

  @Before
  public void setUp() {
    coreController = new CoreController();
    coreController.setJwtUtil(jwtUtil);
    coreController.setUserService(userService);

    user = new User();
    user.setEmail("email");
    user.setTelegramName("telegramName");

    when(jwtUtil.extractUsername(userTelegramToken)).thenReturn(user.getTelegramName());
    when(jwtUtil.extractUsername(userWebappToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractType(userTelegramToken)).thenReturn("telegram");
    when(jwtUtil.extractType(userWebappToken)).thenReturn("webapp");

    when(userService.findByTelegramName(user.getTelegramName())).thenReturn(user);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);
  }

  @Test
  public void getUserFromAuthorizationWithTelegramName() {
    User user = coreController.getUserFromAuthorization(userTelegramTokenWithBearer);

    assertEquals(this.user, user);
  }

  @Test
  public void getUserFromAuthorizationWithEmail() {
    User user = coreController.getUserFromAuthorization(userWebappTokenWithBearer);

    assertEquals(this.user, user);
  }
}
