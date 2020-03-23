package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.google.gson.JsonObject;

@Service
public class UserService implements UserDetailsService {

  @Autowired
  private UserRepository repository;

  @Autowired
  private EntityService entityService;

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
    if (user == null || (user.isDeleted() || (user.getType() != 2
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
    } else if (user.isDeleted() || (user.getType() != 2
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
    } else if (user.isDeleted() || (user.getType() != 2
        && (user.getEntity() == null || user.getEntity().isDeleted()))) {
      throw new UserDisabledException();
    }

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

    return new org.springframework.security.core.userdetails.User(
        user.getTelegramName(), user.getTelegramChat(), grantedAuthorities);
  }

  public User editUser(int userId, JsonObject fieldsToEdit) {
    User oldUser = find(userId);
    if(fieldsToEdit.has("name"))
      oldUser.setName(fieldsToEdit.get("name").getAsString());
    if(fieldsToEdit.has("surname"))
      oldUser.setSurname(fieldsToEdit.get("surname").getAsString());
    if(fieldsToEdit.has("email"))
      oldUser.setEmail(fieldsToEdit.get("email").getAsString());
    if(fieldsToEdit.has("password"))
      oldUser.setPassword(fieldsToEdit.get("password").getAsString());
    if(fieldsToEdit.has("type"))
      oldUser.setType(fieldsToEdit.get("type").getAsInt());
    if(fieldsToEdit.has("telegram_name"))
      oldUser.setTelegramName(fieldsToEdit.get("telegram_name").getAsString());
    if(fieldsToEdit.has("telegram_chat"))
      oldUser.setTelegramChat(fieldsToEdit.get("telegram_chat").getAsString());
    if(fieldsToEdit.has("two_factor_authentication"))
      oldUser.setTfa(fieldsToEdit.get("two_factor_authentication").getAsBoolean());
    if(fieldsToEdit.has("deleted"))
      oldUser.setDeleted(fieldsToEdit.get("deleted").getAsBoolean());
    if(fieldsToEdit.has("entity_id"))
      oldUser.setEntity(entityService.find(fieldsToEdit.get("entity_id").getAsInt()));
    return oldUser;
  }
}