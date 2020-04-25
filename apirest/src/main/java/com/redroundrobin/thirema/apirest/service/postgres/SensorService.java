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
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private final SensorRepository sensorRepo;

  private final AlertRepository alertRepo;

  private final DeviceRepository deviceRepo;

  private final EntityRepository entityRepo;

  private final ViewGraphRepository viewGraphRepo;

  private boolean checkAddEditFieldsSensor(boolean edit, Map<String, Object> fields) {
    String[] editableOrCreatableFields = {"realSensorId", "deviceId", "cmdEnabled", "type"};
    List<String> allowedFields = new ArrayList<>();
    for(int i=0; i<editableOrCreatableFields.length; i++) {
      allowedFields.add(editableOrCreatableFields[i]);
    }

    if (edit) {
      return fields.keySet().stream().anyMatch(allowedFields::contains);
    } else {
      boolean flag = false;
      for(int i=0; i<editableOrCreatableFields.length && !flag; i++) {
        if(!fields.containsKey(editableOrCreatableFields[i])) {
          flag = true;
        }
      }
      return !flag && fields.keySet().size() == editableOrCreatableFields.length;
    }
  }

  private Sensor addEditSensor(Sensor sensor, Map<String, Object> fields)
      throws InvalidFieldsValuesException {
    if (sensor == null) {
      sensor = new Sensor();
    }

    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      switch (entry.getKey()) {
        case "realSensorId":
          sensor.setRealSensorId((int) entry.getValue());
          break;
        case "deviceId":
          Device device = deviceRepo.findById((int) entry.getValue()).orElse(null);
          if (device != null) {
            sensor.setDevice(device);
          } else {
            throw new InvalidFieldsValuesException("The device with the provided id is not found");
          }
          break;
        case "type":
          sensor.setType((String) entry.getValue());
          break;
        case "cmdEnabled":
          sensor.setCmdEnabled((boolean) entry.getValue());
          break;
        default:
      }
    }

    Sensor sensorWithSameDeviceAndRealSensorId = sensorRepo
        .findByDeviceAndRealSensorId(sensor.getDevice(), sensor.getRealSensorId());
    if (sensorWithSameDeviceAndRealSensorId != null
        && !sensorWithSameDeviceAndRealSensorId.equals(sensor)) { //mi sembra ridondante 2^ parte controllo...
      throw new InvalidFieldsValuesException("The sensor with provided device and realSensorId "
          + "already exists");
    }

    return sensorRepo.save(sensor);
  }


  @Autowired
  public SensorService(SensorRepository sensorRepository, AlertRepository alertRepository,
                       DeviceRepository deviceRepository, EntityRepository entityRepository,
                       ViewGraphRepository viewGraphRepository) {
    this.sensorRepo = sensorRepository;
    this.alertRepo = alertRepository;
    this.deviceRepo = deviceRepository;
    this.entityRepo = entityRepository;
    this.viewGraphRepo = viewGraphRepository;
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
    return sensorRepo.findByGatewayIdAndRealDeviceIdAndRealSensorId(gatewayId,realDeviceId,
        realSensorId);
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

  public Sensor addSensor(Map<String, Object> newSensorFields) throws MissingFieldsException,
      InvalidFieldsValuesException {
    if (checkAddEditFieldsSensor(false, newSensorFields)) {
      return addEditSensor(null, newSensorFields);
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

}