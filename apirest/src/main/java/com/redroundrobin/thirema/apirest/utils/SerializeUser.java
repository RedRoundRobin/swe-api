package com.redroundrobin.thirema.apirest.utils;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SerializeUser {
  @Autowired
  private EntityService entityService;


  public User serializeUser(JsonObject rawUser, User.Role type) {
    User newUser = new User();
    newUser.setName(rawUser.get("name").getAsString());
    newUser.setSurname(rawUser.get("surname").getAsString());
    newUser.setEmail(rawUser.get("email").getAsString());
    if (type == User.Role.ADMIN) {
      newUser.setType(User.Role.valueOf(rawUser.get("type").getAsString()));
      newUser.setEntity(entityService.findById(rawUser.get("entity_id").getAsInt()));
    }
    /*manca distinzione sui campi che potrebbero essere null! Se no eccezione*/
    return newUser;
  }

}
