package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/sensors"})
public class SensorController extends CoreController {

  private SensorService sensorService;

  @Autowired
  public SensorController(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  // Get all sensors also with entity id if provided
  @GetMapping(value = {""})
  public ResponseEntity<List<Sensor>> getSensors(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(value = "entity", required = false) Integer entityId) {
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
}
