package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private SensorRepository repo;

  private EntityService entityService;

  @Autowired
  public SensorService(SensorRepository repo, EntityService entityService) {
    this.repo = repo;
    this.entityService = entityService;
  }

  public List<Sensor> findAll() {
    return (List<Sensor>) repo.findAll();
  }

  public Sensor find(int sensorId) {
    return repo.findById(sensorId).get();
  }

  public List<Sensor> findAllByEntityId(int entityId) {
    Entity entity = entityService.find(entityId);
    if (entity != null) {
      return (List<Sensor>) repo.findAllByEntities(entity);
    } else {
      return Collections.emptyList();
    }
  }
}