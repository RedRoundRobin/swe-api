package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EntityController extends CoreController {

  private EntityService entityService;

  public EntityController(EntityService entityService) {
    this.entityService = entityService;
  }

  @GetMapping(value = {"/entities"})
  public ResponseEntity<List<Entity>> getEntities(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "sensor", required = false) Integer sensor) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      if (sensor != null) {
        return ResponseEntity.ok(entityService.findAllBySensorId(sensor));
      } else {
        return ResponseEntity.ok(entityService.findAll());
      }
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
