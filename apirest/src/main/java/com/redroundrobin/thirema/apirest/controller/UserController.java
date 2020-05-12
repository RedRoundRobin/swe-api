package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ConflictException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class UserController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public UserController(JwtUtil jwtUtil, LogService logService, UserService userService) {
    super(jwtUtil, logService, userService);
  }

  // Get all users
  @GetMapping(value = {""})
  public ResponseEntity<List<User>> getUsers(
      @RequestHeader("Authorization") String authorization,
      @RequestParam(value = "entity", required = false) Integer entity,
      @RequestParam(value = "disabledAlert", required = false) Integer disabledAlert,
      @RequestParam(value = "view", required = false) Integer view,
      @RequestParam(value = "telegramName", required = false) String telegramName) {
    User user = getUserFromAuthorization(authorization);
    if (telegramName != null) {
      if (user.getTelegramName().equals(telegramName)) {
        List<User> userList = new ArrayList<>();
        userList.add(userService.findByTelegramName(telegramName));
        return ResponseEntity.ok(userList);
      } else {
        logger.debug("RESPONSE STATUS: BAD_REQUEST. Request with telegramName different from "
            + "logged user telegram name");
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else if (user.getType() == User.Role.ADMIN) {
      if (entity != null) {
        return ResponseEntity.ok(userService.findAllByEntityId(entity));
      } else if (disabledAlert != null) {
        logger.debug("RESPONSE STATUS: BAD_REQUEST. disableAlert != null");
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } else if (view != null) {
        logger.debug("RESPONSE STATUS: BAD_REQUEST. view != null");
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } else {
        return ResponseEntity.ok(userService.findAll());
      }
    } else if (user.getType() == User.Role.MOD
        && disabledAlert == null && view == null
        && (entity == null || user.getEntity().getId() == entity)) {
      return ResponseEntity.ok(userService.findAllByEntityId(user.getEntity().getId()));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId() + " is not an administrator "
          + "and (disabledAlert != null or view != null or user entity is different from entity");
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  // Create new user
  @PostMapping(value = {""})
  public ResponseEntity<User> createUser(@RequestHeader("Authorization") String authorization,
                                         @RequestBody Map<String, Object> jsonStringUser,
                                         HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);

    try {
      User createdUser = userService.addUser(jsonStringUser, user);
      logService.createLog(user.getId(), ip, "user.created",
          Integer.toString(createdUser.getId()));
      return ResponseEntity.ok(createdUser);
    } catch (MissingFieldsException | InvalidFieldsValuesException e) {
      logger.debug(e.toString());
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    } catch (ConflictException e) {
      logger.debug(e.toString());
      return new ResponseEntity(HttpStatus.CONFLICT);
    } catch (NotAuthorizedException e) {
      logger.debug(e.toString());
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping(value = {"/{userid:.+}"})
  public ResponseEntity<User> deleteUser(@RequestHeader("Authorization") String authorization,
                                         @PathVariable("userid") int userToDeleteId,
                                         HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);

    try {
      User deletedUser = userService.deleteUser(user, userToDeleteId);
      logService.createLog(user.getId(), ip, "user.deleted",
          Integer.toString(deletedUser.getId()));
      return ResponseEntity.ok(deletedUser);
    } catch (NotAuthorizedException e) {
      logger.debug(e.toString());
      return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN);
    } catch (InvalidFieldsValuesException e) {
      logger.debug(e.toString());
      return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  // Get user by userId
  @GetMapping(value = {"/{userid:.+}"})
  public ResponseEntity user(@RequestHeader("Authorization") String authorization,
                   @PathVariable("userid") int userId) {
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(userService.findById(userId));
    } else if (user.getType() == User.Role.MOD) {
      User userToReturn = userService.findById(userId);
      if (userToReturn.getEntity() == user.getEntity()) {
        return ResponseEntity.ok(userToReturn);
      }
    } else if (user.getId() == userId) {
      return ResponseEntity.ok(user);
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId() + " is not an administrator "
          + "and is not a moderator and user id is different from userId");
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  // Edit user by userId and a map with data to edit
  @PutMapping(value = {"/{userid:.+}"})
  public ResponseEntity<Map<String, Object>> editUser(
      @RequestHeader("Authorization") String authorization,
      @RequestBody Map<String, Object> fieldsToEdit,
      @PathVariable("userid") int userId,
      HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User editingUser = getUserFromAuthorization(authorization);
    String token = authorization.substring(7);

    String editingUserEmail = editingUser.getEmail();
    User userToEdit = userService.findById(userId);

    if (userToEdit != null) {

      User user;
      try {
        user = userService.editUser(editingUser, userToEdit, fieldsToEdit);

        if (editingUser.getId() == userToEdit.getId()) {
          if (fieldsToEdit.containsKey("password")) {
            logService.createLog(editingUser.getId(), ip, "user.password_edit", "");
          }
          if (!fieldsToEdit.containsKey("password") || fieldsToEdit.size() > 1) {
            logService.createLog(editingUser.getId(), ip, "user.settings_edit", "");
          }
        } else {
          if (fieldsToEdit.containsKey("password")) {
            logService.createLog(editingUser.getId(), ip, "user.password_reset",
                Integer.toString(userId));
          }
          if (fieldsToEdit.keySet().stream().anyMatch(k -> !k.equals("password"))) {
            logService.createLog(editingUser.getId(), ip, "user.edit",
                Integer.toString(userId));
          }
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("user",user);

        if (editingUser.getId() == userToEdit.getId()
            && !user.getEmail().equals(editingUserEmail)) {
          String newToken = jwtUtil.generateTokenWithExpiration("webapp",
              userService.loadUserByEmail(user.getEmail()),
              jwtUtil.extractExpiration(token));
          response.put("token", newToken);
        }

        return ResponseEntity.ok(response);

      } catch (UsernameNotFoundException unfe) {
        logger.debug(unfe.toString());
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
      } catch (NotAuthorizedException | UserDisabledException natef) {
        logger.debug(natef.toString());
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      } catch (ConflictException ce) {
        logger.debug(ce.toString());
        return new ResponseEntity(ce.getMessage(),HttpStatus.CONFLICT);
      } catch (DataIntegrityViolationException dive) {
        logger.debug(dive.toString());
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
      } catch (MissingFieldsException | InvalidFieldsValuesException nf) {
        logger.debug(nf.toString());
        // go to return BAD_REQUEST
      }
    } else {
      logger.debug("RESPONSE STATUS: BAD_REQUEST. User " + userId + " does not exist");
    }
    // when db error is not for duplicate unique or when userToEdit with id furnished is not found
    // or there are missing edit fields or invalid values
    return new ResponseEntity(HttpStatus.BAD_REQUEST);
  }
}
