package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ConflictException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import com.redroundrobin.thirema.apirest.utils.exception.TelegramChatNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import com.redroundrobin.thirema.apirest.utils.exception.ValuesNotAllowedException;
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
      throws ValuesNotAllowedException {
    Set<String> creatable = new HashSet<>();
    creatable.add("name");
    creatable.add("surname");
    creatable.add("email");
    creatable.add("type");
    creatable.add("entityId");
    creatable.add("password");

    boolean onlyCreatableKeys = keys.stream()
        .filter(key -> !creatable.contains(key))
        .count() == 0;

    if (!onlyCreatableKeys) {
      throw new ValuesNotAllowedException();
    }

    return creatable.size() == keys.size();
  }

  private boolean checkFieldsEditable(User.Role role, boolean itself, Set<String> keys)
      throws MissingFieldsException {
    Map<String, Boolean> userFields = new HashMap<>();
    userFields.put("name", true);
    userFields.put("surname", true);
    userFields.put("email", true);
    userFields.put("password", true);
    userFields.put("type", true);
    userFields.put("telegramName", true);
    userFields.put("twoFactorAuthentication", true);
    userFields.put("entityId", true);
    userFields.put("deleted", false);

    switch (role) {
      case ADMIN:
        if (itself) {
          userFields.replace("type", false);
          userFields.replace("entityId", false);
        }

        break;
      case MOD:
        if (!itself) {
          userFields.replace("password", false);
          userFields.replace("telegramName", false);
          userFields.replace("twoFactorAuthentication", false);
        }
        userFields.replace("type", false);
        userFields.replace("entityId", false);

        break;
      case USER:
        userFields.replace("name", false);
        userFields.replace("surname", false);
        userFields.replace("type", false);
        userFields.replace("entityId", false);

        break;
      default:
        return false;
    }

    List<String> editable = new ArrayList<>();
    List<String> notEditable = new ArrayList<>();
    userFields.entrySet().stream()
        .forEach(e -> {
          if (e.getValue()) {
            editable.add(e.getKey());
          } else {
            notEditable.add(e.getKey());
          }
        });
    if (!keys.stream().anyMatch(k -> userFields.keySet().contains(k))) {
      throw new MissingFieldsException("There aren't fields that can be edited");
    } else {
      return keys.stream().allMatch(k -> editable.contains(k) || !notEditable.contains(k));
    }
  }

  private User editAndSave(User userToEdit, Map<String, Object> fieldsToEdit)
      throws ConflictException, InvalidFieldsValuesException {
    if (fieldsToEdit.containsKey("twoFactorAuthentication")
        && (boolean)fieldsToEdit.get("twoFactorAuthentication")
        && (fieldsToEdit.containsKey("telegramName")
        || userToEdit.getTelegramChat().isEmpty())) {
      throw new ConflictException("TFA can't be edited because either telegramName is "
          + "in the request or telegram chat not present");
    }

    if (fieldsToEdit.containsKey("entityId")
        && entityService.findById((int)fieldsToEdit.get("entityId")) == null) {
      throw new InvalidFieldsValuesException(
          "The entity with the entityId furnished doesn't exist");
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
            throw new InvalidFieldsValuesException("The inserted role is not found");
          }
          break;
        case "telegramName":
          userToEdit.setTelegramName((String) value);
          userToEdit.setTfa(false);
          userToEdit.setTelegramChat(null);
          break;
        case "twoFactorAuthentication":
          userToEdit.setTfa((boolean) value);
          break;
        case "entityId":
          userToEdit.setEntity(entityService.findById((int) value));
          break;
        default:
      }
    }

    return save(userToEdit);
  }

  @Autowired
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

  public List<User> findAll() {
    return (List<User>) repo.findAll();
  }

  public List<User> findAllByEntityId(int entityId) {
    Entity entity = entityService.findById(entityId);
    if (entity != null) {
      return (List<User>) repo.findAllByEntity(entity);
    } else {
      return Collections.emptyList();
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
      throws InvalidFieldsValuesException, MissingFieldsException, NotAuthorizedException,
      ConflictException  {

    if (!checkFieldsEditable(User.Role.USER, true, fieldsToEdit.keySet())) {
      throw new NotAuthorizedException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByModerator(User userToEdit, boolean itself, Map<String, Object> fieldsToEdit)
      throws InvalidFieldsValuesException, MissingFieldsException, NotAuthorizedException,
      ConflictException {

    if ((fieldsToEdit.containsKey("type") && (int)fieldsToEdit.get("type") > 0)
        || !checkFieldsEditable(User.Role.MOD, itself, fieldsToEdit.keySet())) {
      throw new NotAuthorizedException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByAdministrator(User userToEdit, boolean itself,
                                  Map<String, Object> fieldsToEdit)
      throws InvalidFieldsValuesException, MissingFieldsException, NotAuthorizedException,
      ConflictException {

    if ((!itself && userToEdit.getType() == User.Role.ADMIN)
        || (fieldsToEdit.containsKey("type") && (int)fieldsToEdit.get("type") == 2)
        || !checkFieldsEditable(User.Role.ADMIN, itself, fieldsToEdit.keySet())) {
      throw new NotAuthorizedException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User serializeUser(JsonObject rawUserToInsert, User insertingUser)
      throws MissingFieldsException, ValuesNotAllowedException,
      ConflictException, NotAuthorizedException {
    if (!checkCreatableFields(rawUserToInsert.keySet())) {
      throw new MissingFieldsException();
    }

    Entity userToInsertEntity;
    if ((userToInsertEntity = entityService.findById(
        (rawUserToInsert.get("entityId")).getAsInt())) == null) {
      throw new ValuesNotAllowedException();
    }

    int userToInsertType;
    if ((userToInsertType =
        rawUserToInsert.get("type").getAsInt()) == 2 ||
        userToInsertType != 1 && userToInsertType != 0) {
        throw new ValuesNotAllowedException();
    }

    //qui so che entity_id dato esiste && so il tipo dello user che si vuole inserire
    if (insertingUser.getType() == User.Role.USER
        || (insertingUser.getType() == User.Role.MOD
        && userToInsertEntity.getId() != insertingUser.getEntity().getId())) {
      throw new NotAuthorizedException("");
    }

    User newUser = new User();
    newUser.setEntity(userToInsertEntity);
    newUser.setType(User.Role.values()[(int)userToInsertType]);

    if (rawUserToInsert.get("name").getAsString() != null
        || rawUserToInsert.get("surname").getAsString() != null
        || rawUserToInsert.get("password").getAsString() != null) {
      newUser.setName(rawUserToInsert.get("name").getAsString());
      newUser.setSurname(rawUserToInsert.get("surname").getAsString());
      newUser.setPassword(rawUserToInsert.get("password").getAsString());
    } else {
      throw new ValuesNotAllowedException();
    }

    String email = rawUserToInsert.get("email").getAsString();
    if(email != null && repo.findByEmail(email) == null) {
      newUser.setEmail(email);
    } else if (email == null) {
      throw new ValuesNotAllowedException();
    } else {
      throw new ConflictException("");
    }

    return save(newUser);
  }

  public User deleteUser(User deletingUser, int userToDeleteId)
      throws NotAuthorizedException, ValuesNotAllowedException {
    User userToDelete;
    if ((userToDelete = findById(userToDeleteId)) == null) {
      throw new ValuesNotAllowedException();
    }

    if (deletingUser.getType() == User.Role.USER
        || deletingUser.getType() == User.Role.MOD
        && ((userToDelete.getType() != User.Role.USER)
        || deletingUser.getEntity().getId() != userToDelete.getEntity().getId())) {
      throw new NotAuthorizedException("");
    }

    userToDelete.setDeleted(true);
    return save(userToDelete);
  }
}