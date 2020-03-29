package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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

  //tutti i gateway
  @GetMapping(value = {""})
  public ResponseEntity<List<Gateway>> getGateways(
      @RequestHeader(value = "Authorization") String authorization) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(gatewayService.findAll());
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  //un determinato gateway
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
  //tutti i dispositivi del gateway
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

  //il dispositivo del gateway
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

  //tutti i sensori che appartengono al dispositivo del gateway
  @GetMapping(value = {"/{gatewayId:.+}/devices/{realDeviceId:.+}/sensors"})
  public ResponseEntity<List<Sensor>> getGatewaysDevicesSensors(@RequestHeader(value = "Authorization")
                                                 String authorization,
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

  //il sensore che appartiene al dispositivo del gateway
  @GetMapping(value = {"/{gatewayId:.+}/devices/{realDeviceId:.+}/sensors/{realSensorId:.+}"})
  public ResponseEntity<Sensor> getGatewaysDevicesSensor(@RequestHeader(value = "Authorization")
                                                          String authorization,
                                                    @PathVariable("gatewayId") int gatewayId,
                                                    @PathVariable("realDeviceId") int realDeviceId,
                                                    @PathVariable("realSensorId") int realSensorId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(sensorService.findByGatewayIdAndRealDeviceIdAndRealSensorId(gatewayId,
          realDeviceId, realSensorId));
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
