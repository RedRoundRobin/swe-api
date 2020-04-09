package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.service.postgres.AlertService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AlertControllerTest {

  private AlertController alertController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

  @MockBean
  private AlertService alertService;

  MockHttpServletRequest httpRequest;

  private String userTokenWithBearer = "Bearer userToken";
  private String modTokenWithBearer = "Bearer modToken";
  private String adminTokenWithBearer = "Bearer adminToken";
  private String userToken = "userToken";
  private String modToken = "modToken";
  private String adminToken = "adminToken";

  private User admin;
  private User mod;
  private User user;

  List<User> allUsers;

  private Alert alert1;
  private Alert alert2;
  private Alert alert3;
  private Alert alert4;

  List<Alert> allAlerts;

  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  List<Entity> allEntities;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;

  List<Sensor> allSensors;


  @Before
  public void setUp() throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException, NotAuthorizedException {
    alertController = new AlertController(alertService, jwtUtil, logService, userService);

    httpRequest = new MockHttpServletRequest();
    httpRequest.setRemoteAddr("localhost");


    // ----------------------------------------- Set Users --------------------------------------
    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    mod = new User(3, "mod", "mod", "mod", "pass", User.Role.MOD);
    user = new User(2, "user", "user", "user", "pass", User.Role.USER);

    allUsers = new ArrayList<>();
    allUsers.add(admin);
    allUsers.add(mod);
    allUsers.add(user);


    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity(1, "entity1", "loc1");
    entity2 = new Entity(2, "entity2", "loc2");
    entity3 = new Entity(3, "entity3", "loc3");

    allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);
    allEntities.add(entity3);


    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 1);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    // ----------------------------------------- Set Alerts --------------------------------------
    alert1 = new Alert(1, 10.0, Alert.Type.GREATER, entity1, sensor1);
    alert2 = new Alert(2, 10.0, Alert.Type.GREATER, entity1, sensor1);
    alert3 = new Alert(3, 10.0, Alert.Type.GREATER, entity2, sensor2);
    alert4 = new Alert(4, 10.0, Alert.Type.GREATER, entity3, sensor3);

    allAlerts = new ArrayList<>();
    allAlerts.add(alert1);
    allAlerts.add(alert2);
    allAlerts.add(alert3);
    allAlerts.add(alert4);



    // Core Controller needed mock
    user.setEntity(entity1);
    mod.setEntity(entity1);
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(modToken)).thenReturn(mod.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(mod.getEmail())).thenReturn(mod);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(alertService.findAll()).thenReturn(allAlerts);
    when(alertService.findAllByEntityId(anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getEntity().getId()))
          .collect(Collectors.toList());
    });
    when(alertService.findAllBySensorId(anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getSensor().getId()))
          .collect(Collectors.toList());
    });
    when(alertService.findAllByEntityIdAndSensorId(anyInt(),anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getEntity().getId())
          && i.getArgument(1).equals(a.getSensor().getId()))
          .collect(Collectors.toList());
    });
    when(alertService.createAlert(any(User.class), any(HashMap.class))).thenAnswer(i -> {
      Map<String, Object> fields = i.getArgument(1);
      if (fields.keySet().contains("sensor") && fields.get("sensor").equals(sensor1.getId())) {
        Alert alert = new Alert();
        alert.setEntity(entity1);
        alert.setThreshold(10.0);
        alert.setType(Alert.Type.GREATER);
        alert.setSensor(sensor1);
        return alert;
      } else {
        throw new MissingFieldsException("");
      }
    });
    doAnswer(i -> {
      if (allSensors.stream().anyMatch(s -> i.getArgument(0).equals(s.getId()))) {
        return true;
      } else {
        throw new ElementNotFoundException("");
      }
    }).when(alertService).deleteAlertsBySensorId(anyInt());
    when(alertService.deleteAlert(any(User.class), anyInt())).thenAnswer(i -> {
      User user = i.getArgument(0);
      if (user.getType() == User.Role.ADMIN && allAlerts.stream().anyMatch(a -> i.getArgument(1).equals(a.getId()))) {
        return true;
      } else if (user.getType() == User.Role.MOD && allAlerts.stream().anyMatch(a -> i.getArgument(1).equals(a.getId()))) {
        return false;
      } else if (user.getType() == User.Role.MOD) {
        throw new NotAuthorizedException("");
      } else {
        throw new ElementNotFoundException("");
      }
    });
    when(alertService.enableUserAlert(any(User.class), any(User.class), anyInt(), anyBoolean())).thenAnswer(i -> {
      User editingUser = i.getArgument(0);
      User userToEdit = i.getArgument(1);
      Alert alert = alertService.findById(i.getArgument(2));
      if (alert != null) {
        if (editingUser.getType() == User.Role.ADMIN || alert.getEntity().equals(userToEdit.getEntity())) {
          if (userToEdit.getType() != User.Role.MOD) {
            return true;
          } else {
            return false;
          }
        } else {
          throw NotAuthorizedException.notAuthorizedMessage("alert");
        }
      } else {
        throw ElementNotFoundException.notFoundMessage("alert");
      }
    });
    when(alertService.findByIdAndEntityId(anyInt(), anyInt())).thenAnswer(i -> {
      Entity entity = allEntities.stream()
          .filter(e -> i.getArgument(1).equals(e.getId()))
          .findFirst().orElse(null);
      Alert alert = allAlerts.stream()
          .filter(a -> i.getArgument(0).equals(a.getId()))
          .findFirst().orElse(null);
      if (entity != null && alert.getEntity().equals(entity)) {
        return alert;
      } else if (entity == null) {
        return null;
      } else {
        throw NotAuthorizedException.notAuthorizedMessage("alert");
      }
    });
    when(alertService.findById(anyInt())).thenAnswer(i -> {
      return allAlerts.stream()
          .filter(a -> i.getArgument(0).equals(a.getId()))
          .findFirst().orElse(null);
    });

    when(userService.findById(anyInt())).thenAnswer(i -> {
      return allUsers.stream()
          .filter(u -> i.getArgument(0).equals(u.getId()))
          .findFirst().orElse(null);
    });
  }

  @Test
  public void getAllAlertsByAdmin() {
    ResponseEntity<Map<String,List<Alert>>> response = alertController.getAlerts(adminTokenWithBearer, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allAlerts, response.getBody().get("enabled"));
  }

  @Test
  public void getAllAlertsByEntityIdByAdmin() {
    ResponseEntity<Map<String,List<Alert>>> response = alertController.getAlerts(adminTokenWithBearer, entity2.getId(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(!response.getBody().get("enabled").isEmpty());
  }

  @Test
  public void getAllAlertsBySensorIdByAdmin() {
    ResponseEntity<Map<String,List<Alert>>> response = alertController.getAlerts(adminTokenWithBearer, null, sensor3.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(!response.getBody().get("enabled").isEmpty());
  }

  @Test
  public void getAllAlertsByEntityIdAndSensorIdByAdmin() {
    ResponseEntity<Map<String,List<Alert>>> response = alertController.getAlerts(adminTokenWithBearer, entity3.getId(), sensor3.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(!response.getBody().get("enabled").isEmpty());
  }

  @Test
  public void getAllAlertsByUser() {
    ResponseEntity<Map<String,List<Alert>>> response = alertController.getAlerts(userTokenWithBearer, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(!response.getBody().get("enabled").isEmpty());
  }

  @Test
  public void getAllAlertsBySensorByUser() {
    ResponseEntity<Map<String,List<Alert>>> response = alertController.getAlerts(userTokenWithBearer, null, sensor1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().get("enabled").size() == 2);
  }

  @Test
  public void getAllAlertsByUserDifferentEntityEmptyResult() {
    ResponseEntity<Map<String,List<Alert>>> response = alertController.getAlerts(userTokenWithBearer, entity2.getId(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().get("enabled").isEmpty());
  }



  @Test
  public void getAlertsByAdminSuccessfull() {
    ResponseEntity<Alert> response = alertController.getAlert(adminTokenWithBearer, alert1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(alert1, response.getBody());
  }

  @Test
  public void getAlertsByModSuccessfull() {
    ResponseEntity<Alert> response = alertController.getAlert(modTokenWithBearer, alert1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(alert1, response.getBody());
  }

  @Test
  public void getAlertsByUserReturnErrorForbidden() {
    ResponseEntity<Alert> response = alertController.getAlert(userTokenWithBearer, alert3.getId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }



  @Test
  public void createAlertByAdminSuccessfull() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", Alert.Type.GREATER.toValue());
    newAlertFields.put("sensor", sensor1.getId());
    newAlertFields.put("entity", entity1.getId());

    Alert alert = new Alert();
    alert.setThreshold(10.0);
    alert.setType(Alert.Type.GREATER);
    alert.setSensor(sensor1);
    alert.setEntity(entity1);

    ResponseEntity<Alert> response = alertController.createAlert(
        adminTokenWithBearer, newAlertFields,httpRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(alert.getThreshold(), response.getBody().getThreshold());
    assertEquals(alert.getType(), response.getBody().getType());
    assertEquals(alert.getEntity(), response.getBody().getEntity());
    assertEquals(alert.getSensor(), response.getBody().getSensor());
  }

  @Test
  public void createAlertByAdminMissingNecessaryFields() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", Alert.Type.GREATER.toValue());
    newAlertFields.put("entity", entity1.getId());

    ResponseEntity<Alert> response = alertController.createAlert(
        adminTokenWithBearer, newAlertFields,httpRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void createAlertByUserNotAllowedError403Forbidden() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", Alert.Type.GREATER.toValue());
    newAlertFields.put("entity", entity1.getId());
    newAlertFields.put("sensor", sensor1.getId());

    ResponseEntity<Alert> response = alertController.createAlert(
        userTokenWithBearer, newAlertFields,httpRequest);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }



  @Test
  public void editAlertByAdminSuccessfull() throws ElementNotFoundException, NotAuthorizedException, MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("threshold", 5.0);

    Alert alert = new Alert();
    alert.setThreshold(5.0);
    alert.setType(alert1.getType());
    alert.setSensor(alert1.getSensor());
    alert.setEntity(alert1.getEntity());

    when(alertService.editAlert(admin, fieldsToEdit, alert1.getId())).thenReturn(alert);

    ResponseEntity<Alert> response = alertController.editAlert(
        adminTokenWithBearer, fieldsToEdit, alert1.getId(), httpRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(alert.getThreshold(), response.getBody().getThreshold());
    assertEquals(alert.getType(), response.getBody().getType());
    assertEquals(alert.getEntity(), response.getBody().getEntity());
    assertEquals(alert.getSensor(), response.getBody().getSensor());
  }

  @Test
  public void editAlertByAdminMissingNecessaryFields() throws ElementNotFoundException, NotAuthorizedException, MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> fieldsToEdit = new HashMap<>();

    when(alertService.editAlert(admin, fieldsToEdit, alert1.getId())).thenThrow(new ElementNotFoundException(""));

    ResponseEntity<Alert> response = alertController.editAlert(
        adminTokenWithBearer, fieldsToEdit, alert1.getId(), httpRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void editAlertByModDifferentEntityNotAllowedError403Forbidden() throws ElementNotFoundException, NotAuthorizedException, MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("threshold", 10.0);

    when(alertService.editAlert(mod, fieldsToEdit, alert3.getId())).thenThrow(new NotAuthorizedException(""));

    ResponseEntity<Alert> response = alertController.editAlert(
        modTokenWithBearer, fieldsToEdit, alert3.getId(), httpRequest);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }



  @Test
  public void deleteAlertsByAdminBySensorIdSuccessfull() {
    ResponseEntity response = alertController.deleteAlerts(adminTokenWithBearer, sensor1.getId(),httpRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void deleteAlertsByAdminByNotExistentSensorIdReturnErrorBadRequest() {
    ResponseEntity response = alertController.deleteAlerts(adminTokenWithBearer, 10,httpRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void deleteAlertsByUserBySensorIdReturnErrorForbidden() {
    ResponseEntity response = alertController.deleteAlerts(userTokenWithBearer, sensor1.getId(),httpRequest);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }



  @Test
  public void deleteAlertSuccessfull() {
    ResponseEntity response = alertController.deleteAlert(adminTokenWithBearer, alert1.getId(),httpRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void deleteAlertSimulateDBError() {
    ResponseEntity response = alertController.deleteAlert(modTokenWithBearer, alert1.getId(),httpRequest);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void deleteAlertByModByDifferentEntityAlertReturnErrorForbidden() {
    ResponseEntity response = alertController.deleteAlert(modTokenWithBearer, 10,httpRequest);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void deleteAlertByUserByNotExistentAlertIdReturnErrorBadRequest() {
    ResponseEntity response = alertController.deleteAlert(adminTokenWithBearer, 10,httpRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }



  @Test
  public void disableUserAlertByUserSuccessfull() {
    ResponseEntity response = alertController.disableUserAlert(userTokenWithBearer, alert2.getId(), user.getId(), false);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void disableUserAlertByModSimulateDBError() {
    ResponseEntity response = alertController.disableUserAlert(modTokenWithBearer, alert2.getId(), mod.getId(), false);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void disableUserAlertByUserDifferentEntityAlertReturnForbidden() {
    ResponseEntity response = alertController.disableUserAlert(userTokenWithBearer, alert3.getId(), user.getId(), false);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void disableUserAlertByUserWithNotExistentAlertIdReturnBadRequest() {
    ResponseEntity response = alertController.disableUserAlert(userTokenWithBearer, 10, user.getId(), false);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
