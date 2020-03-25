package com.redroundrobin.thirema.apirest.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UserControllerTest {

  @MockBean
  JwtUtil jwtTokenUtil;

  @MockBean
  private UserService userService;

  @MockBean
  EntityService entityService;

  private UserController userController;

  private String adminToken = "adminToken";
  private String mod1Token = "mod1Token";
  private String user1Token = "user1Token";
  private String user2Token = "user2Token";

  private User admin;
  private User mod1;
  private User user1;
  private User user2;

  @Before
  public void setUp() {
    userController = new UserController(jwtTokenUtil, userService, entityService);

    admin = new User();
    admin.setUserId(1);
    admin.setName("admin");
    admin.setSurname("admin");
    admin.setEmail("admin");
    admin.setPassword("password");
    admin.setType(User.Role.ADMIN);

    Entity entity1 = new Entity();
    entity1.setEntityId(1);

    mod1 = new User();
    mod1.setUserId(2);
    mod1.setName("mod1");
    mod1.setSurname("mod1");
    mod1.setEmail("mod1");
    mod1.setPassword("password");
    mod1.setType(User.Role.MOD);
    mod1.setEntity(entity1);

    user1 = new User();
    user1.setUserId(3);
    user1.setName("user1");
    user1.setSurname("user1");
    user1.setEmail("user1");
    user1.setPassword("password");
    user1.setType(User.Role.USER);
    user1.setEntity(entity1);

    Entity entity2 = new Entity();
    entity2.setEntityId(2);

    user2 = new User();
    user2.setUserId(4);
    user2.setName("user2");
    user2.setSurname("user2");
    user2.setEmail("user2");
    user2.setPassword("password");
    user2.setType(User.Role.USER);
    user2.setEntity(entity2);

    when(jwtTokenUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.find(admin.getUserId())).thenReturn(admin);

    when(jwtTokenUtil.extractUsername(mod1Token)).thenReturn(mod1.getEmail());
    when(userService.findByEmail(mod1.getEmail())).thenReturn(mod1);
    when(userService.find(mod1.getUserId())).thenReturn(mod1);

    when(jwtTokenUtil.extractUsername(user1Token)).thenReturn(user1.getEmail());
    when(userService.findByEmail(user1.getEmail())).thenReturn(user1);
    when(userService.find(user1.getUserId())).thenReturn(user1);

    when(jwtTokenUtil.extractUsername(user2Token)).thenReturn(user2.getEmail());
    when(userService.findByEmail(user2.getEmail())).thenReturn(user2);
    when(userService.find(user2.getUserId())).thenReturn(user2);
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
  public void editUser1ByAdminUserNotExistError400() throws Exception {

    String token = "token";

    when(userService.find(5)).thenReturn(null);

    JSONObject request = new JSONObject();
    request.put("email", "newemail");

    ResponseEntity response = userController.editUser("Bearer " + token,
        request.toString(), 5);

    ResponseEntity expected = new ResponseEntity(HttpStatus.BAD_REQUEST);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editAdminByItSelfUsernameNotFoundError403() throws Exception {

    String newPassword = "fghmjyktsyd";
    String newEmail = "fghmjyktsyd";
    User editedAdmin = cloneUser(admin);
    editedAdmin.setEmail(newEmail);
    editedAdmin.setPassword(newPassword);

    when(jwtTokenUtil.extractExpiration(adminToken)).thenReturn(new Date());

    when(userService.editItself(eq(admin), any(JsonObject.class))).thenReturn(editedAdmin);

    when(userService.loadUserByEmail(editedAdmin.getEmail())).thenThrow(new UsernameNotFoundException(""));

    JSONObject request = new JSONObject();
    request.put("password", "newpassword");

    ResponseEntity response = userController.editUser("Bearer " + adminToken,
        request.toString(), admin.getUserId());

    HashMap<String, Object> bodyExpected = new HashMap<>();
    bodyExpected.put("user", editedAdmin);
    ResponseEntity expected = new ResponseEntity(HttpStatus.UNAUTHORIZED);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdminEditNotAllowedError403() throws Exception {

    when(userService.editByAdministrator(eq(user1), any(JsonObject.class))).thenThrow(
        new NotAllowedToEditException("fields furnished not allowed"));

    JSONObject request = new JSONObject();
    request.put("user_id", user1.getUserId());

    ResponseEntity response = userController.editUser("Bearer " + adminToken,
        request.toString(), user1.getUserId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdminSuccessfull() throws Exception {

    String newEmail = "newEmail";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);

    when(userService.editByAdministrator(eq(user1), any(JsonObject.class))).thenReturn(editedUser1);

    JSONObject request = new JSONObject();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + adminToken,
        request.toString(), user1.getUserId());

    HashMap<String, Object> expectedBody = new HashMap<>();
    expectedBody.put("user", editedUser1);
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.OK);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByItselfSuccessfull() throws Exception {

    String newEmail = "newEmail";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);

    when(jwtTokenUtil.extractExpiration(user1Token)).thenReturn(new Date());

    when(userService.editItself(eq(user1), any(JsonObject.class))).thenReturn(editedUser1);

    String newToken = "newToken";

    when(userService.loadUserByEmail(editedUser1.getEmail())).thenReturn(
        new org.springframework.security.core.userdetails.User(
            "asdf","asfdrg",Collections.emptyList()));
    when(jwtTokenUtil.generateTokenWithExpiration(anyString(), any(Date.class),
        any(org.springframework.security.core.userdetails.User.class))).thenReturn("newToken");

    JSONObject request = new JSONObject();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request.toString(), user1.getUserId());

    HashMap<String, Object> expectedBody = new HashMap<>();
    expectedBody.put("user", editedUser1);
    expectedBody.put("token", newToken);
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.OK);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByUser1NotAllowedError403() throws Exception {

    JSONObject request = new JSONObject();
    request.put("email", "newEmail");

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request.toString(), user2.getUserId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByMod1Successfull() throws Exception {

    String newEmail = "newEmail";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);

    when(userService.editByModerator(eq(user1), any(JsonObject.class))).thenReturn(editedUser1);

    JSONObject request = new JSONObject();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request.toString(), user1.getUserId());

    HashMap<String, Object> expectedBody = new HashMap<>();
    expectedBody.put("user", editedUser1);
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.OK);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfDuplicateUniqueError409() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editItself(eq(user2), any(JsonObject.class))).thenThrow(
        new DataIntegrityViolationException(
            "ERROR: duplicate key value violates unique constraint \"unique_telegram_name\"\n"
            + "  Dettaglio: Key (telegram_name)=(newEmail) already exists."));

    JSONObject request = new JSONObject();
    request.put("telegram_name", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request.toString(), user2.getUserId());

    String expectedBody = "The value of telegram_name already exists";
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.CONFLICT);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfDuplicateUniqueNotMatcherFindError409() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editItself(eq(user2), any(JsonObject.class))).thenThrow(
        new DataIntegrityViolationException(
            "ERROR: duplicate key value violates unique constraint \"unique_telegram_name\"\n"
                + "  Dettaglio: something"));

    JSONObject request = new JSONObject();
    request.put("telegram_name", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request.toString(), user2.getUserId());

    String expectedBody = "";
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.CONFLICT);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfOtherDbExceptionError409() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editItself(eq(user2), any(JsonObject.class))).thenThrow(
        new DataIntegrityViolationException("ERROR: value too long for type character varying(32)"));

    JSONObject request = new JSONObject();
    request.put("telegram_name", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request.toString(), user2.getUserId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.BAD_REQUEST);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfFieldsToEditContainNotFoundKeys400() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editItself(eq(user2), any(JsonObject.class))).thenThrow(
        new KeysNotFoundException("telegramName doesn't exist"));

    JSONObject request = new JSONObject();
    request.put("telegramName", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request.toString(), user2.getUserId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.BAD_REQUEST);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editMod1ByItselfFieldsToEditContainNotFoundKeys409() throws Exception {

    String newTelegramName = "newEmail";
    boolean tfa = true;

    String tfaError = "TFA can't be edited because either telegram_name is "
        + "in the request or telegram chat not present";
    when(userService.editItself(eq(mod1), any(JsonObject.class))).thenThrow(
        new TfaNotPermittedException(tfaError));

    JSONObject request = new JSONObject();
    request.put("telegramName", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request.toString(), mod1.getUserId());

    ResponseEntity expected = new ResponseEntity(tfaError, HttpStatus.CONFLICT);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }
}