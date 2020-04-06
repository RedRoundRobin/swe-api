package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import java.util.Collections;
import java.util.List;

import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

  private EntityRepository entityRepo;

  private SensorRepository sensorRepo;

  private UserRepository userRepo;

  @Autowired
  public EntityService(EntityRepository entityRepository, SensorRepository sensorRepository,
                       UserRepository userRepository) {
    this.entityRepo = entityRepository;
    this.sensorRepo = sensorRepository;
    this.userRepo = userRepository;
  }

  public List<Entity> findAll() {
    return (List<Entity>) entityRepo.findAll();
  }

  public List<Entity> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return (List<Entity>) entityRepo.findAllBySensors(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Entity> findAllBySensorIdAndUserId(int sensorId, int userId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    User user = userRepo.findById(userId).orElse(null);
    if (sensor != null && user != null) {
      return (List<Entity>) entityRepo.findAllBySensorsAndUsers(sensor, user);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Entity> findAllByUserId(int userId) {
    User user = userRepo.findById(userId).orElse(null);
    if (user != null) {
      return (List<Entity>) entityRepo.findAllByUsers(user);
    } else {
      return Collections.emptyList();
    }
  }

  public Entity findById(int id) {
    return entityRepo.findById(id).orElse(null);
  }
}