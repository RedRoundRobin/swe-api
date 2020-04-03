package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.service.postgres.ViewGraphService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ViewGraphControllerTest {

  @MockBean
  JwtUtil jwtUtil;

  @MockBean
  private UserService userService;

  @MockBean
  ViewGraphService viewGraphService;

  private ViewGraphController viewGraphController;

  private String userTokenWithBearer = "Bearer userToken";
  private String adminTokenWithBearer = "Bearer adminToken";
  private String userToken = "userToken";
  private String adminToken = "adminToken";

  private User admin;
  private User user;
  private List<User> allUsers;

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

  private List<ViewGraph> view1ViewGraphs;
  private List<ViewGraph> view2ViewGraphs;

  private List<View> user1Views;
  private List<View> user2Views;


  @Before
  public void setUp() throws ElementNotFoundException, MissingFieldsException, InvalidFieldsException {
    viewGraphController = new ViewGraphController(viewGraphService);
    viewGraphController.setJwtUtil(jwtUtil);
    viewGraphController.setUserService(userService);


    admin = new User();
    admin.setId(1);
    admin.setEmail("admin");
    admin.setType(User.Role.ADMIN);

    user = new User();
    user.setId(2);
    user.setEmail("user");
    user.setType(User.Role.USER);

    allUsers = new ArrayList<>();
    allUsers.add(user);
    allUsers.add(admin);


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


    // --------------------------------------- Set ViewGraphs -------------------------------------
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


    // -------------------------- Set viewGraphs to sensors and viceversa --------------------------
    List<ViewGraph> sensor1AsSensor1ViewGraphs1 = new ArrayList<>();
    sensor1AsSensor1ViewGraphs1.add(viewGraph1);
    viewGraph1.setSensor1(sensor1);
    List<ViewGraph> sensor2AsSensor2ViewGraphs1 = new ArrayList<>();
    sensor2AsSensor2ViewGraphs1.add(viewGraph1);
    viewGraph1.setSensor2(sensor2);
    sensor2.setViewGraphs1(Collections.emptyList());
    sensor2.setViewGraphs2(sensor2AsSensor2ViewGraphs1);

    List<ViewGraph> sensor1AsSensor2ViewGraphs2 = new ArrayList<>();
    sensor1AsSensor2ViewGraphs2.add(viewGraph2);
    viewGraph2.setSensor2(sensor1);
    sensor1.setViewGraphs1(sensor1AsSensor1ViewGraphs1);
    sensor1.setViewGraphs2(sensor1AsSensor2ViewGraphs2);


    List<ViewGraph> sensor3AsSensor1ViewGraphs2 = new ArrayList<>();
    sensor3AsSensor1ViewGraphs2.add(viewGraph2);
    viewGraph2.setSensor1(sensor3);
    sensor3.setViewGraphs1(sensor3AsSensor1ViewGraphs2);
    sensor3.setViewGraphs2(Collections.emptyList());


    // -------------------------- Set viewGraphs to view and viceversa --------------------------
    view1ViewGraphs = new ArrayList<>();
    view1ViewGraphs.add(viewGraph1);
    view1ViewGraphs.add(viewGraph3);
    view1.setViewGraphs(view1ViewGraphs);
    viewGraph1.setView(view1);
    viewGraph3.setView(view1);

    view2ViewGraphs = new ArrayList<>();
    view2ViewGraphs.add(viewGraph2);
    view2.setViewGraphs(view2ViewGraphs);
    viewGraph2.setView(view2);


    // -------------------------- Set users to view and viceversa --------------------------
    user1Views = new ArrayList<>();
    user1Views.add(view1);
    view1.setUser(admin);
    admin.setViews(user1Views);

    user2Views = new ArrayList<>();
    user2Views.add(view2);
    view2.setUser(user);
    user.setViews(user2Views);


    // -------------------------- Set users to entities and viceversa --------------------------
    List<User> entity1Users = new ArrayList<>();
    entity1Users.add(user);
    entity1.setUsers(entity1Users);
    user.setEntity(entity1);


    // -------------------------- Set sensors to entities and viceversa --------------------------
    List<Sensor> entity1Sensors = new ArrayList<>();
    entity1Sensors.add(sensor1);
    entity1Sensors.add(sensor3);
    entity1.setSensors(entity1Sensors);
    List<Entity> sensor1And3Entities = new ArrayList<>();
    sensor1And3Entities.add(entity1);
    sensor1.setEntities(sensor1And3Entities);
    sensor3.setEntities(sensor1And3Entities);

    List<Sensor> entity2Sensors = new ArrayList<>();
    entity2Sensors.add(sensor2);
    entity2.setSensors(entity2Sensors);
    List<Entity> sensor2Entities = new ArrayList<>();
    sensor2Entities.add(entity2);
    sensor2.setEntities(sensor2Entities);



    // Core Controller needed mock
    user.setEntity(entity1);
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(viewGraphService.findAll()).thenReturn(allViewGraphs);
    when(viewGraphService.findAllByUserId(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream()
          .filter(vg -> i.getArgument(0).equals(vg.getView().getUser().getId())).collect(Collectors.toList());
    });
    when(viewGraphService.findAllByUserIdAndViewId(anyInt(), anyInt())).thenAnswer(i -> {
      User user = allUsers.stream()
          .filter(u -> i.getArgument(0).equals(u.getId())).findFirst().orElse(null);
      if (user != null) {
        return allViewGraphs.stream().filter(vg -> i.getArgument(1).equals(vg.getView().getId())
            && vg.getView().getUser().equals(user)).collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(viewGraphService.findAllByViewId(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream()
          .filter(vg -> i.getArgument(0).equals(vg.getView().getId())).collect(Collectors.toList());
    });
    when(viewGraphService.findAllBySensorId(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream()
          .filter(vg -> i.getArgument(0).equals(vg.getSensor1().getId())
              || i.getArgument(0).equals(vg.getSensor2().getId())).collect(Collectors.toList());
    });
    when(viewGraphService.findById(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream()
          .filter(vg -> i.getArgument(0).equals(vg.getId())).findFirst().orElse(null);
    });
    when(viewGraphService.getPermissionByIdAndUserId(anyInt(), anyInt())).thenAnswer(i -> {
      ViewGraph viewGraph = allViewGraphs.stream()
          .filter(vg -> i.getArgument(0).equals(vg.getId())).findFirst().orElse(null);
      if (viewGraph != null) {
        return i.getArgument(1).equals(viewGraph.getView().getUser().getId());
      } else {
        throw new ElementNotFoundException("not found");
      }
    });
    when(viewGraphService.createViewGraph(any(User.class), any(HashMap.class))).thenAnswer(i -> {
      Map<String, Object> fields = i.getArgument(1);
      if (fields.keySet().contains("sensor1") && fields.get("sensor1").equals(sensor1.getId())) {
        ViewGraph viewGraph = new ViewGraph();
        viewGraph.setView(view1);
        viewGraph.setSensor1(sensor1);
        viewGraph.setCorrelation(ViewGraph.Correlation.COVARIANCE);
        return viewGraph;
      } else {
        throw new MissingFieldsException("");
      }
    });
  }

  @Test
  public void getAllViewGraphsSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(adminTokenWithBearer,
        null, null);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allViewGraphs, response.getBody());
  }

  @Test
  public void getAllViewGraphsByAdminByUserIdAndViewIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(adminTokenWithBearer,
        user.getId(), view2.getId());

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(view2ViewGraphs, response.getBody());
  }

  @Test
  public void getAllViewGraphsByAdminByUserIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(adminTokenWithBearer,
        admin.getId(), null);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(!response.getBody().isEmpty());
  }

  @Test
  public void getAllViewGraphsByAdminByViewIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(adminTokenWithBearer,
        null, view1.getId());

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(view1ViewGraphs, response.getBody());
  }

  @Test
  public void getAllViewGraphsByUserByUserIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(userTokenWithBearer,
        user.getId(), null);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(view2ViewGraphs, response.getBody());
  }

  @Test
  public void getAllViewGraphsByUserByUserIdAndViewIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(userTokenWithBearer,
        user.getId(), view2.getId());

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(view2ViewGraphs, response.getBody());
  }

  @Test
  public void getAllViewGraphsByUserByAdminIdEmpty() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(userTokenWithBearer,
        admin.getId(), null);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }



  @Test
  public void getViewGraphByUserByIdSuccessfull() {

    ResponseEntity<ViewGraph> response = viewGraphController.getViewGraph(userTokenWithBearer,
        viewGraph2.getId());

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(viewGraph2, response.getBody());
  }

  @Test
  public void getViewGraphByUserByIdNotPermitted() {

    ResponseEntity<ViewGraph> response = viewGraphController.getViewGraph(userTokenWithBearer,
        viewGraph1.getId());

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void getViewGraphByUserByNotExistentId() {

    ResponseEntity<ViewGraph> response = viewGraphController.getViewGraph(userTokenWithBearer,
        9);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }



  @Test
  public void createViewGraphByAdminSuccessfull() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("view", view1.getId());
    newViewGraphFields.put("correlation", 1);
    newViewGraphFields.put("sensor1", sensor1.getId());

    ViewGraph viewGraph = new ViewGraph();
    viewGraph.setView(view1);
    viewGraph.setCorrelation(ViewGraph.Correlation.COVARIANCE);
    viewGraph.setSensor1(sensor1);

    ResponseEntity<ViewGraph> response = viewGraphController.createUserViewGraphs(
        adminTokenWithBearer, newViewGraphFields);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(viewGraph.getView(), response.getBody().getView());
    assertEquals(viewGraph.getSensor1(), response.getBody().getSensor1());
    assertEquals(viewGraph.getCorrelation(), response.getBody().getCorrelation());
  }


  @Test
  public void createViewGraphByAdminMissingNecessaryFields() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("view", view1.getId());
    newViewGraphFields.put("sensor1", sensor2.getId());

    ResponseEntity<ViewGraph> response = viewGraphController.createUserViewGraphs(
        adminTokenWithBearer, newViewGraphFields);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }



  @Test
  public void editViewGraph1ByAdminSuccessfull() throws Exception {
    Map<String, Integer> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("sensor1", sensor3.getId());

    viewGraph1.setSensor1(sensor3);
    when(viewGraphService.editViewGraph(admin, viewGraph1.getId(), fieldsToEdit))
        .thenReturn(viewGraph1);

    ResponseEntity<ViewGraph> response = viewGraphController.editViewGraph(
        adminTokenWithBearer, viewGraph1.getId(), fieldsToEdit);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(viewGraph1, response.getBody());
  }

  @Test
  public void editViewGraphByUserElementNotFoundError400() {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("view", view1.getId());

    //when(viewGraphService.editViewGraph(any(User.class), anyInt(), any(Map.class)))
     //   .thenThrow(new ElementNotFoundException(" "));

    ResponseEntity<ViewGraph> response = viewGraphController.editViewGraph(
        userTokenWithBearer, 9, newViewGraphFields);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void editViewGraphByUserViewGraphNotAuthorizedError403() throws Exception {
    Map<String, Integer> newViewGraphFields = new HashMap<>();
    newViewGraphFields.put("view", view1.getId());

    ResponseEntity<ViewGraph> response = viewGraphController.editViewGraph(
        userTokenWithBearer, viewGraph1.getId(), newViewGraphFields);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }



  @Test
  public void deleteViewGraphByAdminSuccessfull() throws ElementNotFoundException {
    when(viewGraphService.deleteViewGraph(anyInt())).thenReturn(true);

    ResponseEntity response = viewGraphController.deleteUserViewGraph(adminTokenWithBearer,
        viewGraph1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void deleteViewGraphByAdminConflictError409() throws ElementNotFoundException {
    when(viewGraphService.deleteViewGraph(anyInt())).thenReturn(false);

    ResponseEntity response = viewGraphController.deleteUserViewGraph(adminTokenWithBearer,
        viewGraph1.getId());

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  public void deleteViewGraphByUserNotAuthorizedError403() {
    ResponseEntity response = viewGraphController.deleteUserViewGraph(userTokenWithBearer,
        viewGraph1.getId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void deleteViewGraphByAdminByNotExistentIdError400() throws ElementNotFoundException {
    when(viewGraphService.deleteViewGraph(anyInt())).thenThrow(new ElementNotFoundException(""));

    ResponseEntity response = viewGraphController.deleteUserViewGraph(adminTokenWithBearer,
        viewGraph1.getId());

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}