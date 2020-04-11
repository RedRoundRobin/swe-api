package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UserControllerTest {

  @MockBean
  JwtUtil jwtUtil;

  @MockBean
  private UserService userService;

  @MockBean
  EntityService entityService;

  @MockBean
  LogService logService;

  private UserController userController;

  MockHttpServletRequest httpRequest;

  private final String admin1Token = "admin1Token";
  private final String admin2Token = "admin2Token";
  private final String mod1Token = "mod1Token";
  private final String mod2Token = "mod2Token";
  private final String mod11Token = "mod11Token";
  private final String user1Token = "user1Token";
  private final String user2Token = "user2Token";

  private User admin1;
  private User admin2;
  private User mod1;
  private User mod2;
  private User mod11;
  private User user1;
  private User user2;

  @Before
  public void setUp() throws Exception {
    userController = new UserController(jwtUtil, logService, userService);

    httpRequest = new MockHttpServletRequest();
    httpRequest.setRemoteAddr("localhost");

    // ----------------------------------------- Set Users --------------------------------------
    admin1 = new User(1, "admin1", "admin1", "admin1", "pass", User.Role.ADMIN);
    admin1.setTelegramName("TNAdmin1");

    admin2 = new User(2, "admin2", "admin2", "admin2", "pass", User.Role.ADMIN);
    admin2.setTelegramName("TNAdmin2");

    mod1 = new User(3, "mod1", "mod1", "mod1", "pass", User.Role.MOD);
    mod1.setTelegramName("TNmod1");

    mod11 = new User(4, "mod11", "mod11", "mod11", "pass", User.Role.MOD);
    mod11.setTelegramName("TNmod11");

    mod2 = new User(7, "mod2", "mod2", "mod2", "pass", User.Role.MOD);
    mod2.setTelegramName("TNmod2");

    user1 = new User(5, "user1", "user1", "user1", "pass", User.Role.USER);
    user1.setTelegramName("TNuser1");

    user2 = new User(6, "user2", "user2", "user2", "pass", User.Role.USER);
    user2.setTelegramName("TNuser2");

    List<User> allUsers = new ArrayList<>();
    allUsers.add(admin1);
    allUsers.add(admin2);
    allUsers.add(mod1);
    allUsers.add(mod11);
    allUsers.add(user1);
    allUsers.add(user2);
    allUsers.add(mod2);

    // ----------------------------------------- Set Entities --------------------------------------
    Entity entity1 = new Entity(1, "entity1", "loc1");
    Entity entity2 = new Entity(2, "entity2", "loc2");

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);

    // ------------------------------- Set Entities to Users --------------------------------------
    mod1.setEntity(entity1);
    mod11.setEntity(entity1);
    user1.setEntity(entity1);

    mod2.setEntity(entity2);
    user2.setEntity(entity2);

    when(jwtUtil.extractType(anyString())).thenReturn("webapp");

    when(jwtUtil.extractRole("Bearer " + admin1Token)).thenReturn(admin1.getType());
    when(jwtUtil.extractUsername(admin1Token)).thenReturn(admin1.getEmail());
    when(userService.findByEmail(admin1.getEmail())).thenReturn(admin1);
    when(userService.findById(admin1.getId())).thenReturn(admin1);

    when(jwtUtil.extractRole("Bearer " + admin2Token)).thenReturn(admin2.getType());
    when(jwtUtil.extractUsername(admin2Token)).thenReturn(admin2.getEmail());
    when(userService.findByEmail(admin2.getEmail())).thenReturn(admin2);
    when(userService.findById(admin2.getId())).thenReturn(admin2);

    when(jwtUtil.extractRole("Bearer " + mod1Token)).thenReturn(mod1.getType());
    when(jwtUtil.extractUsername(mod1Token)).thenReturn(mod1.getEmail());
    when(userService.findByEmail(mod1.getEmail())).thenReturn(mod1);
    when(userService.findById(mod1.getId())).thenReturn(mod1);

    when(jwtUtil.extractRole("Bearer " + mod11Token)).thenReturn(mod11.getType());
    when(jwtUtil.extractUsername(mod11Token)).thenReturn(mod11.getEmail());
    when(userService.findByEmail(mod11.getEmail())).thenReturn(mod11);
    when(userService.findById(mod11.getId())).thenReturn(mod11);

    when(jwtUtil.extractRole("Bearer " + user1Token)).thenReturn(user1.getType());
    when(jwtUtil.extractUsername(user1Token)).thenReturn(user1.getEmail());
    when(userService.findByEmail(user1.getEmail())).thenReturn(user1);
    when(userService.findById(user1.getId())).thenReturn(user1);

    when(jwtUtil.extractRole("Bearer " + user2Token)).thenReturn(user2.getType());
    when(jwtUtil.extractUsername(user2Token)).thenReturn(user2.getEmail());
    when(userService.findByEmail(user2.getEmail())).thenReturn(user2);
    when(userService.findById(user2.getId())).thenReturn(user2);

    when(jwtUtil.extractRole("Bearer " + mod2Token)).thenReturn(mod2.getType());
    when(jwtUtil.extractUsername(mod2Token)).thenReturn(mod2.getEmail());
    when(userService.findByEmail(mod2.getEmail())).thenReturn(mod2);
    when(userService.findById(mod2.getId())).thenReturn(mod2);

    doNothing().when(logService).createLog(anyInt(), anyString(), anyString(), anyString());

    when(userService.findAll()).thenReturn(allUsers);
    when(userService.findAllByEntityId(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      if (id < 3) {
        return allUsers.stream()
            .filter(user -> user.getEntity() != null && user.getEntity().getId() == id)
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(userService.deleteUser(any(User.class), anyInt())).thenAnswer(i -> {
      User deletingUser = i.getArgument(0);
      int userToDeleteId = i.getArgument(1);
      User userToDelete;
      if ((userToDelete = allUsers.stream()
          .filter(user -> user.getId() == userToDeleteId)
          .findFirst().orElse(null)) == null) {
        throw new InvalidFieldsValuesException("The given user_id doesn't correspond to any user");
      }

      if (deletingUser.getType() == User.Role.USER
          || deletingUser.getType() == User.Role.MOD
          && ((userToDelete.getType() != User.Role.USER)
          || deletingUser.getEntity().getId() != userToDelete.getEntity().getId())) {
        throw new NotAuthorizedException("This user cannot delete "
            + "the user with the user_id given");
      }

      userToDelete.setDeleted(true);
      return userToDelete;
  });
    when(userService.serializeUser(any(JsonObject.class), any(User.class))).thenAnswer(i -> {
      JsonObject rawUserToInsert = i.getArgument(0);
      User insertingUser = i.getArgument(1);
      Set<String> creatable = new HashSet<>();
      creatable.add("name");
      creatable.add("surname");
      creatable.add("email");
      creatable.add("type");
      creatable.add("entityId");
      creatable.add("password");  //SOLUZIONE TEMPORANEA: NON PREVISTA DA USE CASES

      boolean onlyCreatableKeys = creatable.containsAll(rawUserToInsert.keySet());

      if (!onlyCreatableKeys)
        throw new InvalidFieldsValuesException("");

      if (!(creatable.size() == rawUserToInsert.keySet().size())) {
        throw new MissingFieldsException("");
      }

      Entity userToInsertEntity;
      if ((userToInsertEntity =
          allEntities.stream().filter(entity ->
              entity.getId() == (rawUserToInsert.get("entityId")).getAsInt())
              .findFirst().orElse(null)) == null) {
        throw new InvalidFieldsValuesException("");
      }

      int userToInsertType;
      if ((userToInsertType =
          rawUserToInsert.get("type").getAsInt()) == 2 ||
          userToInsertType != 1 && userToInsertType != 0) {
        throw new InvalidFieldsValuesException("");
      }

      //qui so che entity_id dato esiste && so il tipo dello user che si vuole inserire
      //NB: IL MODERATORE PUO INSERIRE SOLO MEMBRI!! VA BENE??
      if (insertingUser.getType() == User.Role.USER
          || (insertingUser.getType() == User.Role.MOD
          && userToInsertEntity.getId() != insertingUser.getEntity().getId())) {
        throw new NotAuthorizedException("");
      }

      User newUser = new User();
      newUser.setEntity(userToInsertEntity);
      newUser.setType(User.Role.values()[userToInsertType]);

      if (rawUserToInsert.get("name").getAsString() != null
          || rawUserToInsert.get("surname").getAsString() != null
          || rawUserToInsert.get("password").getAsString() != null) {
        newUser.setName(rawUserToInsert.get("name").getAsString());
        newUser.setSurname(rawUserToInsert.get("surname").getAsString());
        newUser.setPassword(rawUserToInsert.get("password").getAsString());
      } else {
        throw new InvalidFieldsValuesException("");
      }

      String email = rawUserToInsert.get("email").getAsString();
      allUsers.stream().filter(user -> user.getEmail().equals(email)).count();
      if (email != null &&
              allUsers.stream().noneMatch(user -> user.getEmail().equals(email))) //email gia usata
        newUser.setEmail(email);
      else if(email == null) {
        throw new InvalidFieldsValuesException("");
      } else {
        throw new ConflictException("");
      }
      allUsers.add(newUser);
      return allUsers.get(allUsers.size() - 1); //prendo lo user appena inserito
    });
  }

  private User cloneUser(User user) {
    User clone = new User(user.getId(), user.getName(), user.getSurname(), user.getEmail(),
        user.getPassword(), user.getType());
    clone.setTelegramName(user.getTelegramName());
    clone.setEntity(user.getEntity());
    clone.setDeleted(user.isDeleted());
    clone.setTelegramChat(user.getTelegramChat());
    clone.setTfa(user.getTfa());

    return clone;
  }

  @Test
  public void editAdmin2ByAdmin1EditNotAllowedError403() {

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", "newemail");

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, admin2.getId(),httpRequest);

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdmin1UserNotExistError400() {

    when(userService.findById(5)).thenReturn(null);

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", "newemail");

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, 10, httpRequest);

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

    when(jwtUtil.extractExpiration(user1Token)).thenReturn(new Date());

    when(userService.editByUser(eq(user1), any(HashMap.class))).thenReturn(editedUser1);

    when(userService.loadUserByEmail(editedUser1.getEmail())).thenThrow(new UsernameNotFoundException(""));

    HashMap<String, Object> request = new HashMap<>();
    request.put("password", "newpassword");

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request, user1.getId(), httpRequest);

    ResponseEntity expected = new ResponseEntity(HttpStatus.UNAUTHORIZED);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdmin1EditNotAllowedError403() throws Exception {

    when(userService.editByAdministrator(eq(user1), any(HashMap.class), eq(false))).thenThrow(
        new NotAuthorizedException("fields furnished not allowed"));

    HashMap<String, Object> request = new HashMap<>();
    request.put("user_id", user1.getId());

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, user1.getId(), httpRequest);

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByAdmin1Successfull() throws Exception {

    String newEmail = "newEmail";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);

    when(userService.editByAdministrator(eq(user1), any(HashMap.class), eq(false))).thenReturn(editedUser1);

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + admin1Token,
        request, user1.getId(), httpRequest);

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

    when(jwtUtil.extractExpiration(user1Token)).thenReturn(new Date());

    when(userService.editByUser(eq(user1), any(HashMap.class))).thenReturn(editedUser1);

    String newToken = "newToken";

    when(userService.loadUserByEmail(editedUser1.getEmail())).thenReturn(
        new org.springframework.security.core.userdetails.User(
            "asdf","asfdrg",Collections.emptyList()));
    when(jwtUtil.generateTokenWithExpiration(anyString(),
        any(org.springframework.security.core.userdetails.User.class), any(Date.class)))
        .thenReturn("newToken");

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request, user1.getId(), httpRequest);

    HashMap<String, Object> expectedBody = new HashMap<>();
    expectedBody.put("user", editedUser1);
    expectedBody.put("token", newToken);
    ResponseEntity expected = new ResponseEntity(expectedBody, HttpStatus.OK);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByUser1NotAllowedError403() {

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", "newEmail");

    ResponseEntity response = userController.editUser("Bearer " + user1Token,
        request, user2.getId(), httpRequest);

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser1ByMod1Successfull() throws Exception {

    String newEmail = "newEmail";
    User editedUser1 = cloneUser(user1);
    editedUser1.setEmail(newEmail);

    when(userService.editByModerator(eq(user1), any(HashMap.class), eq(false))).thenReturn(editedUser1);

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request, user1.getId(), httpRequest);

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
        request, user2.getId(), httpRequest);

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
        request, user2.getId(), httpRequest);

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
        request, user2.getId(), httpRequest);

    ResponseEntity expected = new ResponseEntity(HttpStatus.BAD_REQUEST);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editUser2ByItselfFieldsToEditContainNotFoundKeys400() throws Exception {

    String newTelegramName = "newEmail";

    when(userService.editByUser(eq(user2), any(HashMap.class))).thenThrow(
        new MissingFieldsException("There aren't fields that can be edited"));

    HashMap<String, Object> request = new HashMap<>();
    request.put("telegram_Name", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + user2Token,
        request, user2.getId(), httpRequest);

    ResponseEntity expected = new ResponseEntity(HttpStatus.BAD_REQUEST);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editMod1ByItselfFieldsToEditContainNotFoundKeys409() throws Exception {

    String newTelegramName = "newEmail";

    String tfaError = "TFA can't be edited because either telegram_name is "
        + "in the request or telegram chat not present";
    when(userService.editByModerator(eq(mod1), any(HashMap.class), eq(true))).thenThrow(
        new ConflictException(tfaError));

    HashMap<String, Object> request = new HashMap<>();
    request.put("telegramName", newTelegramName);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request, mod1.getId(), httpRequest);

    ResponseEntity expected = new ResponseEntity(tfaError, HttpStatus.CONFLICT);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }

  @Test
  public void editMod11ByMod1() throws Exception {

    String newEmail = "newEmail";

    when(userService.editByModerator(eq(mod11), any(HashMap.class), eq(false))).thenThrow(
        new NotAuthorizedException(""));

    HashMap<String, Object> request = new HashMap<>();
    request.put("email", newEmail);

    ResponseEntity response = userController.editUser("Bearer " + mod1Token,
        request, mod11.getId(),httpRequest);

    ResponseEntity expected = new ResponseEntity(HttpStatus.FORBIDDEN);

    // Check status and if are present tfa and token
    assertEquals(expected, response);
  }


  // getUsers method tests
  @Test
  public void getAllUsersByAdmin1Successfull() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, null, null);

    assertSame(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getAllEntity1UsersByAdmin2Successfull() {
    String authorization = "Bearer "+admin2Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,1, null, null);

    assertSame(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getAllUsersByAdmin1NotExistentEntityEmptyList() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,3, null, null);

    assertSame(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  public void getAllUsersByMod1Successfull() {
    String authorization = "Bearer "+mod1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, null, null);

    assertSame(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());

    ResponseEntity<List<User>> response1 = userController.getUsers(authorization,1, null, null);

    assertSame(HttpStatus.OK, response1.getStatusCode());
    assertFalse(response1.getBody().isEmpty());
    assertEquals(response.getBody(), response1.getBody());
  }

  @Test
  public void getAllAlertUsersByMod1NoPermissionError403() {
    String authorization = "Bearer "+mod1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, 1, null);

    assertSame(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void getAllDisabledAlertUsersByAdmin1Error400() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, 1, null);

    assertSame(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void getAllViewUsersByAdmin1Error400() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<List<User>> response = userController.getUsers(authorization,null, null, 1);

    assertSame(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  //creazione di un nuovo utente
  //@PostMapping(value = {"/users/create"})
  @Test
  public void createUserSuccessfulByAdmin1Test() {
    String authorization = "Bearer "+admin1Token;
    JsonObject jsonUser = new JsonObject();
    jsonUser.addProperty("name", "marco");
    jsonUser.addProperty("surname", "polo");
    jsonUser.addProperty("email", "createUserSuccessfulByAdmin1");
    jsonUser.addProperty("type",  0);
    jsonUser.addProperty("entityId", 1);
    jsonUser.addProperty("password", "password");

    ResponseEntity<User> response = userController.createUser(authorization, jsonUser.toString(), httpRequest);
    assertSame(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void createUserSuccessfullyByMod1Test() {
    String authorization = "Bearer "+mod1Token;
    JsonObject jsonUser = new JsonObject();
    jsonUser.addProperty("name", "marco");
    jsonUser.addProperty("surname", "polo");
    jsonUser.addProperty("email", "createUserSuccessfullyByMod1");
    jsonUser.addProperty("type",  0);
    jsonUser.addProperty("entityId", 1);
    jsonUser.addProperty("password", "password");

    ResponseEntity<User> response = userController.createUser(authorization, jsonUser.toString(), httpRequest);
    assertSame(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void createUserByMod1ConflictExceptionTest() {
    String authorization = "Bearer "+mod1Token;
    JsonObject jsonUser = new JsonObject();
    jsonUser.addProperty("name", "marco");
    jsonUser.addProperty("surname", "polo");
    jsonUser.addProperty("email", user1.getEmail());
    jsonUser.addProperty("type",  0);
    jsonUser.addProperty("entityId", 1);
    jsonUser.addProperty("password", "password");

    ResponseEntity<User> response = userController.createUser(authorization, jsonUser.toString(), httpRequest);
    assertSame(HttpStatus.CONFLICT, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void createUserByMod2NotAuthorizedExceptionTest() {
    String authorization = "Bearer "+mod2Token;
    JsonObject jsonUser = new JsonObject();
    jsonUser.addProperty("name", "marco");
    jsonUser.addProperty("surname", "polo");
    jsonUser.addProperty("email", "createUserByMod2");
    jsonUser.addProperty("type",  0);
    jsonUser.addProperty("entityId", 1);
    jsonUser.addProperty("password", "password");

    ResponseEntity<User> response = userController.createUser(authorization, jsonUser.toString(), httpRequest);
    assertSame(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void deleteUser1ByAdmin1SuccesfulTest() {
    String authorization = "Bearer "+admin1Token;

    ResponseEntity<User> response = userController.deleteUser(authorization, user1.getId(), httpRequest);
    assertSame(HttpStatus.OK, response.getStatusCode());

    User expected = user1;
    User actual = response.getBody();

    assertEquals(expected, actual);
    assertTrue(user1.isDeleted());
  }

  @Test
  public void deleteUser1ByMod1SuccesfulTest() {
    String authorization = "Bearer "+mod1Token;

    ResponseEntity<User> response = userController.deleteUser(authorization, user1.getId(), httpRequest);
    assertSame(HttpStatus.OK, response.getStatusCode());
    
    User expected = user1;
    User actual = response.getBody();

    assertEquals(expected, actual);
    assertTrue(user1.isDeleted());
  }

  @Test
  public void deleteUser1ByMod2NotAuthorizedExceptionTest() {
    String authorization = "Bearer "+mod2Token;

    ResponseEntity<User> response = userController.deleteUser(authorization, user1.getId(), httpRequest);
    assertSame(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void deleteUser1ByModValuesNotAllowedExceptionTest() {
    String authorization = "Bearer "+mod2Token;
    int notExistingId = 50;

    ResponseEntity<User> response = userController.deleteUser(authorization, notExistingId, httpRequest);
    assertSame(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
  }

}
