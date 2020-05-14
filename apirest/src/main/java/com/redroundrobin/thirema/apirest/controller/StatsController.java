package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Log;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private DeviceService deviceService;

  private EntityService entityService;

  @Autowired
  public StatsController(JwtUtil jwtUtil, LogService logService, UserService userService,
                         DeviceService deviceService, EntityService entityService) {
    super(jwtUtil, logService, userService);
    this.deviceService = deviceService;
    this.entityService = entityService;
  }

  @Operation(
      summary = "Get stats values",
      description = "The request return a map containing"
          + " couples \"key-list of values\" where the key is a stat name and"
          + " the list of values is an integer. Generic users and moderators"
          + " can see more stats than an admin related to the entity to which they"
          + " belong",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject(
                          name = "Success admin",
                          value = "{\"activeMembers\": \"int\","
                              + "\"registeredUsers\":  \"int\","
                              + "\"registeredDevices\": \"int\","
                              + "\"entitiesNumber\": \"int\"}"
                      ),
                      @ExampleObject(
                          name = "Success user/moderator",
                          value = "{\"activeMembers\": \"int\","
                              + "\"entityActiveMembers\": \"int\","
                              + "\"registeredUsers\":  \"int\","
                              + "\"entityRegisteredUsers\": \"int\","
                              + "\"entitiesNumber\": \"int\","
                              + "\"registeredDevices\": \"int\","
                              + "\"entityRegisteredDevices\": \"int\"}"
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

  @GetMapping(value = "")
  public ResponseEntity<Map<String, Integer>> getStats(@RequestHeader("authorization") String authorization) {
    User user = getUserFromAuthorization(authorization);
    Map<String, Integer> response = new HashMap<>();
    LocalDateTime now = LocalDateTime.now();
    Timestamp oneHourBack = Timestamp.valueOf(now.minusHours(1));
    List<Log> allActiveMembersLog = logService.findAllLoginsByTimeAfter(oneHourBack);
    response.put("activeMembers", allActiveMembersLog.size());
    response.put("registeredUsers", userService.findAll().size());
    response.put("registeredDevices", deviceService.findAll().size());
    response.put("entitiesNumber", entityService.findAll().size());
    if (user.getType() != User.Role.ADMIN) {
      int entityId = user.getEntity().getId();
      List<User> entityUsers = userService.findAllByEntityId(entityId);
      int entityActiveMembers = (int) allActiveMembersLog.stream()
          .filter(l -> entityUsers
              .stream()
              .anyMatch(u -> u.getId() == l.getUserId()))
          .count();
      response.put("entityActiveMembers", entityActiveMembers);
      response.put("entityRegisteredUsers", entityUsers.size());
      response.put("entityRegisteredDevices", deviceService.findAllByEntityId(entityId).size());
    }
    return ResponseEntity.ok(response);
  }
}
