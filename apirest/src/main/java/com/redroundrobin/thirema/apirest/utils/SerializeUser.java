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
  @Autowired
  private EntityService entityService;


  public User serializeUser(JsonObject rawUser, User.Role type) {
    User newUser= new User();
    newUser.setName(rawUser.get("name").getAsString());
    newUser.setSurname(rawUser.get("surname").getAsString());
    newUser.setEmail(rawUser.get("email").getAsString());
    if(type == User.Role.ADMIN) {
      newUser.setType(User.Role.valueOf(rawUser.get("type").getAsString()));
      newUser.setEntity( entityService.find(rawUser.get("entity_id").getAsInt()));
    }
    /*manca distinzione sui campi che potrebbero essere null! Se no eccezione*/
    return newUser;
  }

}
