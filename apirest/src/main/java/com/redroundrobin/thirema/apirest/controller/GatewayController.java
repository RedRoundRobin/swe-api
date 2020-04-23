package com.redroundrobin.thirema.apirest.controller;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

  /*probabilmente mi si deve passare anche il nome del topic! Al momento ce un solo gateway e
  * sto usando il suo topic per le configurazioni: va generalizzata la cosa!*/
  @PutMapping(value = {"/config"})
  public ResponseEntity<ListenableFuture<SendResult<String, String>>> sendGatewayConfigToKafka(@RequestHeader(value = "Authorization") String authorization,
                                                               @RequestBody String gatewayConfig) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      try {
        return ResponseEntity.ok(gatewayService.sendGatewayConfigToKafka(gatewayConfig));
      } catch(Exception e) {
        logger.debug("RESPONSE STATUS: FORBIDDEN. The gateway configuration given "
            + "is not well formed");
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
