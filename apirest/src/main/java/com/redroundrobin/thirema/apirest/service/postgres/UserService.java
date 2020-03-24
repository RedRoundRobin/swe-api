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

  private void editItself(User userToEdit, JsonObject fieldsToEdit)
      throws NotAllowedToEditFields{
    if(fieldsToEdit.has("name") || fieldsToEdit.has("surname")
        || fieldsToEdit.has("user_id") || fieldsToEdit.has("entity_id")
          || fieldsToEdit.has("type") ||   fieldsToEdit.has("telegram_chat")
              || fieldsToEdit.has("deleted"))
      throw new NotAllowedToEditFields("You are not allowed to edit some of the specified fields");

    if(fieldsToEdit.has("email"))
      userToEdit.setEmail(fieldsToEdit.get("email").getAsString());
    if(fieldsToEdit.has("password"))
      userToEdit.setPassword(fieldsToEdit.get("password").getAsString());
    if(fieldsToEdit.has("telegram_name")) {
      userToEdit.setTelegramName(fieldsToEdit.get("telegram_name").getAsString());
      /*Fatto perchè lo dice Mariano in #gen_software*/
      userToEdit.setTfa(false);
      userToEdit.setTelegramChat(null);
    }
    if(fieldsToEdit.has("two_factor_authentication"))
      userToEdit.setTfa(fieldsToEdit.get("two_factor_authentication").getAsBoolean());
  }

  private void editByModerator(User userToEdit, JsonObject fieldsToEdit)
      throws NotAllowedToEditFields{
    if(fieldsToEdit.has("name") || fieldsToEdit.has("password")
        || fieldsToEdit.has("user_id") || fieldsToEdit.has("telegram_name")
    || fieldsToEdit.has("type") || fieldsToEdit.has("telegram_chat")
        || fieldsToEdit.has("two_factor_authentication") || fieldsToEdit.has("entity_id"))
      throw new NotAllowedToEditFields("You are not allowed to edit some of the specified fields");

    if(fieldsToEdit.has("email"))
      userToEdit.setEmail(fieldsToEdit.get("email").getAsString());
    if(fieldsToEdit.has("nome"))
    userToEdit.setPassword(fieldsToEdit.get("nome").getAsString());
    if(fieldsToEdit.has("cognome")) {
      userToEdit.setTelegramName(fieldsToEdit.get("cognome").getAsString());
    }
    /*Domanda: si puo riabilitare utente disabilitato? In adr non mi pare ci sia use case al riguardo..!*/
    if(fieldsToEdit.has("deleted") == true) {
      userToEdit.setDeleted(true);
      userToEdit.setEntity(null);
    }
  }

  private void editByAdministrator(User userToEdit, JsonObject fieldsToEdit)
      throws NotAllowedToEditFields{
    if(fieldsToEdit.has("user_id"))
      throw new NotAllowedToEditFields("You are not allowed to edit some of the specified fields");
    if(fieldsToEdit.has("name"))
      userToEdit.setName(fieldsToEdit.get("name").getAsString());
    if(fieldsToEdit.has("surname"))
      userToEdit.setSurname(fieldsToEdit.get("surname").getAsString());
    if(fieldsToEdit.has("email"))
      userToEdit.setEmail(fieldsToEdit.get("email").getAsString());
    if(fieldsToEdit.has("password")) //Reset password? Cosa significa?
      userToEdit.setPassword(fieldsToEdit.get("password").getAsString());
    if(fieldsToEdit.has("type"))
      userToEdit.setType(fieldsToEdit.get("type").getAsInt());
    if(fieldsToEdit.has("telegram_name")) {
      userToEdit.setTelegramName(fieldsToEdit.get("telegram_name").getAsString());
      userToEdit.setTfa(false);
      userToEdit.setTelegramChat(null);
    }
    if(fieldsToEdit.has("telegram_chat"))
      userToEdit.setTelegramChat(fieldsToEdit.get("telegram_chat").getAsString());
    if(fieldsToEdit.has("two_factor_authentication"))
      userToEdit.setTfa(fieldsToEdit.get("two_factor_authentication").getAsBoolean());
    if(fieldsToEdit.has("deleted") == true) {
      userToEdit.setDeleted(true);
      userToEdit.setEntity(null);
    }
    if(fieldsToEdit.has("entity_id"))
      userToEdit.setEntity(entityService.find(fieldsToEdit.get("entity_id").getAsInt()));
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

  public void editUser(int editingUserType, User userToEdit, JsonObject fieldsToEdit)
      throws NotAllowedToEditFields{
    switch(editingUserType)  {
      case 0 : editItself(userToEdit, fieldsToEdit);
      case 1 : editByModerator(userToEdit, fieldsToEdit);
      case 2 : editByAdministrator(userToEdit, fieldsToEdit);
    }
  }

  public User serializeUser(JsonObject rawUser, int type) {
    return serializeUser.serializeUser(rawUser, type);
  }
}