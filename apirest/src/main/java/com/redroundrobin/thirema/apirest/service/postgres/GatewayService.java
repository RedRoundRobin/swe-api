package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;



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

  @Value("${kafka.topic.gatewayConfig}")
  private String gatewayConfigTopic;

  private boolean checkConfigFields(String gatewayConfig)
      throws JsonProcessingException { //eccezione di ObjectMapper()
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonGatewayConfig = objectMapper.readTree(gatewayConfig);
    String[] firstLevelConfigFields = {"address", "port", "name",
        "maxStoredPackets", "maxStoringTime", "devices"};
    if(firstLevelConfigFields.length != jsonGatewayConfig.size()) { //jsonNode.size() figli 1° livello in teoria
      return false;
    }

    boolean flag = false;
    for(int i=0; i<firstLevelConfigFields.length && !flag; i++) {
      if(!jsonGatewayConfig.has(firstLevelConfigFields[i]))
        flag = true;
    }
    //versione in cui non controllo che siano dispositivi censiti..ok?
    if(flag || !jsonGatewayConfig.get("devices").isArray()) {
      return false;
    }

    ArrayNode devices = (ArrayNode)jsonGatewayConfig.get("devices");
    for(int i=0; i<devices.size() && !flag; i++) {
      JsonNode device = devices.get(i);
      String[] secondLevelConfigFields = {
          "deviceId", "frequency", "sensors"};
      if(secondLevelConfigFields.length != device.size()) { //jsonNode.size() figli 2° livello in teoria
        flag = true;
      }
      for(i=0; i<secondLevelConfigFields.length && !flag; i++) {
        if(!device.has(secondLevelConfigFields[i]))
          flag = true;
      }
      if(!flag) {
        if(!device.get("sensors").isArray()) {
          flag = true;
        } else {
          ArrayNode sensors = (ArrayNode)device.get("sensors");
          for(i=0; i<sensors.size() && !flag; i++) {
            JsonNode sensor = sensors.get(i);
            if(sensor.size() != 1 || !sensor.has("sensorId")) {
              flag = true;
            }
          }
        }
      }
    }
    return !flag;
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

  public void sendGatewayConfigToKafka(String gatewayConfig)
      throws MissingFieldsException, JsonProcessingException {
    if(!checkConfigFields(gatewayConfig)) {
      throw new MissingFieldsException("");
    }
    kafkaTemplate.send(gatewayConfigTopic, gatewayConfig);
  }

  public Gateway addGateway(Map<String, String> newGatewayFields) throws MissingFieldsException,
      InvalidFieldsValuesException {
    if (checkAddEditFields(false, newGatewayFields)) {
      if (gatewayRepo.findByName(newGatewayFields.get("name")) == null) {
        Gateway gateway = new Gateway(newGatewayFields.get("name"));
        return gatewayRepo.save(gateway);
      } else {
        throw new InvalidFieldsValuesException("The gateway with provided name already exists");
      }
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public Gateway editGateway(int gatewayId, Map<String, String> fieldsToEdit) throws MissingFieldsException,
      InvalidFieldsValuesException {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway == null) {
      throw new InvalidFieldsValuesException("The gateway with provided id is not found");
    } else {
      if (checkAddEditFields(true, fieldsToEdit)) {
        if (gatewayRepo.findByName(fieldsToEdit.get("name")) == null) {
          gateway.setName(fieldsToEdit.get("name"));
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

  private boolean checkAddEditFields(boolean edit, Map<String, String> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("name");

    if (edit) {
      return fields.keySet().stream().anyMatch(allowedFields::contains);
    } else {
      return fields.containsKey("name");
    }
  }
}