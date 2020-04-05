package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

  private AlertRepository repo;

  private EntityService entityService;

  private SensorService sensorService;

  private UserService userService;

  @Autowired
  public AlertService(AlertRepository alertRepository) {
    this.repo = alertRepository;
  }

  public List<Alert> findAll() {
    return (List<Alert>) repo.findAll();
  }

  public List<Alert> findAllByEntityId(int entityId) {
    Entity entity = entityService.findById(entityId);
    if (entity != null) {
      return (List<Alert>) repo.findAllByEntity(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorService.findById(sensorId);
    if (sensor != null) {
      return (List<Alert>) repo.findAllBySensor(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllByUserId(int userId) {
    User user = userService.findById(userId);
    if (user != null) {
      return (List<Alert>) repo.findAllByUsers(user);
    } else {
      return Collections.emptyList();
    }
  }

  public Alert findById(int id) {
    return repo.findById(id).orElse(null);
  }

  @Autowired
  public void setEntityService(EntityService entityService) {
    this.entityService = entityService;
  }

  @Autowired
  public void setSensorService(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

}