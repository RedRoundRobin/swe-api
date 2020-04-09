package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.AlertService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class AlertController extends CoreController {

  private AlertService alertService;

  @Autowired
  public AlertController(AlertService alertService, JwtUtil jwtUtil, LogService logService,
                         UserService userService) {
    super(jwtUtil, logService, userService);
    this.alertService = alertService;
  }

  @DeleteMapping(value = {""})
  public ResponseEntity deleteAlerts(@RequestHeader(value = "Authorization") String authorization,
                                     @RequestParam(name = "sensorId") Integer sensorId,
                                     HttpServletRequest httpRequest) {
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      try {
        alertService.deleteAlertsBySensorId(sensorId);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "alert.deleted",
            "alerts with sensorId = "+sensorId);
        return new ResponseEntity(HttpStatus.OK);
      } catch (ElementNotFoundException e) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping(value = {""})
  public ResponseEntity<Map<String,List<Alert>>> getAlerts(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "entityId", required = false) Integer entityId,
      @RequestParam(name = "sensorId", required = false) Integer sensorId) {
    User user = this.getUserFromAuthorization(authorization);

    Map<String,List<Alert>> response = new HashMap<>();

    if (user.getType() == User.Role.ADMIN) {
      if (entityId != null && sensorId != null) {
        response.put("enabled", alertService.findAllByEntityIdAndSensorId(entityId, sensorId));
      } else if (entityId != null) {
        response.put("enabled", alertService.findAllByEntityId(entityId));
      } else if (sensorId != null) {
        response.put("enabled", alertService.findAllBySensorId(sensorId));
      } else {
        response.put("enabled", alertService.findAll());
      }
    } else if (sensorId != null && (entityId == null || user.getEntity().getId() == entityId)) {
      List<Alert> disabledAlerts = alertService.findAllDisabledByUserId(user.getId());
      List<Alert> enabledAlerts = alertService.findAllByEntityIdAndSensorId(user.getEntity().getId(),
          sensorId);
      enabledAlerts.removeAll(disabledAlerts);

      response.put("enabled", enabledAlerts);
      response.put("disabled", disabledAlerts);
    } else if (entityId == null || user.getEntity().getId() == entityId) {
      List<Alert> disabledAlerts = alertService.findAllDisabledByUserId(user.getId());
      List<Alert> enabledAlerts = alertService.findAllByEntityId(user.getEntity().getId());
      enabledAlerts.removeAll(disabledAlerts);

      response.put("enabled", enabledAlerts);
      response.put("disabled", disabledAlerts);
    } else {
      response.put("enabled", Collections.emptyList());
      response.put("disabled", Collections.emptyList());
    }
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = {""})
  public ResponseEntity<Alert> createAlert(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> newAlertFields,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || user.getType() == User.Role.MOD) {
      try {
        Alert alert = alertService.createAlert(user, newAlertFields);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "alert.created",
            Integer.toString(alert.getId()));
        return ResponseEntity.ok(alert);
      } catch (MissingFieldsException | InvalidFieldsValuesException fe) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping(value = {"/{alertId:.+}"})
  public ResponseEntity deleteAlert(@RequestHeader("authorization") String authorization,
                                    @PathVariable("alertId") int alertId,
                                    HttpServletRequest httpRequest) {
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || (user.getType() == User.Role.MOD)) {
      try {
        if (alertService.deleteAlert(user, alertId)) {
          logService.createLog(user.getId(), getIpAddress(httpRequest), "alert.deleted",
              Integer.toString(alertId));
          return new ResponseEntity(HttpStatus.OK);
        } else {
          return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } catch (ElementNotFoundException e) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } catch (NotAuthorizedException e) {
        // go to return FORBIDDEN
      }
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  @PutMapping(value = {"/{alertId:.+}"})
  public ResponseEntity disableUserAlert(
      @RequestHeader("authorization") String authorization,
      @PathVariable("alertId") int alertId,
      @RequestParam("enable") boolean enable) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() != User.Role.ADMIN) {
      try {
        if (!alertService.enableUserAlert(user, alertId, enable)) {
          return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } catch (ElementNotFoundException enfe) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } catch (NotAuthorizedException nae) {
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    }
    return new ResponseEntity(HttpStatus.OK);
  }

}
