package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ViewGraphServiceTest {

  private ViewGraphService viewGraphService;

  @MockBean
  private ViewGraphRepository viewGraphRepo;

  @MockBean
  private SensorRepository sensorRepo;

  @MockBean
  private ViewRepository viewRepo;


  private Entity entity1;
  private Entity entity2;
  private List<Entity> allEntities;

  private ViewGraph viewGraph1;
  private ViewGraph viewGraph2;
  private ViewGraph viewGraph3;
  List<ViewGraph> allViewGraphs;

  private View view1;
  private View view2;
  List<View> allView;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;
  List<Sensor> allSensors;

  private User user1;
  private User user2;
  private List<User> allUsers;

  @Before
  public void setUp() {
    this.viewGraphService = new ViewGraphService(viewGraphRepo, sensorRepo, viewRepo);


    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity();
    entity1.setId(1);
    entity2 = new Entity();
    entity2.setId(2);

    allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);

    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor();
    sensor1.setId(1);
    sensor1.setRealSensorId(1);

    sensor2 = new Sensor();
    sensor2.setId(2);
    sensor2.setRealSensorId(2);

    sensor3 = new Sensor();
    sensor3.setId(3);
    sensor3.setRealSensorId(1);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    // --------------------------------------- Set Users -------------------------------------
    user1 = new User();
    user1.setId(1);

    user2 = new User();
    user2.setId(2);

    allUsers = new ArrayList<>();
    allUsers.add(user1);
    allUsers.add(user2);


    // --------------------------------------- Set Views -------------------------------------
    view1 = new View();
    view1.setId(1);
    view1.setName("view1");

    view2 = new View();
    view2.setId(2);
    view2.setName("view2");

    allView = new ArrayList<>();
    allView.add(view1);
    allView.add(view2);


    // ------------------------------------- Set ViewGraphs ---------------------------------------
    viewGraph1 = new ViewGraph();
    viewGraph1.setId(1);

    viewGraph2 = new ViewGraph();
    viewGraph2.setId(2);

    viewGraph3 = new ViewGraph();
    viewGraph3.setId(3);

    allViewGraphs = new ArrayList<>();
    allViewGraphs.add(viewGraph1);
    allViewGraphs.add(viewGraph2);
    allViewGraphs.add(viewGraph3);


    // ------------------------------- Set sensors to viewGraphs --------------------------------
    viewGraph1.setSensor1(sensor1);
    viewGraph1.setSensor2(sensor2);

    viewGraph2.setSensor2(sensor1);

    viewGraph2.setSensor1(sensor3);


    // --------------------------------- Set view to viewGraphs ----------------------------------
    List<ViewGraph> view1ViewGraphs = new ArrayList<>();
    view1ViewGraphs.add(viewGraph1);
    view1ViewGraphs.add(viewGraph3);
    view1.setViewGraphs(view1ViewGraphs);
    viewGraph1.setView(view1);
    viewGraph3.setView(view1);

    List<ViewGraph> view2ViewGraphs = new ArrayList<>();
    view2ViewGraphs.add(viewGraph2);
    view2.setViewGraphs(view2ViewGraphs);
    viewGraph2.setView(view2);


    // ----------------------------------- Set users to view -------------------------------------
    List<View> user1Views = new ArrayList<>();
    user1Views.add(view1);
    view1.setUser(user1);

    List<View> user2Views = new ArrayList<>();
    user2Views.add(view2);
    view2.setUser(user2);


    // ---------------------------------- Set users to entities ----------------------------------
    user1.setEntity(entity1);

    user2.setEntity(entity2);


    // -------------------------------- Set sensors to entities ----------------------------------
    Set<Sensor> entity1Sensors = new HashSet<>();
    entity1Sensors.add(sensor1);
    entity1Sensors.add(sensor3);
    entity1.setSensors(entity1Sensors);

    Set<Sensor> entity2Sensors = new HashSet<>();
    entity2Sensors.add(sensor2);
    entity2.setSensors(entity2Sensors);



    when(viewGraphRepo.findAll()).thenReturn(allViewGraphs);
    when(viewGraphRepo.findAllBySensor1OrSensor2(any(Sensor.class), any(Sensor.class))).thenAnswer(i -> {
      return allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getSensor1())
          || i.getArgument(0).equals(vg.getSensor1())
          || i.getArgument(1).equals(vg.getSensor2())
          || i.getArgument(1).equals(vg.getSensor2())).collect(Collectors.toList());
    });
    when(viewGraphRepo.findAllByUserId(anyInt())).thenAnswer(i -> {
      List<ViewGraph> viewGraphs = new ArrayList<>();
      if (i.getArgument(0).equals(1)) {
        viewGraphs.add(viewGraph1);
        viewGraphs.add(viewGraph3);
      } else if (i.getArgument(0).equals(2)) {
        viewGraphs.add(viewGraph2);
      }
      return viewGraphs;
    });
    when(viewGraphRepo.findAllByView(any(View.class))).thenAnswer(i -> {
      return allViewGraphs.stream()
          .filter(vg -> i.getArgument(0).equals(vg.getView())).collect(Collectors.toList());
    });
    when(viewGraphRepo.findAllByUserIdAndViewId(anyInt(), anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream()
          .filter(vg -> i.getArgument(0).equals(vg.getView().getUser().getId())
              && i.getArgument(1).equals(vg.getView().getId())).collect(Collectors.toList());
    });
    when(viewGraphRepo.findById(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getId())).findFirst();
    });
    when(viewGraphRepo.findByIdAndUserId(anyInt(), anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getId())
          && i.getArgument(1).equals(vg.getView().getUser().getId())).findFirst().orElse(null);
    });
    when(viewGraphRepo.save(any(ViewGraph.class))).thenAnswer(i -> i.getArgument(0));

    when(sensorRepo.findById(anyInt())).thenAnswer(i -> allSensors.stream()
        .filter(s -> i.getArgument(0).equals(s.getId())).findFirst());
    when(sensorRepo.findBySensorIdAndEntities(anyInt(),any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(1);
      Sensor sensor = allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId())).findFirst().orElse(null);
      if (sensor != null && entity.getSensors().contains(sensor)) {
        return sensor;
      } else {
        return null;
      }
    });

    when(viewRepo.findById(anyInt())).thenAnswer(i -> allView.stream()
        .filter(s -> i.getArgument(0).equals(s.getId())).findFirst());
    when(viewRepo.findByViewIdAndUser(anyInt(), any(User.class))).thenAnswer(i -> {
      View view = allView.stream().filter(v -> i.getArgument(0).equals(v.getId())).findFirst().orElse(null);
      if (view != null && i.getArgument(1).equals(view.getUser())) {
        return view;
      } else {
        return null;
      }
    });
  }

  @Test
  public void findAllViewGraphs() {
    List<ViewGraph> viewGraphs = viewGraphService.findAll();

    assertEquals(allViewGraphs, viewGraphs);
  }



  @Test
  public void findAllViewGraphsByUserIdAndViewId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByUserIdAndViewId(user1.getId(), view1.getId());

    assertTrue(!viewGraphs.isEmpty());
  }



  @Test
  public void findAllViewGraphsBySensor() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllBySensorId(sensor1.getId());

    assertTrue(!viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsByNotExistentSensor() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllBySensorId(10);

    assertTrue(viewGraphs.isEmpty());
  }



  @Test
  public void findAllViewGraphsByUserId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByUserId(user1.getId());

    assertTrue(!viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsByNotExistentUserId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByUserId(10);

    assertTrue(viewGraphs.isEmpty());
  }



  @Test
  public void findAllViewGraphsByViewId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByViewId(view1.getId());

    assertTrue(!viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsByNotExistentViewId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByViewId(10);

    assertTrue(viewGraphs.isEmpty());
  }



  @Test
  public void findViewGraphById() {
    ViewGraph viewGraph = viewGraphService.findById(viewGraph1.getId());

    assertEquals(viewGraph1, viewGraph);
  }



  @Test
  public void getViewGraphPermissionByIdAndUserId() {
    try {
      boolean permitted = viewGraphService.getPermissionByIdAndUserId(9, user1.getId());

      assertTrue(false);
    } catch (ElementNotFoundException nfe) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void getViewGraphPermissionByIdAndUserIdElementNotFound() {
    try {
      boolean permitted = viewGraphService.getPermissionByIdAndUserId(viewGraph1.getId(), user1.getId());

      assertTrue(permitted);
    } catch (Exception e) {
      assertTrue(false);
    }
  }



  @Test
  public void createViewGraphSuccessfull() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", ViewGraph.Correlation.NULL.toValue());
    newViewGraphFields.put("view", view1.getId());
    newViewGraphFields.put("sensor1", sensor1.getId());
    newViewGraphFields.put("sensor2", sensor1.getId());

    try {
      ViewGraph viewGraph = viewGraphService.createViewGraph(user1, newViewGraphFields);

      assertNotNull(viewGraph);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      assertTrue(false);
    }
  }

  @Test
  public void createViewGraphWithoutViewThrowsMissingFieldsException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", ViewGraph.Correlation.NULL.toValue());
    newViewGraphFields.put("sensor1", sensor1.getId());

    try {
      ViewGraph viewGraph = viewGraphService.createViewGraph(user1, newViewGraphFields);

      assertTrue(false);
    } catch (MissingFieldsException e) {
      assertEquals("One or more needed fields are missing", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void createViewGraphWithInvalidViewThrowsInvalidFieldsValuesException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", ViewGraph.Correlation.NULL.toValue());
    newViewGraphFields.put("view", view2.getId());
    newViewGraphFields.put("sensor1", sensor1.getId());

    try {
      ViewGraph viewGraph = viewGraphService.createViewGraph(user1, newViewGraphFields);

      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The view with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void addViewGraphWithInvalidSensor2ThrowsInvalidFieldsValuesException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", ViewGraph.Correlation.NULL.toValue());
    newViewGraphFields.put("view", view1.getId());
    newViewGraphFields.put("sensor2", 9);

    try {
      ViewGraph viewGraph = viewGraphService.createViewGraph(user1, newViewGraphFields);

      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The sensor2 with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }



  @Test
  public void editViewGraphSuccessfull() {
    Map<String, Integer> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("correlation", ViewGraph.Correlation.PEARSON.toValue());

    try {
      ViewGraph viewGraph = viewGraphService.editViewGraph(user1, viewGraph1.getId(), fieldsToEdit);

      assertEquals(ViewGraph.Correlation.PEARSON, viewGraph.getCorrelation());
    } catch (InvalidFieldsValuesException e) {
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void editViewGraphWithNotExistentViewGraphIdThrowsInvalidFieldsValuesException() {
    Map<String, Integer> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("correlation", ViewGraph.Correlation.PEARSON.toValue());

    try {
      ViewGraph viewGraph = viewGraphService.editViewGraph(user1, 9, fieldsToEdit);

      assertEquals(ViewGraph.Correlation.PEARSON, viewGraph.getCorrelation());
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The viewGraph with provided id is not found", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void editViewGraphWithInvalidSensor1ThrowsInvalidFieldsValuesException() {
    Map<String, Integer> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("sensor1", 6);

    try {
      ViewGraph viewGraph = viewGraphService.editViewGraph(user1, viewGraph1.getId(), fieldsToEdit);

      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The sensor1 with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void editViewGraphWithInvalidSensor2ThrowsInvalidFieldsValuesException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", 9);

    try {
      ViewGraph viewGraph = viewGraphService.editViewGraph(user1, viewGraph1.getId(), newViewGraphFields);

      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The correlation with provided id is not found", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void editViewGraphWithNoFieldsThrowsMissingFieldsException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();

    try {
      ViewGraph viewGraph = viewGraphService.editViewGraph(user1, viewGraph1.getId(), newViewGraphFields);

      assertTrue(false);
    } catch (MissingFieldsException e) {
      assertEquals("One or more needed fields are missing", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }



  @Test
  public void deleteViewGraphSuccessfull() {
    when(viewGraphRepo.existsById(anyInt())).thenReturn(true).thenReturn(false);
    doNothing().when(viewGraphRepo).delete(any(ViewGraph.class));
    try {
      boolean deleted = viewGraphService.deleteViewGraph(viewGraph1.getId());

      assertTrue(deleted);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void deleteViewGraphSimulateDbError() {
    when(viewGraphRepo.existsById(anyInt())).thenReturn(true);
    doNothing().when(viewGraphRepo).delete(any(ViewGraph.class));
    try {
      boolean deleted = viewGraphService.deleteViewGraph(viewGraph1.getId());

      assertFalse(deleted);
    } catch (ElementNotFoundException nfe) {
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void deleteViewGraphIdNotFound() {
    when(viewGraphRepo.existsById(anyInt())).thenReturn(false);
    try {
      boolean deleted = viewGraphService.deleteViewGraph(5);

      assertTrue(false);
    } catch (ElementNotFoundException nfe) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }
}
