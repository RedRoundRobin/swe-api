package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.service.postgres.ViewGraphService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ViewGraphControllerTest {

  @MockBean
  JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

  @MockBean
  ViewGraphService viewGraphService;

  private ViewGraphController viewGraphController;

  private final String userTokenWithBearer = "Bearer userToken";
  private final String adminTokenWithBearer = "Bearer adminToken";
  private final String userToken = "userToken";
  private final String adminToken = "adminToken";

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
  public void setUp() throws ElementNotFoundException, MissingFieldsException, InvalidFieldsValuesException {
    viewGraphController = new ViewGraphController(viewGraphService, jwtUtil, logService, userService);

    // ----------------------------------------- Set Users --------------------------------------
    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

    allUsers = new ArrayList<>();
    allUsers.add(user);
    allUsers.add(admin);

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

    // --------------------------------------- Set Views -------------------------------------
    view1 = new View(1, "view1", admin);
    view2 = new View(2, "view2", user);

    allView = new ArrayList<>();
    allView.add(view1);
    allView.add(view2);

    // --------------------------------------- Set ViewGraphs -------------------------------------
    viewGraph1 = new ViewGraph(1, ViewGraph.Correlation.NULL);
    viewGraph2 = new ViewGraph(2, ViewGraph.Correlation.NULL);
    viewGraph3 = new ViewGraph(3, ViewGraph.Correlation.NULL);

    allViewGraphs = new ArrayList<>();
    allViewGraphs.add(viewGraph1);
    allViewGraphs.add(viewGraph2);
    allViewGraphs.add(viewGraph3);

    // --------------------------------- Set sensors to viewGraphs ------------------------------
    viewGraph1.setSensor1(sensor1);
    viewGraph1.setSensor2(sensor2);

    viewGraph2.setSensor1(sensor3);
    viewGraph2.setSensor2(sensor1);

    // ----------------------------------- Set view to viewGraphs -------------------------------
    viewGraph1.setView(view1);
    viewGraph3.setView(view1);

    viewGraph2.setView(view2);

    // ----------------------------------- Set users to view ------------------------------------
    view1.setUser(admin);
    view2.setUser(user);

    // ---------------------------------- Set entities to users ---------------------------------
    user.setEntity(entity1);

    // ---------------------------------- Set sensors to entities -------------------------------
    Set<Sensor> entity1Sensors = new HashSet<>();
    entity1Sensors.add(sensor1);
    entity1Sensors.add(sensor3);
    entity1.setSensors(entity1Sensors);

    Set<Sensor> entity2Sensors = new HashSet<>();
    entity2Sensors.add(sensor2);
    entity2.setSensors(entity2Sensors);

    // Core Controller needed mock
    user.setEntity(entity1);
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(viewGraphService.findAll()).thenReturn(allViewGraphs);
    when(viewGraphService.findAllByUserId(anyInt())).thenAnswer(i -> allViewGraphs.stream()
        .filter(vg -> i.getArgument(0).equals(vg.getView().getUser().getId())).collect(Collectors.toList()));
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
    when(viewGraphService.findAllByViewId(anyInt())).thenAnswer(i -> allViewGraphs.stream()
        .filter(vg -> i.getArgument(0).equals(vg.getView().getId())).collect(Collectors.toList()));
    when(viewGraphService.findAllBySensorId(anyInt())).thenAnswer(i -> allViewGraphs.stream()
        .filter(vg -> i.getArgument(0).equals(vg.getSensor1().getId())
            || i.getArgument(0).equals(vg.getSensor2().getId())).collect(Collectors.toList()));
    when(viewGraphService.findById(anyInt())).thenAnswer(i -> allViewGraphs.stream()
        .filter(vg -> i.getArgument(0).equals(vg.getId())).findFirst().orElse(null));
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
      if (fields.containsKey("sensor1") && fields.get("sensor1").equals(sensor1.getId())) {
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
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getAllViewGraphsByAdminByUserIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(adminTokenWithBearer,
        admin.getId(), null);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getAllViewGraphsByAdminByViewIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(adminTokenWithBearer,
        null, view1.getId());

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getAllViewGraphsByUserByUserIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(userTokenWithBearer,
        user.getId(), null);

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getAllViewGraphsByUserByUserIdAndViewIdSuccessfull() {

    ResponseEntity<List<ViewGraph>> response = viewGraphController.getViewGraphs(userTokenWithBearer,
        user.getId(), view2.getId());

    // Check status and if are present tfa and token
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
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
  public void editViewGraphByUserViewGraphNotAuthorizedError403() {
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
