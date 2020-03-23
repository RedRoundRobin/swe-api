package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
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
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), new ArrayList<>());
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
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), new ArrayList<>());
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
    return new org.springframework.security.core.userdetails.User(
        user.getTelegramName(), user.getTelegramChat(), new ArrayList<>());
  }

  public User editUser(User userToEdit) {
    User oldUser = find(userToEdit.getUserId());
    if((JsonObject)userToEdit.has("name"))
      oldUser.setName(userToEdit.getName());
    if(userToEdit.has("surname"))
      oldUser.setName(userToEdit.getSurname());
    if(userToEdit.has("email"))
      oldUser.setEmail(userToEdit.getEmail());
    if(userToEdit.has("password"))
      oldUser.setName(userToEdit.getPassword());
    if(userToEdit.has("type"))
      oldUser.setName(userToEdit.getType());
    if(userToEdit.has("telegram_name"))
      oldUser.setName(userToEdit.getTelegramName());
    if(userToEdit.has("telegram_chat"))
      oldUser.setName(userToEdit.getTelegramChat());
    if(userToEdit.has("two_factor_authentication"))
      oldUser.setName(userToEdit.getTfa());
    if(userToEdit.has("deleted"))
      oldUser.setName(userToEdit.isDeleted());
    if(userToEdit.has("entity_id"))
      oldUser.setEntity(entityService.find(userToEdit.get("entity_id").getAsInt()));
    return oldUser;

  }
}