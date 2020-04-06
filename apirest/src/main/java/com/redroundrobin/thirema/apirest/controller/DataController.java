package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.service.timescale.SensorService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/data")
public class DataController extends CoreController {

  private SensorService timescaleSensorService;

  private Map<Integer,List<Sensor>> getSensorsValuesByAdmin(Integer[] sensorIds, Integer limit,
                                                            Integer entityId) {
    Map<Integer, List<Sensor>> sensorsData;
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
    return sensorsData;
  }

  private Map<Integer,List<Sensor>> getSensorsValuesByUser(User user, Integer[] sensorIds,
                                                           Integer limit, Integer entityId) {
    Map<Integer, List<Sensor>> sensorsData = new HashMap<>();
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
    return sensorsData;
  }

  @Autowired
  public DataController(@Qualifier("timescaleSensorService") SensorService timescaleSensorService,
                        JwtUtil jwtUtil, LogService logService, UserService userService) {
    super(jwtUtil, logService, userService);
    this.timescaleSensorService = timescaleSensorService;
  }

  @GetMapping(value = {""})
  public ResponseEntity<Map<Integer, List<Sensor>>> getSensorsValues(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "sensors", required = false) Integer[] sensorIds,
      @RequestParam(name = "limit", required = false) Integer limit,
      @RequestParam(name = "entity", required = false) Integer entityId) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(getSensorsValuesByAdmin(sensorIds, limit, entityId));
    } else {
      return ResponseEntity.ok(getSensorsValuesByUser(user, sensorIds, limit, entityId));
    }
  }

  @GetMapping(value = {"/{sensorId:.+}"})
  public ResponseEntity<Sensor> getLastSensorValue(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable("sensorId") int sensorId) {
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(timescaleSensorService.findLastValueBySensorId(sensorId));
    } else {
      return ResponseEntity.ok(timescaleSensorService.findLastValueBySensorIdAndEntityId(sensorId,
          user.getEntity().getId()));
    }
  }
}
