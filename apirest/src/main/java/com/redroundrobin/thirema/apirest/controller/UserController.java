package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private EntityService entityService;

  @Autowired
  private JwtUtil jwtTokenUtil;

  //richiesta fatta da un utente autenticato per vedere i device visibili a un altro utente
  @GetMapping(value = {"/users/{userid:.+}/devices"})
  public ResponseEntity<Object> getUserDevices(@RequestHeader("Authorization") String authorization,
                                               @PathVariable("userid") int requiredUser) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getUserId() == requiredUser || user.getType() == 2
        || user.getType() == 1 && user.getEntity().getEntityId()
        == userService.find(requiredUser).getEntity().getEntityId()) {
      return ResponseEntity.ok(userService.userDevices(requiredUser));
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }


  //dato un token valid restituisce l'ente di appertenenza o tutti gli enti
  //se il token è di un amministratore
  @GetMapping(value = {"/entities"})
  public ResponseEntity<Object> getUserEntity(
      @RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == 2) {
      return ResponseEntity.ok(entityService.findAll());
    } else {
      //utente moderatore || utente membro
      return ResponseEntity.ok(user.getEntity());
    }
  }


  //creazione di un nuovo utente
  @PostMapping(value = {"/users/create"})
  public ResponseEntity<Object> createUser(@RequestHeader("Authorization") String authorization,
                                           @RequestBody JsonObject request) {
    Gson g = new Gson();
    User newUser = g.fromJson(request,User.class);

    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == 2 || user.getType() == 1
        && user.getEntity().getEntityId() == newUser.getEntity().getEntityId()) {
      return ResponseEntity.ok(userService.save(newUser));
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PutMapping(value = {"/users/edit"})
  public ResponseEntity<Object> editUser(@RequestHeader("Authorization") String authorization,
                                         @RequestBody JsonObject request) {
    Gson g = new Gson();
    User editUser = g.fromJson(request,User.class);
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (userService.find(editUser.getUserId()) != null && user.getType() == 2 || user.getType() == 1
        && user.getEntity().getEntityId() == editUser.getEntity().getEntityId()) {
      return ResponseEntity.ok(userService.save(editUser));
    }
    return  new ResponseEntity(HttpStatus.FORBIDDEN);
  }
}
