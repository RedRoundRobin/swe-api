package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
      return null;
    }
  }

  public ViewGraph findById(int id) {
    return repo.findById(id).orElse(null);
  }
}
