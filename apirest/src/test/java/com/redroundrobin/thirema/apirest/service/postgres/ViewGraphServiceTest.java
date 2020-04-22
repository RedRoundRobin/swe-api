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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
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
    entity1 = new Entity(1, "entity1", "loc1");
    entity2 = new Entity(2, "entity2", "loc2");

    allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);

    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 3);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);

    // --------------------------------------- Set Users -------------------------------------
    user1 = new User(1, "name1", "surname1", "email1", "pass1", User.Role.USER);
    user2 = new User(2, "name2", "surname2", "email2", "pass2", User.Role.USER);

    allUsers = new ArrayList<>();
    allUsers.add(user1);
    allUsers.add(user2);

    // --------------------------------------- Set Views -------------------------------------
    view1 = new View(1, "view1", user1);
    view2 = new View(2, "view2", user2);

    allView = new ArrayList<>();
    allView.add(view1);
    allView.add(view2);

    // ------------------------------------- Set ViewGraphs ---------------------------------------
    viewGraph1 = new ViewGraph(1, ViewGraph.Correlation.NULL);
    viewGraph2 = new ViewGraph(2, ViewGraph.Correlation.NULL);
    viewGraph3 = new ViewGraph(3, ViewGraph.Correlation.NULL);

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
    viewGraph1.setView(view1);
    viewGraph2.setView(view2);
    viewGraph3.setView(view1);

    // ----------------------------------- Set users to view -------------------------------------
    view1.setUser(user1);
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
    when(viewGraphRepo.findAllBySensor1OrSensor2(any(Sensor.class), any(Sensor.class))).thenAnswer(i -> allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getSensor1())
        || i.getArgument(0).equals(vg.getSensor1())
        || i.getArgument(1).equals(vg.getSensor2())
        || i.getArgument(1).equals(vg.getSensor2())).collect(Collectors.toList()));
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
    when(viewGraphRepo.findAllByView(any(View.class))).thenAnswer(i -> allViewGraphs.stream()
        .filter(vg -> i.getArgument(0).equals(vg.getView())).collect(Collectors.toList()));
    when(viewGraphRepo.findAllByUserIdAndViewId(anyInt(), anyInt())).thenAnswer(i -> allViewGraphs.stream()
        .filter(vg -> i.getArgument(0).equals(vg.getView().getUser().getId())
            && i.getArgument(1).equals(vg.getView().getId())).collect(Collectors.toList()));
    when(viewGraphRepo.findById(anyInt())).thenAnswer(i -> allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getId())).findFirst());
    when(viewGraphRepo.findByIdAndUserId(anyInt(), anyInt())).thenAnswer(i -> allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getId())
        && i.getArgument(1).equals(vg.getView().getUser().getId())).findFirst().orElse(null));
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

    assertFalse(viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsBySensor() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllBySensorId(sensor1.getId());

    assertFalse(viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsByNotExistentSensor() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllBySensorId(10);

    assertTrue(viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsByUserId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByUserId(user1.getId());

    assertFalse(viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsByNotExistentUserId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByUserId(10);

    assertTrue(viewGraphs.isEmpty());
  }

  @Test
  public void findAllViewGraphsByViewId() {
    List<ViewGraph> viewGraphs = viewGraphService.findAllByViewId(view1.getId());

    assertFalse(viewGraphs.isEmpty());
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
      viewGraphService.getPermissionByIdAndUserId(9, user1.getId());

      fail();
    } catch (ElementNotFoundException nfe) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void getViewGraphPermissionByIdAndUserIdElementNotFound() {
    try {
      boolean permitted = viewGraphService.getPermissionByIdAndUserId(viewGraph1.getId(), user1.getId());

      assertTrue(permitted);
    } catch (Exception e) {
      fail();
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
      ViewGraph viewGraph = viewGraphService.addViewGraph(user1, newViewGraphFields);

      assertNotNull(viewGraph);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      fail();
    }
  }

  @Test
  public void createViewGraphWithoutViewThrowsMissingFieldsException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", ViewGraph.Correlation.NULL.toValue());
    newViewGraphFields.put("sensor1", sensor1.getId());

    try {
      viewGraphService.addViewGraph(user1, newViewGraphFields);

      fail();
    } catch (MissingFieldsException e) {
      assertEquals("One or more needed fields are missing", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void createViewGraphWithInvalidViewThrowsInvalidFieldsValuesException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", ViewGraph.Correlation.NULL.toValue());
    newViewGraphFields.put("view", view2.getId());
    newViewGraphFields.put("sensor1", sensor1.getId());

    try {
      viewGraphService.addViewGraph(user1, newViewGraphFields);

      fail();
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The view with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void addViewGraphWithInvalidSensor2ThrowsInvalidFieldsValuesException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", ViewGraph.Correlation.NULL.toValue());
    newViewGraphFields.put("view", view1.getId());
    newViewGraphFields.put("sensor2", 9);

    try {
      viewGraphService.addViewGraph(user1, newViewGraphFields);

      fail();
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The sensor2 with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editViewGraphSuccessfull() {
    Map<String, Integer> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("correlation", ViewGraph.Correlation.PEARSON.toValue());

    try {
      ViewGraph viewGraph = viewGraphService.editViewGraph(user1, viewGraph1.getId(), fieldsToEdit);

      assertEquals(ViewGraph.Correlation.PEARSON, viewGraph.getCorrelation());
    } catch (Exception e) {
      fail();
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
      fail();
    }
  }

  @Test
  public void editViewGraphWithInvalidSensor1ThrowsInvalidFieldsValuesException() {
    Map<String, Integer> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("sensor1", 6);

    try {
      viewGraphService.editViewGraph(user1, viewGraph1.getId(), fieldsToEdit);

      fail();
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The sensor1 with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editViewGraphWithInvalidSensor2ThrowsInvalidFieldsValuesException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("correlation", 9);

    try {
      viewGraphService.editViewGraph(user1, viewGraph1.getId(), newViewGraphFields);

      fail();
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The correlation with provided id is not found", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void editViewGraphWithNoFieldsThrowsMissingFieldsException() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();

    try {
      viewGraphService.editViewGraph(user1, viewGraph1.getId(), newViewGraphFields);

      fail();
    } catch (MissingFieldsException e) {
      assertEquals("One or more needed fields are missing", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      fail();
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
      fail();
    }
  }

  @Test
  public void deleteViewGraphSimulateDbError() {
    when(viewGraphRepo.existsById(anyInt())).thenReturn(true);
    doNothing().when(viewGraphRepo).delete(any(ViewGraph.class));
    try {
      boolean deleted = viewGraphService.deleteViewGraph(viewGraph1.getId());

      assertFalse(deleted);
    } catch (Exception nfe) {
      fail();
    }
  }

  @Test
  public void deleteViewGraphIdNotFound() {
    when(viewGraphRepo.existsById(anyInt())).thenReturn(false);
    try {
      viewGraphService.deleteViewGraph(5);

      fail();
    } catch (ElementNotFoundException nfe) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }
  }
}
