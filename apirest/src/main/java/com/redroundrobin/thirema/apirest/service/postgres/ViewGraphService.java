package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewGraphService {

  private ViewGraphRepository viewGraphRepo;

  private SensorRepository sensorRepo;

  private ViewRepository viewRepo;

  @Autowired
  public ViewGraphService(ViewGraphRepository viewGraphRepository,
                          SensorRepository sensorRepository, ViewRepository viewRepository) {
    this.viewGraphRepo = viewGraphRepository;
    this.sensorRepo = sensorRepository;
    this.viewRepo = viewRepository;
  }

  private ViewGraph addEditViewGraph(User user, ViewGraph viewGraph, Map<String, Integer> fields)
      throws InvalidFieldsValuesException {
    if (viewGraph == null) {
      viewGraph = new ViewGraph();
    }

    for (Map.Entry<String, Integer> entry : fields.entrySet()) {
      switch (entry.getKey()) {
        case "correlation":
          if (ViewGraph.Correlation.isValid(entry.getValue())) {
            viewGraph.setCorrelation(ViewGraph.Correlation.values()[entry.getValue()]);
          } else {
            throw new InvalidFieldsValuesException("The correlation with provided id is not found");
          }
          break;
        case "view":
          View view = viewRepo.findByViewIdAndUser(entry.getValue(), user);
          if (view != null) {
            viewGraph.setView(view);
          } else {
            throw new InvalidFieldsValuesException("The view with provided id is not found or "
                + "not authorized");
          }
          break;
        case "sensor1":
          Sensor sensor1;
          if (user.getType() == User.Role.ADMIN) {
            sensor1 = sensorRepo.findById(entry.getValue()).orElse(null);
          } else {
            sensor1 = sensorRepo.findBySensorIdAndEntities(entry.getValue(),
                user.getEntity());
          }
          if (sensor1 != null) {
            viewGraph.setSensor1(sensor1);
          } else {
            throw new InvalidFieldsValuesException("The sensor1 with provided id is not found or "
                + "not authorized");
          }
          break;
        case "sensor2":
          Sensor sensor2;
          if (user.getType() == User.Role.ADMIN) {
            sensor2 = sensorRepo.findById(entry.getValue()).orElse(null);
          } else {
            sensor2 = sensorRepo.findBySensorIdAndEntities(entry.getValue(),
                user.getEntity());
          }
          if (sensor2 != null) {
            viewGraph.setSensor2(sensor2);
          } else {
            throw new InvalidFieldsValuesException("The sensor2 with provided id is not found or "
                + "not authorized");
          }
          break;
        default:
      }
    }

    return viewGraphRepo.save(viewGraph);
  }

  private boolean checkFields(boolean edit, Map<String, Integer> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("correlation");
    allowedFields.add("view");
    allowedFields.add("sensor1");
    allowedFields.add("sensor2");

    if (edit) {
      return fields.keySet().stream().anyMatch(key -> allowedFields.contains(key));
    } else {
      return fields.containsKey("correlation") && fields.containsKey("view")
          && (fields.containsKey("sensor1") || fields.containsKey("sensor2"));
    }
  }

  public ViewGraph createViewGraph(User user, Map<String, Integer> newViewGraphFields)
      throws InvalidFieldsValuesException, MissingFieldsException {
    if (this.checkFields(false, newViewGraphFields)) {
      return this.addEditViewGraph(user, null, newViewGraphFields);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }

  public boolean deleteViewGraph(int viewGraphId) throws ElementNotFoundException {
    if (viewGraphRepo.existsById(viewGraphId)) {
      viewGraphRepo.deleteById(viewGraphId);
      return !viewGraphRepo.existsById(viewGraphId);
    } else {
      throw ElementNotFoundException.notFoundMessage("ViewGraph");
    }
  }

  public ViewGraph editViewGraph(User user, int viewGraphId, Map<String, Integer> fieldsToEdit)
      throws InvalidFieldsValuesException, MissingFieldsException {
    ViewGraph viewGraph = findById(viewGraphId);
    if (viewGraph == null) {
      throw new InvalidFieldsValuesException("The viewGraph with provided id is not found");
    } else if (this.checkFields(true, fieldsToEdit)) {
      return this.addEditViewGraph(user, viewGraph, fieldsToEdit);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }

  public List<ViewGraph> findAll() {
    return (List<ViewGraph>) viewGraphRepo.findAll();
  }

  public List<ViewGraph> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return (List<ViewGraph>) viewGraphRepo.findAllBySensor1OrSensor2(sensor, sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<ViewGraph> findAllByUserId(int userId) {
    return (List<ViewGraph>) viewGraphRepo.findAllByUserId(userId);
  }

  public List<ViewGraph> findAllByUserIdAndViewId(int userId, int viewId) {
    return (List<ViewGraph>) viewGraphRepo.findAllByUserIdAndViewId(userId, viewId);
  }

  public List<ViewGraph> findAllByViewId(int viewId) {
    View view = viewRepo.findById(viewId).orElse(null);
    if (view != null) {
      return (List<ViewGraph>) viewGraphRepo.findAllByView(view);
    } else {
      return Collections.emptyList();
    }
  }

  public ViewGraph findById(int id) {
    return viewGraphRepo.findById(id).orElse(null);
  }

  public boolean getPermissionByIdAndUserId(int id, int userId) throws ElementNotFoundException {
    ViewGraph viewGraph = findById(id);
    if (viewGraph == null) {
      throw ElementNotFoundException.notFoundMessage("ViewGraph");
    } else {
      return viewGraph.getView().getUser().getId() == userId;
    }
  }

}
