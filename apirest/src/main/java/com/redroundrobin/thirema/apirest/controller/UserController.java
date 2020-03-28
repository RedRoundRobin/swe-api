package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.EntityNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAllowedToEditException;
import com.redroundrobin.thirema.apirest.utils.exception.TfaNotPermittedException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import com.redroundrobin.thirema.apirest.utils.exception.UserRoleNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  private JwtUtil jwtTokenUtil;

  private UserService userService;

  private EntityService entityService;

  @Autowired
  public UserController(JwtUtil jwtTokenUtil, UserService userService,
                        EntityService entityService) {
    this.jwtTokenUtil = jwtTokenUtil;
    this.userService = userService;
    this.entityService = entityService;
  }

  //richiesta fatta da un utente autenticato per vedere i device visibili a un altro utente
  @GetMapping(value = {"/users/{userid:.+}/devices"})
  public ResponseEntity<Object> getUserDevices(@RequestHeader("Authorization") String authorization,
                                          @PathVariable("userid") int requiredUserId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    User requiredUser = userService.findById(requiredUserId);
    if (requiredUser != null && (user.getUserId() == requiredUserId
        || user.getType() == User.Role.ADMIN  || user.getType() == User.Role.MOD
        && requiredUser.getType() != User.Role.ADMIN
            && user.getEntity().getEntityId() == requiredUser.getEntity().getEntityId())) {
      return ResponseEntity.ok(userService.userDevices(requiredUserId));
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
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

  private boolean canEditMod(User editingUser, User userToEdit) {
    return editingUser.getUserId() == userToEdit.getUserId()
        || (userToEdit.getType() == User.Role.USER
        && editingUser.getEntity().equals(userToEdit.getEntity()));
  }

  /*In input prende JsonObject coi field da modificare dello userId*/
  @PutMapping(value = {"/users/{userid:.+}/edit"})
  public ResponseEntity<Object> editUser(@RequestHeader("Authorization") String authorization,
                                         @RequestBody HashMap<String, Object> fieldsToEdit,
                                         @PathVariable("userid") int userId) {
    String token = authorization.substring(7);
    String editingUserEmail = jwtTokenUtil.extractUsername(token);
    User editingUser = userService.findByEmail(editingUserEmail);
    User userToEdit = userService.findById(userId);

    if (userToEdit != null) {
      HashMap<String, Object> response = new HashMap<>();

      User user;
      try {
        if (editingUser.getType() == User.Role.ADMIN && userToEdit.getType() != User.Role.ADMIN) {

          user = userService.editByAdministrator(userToEdit,
              editingUser.getUserId() == userToEdit.getUserId(), fieldsToEdit);

        } else if (editingUser.getType() == User.Role.MOD && canEditMod(editingUser, userToEdit)) {

          user = userService.editByModerator(userToEdit,
              editingUser.getUserId() == userToEdit.getUserId(), fieldsToEdit);

        } else if (editingUser.getType() == User.Role.USER
            && editingUser.getUserId() == userToEdit.getUserId()) {

          user = userService.editByUser(userToEdit, fieldsToEdit);

          if (!user.getEmail().equals(editingUserEmail)) {
            String newToken = jwtTokenUtil.generateTokenWithExpiration("webapp",
                jwtTokenUtil.extractExpiration(token),
                userService.loadUserByEmail(user.getEmail()));
            response.put("token", newToken);
          }
        } else {
          return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        response.put("user",user);
        return ResponseEntity.ok(response);

      } catch (UsernameNotFoundException unfe) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      } catch (NotAllowedToEditException | UserDisabledException natef) {
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      } catch (TfaNotPermittedException tnpe) {
        return new ResponseEntity(tnpe.getMessage(),HttpStatus.CONFLICT);
      } catch (DataIntegrityViolationException dive) {
        if (dive.getMostSpecificCause().getMessage()
            .startsWith("ERROR: duplicate key value violates unique constraint")) {

          Pattern pattern = Pattern.compile("Key \\((.+)\\)=\\((.+)\\) already exists");
          Matcher matcher = pattern.matcher(dive.getMostSpecificCause().getMessage());

          String errorMessage = "";
          if (matcher.find()) {
            errorMessage = "The value of " + matcher.group(1) + " already exists";
          }

          return new ResponseEntity(errorMessage,HttpStatus.CONFLICT);
        }
      } catch (EntityNotFoundException | KeysNotFoundException | UserRoleNotFoundException nf) {

      }
    }
    // when db error is not for duplicate unique or when userToEdit with id furnished is not found
    // or in case of EntityNotFoundException or KeysNotFoundException or UserRoleNotFoundException
    return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
    } else {
      //utente moderatore || utente membro
      return ResponseEntity.ok(user.getEntity());
    }
  }

  //dato un token valido restituisce l'ente di appertenenza o tutti gli enti
  //se il token è di un amministratore
  @GetMapping(value = {"users/{userId:.+}/entities"})
  public ResponseEntity<Object> getUserEntity(
      @RequestHeader("Authorization") String authorization, @PathVariable("userId") int userId)  {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(entityService.findAll());
    } else {
      User userToRetrieve = userService.findById(userId);
      if (userToRetrieve != null
          && userToRetrieve.getEntity().getEntityId() == user.getEntity().getEntityId()) {
        return ResponseEntity.ok(user.getEntity());
      } else {
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    }
  }

  //tutti gli user
  @GetMapping(value = {"/users"})
  public ResponseEntity<List<User>> getUsers(@RequestHeader("Authorization") String authorization,
          @RequestParam(value = "entity", required = false) Integer entity,
          @RequestParam(value = "disabledAlert", required = false) Integer disabledAlert,
          @RequestParam(value = "view", required = false) Integer view) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == User.Role.ADMIN) {
      if (entity != null) {
        try {
          return ResponseEntity.ok(userService.findAllByEntityId(entity));
        } catch (EntityNotFoundException enfe) {

        }
      } else if (disabledAlert != null) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } else if (view != null) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } else {
        return ResponseEntity.ok(userService.findAll());
      }
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } else if (user.getType() == User.Role.MOD) {
      if ((entity == null && disabledAlert == null && view == null)
          || (entity != null && user.getEntity().getEntityId() == entity)) {
        try {
          return ResponseEntity.ok(userService.findAllByEntityId(user.getEntity().getEntityId()));
        } catch (EntityNotFoundException enfe) {

        }
      }
    }
    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
  }

  //un determinato user
  @GetMapping(value = {"/user/{userid:.+}"})
  public User user(@PathVariable("userid") int userId) {
    return userService.findById(userId);
  }
}
