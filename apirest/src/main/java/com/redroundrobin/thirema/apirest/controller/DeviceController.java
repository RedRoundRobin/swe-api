package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping(value = {"/devices"})
public class DeviceController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final DeviceService deviceService;

  private final SensorService sensorService;

  @Autowired
  public DeviceController(DeviceService deviceService, SensorService sensorService, JwtUtil jwtUtil,
                          LogService logService, UserService userService) {
    super(jwtUtil, logService, userService);
    this.deviceService = deviceService;
    this.sensorService = sensorService;
  }

  // Get all devices optionally filtered by entityId
  @GetMapping(value = {""})
  public ResponseEntity<List<Device>> getDevices(
      @RequestHeader("Authorization") String authorization,
      @RequestParam(value = "entity", required = false) Integer entityId,
      @RequestParam(value = "gatewayId", required = false) Integer gatewayId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      if (entityId != null && gatewayId != null) {
        return ResponseEntity.ok(deviceService.findAllByEntityIdAndGatewayId(entityId, gatewayId));
      } else if (gatewayId != null) {
        return ResponseEntity.ok(deviceService.findAllByGatewayId(gatewayId));
      } else if (entityId != null) {
        return ResponseEntity.ok(deviceService.findAllByEntityId(entityId));
      } else {
        return ResponseEntity.ok(deviceService.findAll());
      }
    } else {
      if (gatewayId != null && (entityId == null || user.getEntity().getId() == entityId)) {
        return ResponseEntity.ok(deviceService.findAllByEntityIdAndGatewayId(
            user.getEntity().getId(), gatewayId));
      } else if (entityId == null || user.getEntity().getId() == entityId) {
        return ResponseEntity.ok(deviceService.findAllByEntityId(user.getEntity().getId()));
      } else {
        return ResponseEntity.ok(Collections.emptyList());
      }
    }
  }

  // Get device by deviceId
  @GetMapping(value = {"/{deviceId:.+}"})
  public ResponseEntity<Device> getDevice(@RequestHeader("Authorization") String authorization,
                          @PathVariable("deviceId") int deviceId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(deviceService.findById(deviceId));
    } else {
      return ResponseEntity.ok(
          deviceService.findByIdAndEntityId(deviceId, user.getEntity().getId()));
    }
  }

  // Get all sensors by deviceId
  @GetMapping(value = {"/{deviceId:.+}/sensors"})
  public ResponseEntity<List<Sensor>> getSensorsByDevice(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("deviceId") int deviceId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(sensorService.findAllByDeviceId(deviceId));
    } else {
      return ResponseEntity.ok(
          sensorService.findAllByDeviceIdAndEntityId(deviceId, user.getEntity().getId()));
    }
  }

  // Get sensor by deviceId and realSensorId
  @GetMapping(value = {"/{deviceId:.+}/sensors/{realSensorId:.+}"})
  public ResponseEntity<Sensor> getSensorByDevice(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("deviceId") int deviceId,
      @PathVariable("realSensorId") int realSensorId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(sensorService.findByDeviceIdAndRealSensorId(deviceId, realSensorId));
    } else {
      return ResponseEntity.ok(sensorService.findByDeviceIdAndRealSensorIdAndEntityId(deviceId,
          realSensorId, user.getEntity().getId()));
    }
  }

  @PostMapping(value = {""})
  public ResponseEntity<Device> createDevice(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> newDeviceFields,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        Device device = deviceService.addDevice(newDeviceFields);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "device.created",
            Integer.toString(device.getId()));
        return ResponseEntity.ok(device);
      } catch (MissingFieldsException | InvalidFieldsValuesException fe) {
        logger.debug(fe.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator.");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PostMapping(value = {"/{deviceId:.+}/sensors"})
  public ResponseEntity<Sensor> createSensor(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> newSensorFields,
      @PathVariable("deviceId") int deviceId,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        newSensorFields.put("deviceId", deviceId);
        Sensor sensor = sensorService.addSensor(newSensorFields);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "sensor.created",
            Integer.toString(sensor.getId()));
        return ResponseEntity.ok(sensor);
      } catch (MissingFieldsException | InvalidFieldsValuesException fe) {
        logger.debug(fe.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator.");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PutMapping(value = {"/{deviceId:.+}"})
  public ResponseEntity<Device> editDevice(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> fieldsToEdit,
      @PathVariable("deviceId") int deviceId,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        Device device = deviceService.editDevice(deviceId, fieldsToEdit);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "device.edit",
            Integer.toString(deviceId));
        return ResponseEntity.ok(device);
      } catch (MissingFieldsException | InvalidFieldsValuesException | ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator.");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PutMapping(value = {"/{deviceId:.+}/sensors/{realSensorId:.+}"})
  public ResponseEntity<Sensor> editSensor(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> fieldsToEdit,
      @PathVariable("deviceId") int deviceId,
      @PathVariable("realSensorId") int realSensorId,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        Sensor sensor = sensorService.editSensor(realSensorId, deviceId, fieldsToEdit);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "sensor.edit",
            Integer.toString(realSensorId));
        return ResponseEntity.ok(sensor);
      } catch (MissingFieldsException | InvalidFieldsValuesException | ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator.");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping(value = {"/cmdEnabled/{cmdEnabled:.+}"})
  public ResponseEntity<List<Device>> getDevicesWithAtLeastOneCmdEnabled(
      @RequestHeader("authorization") String authorization,
      @PathVariable("cmdEnabled") boolean cmdEnabled,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(deviceService.getEnabled(cmdEnabled));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator.");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping(value = {"/{deviceId:.+}"})
  public ResponseEntity deleteDevice(@RequestHeader("authorization") String authorization,
                                    @PathVariable("deviceId") int deviceId,
                                    HttpServletRequest httpRequest) {
    User user = getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        if (deviceService.deleteDevice(deviceId)) {
          logService.createLog(user.getId(), getIpAddress(httpRequest), "device.deleted",
              Integer.toString(deviceId));
          return new ResponseEntity<>(HttpStatus.OK);
        } else {
          logger.debug("RESPONSE STATUS: INTERNAL_SERVER_ERROR. Alert " + deviceId
              + " is not been deleted due to a database error");
          return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
      } catch (ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator.");
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping(value = {"/{deviceId:.+}/sensors/{realSensorId:.+}"})
  public ResponseEntity deleteSensor(@RequestHeader("authorization") String authorization,
                                     @PathVariable("deviceId") int deviceId,
                                     @PathVariable("realSensorId") int realSensorId,
                                     HttpServletRequest httpRequest) {
    User user = getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        if (sensorService.deleteSensor(deviceId, realSensorId)) {
          logService.createLog(user.getId(), getIpAddress(httpRequest), "sensor.deleted",
              Integer.toString(realSensorId));
          return new ResponseEntity<>(HttpStatus.OK);
        } else {
          logger.debug("RESPONSE STATUS: INTERNAL_SERVER_ERROR. Alert " + realSensorId
              + " is not been deleted due to a database error");
          return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
      } catch (ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator.");
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }
}
