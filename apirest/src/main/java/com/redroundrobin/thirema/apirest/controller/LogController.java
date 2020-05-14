package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Log;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/logs")
public class LogController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  public LogController(JwtUtil jwtUtil, LogService logService, UserService userService) {
    super(jwtUtil, logService, userService);
  }

  @Operation(
      summary = "Get logs",
      description = "The request return a list of logs objects",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = Log.class))
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
  public ResponseEntity<List<Log>> getLogs(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "entityId", required = false) Integer entityId,
      @RequestParam(name = "limit", required = false) Integer limit) {
    User user = getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      if (entityId != null && limit != null) {
        return ResponseEntity.ok(logService.findTopNByEntityId(limit, entityId));
      } else if (entityId != null) {
        return ResponseEntity.ok(logService.findAllByEntityId(entityId));
      } else if (limit != null) {
        return ResponseEntity.ok(logService.findTopN(limit));
      } else {
        return ResponseEntity.ok(logService.findAll());
      }
    } else if (user.getType() == User.Role.MOD) {
      if (limit != null && (entityId == null || user.getEntity().getId() == entityId)) {
        return ResponseEntity.ok(logService.findTopNByEntityId(limit, user.getEntity().getId()));
      } else if (entityId == null || user.getEntity().getId() == entityId) {
        return ResponseEntity.ok(logService.findAllByEntityId(user.getEntity().getId()));
      } else {
        return ResponseEntity.ok(Collections.emptyList());
      }
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId() + " is not an administrator"
          + " or a moderator");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
