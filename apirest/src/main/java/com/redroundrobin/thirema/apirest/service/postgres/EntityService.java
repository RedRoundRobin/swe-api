package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

  private EntityRepository repo;

  private SensorService sensorService;

  private UserService userService;

  @Autowired
  public EntityService(EntityRepository entityRepository) {
    this.repo = entityRepository;
  }

  @Autowired
  public void setSensorService(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public List<Entity> findAll() {
    return (List<Entity>) repo.findAll();
  }

  public List<Entity> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorService.findById(sensorId);
    if (sensor != null) {
      return (List<Entity>) repo.findAllBySensors(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Entity> findAllByUserId(int userId) {
    User user = userService.findById(userId);
    if (user != null) {
      return (List<Entity>) repo.findAllByUsers(user);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Entity> findAllBySensorIdAndUserId(int sensorId, int userId) {
    Sensor sensor = sensorService.findById(sensorId);
    User user = userService.findById(userId);
    if (sensor != null && user != null) {
      return (List<Entity>) repo.findAllBySensorsAndUsers(sensor, user);
    } else {
      return Collections.emptyList();
    }
  }

  public Entity findById(int id) {
    return repo.findById(id).orElse(null);
  }
}