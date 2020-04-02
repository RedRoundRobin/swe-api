package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewGraphService {

  private ViewGraphRepository repo;

  private SensorService sensorService;

  private ViewService viewService;

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

  private ViewGraph addEditViewGraph(User user, ViewGraph viewGraph, Map<String, Integer> fields)
      throws InvalidFieldsException {
    if (viewGraph == null) {
      viewGraph = new ViewGraph();
    }

    for (Map.Entry<String, Integer> entry : fields.entrySet()) {
      switch (entry.getKey()) {
        case "correlation":
          if (ViewGraph.Correlation.isValid(entry.getValue())) {
            viewGraph.setCorrelation(ViewGraph.Correlation.values()[entry.getValue()]);
          } else {
            throw new InvalidFieldsException("The correlation with provided id is not found");
          }
          break;
        case "view":
          View view = viewService.findByIdAndUserId(entry.getValue(), user.getId());
          if (view != null) {
            viewGraph.setView(view);
          } else {
            throw new InvalidFieldsException("The view with provided id is not found or "
                + "not authorized");
          }
          break;
        case "sensor1":
          Sensor sensor1 = sensorService.findByIdAndEntityId(entry.getValue(),
              user.getEntity().getId());
          if (sensor1 != null) {
            viewGraph.setSensor1(sensor1);
          } else {
            throw new InvalidFieldsException("The sensor1 with provided id is not found or "
                + "not authorized");
          }
          break;
        case "sensor2":
          Sensor sensor2 = sensorService.findByIdAndEntityId(entry.getValue(),
              user.getEntity().getId());
          if (sensor2 != null) {
            viewGraph.setSensor2(sensor2);
          } else {
            throw new InvalidFieldsException("The sensor2 with provided id is not found or "
                + "not authorized");
          }
          break;
        default:
      }
    }

    return repo.save(viewGraph);
  }

  public ViewGraphService(ViewGraphRepository repo) {
    this.repo = repo;
  }

  @Autowired
  public void setSensorService(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  @Autowired
  public void setViewService(ViewService viewService) {
    this.viewService = viewService;
  }

  public List<ViewGraph> findAll() {
    return (List<ViewGraph>) repo.findAll();
  }

  public List<ViewGraph> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorService.findById(sensorId);
    if (sensor != null) {
      return (List<ViewGraph>) repo.findAllBySensor1OrSensor2(sensor, sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<ViewGraph> findAllByUserId(int userId) {
    return (List<ViewGraph>) repo.findAllByUserId(userId);
  }

  public List<ViewGraph> findAllByUserIdAndViewId(int userId, int viewId) {
    return (List<ViewGraph>) repo.findAllByUserIdAndViewId(userId, viewId);
  }

  public List<ViewGraph> findAllByViewId(int viewId) {
    View view = viewService.findById(viewId);
    if (view != null) {
      return (List<ViewGraph>) repo.findAllByView(view);
    } else {
      return Collections.emptyList();
    }
  }

  public ViewGraph findById(int id) {
    return repo.findById(id).orElse(null);
  }

  public boolean getPermissionByIdAndUserId(int id, int userId) throws ElementNotFoundException {
    ViewGraph viewGraph = findById(id);
    if (viewGraph == null) {
      throw ElementNotFoundException.defaultMessage("ViewGraph");
    } else {
      return viewGraph.getView().getId() == userId;
    }
  }

  public ViewGraph createViewGraph(User user, Map<String, Integer> newViewGraphFields)
      throws InvalidFieldsException, MissingFieldsException {
    if (this.checkFields(false, newViewGraphFields)) {
      return this.addEditViewGraph(user, null, newViewGraphFields);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }

  public ViewGraph editViewGraph(User user, int viewGraphId, Map<String, Integer> fieldsToEdit)
      throws InvalidFieldsException, MissingFieldsException {
    ViewGraph viewGraph = findById(viewGraphId);
    if (viewGraph == null) {
      throw new InvalidFieldsException("The viewGraph with provided id is not found");
    } else if (this.checkFields(true, fieldsToEdit)) {
      return this.addEditViewGraph(user, viewGraph, fieldsToEdit);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }

  public boolean deleteViewGraph(int viewGraphId) throws ElementNotFoundException {
    if (repo.existsById(viewGraphId)) {
      repo.deleteById(viewGraphId);
      return !repo.existsById(viewGraphId);
    } else {
      throw ElementNotFoundException.defaultMessage("ViewGraph");
    }
  }
}
