package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.AlertService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
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

@RestController
@RequestMapping("/alerts")
public class AlertController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final AlertService alertService;

  @Autowired
  public AlertController(AlertService alertService, JwtUtil jwtUtil, LogService logService,
                         UserService userService) {
    super(jwtUtil, logService, userService);
    this.alertService = alertService;
  }

  @Operation(
      summary = "Delete alerts by sensorId",
      description = "The request for delete an alert by sensorId",
      responses = {
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
              description = "Not authorized. Only admins can do it",
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
  @DeleteMapping(value = {""})
  public ResponseEntity deleteAlerts(@RequestHeader(value = "Authorization") String authorization,
                                     @RequestParam(name = "sensorId") Integer sensorId,
                                     HttpServletRequest httpRequest) {
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      try {
        alertService.deleteAlertsBySensorId(sensorId);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "alert.delete",
            "alerts with sensorId = " + sensorId);
        return new ResponseEntity(HttpStatus.OK);
      } catch (ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId() + " is not "
          + "an Administrator.");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @Operation(
      summary = "Get alerts",
      description = "The request return an object with enabled and disabled list of the alerts "
          + "visible by the current user. If admin all alerts will be in enabled",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject(
                          name = "Success",
                          value = "{\"enabled\":[{\"threshold\": \"double\"," 
                              + "\"type\": \"0 | 1 | 2\",\"deleted\": \"boolean\","
                              + "\"entity\": \"int\",\"sensor\": \"int\"," 
                              + "\"lastSent\": \"timestamp\",\"alertId\": \"int\"\n}]," 
                              + "\"disabled\":[{\"threshold\": \"double\","
                              + "\"type\": \"0 | 1 | 2\",\"deleted\": \"boolean\","
                              + "\"entity\": \"int\",\"sensor\": \"int\","
                              + "\"lastSent\": \"timestamp\",\"alertId\": \"int\"}]}"
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
  public ResponseEntity<Map<String,List<Alert>>> getAlerts(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "entityId", required = false) Integer entityId,
      @RequestParam(name = "sensorId", required = false) Integer sensorId) {
    User user = this.getUserFromAuthorization(authorization);

    Map<String,List<Alert>> response = new HashMap<>();

    if (user.getType() == User.Role.ADMIN) {
      if (entityId != null && sensorId != null) {
        response.put("enabled", alertService.findAllByEntityIdAndSensorId(entityId, sensorId));
      } else if (entityId != null) {
        response.put("enabled", alertService.findAllByEntityId(entityId));
      } else if (sensorId != null) {
        response.put("enabled", alertService.findAllBySensorId(sensorId));
      } else {
        response.put("enabled", alertService.findAll());
      }
      response.put("disabled", Collections.emptyList());
    } else if (sensorId != null && (entityId == null || user.getEntity().getId() == entityId)) {
      List<Alert> disabledAlerts = alertService.findAllDisabledByUserId(user.getId());
      List<Alert> enabledAlerts = alertService.findAllByEntityIdAndSensorId(
          user.getEntity().getId(), sensorId);
      enabledAlerts.removeAll(disabledAlerts);

      response.put("enabled", enabledAlerts);
      response.put("disabled", disabledAlerts);
    } else if (entityId == null || user.getEntity().getId() == entityId) {
      List<Alert> disabledAlerts = alertService.findAllDisabledByUserId(user.getId());
      List<Alert> enabledAlerts = alertService.findAllByEntityId(user.getEntity().getId());
      enabledAlerts.removeAll(disabledAlerts);

      response.put("enabled", enabledAlerts);
      response.put("disabled", disabledAlerts);
    } else {
      response.put("enabled", Collections.emptyList());
      response.put("disabled", Collections.emptyList());
    }
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get single alert",
      description = "The request return the alert with corresponding id as alertId if visible "
          + "by the current user.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Alert.class)
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
  @GetMapping(value = {"/{alertId:.+}"})
  public ResponseEntity<Alert> getAlert(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable("alertId") int alertId) {
    User user = this.getUserFromAuthorization(authorization);

    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(alertService.findById(alertId));
    } else {
      try {
        return ResponseEntity.ok(alertService.findByIdAndEntityId(alertId,
            user.getEntity().getId()));
      } catch (NotAuthorizedException nae) {
        logger.debug(nae.toString());
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    }
  }

  @Operation(
      summary = "Create alert",
      description = "The request return an object corresponding to the alert created if"
          + " successful",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Alert.class)
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
  @PostMapping(value = {""})
  public ResponseEntity<Alert> createAlert(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> newAlertFields,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || user.getType() == User.Role.MOD) {
      try {
        Alert alert = alertService.addAlert(user, newAlertFields);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "alert.add",
            Integer.toString(alert.getId()));
        return ResponseEntity.ok(alert);
      } catch (MissingFieldsException | InvalidFieldsValuesException fe) {
        logger.debug(fe.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator or Moderator.");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  @Operation(
      summary = "Edit alert",
      description = "The request return an object corresponding to the alert edited if"
          + " successful",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Alert.class)
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
  @PutMapping(value = {"/{alertId:.+}"})
  public ResponseEntity<Alert> editAlert(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Object> fieldsToEdit,
      @PathVariable("alertId") int alertId,
      HttpServletRequest httpRequest) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || user.getType() == User.Role.MOD) {
      try {
        Alert alert = alertService.editAlert(user, fieldsToEdit, alertId);
        logService.createLog(user.getId(), getIpAddress(httpRequest), "alert.edit",
            Integer.toString(alertId));
        return ResponseEntity.ok(alert);
      } catch (MissingFieldsException | InvalidFieldsValuesException | ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } catch (NotAuthorizedException e) {
        logger.debug(e.toString());
        // go to return FORBIDDEN
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator or Moderator.");
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  @Operation(
      summary = "Delete alert by id",
      description = "The request for delete an alert by its id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
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
  @DeleteMapping(value = {"/{alertId:.+}"})
  public ResponseEntity deleteAlert(@RequestHeader("authorization") String authorization,
                                    @PathVariable("alertId") int alertId,
                                    HttpServletRequest httpRequest) {
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || (user.getType() == User.Role.MOD)) {
      try {
        if (alertService.deleteAlert(user, alertId)) {
          logService.createLog(user.getId(), getIpAddress(httpRequest), "alert.delete",
              Integer.toString(alertId));
          return new ResponseEntity(HttpStatus.OK);
        } else {
          logger.debug("RESPONSE STATUS: INTERNAL_SERVER_ERROR. Alert " + alertId
              + " is not been deleted due to a database error");
          return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } catch (ElementNotFoundException e) {
        logger.debug(e.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } catch (NotAuthorizedException e) {
        logger.debug(e.toString());
        // go to return FORBIDDEN
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an Administrator or Moderator.");
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  @Operation(
      summary = "Enable/disable alert for user",
      description = "The request for enable/disable user alert by alert id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
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
  @PostMapping("/{alertId:.+}")
  public ResponseEntity disableUserAlert(
      @RequestHeader("authorization") String authorization,
      @PathVariable("alertId") int alertId,
      @RequestParam(value = "userId") int userId,
      @RequestParam(value = "enable") boolean enable) {
    User user = this.getUserFromAuthorization(authorization);
    User userToEdit = userService.findById(userId);
    if (user.getType() == User.Role.ADMIN
        || (user.getType() == User.Role.MOD && user.getEntity() == userToEdit.getEntity())
        || user.getId() == userToEdit.getId()) {
      try {
        if (alertService.enableUserAlert(user, userToEdit, alertId, enable)) {
          return new ResponseEntity(HttpStatus.OK);
        } else {
          return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } catch (ElementNotFoundException enfe) {
        logger.debug(enfe.toString());
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } catch (NotAuthorizedException nae) {
        logger.debug(nae.toString());
        // go to return FORBIDDEN
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is a Moderator and the userToEdit is not in the same entity or editing user is "
          + "different from user to edit.");
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

}
