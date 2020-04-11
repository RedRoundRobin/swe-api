package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UserServiceTest {

  private UserService userService;

  @MockBean
  private AlertRepository alertRepo;

  @MockBean
  private EntityRepository entityRepo;

  @MockBean
  private UserRepository userRepo;

  private User admin1;
  private User admin2;
  private User mod1;
  private User mod11;
  private User mod2;
  private User user1;
  private User user2;

  private Entity entity1;
  private Entity entity2;

  @Before
  public void setUp() {

    userService = new UserService(userRepo, alertRepo, entityRepo);

    // ----------------------------------------- Set Users ---------------------------------------
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

    // --------------------------------------- Set Entities ---------------------------------------
    entity1 = new Entity(1, "entity1", "loc1");
    entity2 = new Entity(2, "entity2", "loc2");

    // --------------------------------- Set Entities To Users ------------------------------------
    mod1.setEntity(entity1);
    mod11.setEntity(entity1);

    mod2.setEntity(entity2);

    user1.setEntity(entity1);
    user2.setEntity(entity2);

    when(userRepo.findAll()).thenReturn(allUsers);
    when(userRepo.findById(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      return allUsers.stream()
          .filter(user -> user.getId() == id)
          .findFirst();
    });
    when(userRepo.findByTelegramNameAndTelegramChat(anyString(), anyString())).thenAnswer(i -> {
      String tn = i.getArgument(0);
      String tc = i.getArgument(1);
      Optional<User> userFound = allUsers.stream()
          .filter(user -> user.getTelegramName().equals(tn) && user.getTelegramChat().equals(tc))
          .findFirst();
      return userFound.orElse(null);
    });
    when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    when(userRepo.findByEmail(anyString())).thenAnswer(i -> {
      String emailNewUser = i.getArgument(0);
      return allUsers.stream()
          .filter(user -> user.getEmail().equals(emailNewUser))
          .findFirst().orElse(null);
    });
    when(userRepo.findAllByEntity(any(Entity.class))).thenAnswer(i -> {
      Entity ent = i.getArgument(0);
      return allUsers.stream()
          .filter(user -> user.getEntity() == ent).collect(Collectors.toList());
    });

    when(entityRepo.findById(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      if (id == 1) {
        return Optional.of(entity1);
      } else if (id == 2) {
        return Optional.of(entity2);
      } else {
        return Optional.empty();
      }
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

  // findAll method tests
  @Test
  public void findAllSuccessfull() {
    List<User> users = userService.findAll();
    assertFalse(users.isEmpty());
  }

  // findById method tests
  @Test
  public void findSuccessfull() {
    User user = userService.findById(6);
    assertEquals(user2, user);
  }

  // findByTelegramNameAndTelegramChat method tests
  @Test
  public void findByTelegramNameAndTelegramChatNull() {
    User user = userService.findByTelegramNameAndTelegramChat("name", "chat");
    assertNull(user);
  }

  // loadByUsername method tests
  @Test
  public void loadUser1ByNameSuccessfull() {

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(user1);

    try {
      UserDetails userDetails = userService.loadUserByUsername(user1.getEmail());

      assertNotNull(userDetails);
      assertSame(userDetails.getUsername(), user1.getEmail());
      assertSame(userDetails.getPassword(), user1.getPassword());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertEquals(userDetails.getAuthorities().stream().findFirst().get()
              .toString(), String.valueOf(user1.getType()));
    } catch (UsernameNotFoundException unfe) {
      fail();
    }
  }

  @Test
  public void loadAdmin1ByNameSuccessfull() {

    when(userRepo.findByEmail(admin1.getEmail())).thenReturn(admin1);

    try {
      UserDetails userDetails = userService.loadUserByUsername(admin1.getEmail());

      assertNotNull(userDetails);
      assertSame(userDetails.getUsername(), admin1.getEmail());
      assertSame(userDetails.getPassword(), admin1.getPassword());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertEquals(userDetails.getAuthorities().stream().findFirst().get()
              .toString(), String.valueOf(admin1.getType()));
    } catch (UsernameNotFoundException unfe) {
      fail();
    }
  }

  @Test
  public void loadUser1ByNameThrowUsernameNotFoundException() {

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(null);

    try {
      userService.loadUserByUsername(user1.getEmail());
      fail();
    } catch (UsernameNotFoundException unfe) {
      assertTrue(true);
    }
  }

  // loadByUserEmail method tests
  @Test
  public void loadUser2ByEmailSuccessfull() {

    when(userRepo.findByEmail(user2.getEmail())).thenReturn(user2);

    try {
      UserDetails userDetails = userService.loadUserByEmail(user2.getEmail());

      assertNotNull(userDetails);
      assertSame(userDetails.getUsername(), user2.getEmail());
      assertSame(userDetails.getPassword(), user2.getPassword());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertEquals(userDetails.getAuthorities().stream().findFirst().get()
              .toString(), String.valueOf(user2.getType()));
    } catch(UsernameNotFoundException | UserDisabledException ue) {
      fail();
    }
  }

  @Test
  public void loadUser1ByEmailThrowUsernameNotFoundException() {

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(null);

    try {
      userService.loadUserByEmail(user1.getEmail());
      fail();
    } catch (UsernameNotFoundException unfe) {
      assertTrue(true);
    } catch (UserDisabledException ude) {
      fail();
    }
  }

  @Test
  public void loadUser1ByEmailThrowUserDisabledException() {

    user1.setEntity(null);

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(user1);

    try {
      userService.loadUserByEmail(user1.getEmail());
      fail();
    } catch (UsernameNotFoundException unfe) {
      fail();
    } catch (UserDisabledException ude) {
      assertTrue(true);
    }
  }

  // loadByTelegramName method tests
  @Test
  public void loadMod1ByTelegramNameSuccessfull() {

    String telegramChat = "gwfngsrtr";
    mod1.setTelegramChat(telegramChat);

    when(userRepo.findByTelegramName(mod1.getTelegramName())).thenReturn(mod1);

    try {
      UserDetails userDetails = userService.loadUserByTelegramName(mod1.getTelegramName());

      assertNotNull(userDetails);
      assertSame(userDetails.getUsername(), mod1.getTelegramName());
      assertSame(userDetails.getPassword(), mod1.getTelegramChat());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertEquals(userDetails.getAuthorities().stream().findFirst().get()
              .toString(), String.valueOf(mod1.getType()));
    } catch(UsernameNotFoundException | UserDisabledException | TelegramChatNotFoundException ue) {
      fail();
    }
  }

  @Test
  public void loadMod1ByTelegramNameThrowTelegramChatNotFoundException() {

    when(userRepo.findByTelegramName(mod1.getTelegramName())).thenReturn(mod1);

    try {
      userService.loadUserByTelegramName(mod1.getTelegramName());

      fail();
    } catch(UsernameNotFoundException | UserDisabledException ue) {
      fail();
    } catch (TelegramChatNotFoundException tcnfe) {
      assertTrue(true);
    }
  }

  @Test
  public void loadUser2ByTelegramNameThrowUsernameNotFoundException() {

    when(userRepo.findByTelegramName(user2.getTelegramName())).thenReturn(null);

    try {
      userService.loadUserByTelegramName(user2.getTelegramName());
      fail();
    } catch (UsernameNotFoundException unfe) {
      assertTrue(true);
    } catch (UserDisabledException | TelegramChatNotFoundException ude) {
      fail();
    }
  }

  @Test
  public void loadMod11ByTelegramNameThrowUserDisabledException() {

    mod11.setDeleted(true);

    when(userRepo.findByTelegramName(mod11.getTelegramName())).thenReturn(mod11);

    try {
      userService.loadUserByTelegramName(mod11.getTelegramName());
      fail();
    } catch (UsernameNotFoundException | TelegramChatNotFoundException unfe) {
      fail();
    } catch (UserDisabledException ude) {
      assertTrue(true);
    }
  }

  // serializeUser method tests
  @Test
  public void serializeUserByMod1SuccessfullTest() {
    JsonObject fieldsToCreate = new JsonObject();
    String name = "marco";
    String surname = "franco";
    String email = "email"; //controllo ben formata lato webapp
    String password = "password";
    int type = 0;
    int entityId = 1;
    fieldsToCreate.addProperty("name", name);
    fieldsToCreate.addProperty("surname", surname);
    fieldsToCreate.addProperty("email", email);
    fieldsToCreate.addProperty("password", password);
    fieldsToCreate.addProperty("type", type);
    fieldsToCreate.addProperty("entityId", entityId);

    try {
      userService.serializeUser(fieldsToCreate, mod1);
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void serializeUserByAdmin1SuccessfullTest() {
    JsonObject fieldsToCreate = new JsonObject();
    String name = "marco";
    String surname = "franco";
    String email = "email"; //controllo ben formata lato webapp
    String password = "password";
    int type = 0;
    int entityId = 1;
    fieldsToCreate.addProperty("name", name);
    fieldsToCreate.addProperty("surname", surname);
    fieldsToCreate.addProperty("email", email);
    fieldsToCreate.addProperty("password", password);
    fieldsToCreate.addProperty("type", type);
    fieldsToCreate.addProperty("entityId", entityId);

    try {
      userService.serializeUser(fieldsToCreate, admin1);
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void serializeMod1ByAdmin1SuccessfullTest() {
    JsonObject fieldsToCreate = new JsonObject();
    String name = "marco";
    String surname = "franco";
    String email = "email"; //controllo ben formata lato webapp
    String password = "password";
    int type = 1;
    int entityId = 1;
    fieldsToCreate.addProperty("name", name);
    fieldsToCreate.addProperty("surname", surname);
    fieldsToCreate.addProperty("email", email);
    fieldsToCreate.addProperty("password", password);
    fieldsToCreate.addProperty("type", type);
    fieldsToCreate.addProperty("entityId", entityId);

    try {
      userService.serializeUser(fieldsToCreate, admin1);
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void serializeUserByUser1ExceptionTest() {
    JsonObject fieldsToCreate = new JsonObject();
    String name = "marco";
    String surname = "franco";
    String email = "email"; //controllo ben formata lato webapp
    String password = "password";
    int type = 0;
    int entityId = 1;
    fieldsToCreate.addProperty("name", name);
    fieldsToCreate.addProperty("surname", surname);
    fieldsToCreate.addProperty("email", email);
    fieldsToCreate.addProperty("password", password);
    fieldsToCreate.addProperty("type", type);
    fieldsToCreate.addProperty("entityId", entityId);

    try {
      userService.serializeUser(fieldsToCreate, user1);
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void serializeUserByUser1ExceptionMissingFieldTest() {
    JsonObject fieldsToCreate = new JsonObject();
    String name = "marco";
    String surname = "franco";
    String email = "email"; //controllo ben formata lato webapp
    String password = "password";
    int type = 0;
    int entityId = 1;
    fieldsToCreate.addProperty("name", name);
    fieldsToCreate.addProperty("surname", surname);
    fieldsToCreate.addProperty("email", email);
    fieldsToCreate.addProperty("password", password);
    fieldsToCreate.addProperty("type", type);
    fieldsToCreate.addProperty("entityId", entityId);

    try {
      userService.serializeUser(fieldsToCreate, user1);
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void serializeAdmin2ByAdmin1UnsuccesfulTest() {
    JsonObject fieldsToCreate = new JsonObject();
    String name = "marco";
    String surname = "franco";
    String email = "email"; //controllo ben formata lato webapp
    String password = "password";
    int type = 2;
    int entityId = 1;
    fieldsToCreate.addProperty("name", name);
    fieldsToCreate.addProperty("surname", surname);
    fieldsToCreate.addProperty("email", email);
    fieldsToCreate.addProperty("password", password);
    fieldsToCreate.addProperty("type", type);
    fieldsToCreate.addProperty("entityId", entityId);

    try {
      userService.serializeUser(fieldsToCreate, admin1);
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  // editByAdmin method tests
  @Test
  public void editAdminByItselfSuccessfull() {
    // modificare tutto

    String newTelegramName = "newTelegramName";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("telegramName",newTelegramName);

    User editedUser = cloneUser(admin1);
    editedUser.setTelegramName(newTelegramName);
    editedUser.setTfa(false);
    editedUser.setTelegramChat(null);

    try {
      User user = userService.editByAdministrator(admin1, fieldsToEdit, true);

      assertNotNull(user);
      assertEquals(editedUser.getTelegramName(), user.getTelegramName());
      assertEquals(editedUser.getTelegramChat(), user.getTelegramChat());
      assertEquals(editedUser.getTfa(), user.getTfa());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editUser1ByAdminSuccessfull() {
    // modificare tutto

    String newName = "newName";
    String newSurname = "newSurname";
    String newEmail = "newEmail";
    String newPassword = "newPassword";
    User.Role newType = User.Role.USER;
    String newTelegramName = "newTelegramName";
    int newEntityId = entity2.getId();

    HashMap<String, Object> fieldsToEdit = new HashMap<>();

    fieldsToEdit.put("name",newName);
    fieldsToEdit.put("surname",newSurname);
    fieldsToEdit.put("email",newEmail);
    fieldsToEdit.put("password",newPassword);
    fieldsToEdit.put("type",0);
    fieldsToEdit.put("telegramName",newTelegramName);
    fieldsToEdit.put("entityId", newEntityId);

    User editedUser = cloneUser(user1);
    editedUser.setName(newName);
    editedUser.setSurname(newSurname);
    editedUser.setEmail(newEmail);
    editedUser.setPassword(newPassword);
    editedUser.setType(newType);
    editedUser.setTelegramName(newTelegramName);
    editedUser.setEntity(entity2);

    when(entityRepo.findById(newEntityId)).thenReturn(Optional.of(entity2));

    try {
      User user = userService.editByAdministrator(user1, fieldsToEdit, false);

      assertNotNull(user);
      assertEquals(editedUser.getName(), user.getName());
      assertEquals(editedUser.getSurname(), user.getSurname());
      assertEquals(editedUser.getEmail(), user.getEmail());
      assertEquals(editedUser.getPassword(), user.getPassword());
      assertEquals(editedUser.getType(), user.getType());
      assertEquals(editedUser.getTelegramName(), user.getTelegramName());
      assertEquals(editedUser.getEntity(), user.getEntity());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editUser2ByAdminWithNotExistentType() {
    // settare ad admin type 3

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("type",3);

    try {
      userService.editByAdministrator(user1, fieldsToEdit, false);
      fail();
    } catch (InvalidFieldsValuesException urnfe) {
      assertTrue(urnfe.getMessage().contains("role"));
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editMod1ByAdminEntityNotFoundException() {
    // modificare entity con valore inesistente

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("entityId",3);

    when(entityRepo.findById(3)).thenReturn(Optional.empty());

    try {
      userService.editByAdministrator(mod1, fieldsToEdit, false);
      fail();
    } catch (InvalidFieldsValuesException urnfe) {
      assertTrue(urnfe.getMessage().contains("entity"));
      assertTrue(true);
    } catch (MissingFieldsException | NotAuthorizedException | ConflictException e) {
      System.out.println(e);
      fail();
    }
  }

  @Test
  public void editAdmin2ByAdmin1NotAllowedToEditException() {
    // modificare name, surname, email, deleted

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("type",1);

    try {
      userService.editByAdministrator(admin2, fieldsToEdit, false);

      fail();
    } catch (NotAuthorizedException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  // editByModerator method tests
  @Test
  public void editUser2ByMod1Successfull() {
    // modificare name, surname, email, deleted

    String newName = "newName";
    String newSurname = "newSurname";
    String newEmail = "newEmail";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name",newName);
    fieldsToEdit.put("surname",newSurname);
    fieldsToEdit.put("email",newEmail);

    User editedUser = cloneUser(user2);
    editedUser.setName(newName);
    editedUser.setSurname(newSurname);
    editedUser.setEmail(newEmail);

    try {
      User user = userService.editByModerator(user2, fieldsToEdit, false);

      assertNotNull(user);
      assertEquals(editedUser.getName(), user.getName());
      assertEquals(editedUser.getSurname(), user.getSurname());
      assertEquals(editedUser.getEmail(), user.getEmail());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editMod11ByItselfSuccessfull() {
    // modificare name, surname, email, deleted

    String newName = "newName";

    mod11.setTelegramChat("TC");

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name",newName);
    fieldsToEdit.put("twoFactorAuthentication", true);

    User editedUser = cloneUser(mod11);
    editedUser.setName(newName);
    editedUser.setTfa(true);

    try {
      User user = userService.editByModerator(mod11, fieldsToEdit, true);

      assertNotNull(user);
      assertEquals(editedUser.getName(), user.getName());
      assertEquals(editedUser.getTfa(), user.getTfa());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editMod11ByItselfNotAllowedToEditException() {
    // modificare name, surname, email, deleted

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("entityId",2);

    try {
      userService.editByModerator(mod11, fieldsToEdit, true);

      fail();
    } catch (NotAuthorizedException e) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editUser1ByItselfEditNotAllowed() {
    // modificare name

    String newName = "newName";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name",newName);

    try {
      userService.editByUser(user1, fieldsToEdit);

      fail();
    } catch (NotAuthorizedException natde) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editUser2ByItselfTfaNotPermittedException() {
    // set telegram name and tfa true

    String newTelegramName = "newName";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("telegramName",newTelegramName);
    fieldsToEdit.put("twoFactorAuthentication", true);

    try {
      userService.editByUser(user1, fieldsToEdit);

      fail();
    } catch (ConflictException ce) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editUser2ByItselfWithNotExistentfields() {
    // set telegram name and tfa true

    String newTelegramName = "newName";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("telegramaName",newTelegramName);
    fieldsToEdit.put("tfa", true);

    try {
      userService.editByUser(user1, fieldsToEdit);

      fail();
    } catch (MissingFieldsException mfe) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  // findAllByEntityId method test
  @Test
  public void findAllByEntityIdSuccessfull() {
    List<User> users = userService.findAllByEntityId(1);

    assertFalse(users.isEmpty());
  }

  @Test
  public void findAllByNotExistentEntityIdEmptyList() {
    List<User> users = userService.findAllByEntityId(3);

    assertTrue(users.isEmpty());
  }

  @Test
  public void deleteUser1SuccesfullyByAdmin1Test() {
    try {
      User expected = user1;
      User actual = userService.deleteUser(admin1, user1.getId());
      assertEquals(expected, actual);
      assertTrue(actual.isDeleted());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void deleteUser1SuccesfullyByAdmin2Test() {
    try {
      User expected = user1;
      User actual = userService.deleteUser(admin2, user1.getId());
      assertEquals(expected, actual);
      assertTrue(actual.isDeleted());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void deleteUser1SuccesfullyByMod1Test() {
    try {
      User expected = user1;
      User actual = userService.deleteUser(mod1, user1.getId());
      assertEquals(expected, actual);
      assertTrue(actual.isDeleted());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void deleteUser1NotAuthorizedToDeleteUserExceptionByUser2Test() {
    try {
      User expected = user1;
      User actual = userService.deleteUser(user2, user1.getId());
      assertEquals(expected, actual);
      assertFalse(actual.isDeleted());
    } catch (NotAuthorizedException e) {
      assertTrue(true);
    } catch (InvalidFieldsValuesException e) {
      fail();
    }
  }

  @Test
  public void deleteUser1NotAuthorizedToDeleteUserExceptionByMod11Test() {
      try {
        User expected = user1;
        User actual = userService.deleteUser(mod2, user1.getId());
        assertEquals(expected, actual);
        assertFalse(actual.isDeleted());
      } catch (NotAuthorizedException e) {
        assertTrue(true);
      } catch (InvalidFieldsValuesException e) {
        fail();
      }
  }
}
