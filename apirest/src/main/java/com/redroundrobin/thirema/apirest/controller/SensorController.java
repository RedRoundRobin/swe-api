package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = {"/sensors"})
public class SensorController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final SensorService sensorService;

  @Autowired
  public SensorController(SensorService sensorService, JwtUtil jwtUtil, LogService logService,
                          UserService userService) {
    super(jwtUtil, logService, userService);
    this.sensorService = sensorService;
  }

  // Get all sensors also with entity id if provided
  @GetMapping(value = {""})
  public ResponseEntity<List<Sensor>> getSensors(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(value = "entityId", required = false) Integer entityId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      if (entityId != null) {
        return ResponseEntity.ok(sensorService.findAllByEntityId(entityId));
      } else {
        return ResponseEntity.ok(sensorService.findAll());
      }
    } else {
      if (entityId == null || user.getEntity().getId() == entityId) {
        return ResponseEntity.ok(sensorService.findAllByEntityId(user.getEntity().getId()));
      } else {
        return ResponseEntity.ok(Collections.emptyList());
      }
    }
  }

  // Get all sensors also with entity id if provided
  @GetMapping(value = {"/{sensorId:.+}"})
  public ResponseEntity<Sensor> getSensor(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable("sensorId") int sensorId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(sensorService.findById(sensorId));
    } else {
      return ResponseEntity.ok(sensorService.findByIdAndEntityId(sensorId,
          user.getEntity().getId()));
    }
  }

  @PutMapping(value = {"/{sensorId:.+}"})
  public ResponseEntity<String> sendCommandToSensorToGAtewayThroughToKafka(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable("sensorId") int sensorId,
      @RequestBody Map<String, Object> commandFields,
      HttpServletRequest httpRequest) {
    String ip = this.getIpAddress(httpRequest);
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      try {
        String cmd = sensorService.sendTelegramCommandToSensor(sensorId, commandFields);
        logService.createLog(user.getId(), ip, "sensor.input",
            Integer.toString(sensorId));
        return ResponseEntity.ok(cmd);
      } catch(ElementNotFoundException | NotAuthorizedException e) {
        logger.debug("RESPONSE STATUS: FORBIDDEN." + e.getMessage());
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

}
