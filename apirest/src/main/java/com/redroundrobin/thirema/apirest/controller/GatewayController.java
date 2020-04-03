package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/gateways"})
public class GatewayController extends CoreController {

  private GatewayService gatewayService;

  private DeviceService deviceService;

  private SensorService sensorService;

  @Autowired
  public GatewayController(GatewayService gatewayService, DeviceService deviceService,
                           SensorService sensorService) {
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
    if (user.getType() == User.Role.ADMIN && deviceId != null) {
      List<Gateway> gateways = new ArrayList<>();
      Gateway gateway = gatewayService.findByDeviceId(deviceId);
      if (gateway != null) {
        gateways.add(gateway);
      }
      return ResponseEntity.ok(gateways);
    } else if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(gatewayService.findAll());
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
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
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  // Get all devices by gatewayId
  @GetMapping(value = {"/{gatewayId:.+}/devices"})
  public ResponseEntity<List<Device>> getGatewaysDevices(@RequestHeader(value = "Authorization")
                                                           String authorization,
                                                     @PathVariable("gatewayId") int gatewayid) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(gatewayService.findById(gatewayid).getDevices());
    } else {
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
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
