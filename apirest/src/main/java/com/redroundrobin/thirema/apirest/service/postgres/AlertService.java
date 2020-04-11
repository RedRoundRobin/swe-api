package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

  private final AlertRepository alertRepo;

  private final EntityRepository entityRepo;

  private final SensorRepository sensorRepo;

  private final UserRepository userRepo;

  private boolean checkFields(Map<String, Object> fields, boolean edit) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("threshold");
    allowedFields.add("type");
    allowedFields.add("sensor");
    allowedFields.add("entity");

    if (!edit) {
      return fields.containsKey("threshold") && fields.containsKey("type")
          && (fields.containsKey("sensor") && fields.containsKey("entity"));
    } else {
      return fields.keySet().stream().anyMatch(allowedFields::contains);
    }
  }

  private Alert addEditAlert(User user, Alert alert, Map<String, Object> fields)
      throws InvalidFieldsValuesException {
    if (alert == null) {
      alert = new Alert();
    }

    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      switch (entry.getKey()) {
        case "threshold":
          alert.setThreshold((double) entry.getValue());
          break;
        case "type":
          if (Alert.Type.isValid((int) entry.getValue())) {
            alert.setType(Alert.Type.values()[(int) entry.getValue()]);
          } else {
            throw new InvalidFieldsValuesException("The type with provided id is not found");
          }
          break;
        case "sensor":
          Sensor sensor;
          if (user.getType() == User.Role.ADMIN) {
            sensor = sensorRepo.findById((int) entry.getValue()).orElse(null);
          } else {
            sensor = sensorRepo.findBySensorIdAndEntities((int) entry.getValue(),
                user.getEntity());
          }
          if (sensor != null) {
            alert.setSensor(sensor);
          } else {
            throw new InvalidFieldsValuesException("The sensor with provided id is not found or "
                + "not authorized");
          }
          break;
        case "entity":
          Entity entity;
          if (user.getType() == User.Role.ADMIN
              || user.getEntity().getId() == (int) entry.getValue()) {
            entity = entityRepo.findById((int) entry.getValue()).orElse(null);
          } else {
            entity = null;
          }
          if (entity != null) {
            alert.setEntity(entity);
          } else {
            throw new InvalidFieldsValuesException("The entity with provided id is not found or "
                + "not authorized");
          }
          break;
        default:
      }
    }

    return alertRepo.save(alert);
  }

  @Autowired
  public AlertService(AlertRepository alertRepository, EntityRepository entityRepository,
                      SensorRepository sensorRepository, UserRepository userRepository) {
    this.alertRepo = alertRepository;
    this.entityRepo = entityRepository;
    this.sensorRepo = sensorRepository;
    this.userRepo = userRepository;
  }

  public List<Alert> findAll() {
    return (List<Alert>) alertRepo.findAllByDeletedFalse();
  }

  public List<Alert> findAllByEntityId(int entityId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      return (List<Alert>) alertRepo.findAllByEntityAndDeletedFalse(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllByEntityIdAndSensorId(int entityId, int sensorId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (entity != null && sensor != null) {
      return (List<Alert>) alertRepo.findAllByEntityAndSensorAndDeletedFalse(entity, sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return (List<Alert>) alertRepo.findAllBySensorAndDeletedFalse(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllDisabledByUserId(int userId) {
    User user = userRepo.findById(userId).orElse(null);
    if (user != null) {
      return (List<Alert>) alertRepo.findAllByUsersAndDeletedFalse(user);
    } else {
      return Collections.emptyList();
    }
  }

  public Alert findById(int id) {
    return alertRepo.findById(id).orElse(null);
  }

  public Alert findByIdAndEntityId(int id, int entityId) throws NotAuthorizedException {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      Alert alert = alertRepo.findById(id).orElse(null);
      if (alert == null || alert.getEntity().equals(entity)) {
        return alert;
      } else {
        throw NotAuthorizedException.notAuthorizedMessage("alert");
      }
    } else {
      return null;
    }
  }

  public Alert createAlert(User user, Map<String, Object> newAlertFields)
      throws InvalidFieldsValuesException, MissingFieldsException {
    if (this.checkFields(newAlertFields, false)) {
      return this.addEditAlert(user, null, newAlertFields);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }

  public Alert editAlert(User user, Map<String, Object> fieldsToEdit, int alertId)
      throws InvalidFieldsValuesException, MissingFieldsException, ElementNotFoundException,
      NotAuthorizedException {
    Alert alert = alertRepo.findById(alertId).orElse(null);
    if (alert == null) {
      throw ElementNotFoundException.notFoundMessage("alert");
    } else if (user.getType() == User.Role.MOD && !alert.getEntity().equals(user.getEntity())) {
      throw NotAuthorizedException.notAuthorizedMessage("alert");
    }
    if (this.checkFields(fieldsToEdit, true)) {
      return this.addEditAlert(user, alert, fieldsToEdit);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }

  public boolean enableUserAlert(User editingUser, User userToEdit, int alertId, boolean enable)
      throws ElementNotFoundException, NotAuthorizedException {
    Alert alert = findById(alertId);
    if (alert != null) {
      if (editingUser.getType() == User.Role.ADMIN
          || alert.getEntity().equals(userToEdit.getEntity())) {
        Set<Alert> userDisabledAlerts = userToEdit.getDisabledAlerts();
        if (enable && userDisabledAlerts.contains(alert)) {
          userDisabledAlerts.remove(alert);
        } else if (!enable && !userDisabledAlerts.contains(alert)) {
          userDisabledAlerts.add(alert);
        } else {
          return true;
        }
        userToEdit.setDisabledAlerts(userDisabledAlerts);
        User newUser = userRepo.save(userToEdit);
        return (enable && !newUser.getDisabledAlerts().contains(alert))
                || (!enable && newUser.getDisabledAlerts().contains(alert));
      } else {
        throw NotAuthorizedException.notAuthorizedMessage("alert");
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("alert");
    }
  }

  public boolean deleteAlert(User deletingUser, int alertId) throws ElementNotFoundException,
      NotAuthorizedException {
    Alert alert = alertRepo.findById(alertId).orElse(null);
    if (alert != null) {
      if (deletingUser.getType() == User.Role.ADMIN || (deletingUser.getType() == User.Role.MOD
          && deletingUser.getEntity().equals(alert.getEntity()))) {
        alert.setDeleted(true);
        Alert newAlert = alertRepo.save(alert);
        return newAlert.isDeleted();
      } else {
        throw NotAuthorizedException.notAuthorizedMessage("alert");
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("alert");
    }
  }

  public void deleteAlertsBySensorId(int sensorId) throws ElementNotFoundException {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      alertRepo.setDeletedBySensor(true, sensor);
    } else {
      throw ElementNotFoundException.notFoundMessage("sensor");
    }
  }
}