package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import java.util.Collections;
import java.util.List;

import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

  private AlertRepository alertRepo;

  private EntityRepository entityRepo;

  private SensorRepository sensorRepo;

  private UserRepository userRepo;

  @Autowired
  public AlertService(AlertRepository alertRepository) {
    this.alertRepo = alertRepository;
  }

  public List<Alert> findAll() {
    return (List<Alert>) alertRepo.findAll();
  }

  public List<Alert> findAllByEntityId(int entityId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      return (List<Alert>) alertRepo.findAllByEntity(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return (List<Alert>) alertRepo.findAllBySensor(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllByUserId(int userId) {
    User user = userRepo.findById(userId).orElse(null);
    if (user != null) {
      return (List<Alert>) alertRepo.findAllByUsers(user);
    } else {
      return Collections.emptyList();
    }
  }

  public Alert findById(int id) {
    return alertRepo.findById(id).orElse(null);
  }

  @Autowired
  public void setEntityRepository(EntityRepository entityRepository) {
    this.entityRepo = entityRepository;
  }

  @Autowired
  public void setSensorRepository(SensorRepository sensorRepository) {
    this.sensorRepo = sensorRepository;
  }

  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepo = userRepository;
  }

}