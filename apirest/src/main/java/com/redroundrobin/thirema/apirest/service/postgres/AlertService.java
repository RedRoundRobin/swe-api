package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import java.util.Map;

import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

  private AlertRepository alertRepo;

  private EntityRepository entityRepo;

  private SensorRepository sensorRepo;

  private UserRepository userRepo;

  private boolean checkFields(Map<String, Object> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("threshold");
    allowedFields.add("type");
    allowedFields.add("sensor");
    allowedFields.add("entity");

    return fields.containsKey("threshold") && fields.containsKey("type")
        && (fields.containsKey("sensor") && fields.containsKey("entity"));
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
            sensor = sensorService.findById((int) entry.getValue());
          } else {
            sensor = sensorService.findByIdAndEntityId((int) entry.getValue(),
                user.getEntity().getId());
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
            entity = entityService.findById((int) entry.getValue());
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

    return repo.save(alert);
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
    return (List<Alert>) alertRepo.findAll();
  }

  public List<Alert> findAllByEntityId(int entityId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      return (List<Alert>) alertRepo.findAllByEntity(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllByEntityIdAndSensorId(int entityId, int sensorId) {
    Entity entity = entityService.findById(entityId);
    Sensor sensor = sensorService.findById(sensorId);
    if (entity != null && sensor != null) {
      return (List<Alert>) repo.findAllByEntityAndSensor(entity, sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return (List<Alert>) alertRepo.findAllBySensor(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllByUserId(int userId) {
    User user = userRepo.findById(userId).orElse(null);
    if (user != null) {
      return (List<Alert>) alertRepo.findAllByUsers(user);
    } else {
      return Collections.emptyList();
    }
  }

  public Alert findById(int id) {
    return alertRepo.findById(id).orElse(null);
  }


  public Alert createAlert(User user, Map<String, Object> newAlertFields)
      throws InvalidFieldsValuesException, MissingFieldsException {
    if (this.checkFields(newAlertFields)) {
      return this.addEditAlert(user, null, newAlertFields);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }

  public boolean enableUserAlert(User user, int alertId, boolean enable)
      throws ElementNotFoundException, NotAuthorizedException {
    Alert alert = findById(alertId);
    if (alert != null) {
      if (alert.getEntity().equals(user.getEntity())) {
        List<Alert> userDisabledAlerts = user.getDisabledAlerts();
        if (enable && userDisabledAlerts.contains(alert)) {
          userDisabledAlerts.remove(alert);
        } else if (!enable && !userDisabledAlerts.contains(alert)) {
          userDisabledAlerts.add(alert);
        } else {
          return true;
        }
        System.out.println("finish");
        user.setDisabledAlerts(userDisabledAlerts);
        User newUser = userRepo.save(user);
        if ((enable && !newUser.getDisabledAlerts().contains(alert))
            || (!enable && newUser.getDisabledAlerts().contains(alert))) {
          return true;
        } else {
          return false;
        }
      } else {
        throw new NotAuthorizedException("The alert with provided id is not authorized");
      }
    } else {
      throw new ElementNotFoundException("The alert with provided id is not found");
    }
  }
}