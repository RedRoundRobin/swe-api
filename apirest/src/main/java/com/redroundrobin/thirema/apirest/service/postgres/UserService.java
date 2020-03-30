package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.exception.EntityNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.TfaNotPermittedException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedToDeleteUserException;
import com.redroundrobin.thirema.apirest.utils.exception.UserRoleNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.ValuesNotAllowedException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedToInsertUserException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import com.redroundrobin.thirema.apirest.utils.exception.TelegramChatNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAllowedToEditException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

  private UserRepository repo;

  private AlertService alertService;

  private EntityService entityService;

  private boolean checkCreatableFields(Set<String> keys)
      throws KeysNotFoundException {
    Set<String> creatable = new HashSet<>();
    creatable.add("name");
    creatable.add("surname");
    creatable.add("email");
    creatable.add("type");
    creatable.add("entity_id");
    creatable.add("password");  //SOLUZIONE TEMPORANEA: NON PREVISTA DA USE CASES

    boolean onlyCreatableKeys = keys.stream()
        .filter(key -> !creatable.contains(key))
        .count() == 0;

    if (!onlyCreatableKeys)
      throw new KeysNotFoundException("There are some keys that either do not exist or you are not" +
          "allowed to edit them");

    return creatable.size() == keys.size();
  }

  public UserService(UserRepository userRepository) {
    this.repo = userRepository;
  }

  @Autowired
  public void setAlertService(AlertService alertService) {
    this.alertService = alertService;
  }

  @Autowired
  public void setEntityService(EntityService entityService) {
    this.entityService = entityService;
  }

  private boolean checkFieldsEditable(User.Role role, boolean itself, Set<String> keys)
      throws KeysNotFoundException {
    Set<String> editable = new HashSet<>();
    editable.add("name");
    editable.add("surname");
    editable.add("email");
    editable.add("password");
    editable.add("type");
    editable.add("telegram_name");
    editable.add("two_factor_authentication");
    editable.add("deleted");
    editable.add("entity_id");

    boolean onlyExistingKeys = keys.stream()
        .filter(key -> !editable.contains(key))
        .count() == 0;

    if (!onlyExistingKeys) {
      throw new KeysNotFoundException("There are some keys that don't exist");
    } else {
      switch (role) {
        case ADMIN:
          if (itself) {
            editable.remove("type");
            editable.remove("deleted");
            editable.remove("entity_id");
          }

          break;
        case MOD:
          if (itself) {
            editable.remove("deleted");
          } else {
            editable.remove("password");
            editable.remove("telegram_name");
            editable.remove("two_factor_authentication");
          }
          editable.remove("type");
          editable.remove("entity_id");

          break;
        case USER:
          editable.remove("name");
          editable.remove("surname");
          editable.remove("type");
          editable.remove("deleted");
          editable.remove("entity_id");

          break;
        default:
          editable.clear();
      }

      return keys.stream()
          .filter(key -> !editable.contains(key))
          .count() == 0;
    }
  }

  private User editAndSave(User userToEdit, Map<String, Object> fieldsToEdit)
      throws EntityNotFoundException, TfaNotPermittedException, UserRoleNotFoundException {
    if (fieldsToEdit.containsKey("two_factor_authentication")
        && (boolean)fieldsToEdit.get("two_factor_authentication")
        && (fieldsToEdit.containsKey("telegram_name")
        || userToEdit.getTelegramChat().isEmpty())) {
      throw new TfaNotPermittedException("TFA can't be edited because either telegram_name is "
          + "in the request or telegram chat is not present");
    }


    if (fieldsToEdit.containsKey("entity_id")
        && entityService.findById((int)fieldsToEdit.get("entity_id")) == null) {
      throw new EntityNotFoundException("The entity with the entityId given doesn't exist");
    }

    for (Map.Entry<String, Object> entry : fieldsToEdit.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      switch (key) {
        case "name":
          userToEdit.setName((String) value);
          break;
        case "surname":
          userToEdit.setSurname((String) value);
          break;
        case "email":
          userToEdit.setEmail((String) value);
          break;
        case "password":
          userToEdit.setPassword((String) value);
          break;
        case "type":
          try {
            userToEdit.setType(User.Role.values()[(int)value]);
          } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException iae) {
            throw new UserRoleNotFoundException("The inserted role is not found");
          }
          break;
        case "telegram_name":
          userToEdit.setTelegramName((String) value);
          userToEdit.setTfa(false);
          userToEdit.setTelegramChat(null);
          break;
        case "two_factor_authentication":
          userToEdit.setTfa((boolean) value);
          break;
        case "deleted":
          userToEdit.setDeleted((boolean) value);
          break;
        case "entity_id":
          userToEdit.setEntity(entityService.findById((int) value));
          break;
        default:
      }
    }

    return save(userToEdit);
  }

  public List<User> findAll() {
    return (List<User>) repo.findAll();
  }

  public List<User> findAllByEntityId(int entityId) throws EntityNotFoundException {
    Entity entity = entityService.findById(entityId);
    if (entity != null) {
      return (List<User>) repo.findAllByEntity(entity);
    } else {
      throw new EntityNotFoundException("Entity with id furnished not found");
    }
  }

  public List<User> findAllByDisabledAlerts(List<Integer> alertsIds) {
    List<Alert> alerts = new ArrayList<>();
    alertsIds.forEach(aid -> {
      Alert alert = alertService.findById(aid);
      if (alert != null) {
        alerts.add(alert);
      }
    });
    if (!alerts.isEmpty()) {
      return (List<User>) repo.findAllByDisabledAlertsIn(alerts);
    } else {
      return Collections.emptyList();
    }
  }

  public List<User> findAllByDisabledAlert(int alertId) {
    Alert alert = alertService.findById(alertId);
    if (alert != null) {
      return (List<User>) repo.findAllByDisabledAlerts(alert);
    } else {
      return Collections.emptyList();
    }
  }

  public User findById(int id) {
    Optional<User> optUser = repo.findById(id);
    return optUser.orElse(null);
  }

  public User findByTelegramName(String telegramName) {
    return repo.findByTelegramName(telegramName);
  }

  public User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat) {
    return repo.findByTelegramNameAndTelegramChat(telegramName, telegramChat);
  }

  public User findByEmail(String email) {
    return repo.findByEmail(email);
  }

  public User save(User user) {
    return repo.save(user);
  }

  public List<Device> userDevices(int userId) {
    return repo.userDevices(userId);
  }

  @Override
  public UserDetails loadUserByUsername(String s) {
    User user = this.findByEmail(s);
    if (user == null || (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted())))) {
      throw new UsernameNotFoundException("");
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), grantedAuthorities);
  }

  /**
   * Method that return the UserDetails created with the email furnished as @s and the
   * password taken from the database.
   *
   * @param s the email to create the UserDetails
   * @return the UserDetails generated from the email and the password.
   * @throws UsernameNotFoundException thrown if no user with furnished email found.
   * @throws UserDisabledException thrown if user with furnished email found but the user is
  disabled.
   */
  public UserDetails loadUserByEmail(String s) throws UserDisabledException {
    User user = this.findByEmail(s);
    if (user == null) {
      throw new UsernameNotFoundException("");
    } else if (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new UserDisabledException("User has been deleted or don't have an entity");
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), grantedAuthorities);
  }

  /**
   * Method that return the UserDetails created with the telegram name given as @s and the
   * chat id taken from the database.
   *
   * @param s the telegram name to create the UserDetails
   * @return the UserDetails generated from the telegram name and the chat id.
   * @throws UsernameNotFoundException thrown if no telegram name found.
   * @throws UserDisabledException thrown if telegram name found but the user is disabled.
   */
  public UserDetails loadUserByTelegramName(String s)
      throws UserDisabledException, TelegramChatNotFoundException {
    User user = this.findByTelegramName(s);
    if (user == null) {
      throw new UsernameNotFoundException("");
    } else if (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new UserDisabledException("User has been deleted or don't have an entity");
    } else if (user.getTelegramChat() == null || user.getTelegramChat().isEmpty()) {
      throw new TelegramChatNotFoundException();
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getTelegramName(), user.getTelegramChat(), grantedAuthorities);
  }


  public User editByUser(User userToEdit, Map<String, Object> fieldsToEdit)
      throws NotAllowedToEditException, KeysNotFoundException, EntityNotFoundException,
      TfaNotPermittedException, UserRoleNotFoundException {

    if (!checkFieldsEditable(User.Role.USER, true, fieldsToEdit.keySet())) {
      throw new NotAllowedToEditException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByModerator(User userToEdit, boolean itself, Map<String, Object> fieldsToEdit)
      throws NotAllowedToEditException, KeysNotFoundException, EntityNotFoundException,
      TfaNotPermittedException, UserRoleNotFoundException {

    if (!checkFieldsEditable(User.Role.MOD, itself, fieldsToEdit.keySet())) {
      throw new NotAllowedToEditException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByAdministrator(User userToEdit, boolean itself,
                                  Map<String, Object> fieldsToEdit)
      throws NotAllowedToEditException, KeysNotFoundException, EntityNotFoundException,
      TfaNotPermittedException, UserRoleNotFoundException {

    if ((!itself && userToEdit.getType() == User.Role.ADMIN)
        || (fieldsToEdit.containsKey("type") && (int)fieldsToEdit.get("type") == 2)
        || !checkFieldsEditable(User.Role.ADMIN, itself, fieldsToEdit.keySet())) {
      throw new NotAllowedToEditException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User serializeUser(JsonObject rawUserToInsert, User insertingUser)
      throws KeysNotFoundException, MissingFieldsException, ValuesNotAllowedException,
      UserRoleNotFoundException, EntityNotFoundException, NotAuthorizedToInsertUserException {
    if(!checkCreatableFields(rawUserToInsert.keySet())) {
      throw new MissingFieldsException("Some necessary fields are missing: cannot create user");
    }

    Entity userToInsertEntity;
    if ((userToInsertEntity = entityService.findById(
        (rawUserToInsert.get("entity_id")).getAsInt())) == null) {
      throw new EntityNotFoundException("The entity with the entityId given doesn't exist");
    }

    User.Role userToInsertType;
    try {
        userToInsertType = User.Role.valueOf(rawUserToInsert.get("type").getAsString());
    }
    catch (IllegalArgumentException iae) {
      throw new UserRoleNotFoundException("The given role doesn't exist");
    }
    catch (NullPointerException nptr) {
      throw new UserRoleNotFoundException("The type parameter cannot be null");
    }

    //qui so che entity_id dato esiste && so il tipo dello user che si vuole inserire
    //NB: IL MODERATORE PUO INSERIRE SOLO MEMBRI!! VA BENE??
    if(insertingUser.getType() != User.Role.ADMIN &&
        insertingUser.getType() == User.Role.MOD &&
        (userToInsertEntity.getId() != insertingUser.getEntity().getId()
        || userToInsertType != User.Role.USER) )
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
      if (email != null && repo.findByEmail(email) == null)
        newUser.setEmail(email);
      else throw new ValuesNotAllowedException("Either the email field you inserted is" +
          "null or it is already used");

      return save(newUser);
    }

  public User deleteUser(User deletingUser, int userToDeleteId)
      throws NotAuthorizedToDeleteUserException, ValuesNotAllowedException {
    User userToDelete;
    if((userToDelete = findById(userToDeleteId)) == null) {
      throw new ValuesNotAllowedException("The given user_id doesn't correspond to any user");
    }

    if(deletingUser.getType() == User.Role.USER
        || deletingUser.getType() == User.Role.MOD && (
            userToDelete.getType() != User.Role.USER))
      throw new NotAuthorizedToDeleteUserException("This user cannot delete the user with" +
          "the user_id given");

    userToDelete.setDeleted(true);
    userToDelete.setEntity(null);
    return save(userToDelete);
  }
}