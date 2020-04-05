package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private SensorRepository repo;

  private AlertService alertService;

  private DeviceService deviceService;

  private EntityService entityService;

  private ViewGraphService viewGraphService;

  @Autowired
  public SensorService(SensorRepository sensorRepository) {
    this.repo = sensorRepository;
  }

  public List<Sensor> findAll() {
    return (List<Sensor>) repo.findAll();
  }

  public List<Sensor> findAllByDeviceId(int deviceId) {
    Device device = deviceService.findById(deviceId);
    if (device != null) {
      return (List<Sensor>) repo.findAllByDevice(device);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Sensor> findAllByDeviceIdAndEntityId(int deviceId, int entityId) {
    Device device = deviceService.findById(deviceId);
    Entity entity = entityService.findById(entityId);
    if (device != null && entity != null) {
      return (List<Sensor>) repo.findAllByDeviceAndEntities(device, entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Sensor> findAllByEntityId(int entityId) {
    Entity entity = entityService.findById(entityId);
    if (entity != null) {
      return (List<Sensor>) repo.findAllByEntities(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Sensor> findAllByGatewayIdAndRealDeviceId(int gatewayId, int realDeviceId) {
    return (List<Sensor>) repo.findAllByGatewayIdAndRealDeviceId(gatewayId, realDeviceId);
  }

  public List<Sensor> findAllByViewGraphId(int viewGraphId) {
    ViewGraph viewGraph = viewGraphService.findById(viewGraphId);
    if (viewGraph != null) {
      return (List<Sensor>) repo.findAllByViewGraphs1OrViewGraphs2(viewGraph, viewGraph);
    } else {
      return Collections.emptyList();
    }
  }

  public Sensor findByAlertId(int alertId) {
    Alert alert = alertService.findById(alertId);
    if (alert != null) {
      return repo.findByAlerts(alert);
    } else {
      return null;
    }
  }

  public Sensor findByDeviceIdAndRealSensorId(int deviceId, int realSensorId) {
    Device device = deviceService.findById(deviceId);
    if (device != null) {
      return repo.findByDeviceAndRealSensorId(device, realSensorId);
    } else {
      return null;
    }
  }

  public Sensor findByDeviceIdAndRealSensorIdAndEntityId(int deviceId, int realSensorId,
                                                         int entityId) {
    Device device = deviceService.findById(deviceId);
    Entity entity = entityService.findById(entityId);
    if (device != null && entity != null) {
      return repo.findByDeviceAndRealSensorIdAndEntities(device, realSensorId, entity);
    } else {
      return null;
    }
  }

  public Sensor findByGatewayIdAndRealDeviceIdAndRealSensorId(int gatewayId, int realDeviceId,
                                                              int realSensorId) {
    return repo.findByGatewayIdAndRealDeviceIdAndRealSensorId(gatewayId,realDeviceId,realSensorId);
  }

  public Sensor findById(int id) {
    return repo.findById(id).orElse(null);
  }

  public Sensor findByIdAndEntityId(int id, int entityId) {
    Entity entity = entityService.findById(entityId);
    if (entity != null) {
      return repo.findBySensorIdAndEntities(id, entity);
    } else {
      return null;
    }
  }

  @Autowired
  public void setAlertService(AlertService alertService) {
    this.alertService = alertService;
  }

  @Autowired
  public void setDeviceService(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @Autowired
  public void setEntityService(EntityService entityService) {
    this.entityService = entityService;
  }

  @Autowired
  public void setViewGraphService(ViewGraphService viewGraphService) {
    this.viewGraphService = viewGraphService;
  }

}