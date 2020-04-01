package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.service.timescale.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/data")
public class DataController extends CoreController {

  private SensorService timescaleSensorService;

  private com.redroundrobin.thirema.apirest.service.postgres.SensorService sensorService;

  @Autowired
  public DataController(@Qualifier("timescaleSensorService") SensorService timescaleSensorService) {
    this.timescaleSensorService = timescaleSensorService;
  }

  public void setSensorService(
      com.redroundrobin.thirema.apirest.service.postgres.SensorService sensorService) {
    this.sensorService = sensorService;
  }

  @GetMapping(value = {""})
  public ResponseEntity<Map<Integer, List<Sensor>>> getTimescaleAlerts(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "sensors", required = false) Integer[] sensorIds,
      @RequestParam(name = "limit", required = false) Integer limit,
      @RequestParam(name = "entity", required = false) Integer entityId) {
    User user = this.getUserFromAuthorization(authorization);
    Map<Integer, List<Sensor>> sensorsData = new HashMap<>();

    if (user.getType() == User.Role.ADMIN) {
      if (sensorIds != null && limit != null && entityId != null) {
        sensorsData = timescaleSensorService.findTopNBySensorIdListAndEntityId(limit,
            Arrays.asList(sensorIds), entityId);
      } else if (sensorIds != null && entityId != null) {
        sensorsData = timescaleSensorService.findAllBySensorIdListAndEntityId(
            Arrays.asList(sensorIds), entityId);
      } else if (limit != null && entityId != null) {
        sensorsData = timescaleSensorService.findTopNForEachSensorByEntityId(limit, entityId);
      } else if (entityId != null) {
        sensorsData = timescaleSensorService.findAllForEachSensorByEntityId(entityId);
      } else if (sensorIds != null && limit != null) {
        sensorsData = timescaleSensorService.findTopNBySensorIdList(limit,
            Arrays.asList(sensorIds));
      } else if (sensorIds != null) {
        sensorsData = timescaleSensorService.findAllBySensorIdList(Arrays.asList(sensorIds));
      } else if (limit != null) {
        sensorsData = timescaleSensorService.findTopNForEachSensor(limit);
      } else {
        sensorsData = timescaleSensorService.findAllForEachSensor();
      }
    } else {
      if (sensorIds != null && limit != null && (entityId == null
          || entityId == user.getEntity().getId())) {
        sensorsData = timescaleSensorService.findTopNBySensorIdListAndEntityId(limit,
            Arrays.asList(sensorIds), user.getEntity().getId());
      } else if (sensorIds != null && (entityId == null || entityId == user.getEntity().getId())) {
        sensorsData = timescaleSensorService.findAllBySensorIdListAndEntityId(
            Arrays.asList(sensorIds), user.getEntity().getId());
      } else if (limit != null && (entityId == null || entityId == user.getEntity().getId())) {
        sensorsData = timescaleSensorService.findTopNForEachSensorByEntityId(
            limit, user.getEntity().getId());
      } else if (entityId == null || entityId == user.getEntity().getId()) {
        sensorsData = timescaleSensorService.findAllForEachSensorByEntityId(
            user.getEntity().getId());
      }
    }

    return ResponseEntity.ok(sensorsData);
  }
}
