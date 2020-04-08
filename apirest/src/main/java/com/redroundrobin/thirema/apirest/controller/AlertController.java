package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
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

  @GetMapping(value = {""})
  public ResponseEntity<List<Alert>> getAlerts(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "entityId", required = false) Integer entityId,
      @RequestParam(name = "sensorId", required = false) Integer sensorId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      if (entityId != null && sensorId != null) {
        return ResponseEntity.ok(alertService.findAllByEntityIdAndSensorId(entityId, sensorId));
      } else if (entityId != null) {
        return ResponseEntity.ok(alertService.findAllByEntityId(entityId));
      } else if (sensorId != null) {
        return ResponseEntity.ok(alertService.findAllBySensorId(sensorId));
      } else {
        return ResponseEntity.ok(alertService.findAll());
      }
    } else if (sensorId != null && (entityId == null || user.getEntity().getId() == entityId)) {
      return ResponseEntity.ok(alertService.findAllByEntityIdAndSensorId(user.getEntity().getId(),
          sensorId));
    } else if (entityId == null || user.getEntity().getId() == entityId) {
      return ResponseEntity.ok(alertService.findAllByEntityId(user.getEntity().getId()));
    } else {
      return ResponseEntity.ok(Collections.emptyList());
    }
  }

  @PostMapping(value = {""})
  public ResponseEntity<Alert> createAlert(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> newAlertFields) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || user.getType() == User.Role.MOD) {
      try {
        return ResponseEntity.ok(alertService.createAlert(user, newAlertFields));
      } catch (MissingFieldsException | InvalidFieldsValuesException fe) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PutMapping(value = {"/{alertId:.+}"})
  public ResponseEntity<Alert> disableUserAlert(
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
