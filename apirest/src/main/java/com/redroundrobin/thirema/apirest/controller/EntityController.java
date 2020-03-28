package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EntityController {

  private EntityService entityService;

  public EntityController(EntityService entityService) {
    this.entityService = entityService;
  }

  @GetMapping(value = {"/entities"})
  public ResponseEntity<List<Entity>> getEntities(
      @RequestParam(name = "sensor", required = false) Integer sensor) {
    if (sensor != null) {
      return ResponseEntity.ok(entityService.findAllBySensorId(sensor));
    } else {
      return ResponseEntity.ok(entityService.findAll());
    }
  }
}
