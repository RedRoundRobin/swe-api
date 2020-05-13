package com.redroundrobin.thirema.apirest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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


  @Operation(
      summary = "Get gateways",
      description = "The request returns a list of gateways objects",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = Gateway.class))
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

  @Operation(
      summary = "Get gateway",
      description = "The request return a gateway by the gateway id if it is visible for the current "
          + "user",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Gateway.class)
              )
          ),
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


  @Operation(
      summary = "Get devices",
      description = "The request return a list of devices connected to the gateway that"
          + "has the given gateway id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = Device.class))
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

  @Operation(
      summary = "Get device",
      description = "The request returns the device with the specified realDeviceId if it is connected to"
          + "the gateway with the specified gatewayId and if it is visible for the current user",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Device.class)
              )
          ),
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

  @Operation(
      summary = "Get sensors' list",
      description = "The request returns the sensors connected to the device with the given realDeviceId, "
          + "only if it is connected to the gateway with the given gatewayId.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = Sensor.class))
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

  @Operation(
      summary = "Get sensor",
      description = "The request returns the sensor with the specified realSensorId, which is"
          + "connected to the device with the specified realDeviceId, which is connected to"
          + "the gateway with the specified gatewayId, and only if it is visible for the current user",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Device.class)
              )
          ),
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

  @Operation(
      summary = "Create gateway",
      description = "The request returns the gateway that hass been created, if this "
          + "operation was successful",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Gateway.class)
              )
          ),
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

  @Operation(
      summary = "Edit a gateway or send a configuration to a gateway",
      description = "The request returns the gateway that has been edited, if this "
          + "operation was successful. If the request body contains : \"reconfig:true\","
          + "then the new configuration will be sent to the specified gateway",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Object.class)
              )
          ),
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
          logService.createLog(user.getId(),ip,"gateway.reconfig",
              Integer.toString(gatewayId));
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

  @Operation(
      summary = "Delete gateway",
      description = "The request deletes the specified gateway",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json"
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
              responseCode = "409",
              description = "Database error during the delete",
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
