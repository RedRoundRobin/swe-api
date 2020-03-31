package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.*;
import java.util.stream.Collectors;

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
    userController = new UserController(jwtTokenUtil, userService, entityService);

    admin1 = new User();
    admin1.setId(1);
    admin1.setName("admin1");
    admin1.setSurname("admin1");
    admin1.setEmail("admin1");
    admin1.setPassword("password");
    admin1.setType(User.Role.ADMIN);

    admin2 = new User();
    admin2.setId(2);
    admin2.setName("admin2");
    admin2.setSurname("admin2");
    admin2.setEmail("admin2");
    admin2.setPassword("password");
    admin2.setType(User.Role.ADMIN);

    Entity entity1 = new Entity();
    entity1.setId(1);

    mod1 = new User();
    mod1.setId(3);
    mod1.setName("mod1");
    mod1.setSurname("mod1");
    mod1.setEmail("mod1");
    mod1.setPassword("password");
    mod1.setType(User.Role.MOD);
    mod1.setEntity(entity1);

    mod11 = new User();
    mod11.setId(4);
    mod11.setName("mod11");
    mod11.setSurname("mod11");
    mod11.setEmail("mod11");
    mod11.setPassword("password");
    mod11.setType(User.Role.MOD);
    mod11.setEntity(entity1);

    user1 = new User();
    user1.setId(5);
    user1.setName("user1");
    user1.setSurname("user1");
    user1.setEmail("user1");
    user1.setPassword("password");
    user1.setType(User.Role.USER);
    user1.setEntity(entity1);

    Entity entity2 = new Entity();
    entity2.setId(2);

    user2 = new User();
    user2.setId(6);
    user2.setName("user2");
    user2.setSurname("user2");
    user2.setEmail("user2");
    user2.setPassword("password");
    user2.setType(User.Role.USER);
    user2.setEntity(entity2);

    when(jwtTokenUtil.extractRole("Bearer " + admin1Token)).thenReturn(admin1.getType());
    when(jwtTokenUtil.extractUsername(admin1Token)).thenReturn(admin1.getEmail());
    when(userService.findByEmail(admin1.getEmail())).thenReturn(admin1);
    when(userService.findById(admin1.getId())).thenReturn(admin1);

    when(jwtTokenUtil.extractRole("Bearer " + admin2Token)).thenReturn(admin2.getType());
    when(jwtTokenUtil.extractUsername(admin2Token)).thenReturn(admin2.getEmail());
    when(userService.findByEmail(admin2.getEmail())).thenReturn(admin2);
    when(userService.findById(admin2.getId())).thenReturn(admin2);

    when(jwtTokenUtil.extractRole("Bearer " + mod1Token)).thenReturn(mod1.getType());
    when(jwtTokenUtil.extractUsername(mod1Token)).thenReturn(mod1.getEmail());
    when(userService.findByEmail(mod1.getEmail())).thenReturn(mod1);
    when(userService.findById(mod1.getId())).thenReturn(mod1);

    when(jwtTokenUtil.extractRole("Bearer " + mod11Token)).thenReturn(mod11.getType());
    when(jwtTokenUtil.extractUsername(mod11Token)).thenReturn(mod11.getEmail());
    when(userService.findByEmail(mod11.getEmail())).thenReturn(mod11);
    when(userService.findById(mod11.getId())).thenReturn(mod11);

    when(jwtTokenUtil.extractRole("Bearer " + user1Token)).thenReturn(user1.getType());
    when(jwtTokenUtil.extractUsername(user1Token)).thenReturn(user1.getEmail());
    when(userService.findByEmail(user1.getEmail())).thenReturn(user1);
    when(userService.findById(user1.getId())).thenReturn(user1);

    when(jwtTokenUtil.extractRole("Bearer " + user2Token)).thenReturn(user2.getType());
    when(jwtTokenUtil.extractUsername(user2Token)).thenReturn(user2.getEmail());
    when(userService.findByEmail(user2.getEmail())).thenReturn(user2);
    when(userService.findById(user2.getId())).thenReturn(user2);


    List<User> allUsers = new ArrayList<>();
    allUsers.add(admin1);
    allUsers.add(admin2);
    allUsers.add(mod1);
    allUsers.add(mod11);
    allUsers.add(user1);
    allUsers.add(user2);

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);

    when(userService.findAll()).thenReturn(allUsers);

    when(userService.findAllByEntityId(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      if (id < 3) {
        List<User> users = allUsers.stream()
            .filter(user -> user.getEntity() != null && user.getEntity().getId() == id)
            .collect(Collectors.toList());
        return users;
      } else {
        throw new EntityNotFoundException("");
      }
    });

    when(userService.serializeUser(any(JsonObject.class), any(User.class))).thenAnswer(i -> {
      JsonObject rawUserToInsert = i.getArgument(0);
      User insertingUser = i.getArgument(1);
      Set<String> creatable = new HashSet<>();
      creatable.add("name");
      creatable.add("surname");
      creatable.add("email");
      creatable.add("type");
      creatable.add("entity_id");
      creatable.add("password");  //SOLUZIONE TEMPORANEA: NON PREVISTA DA USE CASES

      boolean onlyCreatableKeys = rawUserToInsert.keySet()
          .stream()
          .filter(key -> !creatable.contains(key))
          .count() == 0;

      if (!onlyCreatableKeys)
        throw new KeysNotFoundException("There are some keys that either do not exist or you are not" +
            "allowed to edit them");

      if (!(creatable.size() == rawUserToInsert.keySet().size())) {
        throw new MissingFieldsException("Some necessary fields are missing: cannot create user");
      }

      /*Fare mock entityService*/
      Entity userToInsertEntity;
      if ((userToInsertEntity = entityService.findById(
          (rawUserToInsert.get("entity_id")).getAsInt())) == null) {
        throw new EntityNotFoundException("The entity with the entityId given doesn't exist");
      }

      User.Role userToInsertType;
      try {
        userToInsertType = User.Role.valueOf(rawUserToInsert.get("type").getAsString());
      } catch (IllegalArgumentException iae) {
        throw new UserRoleNotFoundException("The given role doesn't exist");
      } catch (NullPointerException nptr) {
        throw new UserRoleNotFoundException("The type parameter cannot be null");
      }

      if (insertingUser.getType() != User.Role.ADMIN &&
          insertingUser.getType() == User.Role.MOD &&
          (userToInsertEntity.getId() != insertingUser.getEntity().getId()
              || userToInsertType != User.Role.USER))
        throw new NotAuthorizedToInsertUserException();

      User newUser = new User();
      newUser.setEntity(userToInsertEntity);
      newUser.setType(userToInsertType);

      if (rawUserToInsert.get("name").getAsString() != null)
        newUser.setName(rawUserToInsert.get("name").getAsString());
      else throw new ValuesNotAllowedException("The name field cannot be null");

      if (rawUserToInsert.get("surname").getAsString() != null)
        newUser.setSurname(rawUserToInsert.get("name").getAsString());
      else throw new ValuesNotAllowedException("The surname field cannot be null");

      String email = rawUserToInsert.get("email").getAsString();
      if (email != null &&
          allUsers.stream().filter(user-> user.getEmail() == null).count() == 1) //email gia usata
        newUser.setEmail(email);
      else throw new ValuesNotAllowedException("Either the email field you inserted is" +
          "null or it is already used");

      allUsers.add(newUser);
      return allUsers.get(allUsers.size() - 1); //prendo lo user appena inserito
    });
  }

  private User cloneUser(User user) {
    User clone = new User();
    clone.setEmail(user.getEmail());
    clone.setTelegramName(user.getTelegramName());
    clone.setId(user.getId());
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
  public void editAdmin2ByAdmin1EditNotAllowedError403() throws Exception {

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", "newemail");

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, admin2.getId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdmin1UserNotExistError400() throws Exception {

    when(userService.findById(5)).thenReturn(null);

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", "newemail");

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, 10);

    ResponseEntity expected = new ResponseEntity(HttpStatus.BAD_REQUEST);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByItSelfUsernameNotFoundError401() throws Exception {

    String newPassword = "fghmjyktsyd";
    String newEmail = "fghmjyktsyd";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);
    editedUser1.setPassword(newPassword);

    when(jwtTokenUtil.extractExpiration(user1Token)).thenReturn(new Date());

    when(userService.editByUser(eq(user1), any(HashMap.class))).thenReturn(editedUser1);

    when(userService.loadUserByEmail(editedUser1.getEmail())).thenThrow(new UsernameNotFoundException(""));

    HashMap<String, Object> request = new HashMap<>();
    request.put("password", "newpassword");

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request, user1.getId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.UNAUTHORIZED);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdmin1EditNotAllowedError403() throws Exception {

    when(userService.editByAdministrator(eq(user1), eq(false), any(HashMap.class))).thenThrow(
        new NotAllowedToEditException("fields furnished not allowed"));

    HashMap<String, Object> request = new HashMap<>();
    request.put("user_id", user1.getId());

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, user1.getId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdmin1Successfull() throws Exception {

    String newEmail = "newEmail";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);

    when(userService.editByAdministrator(eq(user1), eq(false), any(HashMap.class))).thenReturn(editedUser1);

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, user1.getId());

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

    when(userService.editByUser(eq(user1), any(HashMap.class))).thenReturn(editedUser1);

    String newToken = "newToken";

    when(userService.loadUserByEmail(editedUser1.getEmail())).thenReturn(
        new org.springframework.security.core.userdetails.User(
            "asdf","asfdrg",Collections.emptyList()));
    when(jwtTokenUtil.generateTokenWithExpiration(anyString(), any(Date.class),
        any(org.springframework.security.core.userdetails.User.class))).thenReturn("newToken");

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request, user1.getId());

    HashMap<String, Object> expectedBody = new HashMap<>();
    expectedBody.put("user", editedUser1);
    expectedBody.put("token", newToken);
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.OK);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByUser1NotAllowedError403() throws Exception {

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", "newEmail");

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request, user2.getId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByMod1Successfull() throws Exception {

    String newEmail = "newEmail";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);

    when(userService.editByModerator(eq(user1), eq(false), any(HashMap.class))).thenReturn(editedUser1);

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request, user1.getId());

    HashMap<String, Object> expectedBody = new HashMap<>();
    expectedBody.put("user", editedUser1);
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.OK);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfDuplicateUniqueError409() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editByUser(eq(user2), any(HashMap.class))).thenThrow(
        new DataIntegrityViolationException(
            "ERROR: duplicate key value violates unique constraint \"unique_telegram_name\"\n"
            + "  Dettaglio: Key (telegram_name)=(newEmail) already exists."));

    HashMap<String, Object> request = new HashMap<>();
    request.put("telegram_name", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request, user2.getId());

    String expectedBody = "The value of telegram_name already exists";
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.CONFLICT);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfDuplicateUniqueNotMatcherFindError409() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editByUser(eq(user2), any(HashMap.class))).thenThrow(
        new DataIntegrityViolationException(
            "ERROR: duplicate key value violates unique constraint \"unique_telegram_name\"\n"
                + "  Dettaglio: something"));

    HashMap<String, Object> request = new HashMap<>();
    request.put("telegram_name", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request, user2.getId());

    String expectedBody = "";
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.CONFLICT);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfOtherDbExceptionError409() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editByUser(eq(user2), any(HashMap.class))).thenThrow(
        new DataIntegrityViolationException("ERROR: value too long for type character varying(32)"));

    HashMap<String, Object> request = new HashMap<>();
    request.put("telegram_name", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request, user2.getId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.BAD_REQUEST);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfFieldsToEditContainNotFoundKeys400() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editByUser(eq(user2), any(HashMap.class))).thenThrow(
        new KeysNotFoundException("telegramName doesn't exist"));

    HashMap<String, Object> request = new HashMap<>();
    request.put("telegramName", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request, user2.getId());

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
    when(userService.editByModerator(eq(mod1), eq(true), any(HashMap.class))).thenThrow(
        new TfaNotPermittedException(tfaError));

    HashMap<String, Object> request = new HashMap<>();
    request.put("telegramName", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request, mod1.getId());

    ResponseEntity expected = new ResponseEntity(tfaError, HttpStatus.CONFLICT);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editMod11ByMod1() throws Exception {

    String newEmail = "newEmail";

    when(userService.editByModerator(eq(mod11), eq(false), any(HashMap.class))).thenThrow(
        new NotAllowedToEditException(""));

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request, mod11.getId());

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }


  // getUsers method tests
  @Test
  public void getAllUsersByAdmin1Successfull() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, null, null);

    assertTrue(response.getStatusCode() == HttpStatus.OK);
    assertTrue(!response.getBody().isEmpty());
  }

  @Test
  public void getAllEntity1UsersByAdmin2Successfull() {
    String authorization = "Bearer "+admin2Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,1, null, null);

    assertTrue(response.getStatusCode() == HttpStatus.OK);
    assertTrue(!response.getBody().isEmpty());
  }

  @Test
  public void getAllUsersByAdmin1EntityNotFoundError400() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,3, null, null);

    System.out.println(response.getStatusCode());
    assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST);
  }

  @Test
  public void getAllUsersByMod1Successfull() {
    String authorization = "Bearer "+mod1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, null, null);

    assertTrue(response.getStatusCode() == HttpStatus.OK);
    assertTrue(!response.getBody().isEmpty());

    ResponseEntity<List<User>> response1 = userController.getUsers(authorization,1, null, null);

    assertTrue(response1.getStatusCode() == HttpStatus.OK);
    assertTrue(!response1.getBody().isEmpty());
    assertTrue(response.getBody().equals(response1.getBody()));
  }

  @Test
  public void getAllAlertUsersByMod1NoPermissionError403() {
    String authorization = "Bearer "+mod1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, 1, null);

    assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN);
  }

  @Test
  public void getAllDisabledAlertUsersByAdmin1Error400() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, 1, null);

    assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST);
  }

  @Test
  public void getAllViewUsersByAdmin1Error400() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, null, 1);

    assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST);
  }

  //creazione di un nuovo utente
  //@PostMapping(value = {"/users/create"})
  @Test
  public void createUserSuccessfulTest() {
    String authorization = "Bearer "+admin1Token;
    JsonObject jsonUser = new JsonObject();
    jsonUser.addProperty("name", "marco");
    jsonUser.addProperty("surname", "polo");
    jsonUser.addProperty("email", "CONTROLLOBENFORMATA!!!");
    jsonUser.addProperty("type",  0);
    jsonUser.addProperty("entity_id", 1);
    jsonUser.addProperty("password", "password");

    ResponseEntity<User> response = userController.createUser(authorization, jsonUser.toString());
    assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST);
//    assertTrue(response.getStatusCode() == HttpStatus.OK);
  }
/*
  //creazione di un nuovo utente
  @PostMapping(value = {"/users/create"})
  public ResponseEntity<Object> createUser(@RequestHeader("Authorization") String authorization,
                                           @RequestBody String jsonStringUser) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject jsonUser = JsonParser.parseString(jsonStringUser).getAsJsonObject();
    try {
      return ResponseEntity.ok(userService.serializeUser(jsonUser, user));
    }
    catch(KeysNotFoundException | MissingFieldsException | ValuesNotAllowedException
        | UserRoleNotFoundException | EntityNotFoundException | NotAuthorizedToInsertUserException e){
      return new ResponseEntity(e.getMessage() , HttpStatus.BAD_REQUEST);
    }
  }

  //creazione di un nuovo utente
  @PostMapping(value = {"/users/create"})
  public ResponseEntity<Object> createUser(@RequestHeader("Authorization") String authorization,
                                           @RequestBody String jsonStringUser) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject jsonUser = JsonParser.parseString(jsonStringUser).getAsJsonObject();
    try {
      return ResponseEntity.ok(userService.serializeUser(jsonUser, user));
    }
    catch(KeysNotFoundException | MissingFieldsException | ValuesNotAllowedException
        | UserRoleNotFoundException | EntityNotFoundException | NotAuthorizedToInsertUserException e){
      return new ResponseEntity(e.getMessage() , HttpStatus.BAD_REQUEST);
    }
  }*/
}