package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.service.timescale.SensorService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  
  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final SensorService timescaleSensorService;

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

  @Operation(
      summary = "Get sensors values",
      description = "The request return a map containing"
          + "couples \"key-list of values\" where the key is a sensor id and"
          + "the list of values is made of the records of values "
          + "related to the sensor with that id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject(
                          name = "Success",
                          value = "{\"1\":[{\"time\": \"timestamp\","
                              + "\"realSensorId\": \"int\",\"realDeviceId\": \"int\","
                              + "\"gatewayId\": \"String\",\"value\": \"double\"},"
                              + "\"time\": \"timestamp\","
                              + "\"realSensorId\": \"int\",\"realDeviceId\": \"int\","
                              + "\"gatewayId\": \"String\",\"value\": \"double\"}\n}],"
                              + "\"2\":[{\"time\": \"timestamp\","
                              + "\"realSensorId\": \"int\",\"realDeviceId\": \"int\","
                              + "\"gatewayId\": \"String\",\"value\": \"double\"}]}]}"
                      )
                  }
              )),
          @ApiResponse(
              responseCode = "400",
              description = "There is an error in the request",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "The authentication failed",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Not authorized",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Server error",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          )
      })

  @GetMapping(value = {""})
  public ResponseEntity<Map<Integer, List<Sensor>>> getSensorsValues(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "sensors", required = false) Integer[] sensorIds,
      @RequestParam(name = "limit", required = false) Integer limit,
      @RequestParam(name = "entityId", required = false) Integer entityId) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(getSensorsValuesByAdmin(sensorIds, limit, entityId));
    } else {
      return ResponseEntity.ok(getSensorsValuesByUser(user, sensorIds, limit, entityId));
    }
  }

  @Operation(
      summary = "Get last sensor value",
      description = "The request return the last value record reletad to the  sensor"
          + "that is identified by the given id.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Sensor.class)
              )),
          @ApiResponse(
              responseCode = "400",
              description = "There is an error in the request",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "The authentication failed",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Not authorized",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Server error",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          )
      })

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
