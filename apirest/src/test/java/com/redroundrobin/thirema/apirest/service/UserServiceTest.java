package com.redroundrobin.thirema.apirest.service;

import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.SerializeUser;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

  @MockBean
  private UserRepository userRepo;

  @MockBean
  private EntityService entityService;

  @MockBean
  private SerializeUser serializeUser;

  private UserService userService;

  private User admin1;
  private User admin2;
  private User mod1;
  private User mod11;
  private User user1;
  private User user2;

  private Entity entity1;
  private Entity entity2;

  @Before
  public void setUp() {
    userService = new UserService(userRepo, entityService, serializeUser);

    admin1 = new User();
    admin1.setUserId(1);
    admin1.setName("admin1");
    admin1.setSurname("admin1");
    admin1.setEmail("admin1");
    admin1.setTelegramName("TNAdmin1");
    admin1.setPassword("password");
    admin1.setType(User.Role.ADMIN);

    admin2 = new User();
    admin2.setUserId(2);
    admin2.setName("admin2");
    admin2.setSurname("admin2");
    admin2.setEmail("admin2");
    admin2.setTelegramName("TNAdmin2");
    admin2.setPassword("password");
    admin2.setType(User.Role.ADMIN);

    entity1 = new Entity();
    entity1.setEntityId(1);

    mod1 = new User();
    mod1.setUserId(3);
    mod1.setName("mod1");
    mod1.setSurname("mod1");
    mod1.setEmail("mod1");
    mod1.setTelegramName("TNmod1");
    mod1.setPassword("password");
    mod1.setType(User.Role.MOD);
    mod1.setEntity(entity1);

    mod11 = new User();
    mod11.setUserId(4);
    mod11.setName("mod11");
    mod11.setSurname("mod11");
    mod11.setEmail("mod11");
    mod11.setTelegramName("TNmod11");
    mod11.setPassword("password");
    mod11.setType(User.Role.MOD);
    mod11.setEntity(entity1);

    user1 = new User();
    user1.setUserId(5);
    user1.setName("user1");
    user1.setSurname("user1");
    user1.setEmail("user1");
    user1.setTelegramName("TNuser1");
    user1.setPassword("password");
    user1.setType(User.Role.USER);
    user1.setEntity(entity1);

    entity2 = new Entity();
    entity2.setEntityId(2);

    user2 = new User();
    user2.setUserId(6);
    user2.setName("user2");
    user2.setSurname("user2");
    user2.setEmail("user2");
    user2.setTelegramName("TNuser2");
    user2.setPassword("password");
    user2.setType(User.Role.USER);
    user2.setEntity(entity2);

    List<User> allUsers = new ArrayList<>();
    allUsers.add(admin1);
    allUsers.add(admin2);
    allUsers.add(mod1);
    allUsers.add(mod11);
    allUsers.add(user1);
    allUsers.add(user2);

    when(userRepo.findAll()).thenReturn(allUsers);
    when(userRepo.findById(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      Optional<User> userFound = allUsers.stream()
          .filter(user -> user.getUserId() == id)
          .findFirst();
      return userFound;
    });

    when(userRepo.findByTelegramNameAndTelegramChat(anyString(), anyString())).thenAnswer(i -> {
      String tn = i.getArgument(0);
      String tc = i.getArgument(1);
      Optional<User> userFound = allUsers.stream()
          .filter(user -> user.getTelegramName() == tn && user.getTelegramChat() == tc)
          .findFirst();
      return userFound.isPresent() ? userFound.get() : null;
    });

    List<Device> devices = new ArrayList<>();
    when(userRepo.userDevices(anyInt())).thenReturn(devices);

    when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    when(entityService.find(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      if (id == 1) {
        return entity1;
      } else if (id == 2) {
        return entity2;
      } else {
        return null;
      }
    });

    when(userRepo.findAllByEntity(any(Entity.class))).thenAnswer(i -> {
      Entity ent = i.getArgument(0);
      List<User> users = allUsers.stream()
          .filter(user -> user.getEntity() == ent).collect(Collectors.toList());
      return users;
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



  // findAll method tests
  @Test
  public void findAllSuccessfull() {
    List<User> users = userService.findAll();
    assertTrue(!users.isEmpty());
  }



  // findById method tests
  @Test
  public void findSuccessfull() {
    User user = userService.find(6);
    assertEquals(user2, user);
  }



  // findById method tests
  @Test
  public void userDevicesEmpty() {
    List<Device> devices = userService.userDevices(6);
    assertTrue(devices.isEmpty());
  }



  // findByTelegramNameAndTelegramChat method tests
  @Test
  public void findByTelegramNameAndTelegramChatNull() {
    User user = userService.findByTelegramNameAndTelegramChat("name", "chat");
    assertNull(user);
  }



  // loadByUsername method tests
  @Test
  public void loadUser1ByNameSuccessfull() throws Exception {

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(user1);

    try {
      UserDetails userDetails = userService.loadUserByUsername(user1.getEmail());

      assertNotNull(userDetails);
      assertTrue(userDetails.getUsername() == user1.getEmail());
      assertTrue(userDetails.getPassword() == user1.getPassword());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertTrue(((SimpleGrantedAuthority) userDetails.getAuthorities().stream().findFirst().get())
          .toString().equals(String.valueOf(user1.getType())));
    } catch (UsernameNotFoundException unfe) {
      assertTrue(false);
    }
  }

  @Test
  public void loadAdmin1ByNameSuccessfull() {

    when(userRepo.findByEmail(admin1.getEmail())).thenReturn(admin1);

    try {
      UserDetails userDetails = userService.loadUserByUsername(admin1.getEmail());

      assertNotNull(userDetails);
      assertTrue(userDetails.getUsername() == admin1.getEmail());
      assertTrue(userDetails.getPassword() == admin1.getPassword());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertTrue(((SimpleGrantedAuthority) userDetails.getAuthorities().stream().findFirst().get())
          .toString().equals(String.valueOf(admin1.getType())));
    } catch (UsernameNotFoundException unfe) {
      assertTrue(false);
    }
  }

  @Test
  public void loadUser1ByNameThrowUsernameNotFoundException() {

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(null);

    try {
      UserDetails userDetails = userService.loadUserByUsername(user1.getEmail());
      assertTrue(false);
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
      assertTrue(userDetails.getUsername() == user2.getEmail());
      assertTrue(userDetails.getPassword() == user2.getPassword());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertTrue(((SimpleGrantedAuthority) userDetails.getAuthorities().stream().findFirst().get())
          .toString().equals(String.valueOf(user2.getType())));
    } catch(UsernameNotFoundException | UserDisabledException ue) {
      assertTrue(false);
    }
  }

  @Test
  public void loadUser1ByEmailThrowUsernameNotFoundException() {

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(null);

    try {
      UserDetails userDetails = userService.loadUserByEmail(user1.getEmail());
      assertTrue(false);
    } catch (UsernameNotFoundException unfe) {
      assertTrue(true);
    } catch (UserDisabledException ude) {
      assertTrue(false);
    }
  }

  @Test
  public void loadUser1ByEmailThrowUserDisabledException() {

    user1.setEntity(null);

    when(userRepo.findByEmail(user1.getEmail())).thenReturn(user1);

    try {
      UserDetails userDetails = userService.loadUserByEmail(user1.getEmail());
      assertTrue(false);
    } catch (UsernameNotFoundException unfe) {
      assertTrue(false);
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
      assertTrue(userDetails.getUsername() == mod1.getTelegramName());
      assertTrue(userDetails.getPassword() == mod1.getTelegramChat());
      assertTrue(userDetails.getAuthorities().stream().findFirst().isPresent());
      assertTrue(((SimpleGrantedAuthority) userDetails.getAuthorities().stream().findFirst().get())
          .toString().equals(String.valueOf(mod1.getType())));
    } catch(UsernameNotFoundException | UserDisabledException ue) {
      assertTrue(false);
    } catch (TelegramChatNotFoundException tcnfe) {
      assertTrue(false);
    }
  }

  @Test
  public void loadMod1ByTelegramNameThrowTelegramChatNotFoundException() {

    when(userRepo.findByTelegramName(mod1.getTelegramName())).thenReturn(mod1);

    try {
      UserDetails userDetails = userService.loadUserByTelegramName(mod1.getTelegramName());

      assertTrue(false);
    } catch(UsernameNotFoundException | UserDisabledException ue) {
      assertTrue(false);
    } catch (TelegramChatNotFoundException tcnfe) {
      assertTrue(true);
    }
  }

  @Test
  public void loadUser2ByTelegramNameThrowUsernameNotFoundException() {

    when(userRepo.findByTelegramName(user2.getTelegramName())).thenReturn(null);

    try {
      UserDetails userDetails = userService.loadUserByTelegramName(user2.getTelegramName());
      assertTrue(false);
    } catch (UsernameNotFoundException unfe) {
      assertTrue(true);
    } catch (UserDisabledException | TelegramChatNotFoundException ude) {
      assertTrue(false);
    }
  }

  @Test
  public void loadMod11ByTelegramNameThrowUserDisabledException() {

    mod11.setDeleted(true);

    when(userRepo.findByTelegramName(mod11.getTelegramName())).thenReturn(mod11);

    try {
      UserDetails userDetails = userService.loadUserByTelegramName(mod11.getTelegramName());
      assertTrue(false);
    } catch (UsernameNotFoundException | TelegramChatNotFoundException unfe) {
      assertTrue(false);
    } catch (UserDisabledException ude) {
      assertTrue(true);
    }
  }



  // serializeUser method tests
  /*@Test
  public void serializeUserSuccessfull() {

  }*/



  // editByAdmin method tests
  @Test
  public void editAdminByItselfSuccessfull() {
    // modificare tutto

    String newTelegramName = "newTelegramName";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("telegram_name",newTelegramName);

    User editedUser = cloneUser(admin1);
    editedUser.setTelegramName(newTelegramName);
    editedUser.setTfa(false);
    editedUser.setTelegramChat(null);

    try {
      User user = userService.editByAdministrator(admin1, true, fieldsToEdit);

      assertNotNull(user);
      assertEquals(editedUser.getTelegramName(), user.getTelegramName());
      assertEquals(editedUser.getTelegramChat(), user.getTelegramChat());
      assertEquals(editedUser.getTfa(), user.getTfa());
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
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
    boolean newDeleted = true;
    int newEntityId = entity2.getEntityId();

    HashMap<String, Object> fieldsToEdit = new HashMap<>();

    fieldsToEdit.put("name",newName);
    fieldsToEdit.put("surname",newSurname);
    fieldsToEdit.put("email",newEmail);
    fieldsToEdit.put("password",newPassword);
    fieldsToEdit.put("type",0);
    fieldsToEdit.put("telegram_name",newTelegramName);
    fieldsToEdit.put("deleted", newDeleted);
    fieldsToEdit.put("entity_id", newEntityId);

    User editedUser = cloneUser(user1);
    editedUser.setName(newName);
    editedUser.setSurname(newSurname);
    editedUser.setEmail(newEmail);
    editedUser.setPassword(newPassword);
    editedUser.setType(newType);
    editedUser.setTelegramName(newTelegramName);
    editedUser.setEntity(entity2);

    when(entityService.find(newEntityId)).thenReturn(entity2);

    try {
      User user = userService.editByAdministrator(user1, false, fieldsToEdit);

      assertNotNull(user);
      assertEquals(editedUser.getName(), user.getName());
      assertEquals(editedUser.getSurname(), user.getSurname());
      assertEquals(editedUser.getEmail(), user.getEmail());
      assertEquals(editedUser.getPassword(), user.getPassword());
      assertEquals(editedUser.getType(), user.getType());
      assertEquals(editedUser.getTelegramName(), user.getTelegramName());
      assertEquals(editedUser.getEntity(), user.getEntity());
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void editUser2ByAdminWithNotExistentType() {
    // settare ad admin type 3

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("type",3);

    try {
      User user = userService.editByAdministrator(user1, false, fieldsToEdit);
      assertTrue(false);
    } catch (UserRoleNotFoundException urnfe) {
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void editMod1ByAdminEntityNotFoundException() {
    // modificare entity con valore inesistente

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("entity_id",3);

    when(entityService.find(3)).thenReturn(null);

    try {
      User user = userService.editByAdministrator(mod1, false, fieldsToEdit);
      assertTrue(false);
    } catch (EntityNotFoundException urnfe) {
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void editAdmin2ByAdmin1NotAllowedToEditException() {
    // modificare name, surname, email, deleted

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("type",1);

    try {
      User user = userService.editByAdministrator(admin2, false, fieldsToEdit);

      assertTrue(false);
    } catch (NotAllowedToEditException e) {
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }



  // editByModerator method tests
  @Test
  public void editUser2ByMod1Successfull() {
    // modificare name, surname, email, deleted

    String newName = "newName";
    String newSurname = "newSurname";
    String newEmail = "newEmail";
    boolean newDeleted = true;

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name",newName);
    fieldsToEdit.put("surname",newSurname);
    fieldsToEdit.put("email",newEmail);
    fieldsToEdit.put("deleted", newDeleted);

    User editedUser = cloneUser(user2);
    editedUser.setName(newName);
    editedUser.setSurname(newSurname);
    editedUser.setEmail(newEmail);
    editedUser.setDeleted(newDeleted);

    try {
      User user = userService.editByModerator(user2, false, fieldsToEdit);

      assertNotNull(user);
      assertEquals(editedUser.getName(), user.getName());
      assertEquals(editedUser.getSurname(), user.getSurname());
      assertEquals(editedUser.getEmail(), user.getEmail());
      assertEquals(editedUser.isDeleted(), user.isDeleted());
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void editMod11ByItselfSuccessfull() {
    // modificare name, surname, email, deleted

    String newName = "newName";
    boolean tfa = true;

    mod11.setTelegramChat("TC");

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name",newName);
    fieldsToEdit.put("two_factor_authentication",tfa);

    User editedUser = cloneUser(mod11);
    editedUser.setName(newName);
    editedUser.setTfa(tfa);

    try {
      User user = userService.editByModerator(mod11, true, fieldsToEdit);

      assertNotNull(user);
      assertEquals(editedUser.getName(), user.getName());
      assertEquals(editedUser.getTfa(), user.getTfa());
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void editMod11ByItselfNotAllowedToEditException() {
    // modificare name, surname, email, deleted

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("entity_id",2);

    try {
      User user = userService.editByModerator(mod11, true, fieldsToEdit);

      assertTrue(false);
    } catch (NotAllowedToEditException e) {
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }



  // editByUser method tests
  @Test
  public void editUser1ByItselfKeysNotFoundException() {
    // modificare telegramName

    String newTelegramName = "newTelegramName";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("telegramName",newTelegramName);

    try {
      User user = userService.editByUser(user1, fieldsToEdit);

      assertTrue(false);
    } catch (KeysNotFoundException knfe) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void editUser1ByItselfEditNotAllowed() {
    // modificare name

    String newName = "newName";

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name",newName);

    try {
      User user = userService.editByUser(user1, fieldsToEdit);

      assertTrue(false);
    } catch (NotAllowedToEditException natde) {
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void editUser2ByItselfTfaNotPermittedException() {
    // set telegram name and tfa true

    String newTelegramName = "newName";
    boolean newTfa = true;

    HashMap<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("telegram_name",newTelegramName);
    fieldsToEdit.put("two_factor_authentication",newTfa);

    try {
      User user = userService.editByUser(user1, fieldsToEdit);

      assertTrue(false);
    } catch (TfaNotPermittedException tnpe) {
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }



  // findAllByEntityId method test
  @Test
  public void findAllByEntityIdSuccessfull() {
    try {
      List<User> users = userService.findAllByEntityId(1);

      assertTrue(!users.isEmpty());
    } catch (EntityNotFoundException enfe) {
      assertTrue(false);
    }
  }

  @Test
  public void findAllByEntityIdEntityNotFoundException() {
    try {
      List<User> users = userService.findAllByEntityId(3);

      assertTrue(false);
    } catch (EntityNotFoundException enfe) {
      assertTrue(true);
    }
  }
}
