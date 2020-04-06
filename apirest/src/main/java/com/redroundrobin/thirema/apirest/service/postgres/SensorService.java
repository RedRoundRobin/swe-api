package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import java.util.Collections;
import java.util.List;

import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private SensorRepository sensorRepo;

  private AlertRepository alertRepo;

  private DeviceRepository deviceRepo;

  private EntityRepository entityRepo;

  private ViewGraphRepository viewGraphRepo;

  @Autowired
  public SensorService(SensorRepository sensorRepository) {
    this.sensorRepo = sensorRepository;
  }

  public List<Sensor> findAll() {
    return (List<Sensor>) sensorRepo.findAll();
  }

  public List<Sensor> findAllByDeviceId(int deviceId) {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    if (device != null) {
      return (List<Sensor>) sensorRepo.findAllByDevice(device);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Sensor> findAllByDeviceIdAndEntityId(int deviceId, int entityId) {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (device != null && entity != null) {
      return (List<Sensor>) sensorRepo.findAllByDeviceAndEntities(device, entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Sensor> findAllByEntityId(int entityId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      return (List<Sensor>) sensorRepo.findAllByEntities(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Sensor> findAllByGatewayIdAndRealDeviceId(int gatewayId, int realDeviceId) {
    return (List<Sensor>) sensorRepo.findAllByGatewayIdAndRealDeviceId(gatewayId, realDeviceId);
  }

  public List<Sensor> findAllByViewGraphId(int viewGraphId) {
    ViewGraph viewGraph = viewGraphRepo.findById(viewGraphId).orElse(null);
    if (viewGraph != null) {
      return (List<Sensor>) sensorRepo.findAllByViewGraphs1OrViewGraphs2(viewGraph, viewGraph);
    } else {
      return Collections.emptyList();
    }
  }

  public Sensor findByAlertId(int alertId) {
    Alert alert = alertRepo.findById(alertId).orElse(null);
    if (alert != null) {
      return sensorRepo.findByAlerts(alert);
    } else {
      return null;
    }
  }

  public Sensor findByDeviceIdAndRealSensorId(int deviceId, int realSensorId) {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    if (device != null) {
      return sensorRepo.findByDeviceAndRealSensorId(device, realSensorId);
    } else {
      return null;
    }
  }

  public Sensor findByDeviceIdAndRealSensorIdAndEntityId(int deviceId, int realSensorId,
                                                         int entityId) {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (device != null && entity != null) {
      return sensorRepo.findByDeviceAndRealSensorIdAndEntities(device, realSensorId, entity);
    } else {
      return null;
    }
  }

  public Sensor findByGatewayIdAndRealDeviceIdAndRealSensorId(int gatewayId, int realDeviceId,
                                                              int realSensorId) {
    return sensorRepo.findByGatewayIdAndRealDeviceIdAndRealSensorId(gatewayId,realDeviceId,realSensorId);
  }

  public Sensor findById(int id) {
    return sensorRepo.findById(id).orElse(null);
  }

  public Sensor findByIdAndEntityId(int id, int entityId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      return sensorRepo.findBySensorIdAndEntities(id, entity);
    } else {
      return null;
    }
  }

  @Autowired
  public void setAlertRepository(AlertRepository alertRepo) {
    this.alertRepo = alertRepo;
  }

  @Autowired
  public void setDeviceRepository(DeviceRepository deviceRepo) {
    this.deviceRepo = deviceRepo;
  }

  @Autowired
  public void setEntityRepository(EntityRepository entityRepo) {
    this.entityRepo = entityRepo;
  }

  @Autowired
  public void setViewGraphRepository(ViewGraphRepository viewGraphRepo) {
    this.viewGraphRepo = viewGraphRepo;
  }

}