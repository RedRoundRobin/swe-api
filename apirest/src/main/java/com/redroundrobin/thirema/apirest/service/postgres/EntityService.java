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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

  private final EntityRepository entityRepo;

  private AlertRepository alertRepo;

  private final SensorRepository sensorRepo;

  private final UserRepository userRepo;

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private boolean checkAddEditFields(boolean edit, Map<String, Object> fields) {
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

  public Entity addEntity(Map<String, Object> newEntityFields) throws MissingFieldsException {
    if (checkAddEditFields(false, newEntityFields)) {
      Entity entity = new Entity((String)newEntityFields.get("name"),
          (String)newEntityFields.get("location"));
      return entityRepo.save(entity);
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public Entity editEntity(int entityId, Map<String, Object> fieldsToEdit)
      throws MissingFieldsException, InvalidFieldsValuesException {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity == null) {
      throw new InvalidFieldsValuesException("The entity with provided id is not found");
    } else if (checkAddEditFields(true, fieldsToEdit)) {
      if (fieldsToEdit.containsKey("name")) {
        entity.setName((String)fieldsToEdit.get("name"));
      }

      if (fieldsToEdit.containsKey("location")) {
        entity.setLocation((String)fieldsToEdit.get("location"));
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

  public boolean enableOrDisableSensorToEntity(int entityId,
                                               Map<String, Object> fieldsToEdit)
      throws ElementNotFoundException, MissingFieldsException {
    Entity entityToEdit = entityRepo.findById(entityId).orElse(null);
    if(entityToEdit == null) {
      throw ElementNotFoundException.notFoundMessage("entity");
    }

    if (!fieldsToEdit.containsKey("toInsert") || !fieldsToEdit.containsKey("toDelete")) {
      throw MissingFieldsException.defaultMessage();
    }/* else {
      if (fieldsToEdit.containsKey("toInsert") && fieldsToEdit.get("toInsert") instanceof List)
    }*/

    Set<Sensor> entitySensors = entityToEdit.getSensors();

    if (fieldsToEdit.containsKey("toInsert")) {
      List<Integer> sensorsToInsert = (ArrayList<Integer>) fieldsToEdit.get("toInsert");
      for (Integer sensorId : sensorsToInsert) {
        Sensor sensorToInsert = sensorRepo.findById(sensorId).orElse(null);
        if (sensorToInsert != null && !entitySensors.contains(sensorToInsert)) {
          entitySensors.add(sensorToInsert);
        }
      }
    }

    if (fieldsToEdit.containsKey("toDelete")) {
      List<Integer> sensorsToDelete = (ArrayList<Integer>) fieldsToEdit.get("toDelete");
      for (Integer sensorId : sensorsToDelete) {
        Sensor sensorToDelete = sensorRepo.findById(sensorId).orElse(null);
        if (sensorToDelete != null && entitySensors.contains(sensorToDelete)) {
          entitySensors.remove(sensorToDelete);
          alertRepo.deleteAlertsBySensor(sensorToDelete);
        }
      }
    }

    entityToEdit.setSensors(entitySensors);
    entityRepo.save(entityToEdit);
    return true;
  }
}