package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/entities")
public class EntityController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final EntityService entityService;

  public EntityController(EntityService entityService, JwtUtil jwtUtil, LogService logService,
                          UserService userService) {
    super(jwtUtil, logService, userService);
    this.entityService = entityService;
  }

  private boolean checkPermission(User loggedUser, Integer sensorId, Integer userId) {
    boolean sensorOk = true;
    boolean userOk = true;

    if (sensorId != null) {
      sensorOk = loggedUser.getEntity().getSensors().stream().anyMatch(s -> s.getId() == sensorId);
    }

    if (userId != null) {
      if (loggedUser.getType() == User.Role.MOD) {
        List<Entity> entities = entityService.findAllByUserId(userId);
        userOk = entities.contains(loggedUser.getEntity());
      } else {
        userOk = userId == loggedUser.getId();
      }
    }

    return sensorOk && userOk;
  }

  @GetMapping(value = {""})
  public ResponseEntity<List<Entity>> getEntities(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "sensorId", required = false) Integer sensorId,
      @RequestParam(name = "userId", required = false) Integer userId) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      if (sensorId == null && userId == null) {
        return ResponseEntity.ok(entityService.findAll());
      } else if (userId == null) {
        return ResponseEntity.ok(entityService.findAllBySensorId(sensorId));
      } else if (sensorId == null) {
        return ResponseEntity.ok(entityService.findAllByUserId(userId));
      } else {
        return ResponseEntity.ok(entityService.findAllBySensorIdAndUserId(sensorId, userId));
      }
    } else if (checkPermission(user, sensorId, userId)) {
      return ResponseEntity.ok(entityService.findAllByUserId(user.getId()));
    } else {
      return ResponseEntity.ok(Collections.emptyList());
    }
  }

  @GetMapping(value = {"/{entityId:.+}"})
  public ResponseEntity<Entity> getEntity(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable(name = "entityId") Integer entityId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || user.getEntity().getId() == entityId) {
      return ResponseEntity.ok(entityService.findById(entityId));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator or the entity Id is not the same as the user entity");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PostMapping(value = {""})
  public ResponseEntity<Entity> addEntity(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestBody Map<String, Object> newEntityFields,
      HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        Entity entity = entityService.addEntity(newEntityFields);
        logService.createLog(user.getId(),ip,"entity.add",
            Integer.toString(entity.getId()));
        return ResponseEntity.ok(entity);
      } catch (MissingFieldsException e) {
        logger.debug(e.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PutMapping(value = {"/{entityId:.+}"})
  public ResponseEntity<Entity> editEntity(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable(value = "entityId") int entityId,
      @RequestBody Map<String, Object> fieldsToEditOrsensorsToEnableOrDisable,
      HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN
        && fieldsToEditOrsensorsToEnableOrDisable.containsKey("enableOrDisableSensors")
        && (boolean)fieldsToEditOrsensorsToEnableOrDisable.get("enableOrDisableSensors")) {
      try {
        fieldsToEditOrsensorsToEnableOrDisable.remove("enableOrDisableSensors");
        if (entityService.enableOrDisableSensorToEntity(entityId,
            fieldsToEditOrsensorsToEnableOrDisable)) {
          logService.createLog(user.getId(),ip,"entity.assign_sensor",
              Integer.toString(entityId));
          return new ResponseEntity(HttpStatus.OK);
        } else {
          return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } catch (MissingFieldsException | ElementNotFoundException enfe) {
        logger.debug(enfe.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else if(user.getType() == User.Role.ADMIN) {
      try {
        Entity entity = entityService.editEntity(
            entityId, fieldsToEditOrsensorsToEnableOrDisable);
        logService.createLog(user.getId(),ip,"entity.edit",
            Integer.toString(entity.getId()));
        return ResponseEntity.ok(entity);
      } catch (MissingFieldsException | InvalidFieldsValuesException e) {
        logger.debug(e.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping(value = {"/{entityId:.+}"})
  public ResponseEntity deleteEntity(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable(value = "entityId") int entityId,
      HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        if (entityService.deleteEntity(entityId)) {
          logService.createLog(user.getId(),ip,"entity.delete",
              Integer.toString(entityId));
          return new ResponseEntity<>(HttpStatus.OK);
        } else {
          logger.debug("RESPONSE STATUS: CONFLICT. There was a db error during the deletion of "
              + "the entity");
          return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
      } catch (ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin");
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }

}
