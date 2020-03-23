package com.redroundrobin.thirema.apirest.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;

@Service
public class SerializeUser {

  private String name;
  private String surname;
  private String email;
  private String password;
  private int type;
  private String telegramName;
  private String telegramChat;
  private boolean tfa;
  private boolean deleted;
  @Autowired
  private EntityService entityService;


  public User serializeUser(JsonObject rawUser) {
    User newUser= new User();
    newUser.setName(rawUser.get("name").getAsString());
    newUser.setSurname(rawUser.get("surname").getAsString());
    newUser.setEmail(rawUser.get("email").getAsString());
    newUser.setPassword(rawUser.get("password").getAsString());
    newUser.setType(rawUser.get("type").getAsInt());
    newUser.setTelegramName(rawUser.get("telegram_name").getAsString());
    newUser.setTelegramChat(rawUser.get("telegram_chat").getAsString());
    newUser. setTfa(rawUser.get("two_factor_authentication").getAsBoolean());
    newUser.setDeleted(rawUser.get("deleted").getAsBoolean());
    newUser.setEntity( entityService.find(rawUser.get("entity_id").getAsInt()));
    /*manca distinzione sui campi che potrebbero essere null! Se no eccezione*/
    return newUser;
  }

}
