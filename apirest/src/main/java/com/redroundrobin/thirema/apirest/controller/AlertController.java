package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController extends CoreController {

  private AlertService alertService;

  @Autowired
  public AlertController(AlertService alertService) {
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
}
