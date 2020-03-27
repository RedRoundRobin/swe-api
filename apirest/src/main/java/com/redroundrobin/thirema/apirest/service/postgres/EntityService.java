package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

  private EntityRepository repo;

  private SensorService sensorService;

  @Autowired
  public EntityService(EntityRepository entityRepository, SensorService sensorService) {
    this.repo = entityRepository;
    this.sensorService = sensorService;
  }

  public List<Entity> findAll() {
    return (List<Entity>) repo.findAll();
  }

  public List<Entity> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorService.find(sensorId);
    if (sensor != null) {
      return repo.findAllBySensors(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public Entity find(int id) {
    return repo.findById(id).orElse(null);
  }
}