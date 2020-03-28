package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewGraphService {

  private ViewGraphRepository repo;

  private SensorService sensorService;

  public ViewGraphService(ViewGraphRepository repo) {
    this.repo = repo;
  }

  @Autowired
  public void setSensorService(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  public List<ViewGraph> findAll() {
    return (List<ViewGraph>) repo.findAll();
  }

  public List<ViewGraph> findAllBySensor(int sensorId) {
    Sensor sensor = sensorService.findById(sensorId);
    if (sensor != null) {
      return (List<ViewGraph>) repo.findAllBySensor1OrSensor2(sensor, sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public ViewGraph findById(int id) {
    return repo.findById(id).orElse(null);
  }
}
