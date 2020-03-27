package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.ViewGraphService;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.EntityNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAllowedToEditException;
import com.redroundrobin.thirema.apirest.utils.exception.TfaNotPermittedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ViewGraphControllerTest {

  @MockBean
  JwtUtil jwtTokenUtil;

  @MockBean
  private UserService userService;

  @MockBean
  ViewGraphService viewGraphService;

  private ViewGraphController viewGraphController;

  private String admin1Token = "admin1Token";
  private String admin2Token = "admin2Token";
  private String mod1Token = "mod1Token";
  private String mod11Token = "mod11Token";
  private String user1Token = "user1Token";
  private String user2Token = "user2Token";

  private User admin1;
  private User admin2;
  private User mod1;
  private User mod11;
  private User user1;
  private User user2;

  @Before
  public void setUp() throws Exception {
    viewGraphController = new ViewGraphController(jwtTokenUtil, userService, viewGraphService);

    admin1 = new User();
    admin1.setUserId(1);
    admin1.setName("admin1");
    admin1.setSurname("admin1");
    admin1.setEmail("admin1");
    admin1.setPassword("password");
    admin1.setType(User.Role.ADMIN);

    admin2 = new User();
    admin2.setUserId(2);
    admin2.setName("admin2");
    admin2.setSurname("admin2");
    admin2.setEmail("admin2");
    admin2.setPassword("password");
    admin2.setType(User.Role.ADMIN);

    Entity entity1 = new Entity();
    entity1.setEntityId(1);

    mod1 = new User();
    mod1.setUserId(3);
    mod1.setName("mod1");
    mod1.setSurname("mod1");
    mod1.setEmail("mod1");
    mod1.setPassword("password");
    mod1.setType(User.Role.MOD);
    mod1.setEntity(entity1);

    mod11 = new User();
    mod11.setUserId(4);
    mod11.setName("mod11");
    mod11.setSurname("mod11");
    mod11.setEmail("mod11");
    mod11.setPassword("password");
    mod11.setType(User.Role.MOD);
    mod11.setEntity(entity1);

    user1 = new User();
    user1.setUserId(5);
    user1.setName("user1");
    user1.setSurname("user1");
    user1.setEmail("user1");
    user1.setPassword("password");
    user1.setType(User.Role.USER);
    user1.setEntity(entity1);

    Entity entity2 = new Entity();
    entity2.setEntityId(2);

    user2 = new User();
    user2.setUserId(6);
    user2.setName("user2");
    user2.setSurname("user2");
    user2.setEmail("user2");
    user2.setPassword("password");
    user2.setType(User.Role.USER);
    user2.setEntity(entity2);

    when(jwtTokenUtil.extractRole("Bearer "+admin1Token)).thenReturn(admin1.getType());
    when(jwtTokenUtil.extractUsername(admin1Token)).thenReturn(admin1.getEmail());
    when(userService.findByEmail(admin1.getEmail())).thenReturn(admin1);
    when(userService.find(admin1.getUserId())).thenReturn(admin1);

    when(jwtTokenUtil.extractRole("Bearer "+admin2Token)).thenReturn(admin2.getType());
    when(jwtTokenUtil.extractUsername(admin2Token)).thenReturn(admin2.getEmail());
    when(userService.findByEmail(admin2.getEmail())).thenReturn(admin2);
    when(userService.find(admin2.getUserId())).thenReturn(admin2);

    when(jwtTokenUtil.extractRole("Bearer "+mod1Token)).thenReturn(mod1.getType());
    when(jwtTokenUtil.extractUsername(mod1Token)).thenReturn(mod1.getEmail());
    when(userService.findByEmail(mod1.getEmail())).thenReturn(mod1);
    when(userService.find(mod1.getUserId())).thenReturn(mod1);

    when(jwtTokenUtil.extractRole("Bearer "+mod11Token)).thenReturn(mod11.getType());
    when(jwtTokenUtil.extractUsername(mod11Token)).thenReturn(mod11.getEmail());
    when(userService.findByEmail(mod11.getEmail())).thenReturn(mod11);
    when(userService.find(mod11.getUserId())).thenReturn(mod11);

    when(jwtTokenUtil.extractRole("Bearer "+user1Token)).thenReturn(user1.getType());
    when(jwtTokenUtil.extractUsername(user1Token)).thenReturn(user1.getEmail());
    when(userService.findByEmail(user1.getEmail())).thenReturn(user1);
    when(userService.find(user1.getUserId())).thenReturn(user1);

    when(jwtTokenUtil.extractRole("Bearer "+user2Token)).thenReturn(user2.getType());
    when(jwtTokenUtil.extractUsername(user2Token)).thenReturn(user2.getEmail());
    when(userService.findByEmail(user2.getEmail())).thenReturn(user2);
    when(userService.find(user2.getUserId())).thenReturn(user2);


    List<User> allUsers = new ArrayList<>();
    allUsers.add(admin1);
    allUsers.add(admin2);
    allUsers.add(mod1);
    allUsers.add(mod11);
    allUsers.add(user1);
    allUsers.add(user2);

    when(userService.findAll()).thenReturn(allUsers);

    when(userService.findAllByEntityId(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      if( id < 3 ) {
        List<User> users = allUsers.stream()
            .filter(user -> user.getEntity() != null && user.getEntity().getEntityId() == id)
            .collect(Collectors.toList());
        return users;
      } else {
        throw new EntityNotFoundException("");
      }
    });
  }

  private User cloneUser(User user) {
    User clone = new User();
    clone.setEmail(user.getEmail());
    clone.setTelegramName(user.getTelegramName());
    clone.setUserId(user.getUserId());
    clone.setEntity(user.getEntity());
    clone.setDeleted(user.isDeleted());
    clone.setTelegramChat(user.getTelegramChat());
    clone.setName(user.getName());
    clone.setSurname(user.getSurname());
    clone.setType(user.getType());
    clone.setTfa(user.getTfa());
    clone.setPassword(user.getPassword());

    return clone;
  }

  @Test
  public void getAllViewGraphsSuccessfull() throws Exception {

    ResponseEntity response = viewGraphController.getViewGraphs("Bearer " + admin1Token);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void getAllViewGraphsByUserError403() throws Exception {

    ResponseEntity response = viewGraphController.getViewGraphs("Bearer " + user1Token);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}