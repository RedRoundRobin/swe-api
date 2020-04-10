package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Log;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.service.timescale.SensorService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.CollationElementIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/logs")
public class LogController extends CoreController {

  public LogController(JwtUtil jwtUtil, LogService logService, UserService userService) {
    super(jwtUtil, logService, userService);
  }

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
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
