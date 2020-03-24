package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.SerializeUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.redroundrobin.thirema.apirest.utils.NotAllowedToEditFields;
import java.util.List;

@RestController
public class UserController {

  @Autowired
  private JwtUtil jwtTokenUtil;

  @Autowired
  private SerializeUser serializeNewUser;

  @Autowired
  private UserService userService;

  @Autowired
  private EntityService entityService;

  //richiesta fatta da un utente autenticato per vedere i device visibili a un altro utente
  @GetMapping(value = {"/users/{userid:.+}/devices"})
  public ResponseEntity<?> getUserDevices(@RequestHeader("Authorization") String authorization,
                                          @PathVariable("userid") int requiredUserId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    User requiredUser = userService.find(requiredUserId);
    if (requiredUser != null && (user.getUserId() == requiredUserId || user.getType() == User.Role.ADMIN ||
        user.getType() == User.Role.MOD && requiredUser.getType() != User.Role.ADMIN
            && user.getEntity().getEntityId() == requiredUser.getEntity().getEntityId()))
      return ResponseEntity.ok(userService.userDevices(requiredUserId));
    else return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  //creazione di un nuovo utente
  @PostMapping(value = {"/users/create"})
  public ResponseEntity<Object> createUser(@RequestHeader("Authorization") String authorization,
                                           @RequestBody String jsonStringUser) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject jsonUser = JsonParser.parseString(jsonStringUser).getAsJsonObject();
    User newUser = userService.serializeUser(jsonUser, user.getType());
    if (user.getType() == User.Role.ADMIN || user.getType() == User.Role.ADMIN
        && user.getEntity().getEntityId() == newUser.getEntity().getEntityId()) {
      return ResponseEntity.ok(userService.save(newUser));
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  /*In input prende JsonObject coi field da modificare dello userId*/
  @PutMapping(value = {"/users/{userid:.+}/edit"})
  public ResponseEntity<Object> editUser(@RequestHeader("Authorization") String authorization,
                                         @RequestBody String rawFieldsToEdit, @PathVariable("userid") int userId) {
    String token = authorization.substring(7);
    User editingUser = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject fieldsToEdit = JsonParser.parseString(rawFieldsToEdit).getAsJsonObject();
    User userToEdit = userService.find(userId);

    if(userToEdit != null) {
      User user;
      try {
        if (editingUser.getType() == User.Role.ADMIN) {
          user = userService.editByAdministrator(userToEdit, fieldsToEdit);
        } else if (editingUser.getUserId() == userToEdit.getUserId()) {
          user = userService.editItself(userToEdit, fieldsToEdit);
        } else if (editingUser.getType() == User.Role.MOD
            && editingUser.getEntity().equals(userToEdit.getEntity())) {
          user = userService.editByModerator(userToEdit, fieldsToEdit);
        } else {
          return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
      } catch (NotAllowedToEditFields natef) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
      return ResponseEntity.ok(user);
    } else {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  //dato un token valido restituisce l'ente di appertenenza o tutti gli enti
  //se il token è di un amministratore
  @GetMapping(value = {"users/entities"})
  public ResponseEntity<Object> getAnotherUserEntity(
      @RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(entityService.findAll());
    }
    else {
      //utente moderatore || utente membro
      return ResponseEntity.ok(user.getEntity());
    }
  }

  //dato un token valido restituisce l'ente di appertenenza o tutti gli enti
  //se il token è di un amministratore
  @GetMapping(value = {"users/{userId:.+}/entities"})
  public ResponseEntity<Object> getUserEntity(
      @RequestHeader("Authorization") String authorization, @PathVariable("userId") int userId )  {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(entityService.findAll());
    }
    else{
      User userToRetrieve = userService.find(userId);
      if(userToRetrieve != null &&
                userToRetrieve.getEntity().getEntityId() == user.getEntity().getEntityId())
        return ResponseEntity.ok(user.getEntity());
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  //tutti gli user
  @GetMapping(value = {"/users"})
  public List<User> users() {
    return userService.findAll();
  }

  //un determinato user
  @GetMapping(value = {"/user/{userid:.+}"})
  public User user(@PathVariable("userid") int userId) {
    return userService.find(userId);
  }
}
