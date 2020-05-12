package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.utils.GatewaysProperties;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;



import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

  private final GatewayRepository gatewayRepo;

  private final DeviceRepository deviceRepo;

  private final KafkaTemplate<String, String> kafkaTemplate;

  private boolean checkAddEditFields(boolean edit, Map<String, Object> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("name");

    if (edit) {
      return fields.keySet().stream().anyMatch(allowedFields::contains);
    } else {
      return fields.containsKey("name");
    }
  }

  private String sentConfig(int gatewayId)
      throws JsonProcessingException { //eccezione di ObjectMapper()
    if(!gatewayRepo.existsById(gatewayId)) {
      return null;
    } else {
      Gateway gateway = gatewayRepo.findById(gatewayId).get();
      String gatewayConfigTopic = GatewaysProperties.getConfigTopicPrefix() + gateway.getName();
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode jsonGatewayConfig = objectMapper.createObjectNode();
      jsonGatewayConfig.put("maxStoredPackets", GatewaysProperties.getMaxStoredPackets());
      jsonGatewayConfig.put("maxStoringTime", GatewaysProperties.getMaxStoringTime());
      ArrayNode devicesConfig = jsonGatewayConfig.putArray("devices");
      List<Device> devices = (List<Device>)deviceRepo.findAllByGatewayId(gatewayId);
      for(Device device: devices) {
        ObjectNode completeDevice = objectMapper.createObjectNode();
        completeDevice.put("deviceId", device.getRealDeviceId());
        completeDevice.put("frequency", device.getFrequency());
        ArrayNode sensorsConfig = objectMapper.createArrayNode();
        List<Sensor> sensors = (List<Sensor>)deviceRepo.findAllByDeviceId(device.getId());
        for(Sensor sensor: sensors) {
          ObjectNode completeSensor = objectMapper.createObjectNode();
          completeSensor.put("sensorId", sensor.getRealSensorId());
          completeSensor.put("cmdEnabled", sensor.getCmdEnabled());
          sensorsConfig.add(completeSensor);
        }
        completeDevice.set("sensors", sensorsConfig);
        devicesConfig.add(completeDevice);
      }
      kafkaTemplate.send(gatewayConfigTopic, jsonGatewayConfig.toString());
      gateway.setLastSent(Timestamp.from(Instant.now()));
      gatewayRepo.save(gateway);
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString((JsonNode)jsonGatewayConfig);
    }
  }

  @Autowired
  public GatewayService(GatewayRepository gatewayRepository,
                        DeviceRepository deviceRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
    this.gatewayRepo = gatewayRepository;
    this.deviceRepo = deviceRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  public List<Gateway> findAll() {
    return (List<Gateway>) gatewayRepo.findAll();
  }

  public List<Gateway> findAllByEntityId(int entityId) {
    return (List<Gateway>) gatewayRepo.findAllByEntityId(entityId);
  }

  public Gateway findByDeviceId(int deviceId) {
    return gatewayRepo.findByDevice(deviceId);
  }

  public Gateway findByDeviceIdAndEntityId(int deviceId, int entityId) {
    return gatewayRepo.findByDeviceIdAndEntityId(deviceId, entityId);
  }

  public Gateway findById(int id) {
    return gatewayRepo.findById(id).orElse(null);
  }

  public Gateway findByIdAndEntityId(int id, int entityId) {
    return gatewayRepo.findByIdAndEntityId(id, entityId);
  }

  public String sendGatewayConfigToKafka(int gatewayId)
      throws InvalidFieldsValuesException, JsonProcessingException {
    String jsonGatewayConfig = sentConfig(gatewayId);
    if(jsonGatewayConfig == null) {
      throw new InvalidFieldsValuesException("");
    }
    return jsonGatewayConfig;
  }

  public Gateway addGateway(Map<String, Object> newGatewayFields) throws MissingFieldsException,
      InvalidFieldsValuesException {
    if (checkAddEditFields(false, newGatewayFields)) {
      if (gatewayRepo.findByName((String)newGatewayFields.get("name")) == null) {
        Gateway gateway = new Gateway((String)newGatewayFields.get("name"));
        return gatewayRepo.save(gateway);
      } else {
        throw new InvalidFieldsValuesException("The gateway with provided name already exists");
      }
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public Gateway editGateway(int gatewayId, Map<String, Object> fieldsToEdit) throws MissingFieldsException,
      InvalidFieldsValuesException {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway == null) {
      throw new InvalidFieldsValuesException("The gateway with provided id is not found");
    } else {
      if (checkAddEditFields(true, fieldsToEdit)) {
        if (gatewayRepo.findByName((String)fieldsToEdit.get("name")) == null) {
          gateway.setName((String)fieldsToEdit.get("name"));
          return gatewayRepo.save(gateway);
        } else {
          throw new InvalidFieldsValuesException("The gateway with provided name already exists");
        }
      } else {
        throw MissingFieldsException.defaultMessage();
      }
    }
  }

  public boolean deleteGateway(int gatewayId) throws ElementNotFoundException {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      gatewayRepo.delete(gateway);
      if (!gatewayRepo.existsById(gatewayId)) {
        return true;
      } else {
        return false;
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("gateway");
    }
  }

}