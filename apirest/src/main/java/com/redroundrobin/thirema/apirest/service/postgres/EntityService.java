package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

  private final EntityRepository entityRepo;

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
  public EntityService(EntityRepository entityRepository, SensorRepository sensorRepository,
                       UserRepository userRepository) {
    this.entityRepo = entityRepository;
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
        return true;
      } else {
        return false;
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("entity");
    }
  }
}