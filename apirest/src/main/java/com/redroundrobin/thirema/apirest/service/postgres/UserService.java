package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ConflictException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import com.redroundrobin.thirema.apirest.utils.exception.TelegramChatNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
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

  private final UserRepository userRepo;

  private final AlertRepository alertRepo;

  private final EntityRepository entityRepo;

  @Autowired
  public UserService(UserRepository userRepository, AlertRepository alertRepository,
                     EntityRepository entityRepository) {
    this.userRepo = userRepository;
    this.alertRepo = alertRepository;
    this.entityRepo = entityRepository;
  }

  private boolean checkCreatableFields(Set<String> keys)
      throws InvalidFieldsValuesException {
    Set<String> creatable = new HashSet<>();
    creatable.add("name");
    creatable.add("surname");
    creatable.add("email");
    creatable.add("type");
    creatable.add("entityId");
    creatable.add("password");

    boolean onlyCreatableKeys = creatable.containsAll(keys);

    if (!onlyCreatableKeys) {
      throw new InvalidFieldsValuesException("");
    }

    return creatable.size() == keys.size();
  }

  private boolean checkEditableFields(User.Role role, boolean itself, Set<String> keys)
      throws MissingFieldsException {
    Map<String, Boolean> userFields = new HashMap<>();
    userFields.put("name", true);
    userFields.put("surname", true);
    userFields.put("email", true);
    userFields.put("password", true);
    userFields.put("type", true);
    userFields.put("telegramName", true);
    userFields.put("tfa", true);
    userFields.put("entityId", true);
    userFields.put("deleted", true);

    switch (role) {
      case ADMIN:
        if (itself) {
          userFields.replace("type", false);
          userFields.replace("deleted", false);
          userFields.replace("entityId", false);
        }

        break;
      case MOD:
        if (!itself) {
          userFields.replace("password", false);
          userFields.replace("telegramName", false);
          userFields.replace("tfa", false);
        } else {
          userFields.replace("deleted", false);
        }
        userFields.replace("type", false);
        userFields.replace("entityId", false);

        break;
      case USER:
        userFields.replace("name", false);
        userFields.replace("surname", false);
        userFields.replace("type", false);
        userFields.replace("entityId", false);
        userFields.replace("deleted", false);

        break;
      default:
        return false;
    }

    List<String> editable = new ArrayList<>();
    List<String> notEditable = new ArrayList<>();
    userFields.forEach((key, value) -> {
      if (value) {
        editable.add(key);
      } else {
        notEditable.add(key);
      }
    });
    if (keys.stream().noneMatch(userFields::containsKey)) {
      throw new MissingFieldsException("There aren't fields that can be edited");
    } else {
      return keys.stream().allMatch(k -> editable.contains(k) || !notEditable.contains(k));
    }
  }

  private User editAndSave(User userToEdit, Map<String, Object> fieldsToEdit)
      throws ConflictException, InvalidFieldsValuesException {
    if (fieldsToEdit.containsKey("tfa")
        && (boolean)fieldsToEdit.get("tfa")
        && (fieldsToEdit.containsKey("telegramName")
        || userToEdit.getTelegramChat().isEmpty())) {
      throw new ConflictException("TFA can't be edited because either telegramName is "
          + "in the request or telegram chat not present");
    }

    if (fieldsToEdit.containsKey("entityId")
        && entityRepo.findById((int)fieldsToEdit.get("entityId")).isEmpty()) {
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
        case "tfa":
          userToEdit.setTfa((boolean) value);
          if (!(boolean) value) {
            userToEdit.setTelegramChat(null);
          }
          break;
        case "entityId":
          userToEdit.setEntity(entityRepo.findById((int)fieldsToEdit.get("entityId")).orElse(null));
          break;
        case "deleted":
          userToEdit.setDeleted((boolean) value);
          break;
        default:
      }
    }

    return userRepo.save(userToEdit);
  }

  public User deleteUser(User deletingUser, int userToDeleteId)
      throws NotAuthorizedException, InvalidFieldsValuesException {
    User userToDelete;
    if ((userToDelete = findById(userToDeleteId)) == null) {
      throw new InvalidFieldsValuesException("");
    }

    if (deletingUser.getType() == User.Role.USER
        || deletingUser.getType() == User.Role.MOD
        && ((userToDelete.getType() != User.Role.USER)
        || deletingUser.getEntity().getId() != userToDelete.getEntity().getId())) {
      throw new NotAuthorizedException("");
    }

    userToDelete.setDeleted(true);
    return userRepo.save(userToDelete);
  }

  public User editByAdministrator(User userToEdit, Map<String, Object> fieldsToEdit, boolean itself)
      throws InvalidFieldsValuesException, MissingFieldsException, NotAuthorizedException,
      ConflictException {

    if ((!itself && userToEdit.getType() == User.Role.ADMIN)
        || (fieldsToEdit.containsKey("type") && (int)fieldsToEdit.get("type") == 2)
        || !checkEditableFields(User.Role.ADMIN, itself, fieldsToEdit.keySet())) {
      throw new NotAuthorizedException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByModerator(User userToEdit, Map<String, Object> fieldsToEdit, boolean itself)
      throws InvalidFieldsValuesException, MissingFieldsException, NotAuthorizedException,
      ConflictException {

    if ((fieldsToEdit.containsKey("type") && (int)fieldsToEdit.get("type") > 0)
        || !checkEditableFields(User.Role.MOD, itself, fieldsToEdit.keySet())) {
      throw new NotAuthorizedException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByUser(User userToEdit, Map<String, Object> fieldsToEdit)
      throws InvalidFieldsValuesException, MissingFieldsException, NotAuthorizedException,
      ConflictException  {

    if (!checkEditableFields(User.Role.USER, true, fieldsToEdit.keySet())) {
      throw new NotAuthorizedException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editUserTelegramChat(User user, String telegramChat) {
    user.setTelegramChat(telegramChat);
    return userRepo.save(user);
  }

  public List<User> findAll() {
    return (List<User>) userRepo.findAll();
  }

  public List<User> findAllByDisabledAlert(int alertId) {
    Alert alert = alertRepo.findById(alertId).orElse(null);
    if (alert != null) {
      return (List<User>) userRepo.findAllByDisabledAlerts(alert);
    } else {
      return Collections.emptyList();
    }
  }

  public List<User> findAllByDisabledAlerts(List<Integer> alertsIds) {
    List<Alert> alerts = new ArrayList<>();
    alertsIds.forEach(aid -> alertRepo.findById(aid).ifPresent(alerts::add));
    if (!alerts.isEmpty()) {
      return (List<User>) userRepo.findAllByDisabledAlertsIn(alerts);
    } else {
      return Collections.emptyList();
    }
  }

  public List<User> findAllByEntityId(int entityId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      return (List<User>) userRepo.findAllByEntity(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public User findByEmail(String email) {
    return userRepo.findByEmail(email);
  }

  public User findById(int id) {
    Optional<User> optUser = userRepo.findById(id);
    return optUser.orElse(null);
  }

  public User findByTelegramName(String telegramName) {
    return userRepo.findByTelegramName(telegramName);
  }

  public User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat) {
    return userRepo.findByTelegramNameAndTelegramChat(telegramName, telegramChat);
  }

  /**
   * Method that return the UserDetails created with the email furnished as @s and the
   * password taken from the database.
   *
   * @param email the email to create the UserDetails
   * @return the UserDetails generated from the email and the password.
   * @throws UsernameNotFoundException thrown if no user with furnished email found.
   * @throws UserDisabledException thrown if user with furnished email found but the user is
  disabled.
   */
  public UserDetails loadUserByEmail(String email) throws UserDisabledException {
    User user = this.findByEmail(email);
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
   * @param telegramName the telegram name to create the UserDetails
   * @return the UserDetails generated from the telegram name and the chat id.
   * @throws UsernameNotFoundException thrown if no telegram name found.
   * @throws UserDisabledException thrown if telegram name found but the user is disabled.
   */
  public UserDetails loadUserByTelegramName(String telegramName)
      throws UserDisabledException, TelegramChatNotFoundException {
    User user = this.findByTelegramName(telegramName);
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

  @Override
  public UserDetails loadUserByUsername(String username) {
    User user = this.findByEmail(username);
    if (user == null || (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted())))) {
      throw new UsernameNotFoundException("");
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), grantedAuthorities);
  }

  public User serializeUser(JsonObject rawUserToInsert, User insertingUser)
      throws MissingFieldsException, InvalidFieldsValuesException,
      ConflictException, NotAuthorizedException {
    if (!checkCreatableFields(rawUserToInsert.keySet())) {
      throw new MissingFieldsException("");
    }

    Entity userToInsertEntity;
    if ((userToInsertEntity = entityRepo.findById(
        (rawUserToInsert.get("entityId")).getAsInt()).orElse(null)) == null) {
      throw new InvalidFieldsValuesException("");
    }

    int userToInsertType;
    if ((userToInsertType = rawUserToInsert.get("type").getAsInt()) == 2
        || userToInsertType != 1 && userToInsertType != 0) {
      throw new InvalidFieldsValuesException("");
    }

    //qui so che entity_id dato esiste && so il tipo dello user che si vuole inserire
    if (insertingUser.getType() == User.Role.USER
        || (insertingUser.getType() == User.Role.MOD
        && userToInsertEntity.getId() != insertingUser.getEntity().getId())) {
      throw new NotAuthorizedException("");
    }

    User newUser;

    String email = rawUserToInsert.get("email").getAsString();
    if (rawUserToInsert.get("name").getAsString() != null
        || rawUserToInsert.get("surname").getAsString() != null
        || rawUserToInsert.get("password").getAsString() != null
        || email != null && userRepo.findByEmail(email) == null) {
      newUser = new User(rawUserToInsert.get("name").getAsString(),
          rawUserToInsert.get("surname").getAsString(), email,
          rawUserToInsert.get("password").getAsString(), User.Role.values()[userToInsertType]);
    } else if (email != null && userRepo.findByEmail(email) != null) {
      throw new ConflictException("");
    } else {
      throw new InvalidFieldsValuesException("");
    }

    newUser.setEntity(userToInsertEntity);

    return userRepo.save(newUser);
  }

}