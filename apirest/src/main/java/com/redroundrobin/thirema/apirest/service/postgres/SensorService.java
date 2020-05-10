package com.redroundrobin.thirema.apirest.service.postgres;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private final SensorRepository sensorRepo;

  private final AlertRepository alertRepo;

  private final DeviceRepository deviceRepo;

  private final EntityRepository entityRepo;

  private final ViewGraphRepository viewGraphRepo;

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Value(value = "${gateways.topic.telegram.prefix}")
  private String gatewayCommandsPrefix;

  private boolean checkAddEditFields(boolean edit, Map<String, Object> fields) {
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
        if(!fields.containsKey(editableOrCreatableFields[i])
            && editableOrCreatableFields[i] != "cmdEnabled") {
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
                       ViewGraphRepository viewGraphRepository,
                       KafkaTemplate<String, String> kafkaTemplate) {
    this.sensorRepo = sensorRepository;
    this.alertRepo = alertRepository;
    this.deviceRepo = deviceRepository;
    this.entityRepo = entityRepository;
    this.viewGraphRepo = viewGraphRepository;
    this.kafkaTemplate = kafkaTemplate;
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
    if (checkAddEditFields(false, newSensorFields)) {
      return addEditSensor(null, newSensorFields);
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public Sensor editSensor(int realSensorId, int deviceId, Map<String, Object> newSensorFields)
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    if (device == null) {
      throw ElementNotFoundException.notFoundMessage("device");
    }
    Sensor sensor = sensorRepo.findByDeviceAndRealSensorId(device, realSensorId);/*.orElse(null);!!*/
    if (sensor == null) {
      throw ElementNotFoundException.notFoundMessage("sensor");
    }
    if (checkAddEditFields(true, newSensorFields)) {
      return addEditSensor(sensor, newSensorFields);
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public boolean deleteSensor(int deviceId, int realSensorId) throws ElementNotFoundException {
  Device device = deviceRepo.findById(deviceId).orElse(null);
  if (device == null) {
    throw ElementNotFoundException.notFoundMessage("device");
  }
  Sensor sensor = sensorRepo.findByDeviceAndRealSensorId(device, realSensorId);/*.orElse(null);!!*/
  if (sensor == null) {
    throw ElementNotFoundException.notFoundMessage("sensor");
  }
  int sensorId = sensor.getId();
  sensorRepo.delete(sensor);
  return !sensorRepo.existsById(sensorId);
  }

  private boolean checkTelegramCommandFields(Set<String> keys) {
    Set<String> commandFields = new HashSet<>();
    commandFields.add("data");
    return commandFields.containsAll(keys);
  }

  public String sendTelegramCommandToSensor(int sensorId, Map<String,Object> keys)
      throws ElementNotFoundException, NotAuthorizedException {
    if(!checkTelegramCommandFields(keys.keySet()) ||
        (int)keys.get("data") < 0 || (int)keys.get("data") > 1) {
      throw new ElementNotFoundException("The data field is missing, its not the only field given"
          + "or it's value is not correct");
    }

    if(!sensorRepo.existsById(sensorId)) {
      throw new ElementNotFoundException("The given sensorId doesn't match "
          + "any sensor in the database");
    }

    Sensor sensor = sensorRepo.findById(sensorId).get();
    if(!sensor.getCmdEnabled()) {
      throw new NotAuthorizedException("The sensor with the sensorId given"
          + "is not allowed to receive commands");
    }
    Device device = null;
    Gateway gateway = null;
    if((device = deviceRepo.findBySensors(sensor)) == null
        || (gateway = device.getGateway()) == null) {
      throw new ElementNotFoundException(" The sensor given is "
          + "not connected to a device or it's device "
          + "is not connected to a gateway");
    }

    String gatewayConfigTopic =
        gatewayCommandsPrefix + gateway.getName();
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode jsonSensorCommand = objectMapper.createObjectNode();
    jsonSensorCommand.put("realSensorId", sensor.getRealSensorId());
    jsonSensorCommand.put("realDeviceId", device.getRealDeviceId());
    jsonSensorCommand.put("data", (int)keys.get("data"));
    kafkaTemplate.send(gatewayConfigTopic, jsonSensorCommand.toString());
    return jsonSensorCommand.toString();
  }

}