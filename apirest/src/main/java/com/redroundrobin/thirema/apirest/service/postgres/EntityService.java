package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
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
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

  private final EntityRepository entityRepo;

  private AlertRepository alertRepo;

  private final SensorRepository sensorRepo;

  private final UserRepository userRepo;

  private boolean checkAddEditFields(boolean edit, Map<String, String> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("name");
    allowedFields.add("location");

    if (edit) {
      return fields.keySet().stream().anyMatch(allowedFields::contains);
    } else {
      return fields.containsKey("name") && fields.containsKey("location");
    }
  }

  @Autowired
  public EntityService(EntityRepository entityRepository, AlertRepository alertRepository,
                       SensorRepository sensorRepository, UserRepository userRepository) {
    this.entityRepo = entityRepository;
    this.alertRepo = alertRepository;
    this.sensorRepo = sensorRepository;
    this.userRepo = userRepository;
  }

  public List<Entity> findAll() {
    return (List<Entity>) entityRepo.findAll();
  }

  public List<Entity> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return (List<Entity>) entityRepo.findAllBySensors(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Entity> findAllBySensorIdAndUserId(int sensorId, int userId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    User user = userRepo.findById(userId).orElse(null);
    if (sensor != null && user != null) {
      return (List<Entity>) entityRepo.findAllBySensorsAndUsers(sensor, user);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Entity> findAllByUserId(int userId) {
    User user = userRepo.findById(userId).orElse(null);
    if (user != null) {
      return (List<Entity>) entityRepo.findAllByUsers(user);
    } else {
      return Collections.emptyList();
    }
  }

  public Entity findById(int id) {
    return entityRepo.findById(id).orElse(null);
  }

  public Entity addEntity(Map<String, String> newEntityFields) throws MissingFieldsException {
    if (checkAddEditFields(false, newEntityFields)) {
      Entity entity = new Entity(newEntityFields.get("name"), newEntityFields.get("location"));
      return entityRepo.save(entity);
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public Entity editEntity(int entityId, Map<String, String> fieldsToEdit) throws MissingFieldsException, InvalidFieldsValuesException {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity == null) {
      throw new InvalidFieldsValuesException("The entity with provided id is not found");
    } else if (checkAddEditFields(true, fieldsToEdit)) {
      if (fieldsToEdit.containsKey("name")) {
        entity.setName(fieldsToEdit.get("name"));
      }

      if (fieldsToEdit.containsKey("location")) {
        entity.setLocation(fieldsToEdit.get("location"));
      }

      return entityRepo.save(entity);
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public boolean deleteEntity(int entityId) throws ElementNotFoundException {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      entity.setDeleted(true);
      if (entityRepo.save(entity).isDeleted()) {
        userRepo.setDeletedByEntity(entity);
        alertRepo.setDeletedByEntity(entity);
        return true;
      } else {
        return false;
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("entity");
    }
  }

  public boolean enableOrDisableSensorToEntity(int entityId, String SensorsToEnableOrDisable)
      throws ElementNotFoundException, JsonProcessingException {
    Entity entityToEdit = null;
    if(entityRepo.existsById(entityId)) {
      entityToEdit = entityRepo.findById(entityId).orElse(null);
    } else {
      throw ElementNotFoundException.notFoundMessage("entity");
    }

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonSensorsToEnableOrDisable =
        objectMapper.readTree(SensorsToEnableOrDisable);

    ArrayNode sensorsToInsert = (ArrayNode)jsonSensorsToEnableOrDisable.get("toInsert");
    boolean flag = false;
    Set<Sensor> sensorsEnabled = entityToEdit.getSensors();
    for(int i=0; i < sensorsToInsert.size() && !flag; i++) {
      int sensorToInsertId = sensorsToInsert.get(i).asInt();
      Sensor sensorToInsert = null;
      if(!sensorRepo.existsById(sensorToInsertId)
          || sensorsEnabled.contains(
              sensorToInsert = sensorRepo.findById(sensorToInsertId).orElse(null))) {
        flag= true;
      } else {
        sensorsEnabled.add(sensorToInsert);
      }
    } if(!flag) {
      throw new ElementNotFoundException("sensor not found or "
          + "already inserted in the given entity");
    }

    ArrayNode sensorsToDelete = (ArrayNode)jsonSensorsToEnableOrDisable.get("toDelete");
    for(int i=0; i < sensorsToDelete.size() && !flag; i++) {
      int sensorsToDeleteId = sensorsToDelete.get(i).asInt();
      Sensor sensorToDelete = null;
      if(!sensorRepo.existsById(sensorsToDeleteId)
          || sensorsEnabled.contains(
          sensorToDelete = sensorRepo.findById(sensorsToDeleteId).orElse(null))) {
        flag = false;
      } else {
        sensorsEnabled.remove(sensorToDelete);
      }
    } if(!flag) {
      throw ElementNotFoundException.notFoundMessage("sensor not found or "
          + "already not present in the given entity");
    }

    entityToEdit.setSensors(sensorsEnabled);
    entityRepo.save(entityToEdit);
    return true;
     /* userToEdit.setDisabledAlerts(userDisabledAlerts);
      User newUser = userRepo.save(userToEdit);
      return (enable && !newUser.getDisabledAlerts().containsAll(alert))
          || (!enable && newUser.getDisabledAlerts().contains(alert));
    } else {
      throw NotAuthorizedException.notAuthorizedMessage("alert");
    }*/
  }

}