package com.redroundrobin.thirema.apirest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
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
@RequestMapping(value = {"/gateways"})
public class GatewayController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final GatewayService gatewayService;

  private final DeviceService deviceService;

  private final SensorService sensorService;

  @Autowired
  public GatewayController(GatewayService gatewayService, DeviceService deviceService,
                           SensorService sensorService, JwtUtil jwtUtil, LogService logService,
                           UserService userService) {
    super(jwtUtil, logService, userService);
    this.gatewayService = gatewayService;
    this.deviceService = deviceService;
    this.sensorService = sensorService;
  }

  // Get all gateways
  @GetMapping(value = {""})
  public ResponseEntity<List<Gateway>> getGateways(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "deviceId", required = false) Integer deviceId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      if (deviceId != null) {
        List<Gateway> gateways = new ArrayList<>();
        Gateway gateway = gatewayService.findByDeviceId(deviceId);
        if (gateway != null) {
          gateways.add(gateway);
        }
        return ResponseEntity.ok(gateways);
      } else {
        return ResponseEntity.ok(gatewayService.findAll());
      }
    } else if (deviceId != null) {
      List<Gateway> gateways = new ArrayList<>();
      Gateway gateway = gatewayService.findByDeviceIdAndEntityId(deviceId,
          user.getEntity().getId());
      if (gateway != null) {
        gateways.add(gateway);
      }
      return ResponseEntity.ok(gateways);
    } else {
      return ResponseEntity.ok(gatewayService.findAllByEntityId(user.getEntity().getId()));
    }
  }

  // Get gateway by gatewayId
  @GetMapping(value = {"/{gatewayId:.+}"})
  public ResponseEntity<Gateway> getGateway(@RequestHeader(value = "Authorization")
                                               String authorization,
                                         @PathVariable("gatewayId") int gatewayId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(gatewayService.findById(gatewayId));
    } else {
      return ResponseEntity.ok(gatewayService.findByIdAndEntityId(gatewayId,
          user.getEntity().getId()));
    }
  }

  // Get all devices by gatewayId
  @GetMapping(value = {"/{gatewayId:.+}/devices"})
  public ResponseEntity<List<Device>> getGatewaysDevices(@RequestHeader(value = "Authorization")
                                                           String authorization,
                                                     @PathVariable("gatewayId") int gatewayid) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(deviceService.findAllByGatewayId(gatewayid));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  // Get device by gatewayId and realDeviceId
  @GetMapping(value = {"/{gatewayId:.+}/devices/{realDeviceId:.+}"})
  public ResponseEntity<Device> getGatewaysDevice(@RequestHeader(value = "Authorization")
                                                    String authorization,
                                              @PathVariable("gatewayId") int gatewayId,
                                              @PathVariable("realDeviceId") int realDeviceId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(deviceService.findByGatewayIdAndRealDeviceId(gatewayId,
          realDeviceId));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  // Get all sensors by gatewayId and realDeviceId
  @GetMapping(value = {"/{gatewayId:.+}/devices/{realDeviceId:.+}/sensors"})
  public ResponseEntity<List<Sensor>> getGatewaysDevicesSensors(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable("gatewayId") int gatewayId,
      @PathVariable("realDeviceId") int realDeviceId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(sensorService.findAllByGatewayIdAndRealDeviceId(gatewayId,
          realDeviceId));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  // Get sensor by gatewayId and realDeviceId and realSensorId
  @GetMapping(value = {"/{gatewayId:.+}/devices/{realDeviceId:.+}/sensors/{realSensorId:.+}"})
  public ResponseEntity<Sensor> getGatewaysDevicesSensor(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable("gatewayId") int gatewayId,
      @PathVariable("realDeviceId") int realDeviceId,
      @PathVariable("realSensorId") int realSensorId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(sensorService.findByGatewayIdAndRealDeviceIdAndRealSensorId(
          gatewayId, realDeviceId, realSensorId));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PostMapping(value = {""})
  public ResponseEntity<Gateway> addGateway(@RequestHeader("Authorization") String authorization,
                                         @RequestBody Map<String, Object> newGatewayFields,
                                         HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        Gateway gateway = gatewayService.addGateway(newGatewayFields);
        logService.createLog(user.getId(),ip,"gateway.created",
            Integer.toString(gateway.getId()));
        return ResponseEntity.ok(gateway);
      } catch (MissingFieldsException | InvalidFieldsValuesException e) {
        logger.debug(e.toString());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @PutMapping(value = {"/{gatewayId:.+}"})
  public ResponseEntity<Object> editGateway(@RequestHeader("Authorization") String authorization,
                                            @PathVariable(value = "gatewayId") int gatewayId,
                                            @RequestBody Map<String, Object> newGatewayFields,
                                            HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        if(newGatewayFields.containsKey("reconfig")
            && ((boolean)newGatewayFields.get("reconfig"))) {
          return ResponseEntity.ok(gatewayService.sendGatewayConfigToKafka(gatewayId));
        } else {
          Gateway gateway = gatewayService.editGateway(gatewayId, newGatewayFields);
          logService.createLog(user.getId(),ip,"gateway.edit",
              Integer.toString(gatewayId));
          return ResponseEntity.ok(gateway);
        }
      } catch (MissingFieldsException | InvalidFieldsValuesException
          | JsonProcessingException e) {
        logger.debug(e.toString());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping(value = {"/{gatewayId:.+}"})
  public ResponseEntity deleteGateway(@RequestHeader("Authorization") String authorization,
                                             @PathVariable(value = "gatewayId") int gatewayId,
                                             HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    User user = getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      try {
        if (gatewayService.deleteGateway(gatewayId)) {
          logService.createLog(user.getId(),ip,"gateway.delete",
              Integer.toString(gatewayId));
          return new ResponseEntity(HttpStatus.OK);
        } else {
          logger.debug("RESPONSE STATUS: CONFLICT. There was a db error during the deletion of "
              + "the gateway");
          return new ResponseEntity(HttpStatus.CONFLICT);
        }
      } catch (ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }



}
