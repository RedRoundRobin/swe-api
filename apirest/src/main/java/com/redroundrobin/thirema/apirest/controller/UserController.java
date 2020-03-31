package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.EntityNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAllowedToEditException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedToDeleteUserException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedToInsertUserException;
import com.redroundrobin.thirema.apirest.utils.exception.TfaNotPermittedException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import com.redroundrobin.thirema.apirest.utils.exception.UserRoleNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.ValuesNotAllowedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/users")
public class UserController {

  private JwtUtil jwtTokenUtil;

  private UserService userService;

  private boolean canEditMod(User editingUser, User userToEdit) {
    return editingUser.getId() == userToEdit.getId()
        || (userToEdit.getType() == User.Role.USER
        && editingUser.getEntity().equals(userToEdit.getEntity()));
  }

  @Autowired
  public UserController(JwtUtil jwtTokenUtil, UserService userService,
                        EntityService entityService) {
    this.jwtTokenUtil = jwtTokenUtil;
    this.userService = userService;
  }

  // Get all users
  @GetMapping(value = {""})
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
          // go to return BAD_REQUEST
        }
      } else if (disabledAlert != null) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } else if (view != null) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } else {
        return ResponseEntity.ok(userService.findAll());
      }
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } else if (user.getType() == User.Role.MOD
        && (entity == null && disabledAlert == null && view == null)
        || (entity != null && user.getEntity().getId() == entity)) {
      try {
        return ResponseEntity.ok(userService.findAllByEntityId(user.getEntity().getId()));
      } catch (EntityNotFoundException enfe) {
        // go to return FORBIDDE
      }
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  // Create new user
  @PostMapping(value = {""})
  public ResponseEntity<User> createUser(@RequestHeader("Authorization") String authorization,
                                         @RequestBody String jsonStringUser) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject jsonUser = JsonParser.parseString(jsonStringUser).getAsJsonObject();
    try {
      return ResponseEntity.ok(userService.serializeUser(jsonUser, user));
    } catch (KeysNotFoundException | MissingFieldsException | ValuesNotAllowedException
        | UserRoleNotFoundException | EntityNotFoundException
        | NotAuthorizedToInsertUserException e) {
      return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @DeleteMapping(value = {"/{userid:.+}"})
  public ResponseEntity<?> deleteEntityUser(@RequestHeader("Authorization") String authorization,
                                            @PathVariable("userid") int userToDeleteId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    try {
      return ResponseEntity.ok(userService.deleteUser(user, userToDeleteId));
    } catch (NotAuthorizedToDeleteUserException e) {
      return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (ValuesNotAllowedException e) {
      return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  // Get user by userId
  @GetMapping(value = {"/{userid:.+}"})
  public User user(@PathVariable("userid") int userId) {
    return userService.findById(userId);
  }

  // Edit user by userId and a map with data to edit
  @PutMapping(value = {"/{userid:.+}"})
  public ResponseEntity<Map<String, Object>> editUser(
      @RequestHeader("Authorization") String authorization,
      @RequestBody Map<String, Object> fieldsToEdit,
      @PathVariable("userid") int userId) {
    String token = authorization.substring(7);
    String editingUserEmail = jwtTokenUtil.extractUsername(token);
    User editingUser = userService.findByEmail(editingUserEmail);
    User userToEdit = userService.findById(userId);

    if (userToEdit != null) {
      HashMap<String, Object> response = new HashMap<>();

      User user;
      try {
        if (editingUser.getType() == User.Role.ADMIN && (userToEdit.getType() != User.Role.ADMIN
            || editingUser.getId() == userToEdit.getId())) {

            String email = editingUser.getEmail();

            user = userService.editByAdministrator(userToEdit,
                editingUser.getId() == userToEdit.getId(), fieldsToEdit);

            if (user.getEmail() != email) {
              String newToken = jwtTokenUtil.generateTokenWithExpiration("webapp",
                  jwtTokenUtil.extractExpiration(token),
                  userService.loadUserByEmail(user.getEmail()));
              response.put("token", newToken);
            }
        } else if (editingUser.getType() == User.Role.MOD && canEditMod(editingUser, userToEdit)) {

          String email = editingUser.getEmail();

          user = userService.editByModerator(userToEdit,
              editingUser.getId() == userToEdit.getId(), fieldsToEdit);

          if (user.getEmail() != email) {
            String newToken = jwtTokenUtil.generateTokenWithExpiration("webapp",
                jwtTokenUtil.extractExpiration(token),
                userService.loadUserByEmail(user.getEmail()));
            response.put("token", newToken);
          }

        } else if (editingUser.getType() == User.Role.USER
            && editingUser.getId() == userToEdit.getId()) {

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
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
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
      } catch (EntityNotFoundException | UserRoleNotFoundException nf) {
        // go to return BAD_REQUEST
      }
    }
    // when db error is not for duplicate unique or when userToEdit with id furnished is not found
    // or in case of EntityNotFoundException or UserRoleNotFoundException
    return new ResponseEntity(HttpStatus.BAD_REQUEST);
  }

  // Get all devices by userId
  @GetMapping(value = {"/{userid:.+}/devices"})
  public ResponseEntity<Object> getUserDevices(@RequestHeader("Authorization") String authorization,
                                          @PathVariable("userid") int requiredUserId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    User requiredUser = userService.findById(requiredUserId);
    if (requiredUser != null && (user.getId() == requiredUserId
        || user.getType() == User.Role.ADMIN  || user.getType() == User.Role.MOD
        && requiredUser.getType() != User.Role.ADMIN
            && user.getEntity().getId() == requiredUser.getEntity().getId())) {
      return ResponseEntity.ok(userService.userDevices(requiredUserId));
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
