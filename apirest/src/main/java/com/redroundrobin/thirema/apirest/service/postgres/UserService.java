package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.SerializeUser;
import com.redroundrobin.thirema.apirest.utils.exception.EntityNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAllowedToEditException;
import com.redroundrobin.thirema.apirest.utils.exception.TelegramChatNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.TfaNotPermittedException;
import com.redroundrobin.thirema.apirest.utils.exception.UserRoleNotFoundException;
import java.util.ArrayList;
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

  private UserRepository repository;

  private EntityService entityService;

  private SerializeUser serializeUser;

  @Autowired
  public UserService(UserRepository repository, EntityService entityService,
                     SerializeUser serializeUser) {
    this.repository = repository;
    this.entityService = entityService;
    this.serializeUser = serializeUser;
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
      throw new KeysNotFoundException("There are some keys that doesn't exist");
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

  private User editAndSave(User userToEdit, HashMap<String, Object> fieldsToEdit)
      throws EntityNotFoundException, TfaNotPermittedException, UserRoleNotFoundException {
    if (fieldsToEdit.containsKey("two_factor_authentication")
        && (boolean)fieldsToEdit.get("two_factor_authentication")
        && (fieldsToEdit.containsKey("telegram_name")
        || userToEdit.getTelegramChat().isEmpty())) {
      throw new TfaNotPermittedException("TFA can't be edited because either telegram_name is "
          + "in the request or telegram chat not present");
    }

    if (fieldsToEdit.containsKey("entity_id")
        && entityService.find((int)fieldsToEdit.get("entity_id")) == null) {
      throw new EntityNotFoundException("The entity with the entityId furnished doesn't exist");
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
          userToEdit.setEntity(entityService.find((int) value));
          break;
        default:
      }
    }

    return save(userToEdit);
  }

  public List<User> findAll() {
    return (List<User>) repository.findAll();
  }

  public User find(int id) {
    Optional<User> optUser = repository.findById(id);
    return optUser.orElse(null);
  }

  public User findByTelegramName(String telegramName) {
    return repository.findByTelegramName(telegramName);
  }

  public User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat) {
    return repository.findByTelegramNameAndTelegramChat(telegramName, telegramChat);
  }

  public User findByEmail(String email) {
    return repository.findByEmail(email);
  }

  public User save(User user) {
    return repository.save(user);
  }

  public List<Device> userDevices(int userId) {
    return repository.userDevices(userId);
  }

  @Override
  public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
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
  public UserDetails loadUserByEmail(String s)
      throws UsernameNotFoundException, UserDisabledException {
    User user = this.findByEmail(s);
    if (user == null) {
      throw new UsernameNotFoundException("");
    } else if (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new UserDisabledException();
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), grantedAuthorities);
  }

  /**
   * Method that return the UserDetails created with the telegram name furnished as @s and the
   * chat id taken from the database.
   *
   * @param s the telegram name to create the UserDetails
   * @return the UserDetails generated from the telegram name and the chat id.
   * @throws UsernameNotFoundException thrown if no telegram name found.
   * @throws UserDisabledException thrown if telegram name found but the user is disabled.
   */
  public UserDetails loadUserByTelegramName(String s)
      throws UsernameNotFoundException, UserDisabledException, TelegramChatNotFoundException {
    User user = this.findByTelegramName(s);
    if (user == null) {
      throw new UsernameNotFoundException("");
    } else if (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new UserDisabledException();
    } else if (user.getTelegramChat() == null || user.getTelegramChat().isEmpty()) {
      throw new TelegramChatNotFoundException();
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getTelegramName(), user.getTelegramChat(), grantedAuthorities);
  }

  public User serializeUser(JsonObject rawUser, User.Role type) {
    return serializeUser.serializeUser(rawUser, type);
  }

  public User editByUser(User userToEdit, HashMap<String, Object> fieldsToEdit)
      throws NotAllowedToEditException, KeysNotFoundException, EntityNotFoundException,
      TfaNotPermittedException, UserRoleNotFoundException {

    if (!checkFieldsEditable(User.Role.USER, true, fieldsToEdit.keySet())) {
      throw new NotAllowedToEditException(
          "You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByModerator(User userToEdit, boolean itself, HashMap<String, Object> fieldsToEdit)
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
                                  HashMap<String, Object> fieldsToEdit)
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

}