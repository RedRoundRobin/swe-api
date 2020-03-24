package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonElement;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.utils.NotAllowedToEditFields;
import com.redroundrobin.thirema.apirest.utils.SerializeUser;

@Service
public class UserService implements UserDetailsService {

  @Autowired
  private UserRepository repository;

  @Autowired
  private EntityService entityService;

  @Autowired
  private SerializeUser serializeUser;

  private User editAndSave(User userToEdit, JsonObject fieldsToEdit) {
    Boolean enableableTfa = !fieldsToEdit.has("telegram_name");

    for( Map.Entry<String, JsonElement> entry : fieldsToEdit.entrySet() ) {
      switch (entry.getKey()) {
        case "name":
          userToEdit.setName(entry.getValue().getAsString());
          break;
        case "surname":
          userToEdit.setSurname(entry.getValue().getAsString());
          break;
        case "email":
          userToEdit.setEmail(entry.getValue().getAsString());
          break;
        case "password":
          userToEdit.setPassword(entry.getValue().getAsString());
          break;
        case "type":
          try {
            userToEdit.setType(User.Role.valueOf(entry.getValue().getAsString()));
          } catch ( IllegalArgumentException iae ) {
            // il ruole / tipo è diverso da 0, 1, 2
          }
          break;
        case "telegram_name":
          userToEdit.setTelegramName(entry.getValue().getAsString());
          userToEdit.setTfa(false);
          userToEdit.setTelegramChat(null);
          break;
        case "two_factor_authentication":
          if( enableableTfa ) {
            userToEdit.setTfa(entry.getValue().getAsBoolean());
          }
          break;
        case "deleted":
          userToEdit.setDeleted(true);
          break;
        case "entity_id":
          Entity entity = entityService.find(entry.getValue().getAsInt());
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
    return repository.findById(id).get();
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
    User user = this.repository.findByEmail(s);
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
    User user = this.repository.findByEmail(s);
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
      throws UsernameNotFoundException, UserDisabledException {
    User user = this.repository.findByTelegramName(s);
    if (user == null) {
      throw new UsernameNotFoundException("");
    } else if (user.isDeleted() || (user.getType() != User.Role.ADMIN
        && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new UserDisabledException();
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getTelegramName(), user.getTelegramChat(), grantedAuthorities);
  }

  public User serializeUser(JsonObject rawUser, User.Role type) {
    return serializeUser.serializeUser(rawUser, type);
  }

  public User editItself(User userToEdit, JsonObject fieldsToEdit)
      throws NotAllowedToEditFields {
    Set<String> allowed = new HashSet<>();
    allowed.add("email");
    allowed.add("password");
    allowed.add("telegram_name");
    allowed.add("two_factor_authentication");

    boolean onlyEditableKeys = fieldsToEdit.keySet().stream()
        .filter(key -> !allowed.contains(key))
        .count() == 0;

    if(!onlyEditableKeys) {
      throw new NotAllowedToEditFields("You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByModerator(User userToEdit, JsonObject fieldsToEdit)
      throws NotAllowedToEditFields{
    Set<String> allowed = new HashSet<>();
    allowed.add("email");
    allowed.add("name");
    allowed.add("surname");
    allowed.add("deleted");

    boolean onlyEditableKeys = fieldsToEdit.keySet().stream()
        .filter(key -> !allowed.contains(key))
        .count() == 0;

    if(!onlyEditableKeys) {
      throw new NotAllowedToEditFields("You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

  public User editByAdministrator(User userToEdit, JsonObject fieldsToEdit)
      throws NotAllowedToEditFields{
    Set<String> allowed = new HashSet<>();
    allowed.add("name");
    allowed.add("surname");
    allowed.add("email");
    allowed.add("type");
    allowed.add("telegram_name");
    allowed.add("two_factor_authentication");
    allowed.add("deleted");
    allowed.add("entity_id");

    boolean onlyEditableKeys = fieldsToEdit.keySet().stream()
        .filter(key -> !allowed.contains(key))
        .count() == 0;

    if(!onlyEditableKeys) {
      throw new NotAllowedToEditFields("You are not allowed to edit some of the specified fields");
    } else {
      return this.editAndSave(userToEdit, fieldsToEdit);
    }
  }

}