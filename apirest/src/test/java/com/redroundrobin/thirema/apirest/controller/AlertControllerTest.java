package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.AlertService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AlertControllerTest {

  private AlertController alertController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private UserService userService;

  @MockBean
  private AlertService alertService;

  private String userTokenWithBearer = "Bearer userToken";
  private String adminTokenWithBearer = "Bearer adminToken";
  private String userToken = "userToken";
  private String adminToken = "adminToken";

  private User admin;
  private User user;

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

  List<Alert> entity1Alerts;
  List<Alert> entity2Alerts;
  List<Alert> entity3Alerts;

  List<Sensor> entity1And2Sensors;
  List<Sensor> entity3Sensors;

  List<Alert> sensor1Alerts;
  List<Alert> sensor2Alerts;
  List<Alert> sensor3Alerts;


  @Before
  public void setUp() {
    alertController = new AlertController(alertService);
    alertController.setJwtUtil(jwtUtil);
    alertController.setUserService(userService);


    admin = new User();
    admin.setId(1);
    admin.setEmail("admin");
    admin.setType(User.Role.ADMIN);

    user = new User();
    user.setId(2);
    user.setEmail("user");
    user.setType(User.Role.USER);


    // ----------------------------------------- Set Alerts --------------------------------------
    alert1 = new Alert();
    alert1.setAlertId(1);

    alert2 = new Alert();
    alert2.setAlertId(2);

    alert3 = new Alert();
    alert3.setAlertId(3);

    alert4 = new Alert();
    alert4.setAlertId(4);

    allAlerts = new ArrayList<>();
    allAlerts.add(alert1);
    allAlerts.add(alert2);
    allAlerts.add(alert3);
    allAlerts.add(alert4);


    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity();
    entity1.setId(1);
    entity1.setName("entity1");

    entity2 = new Entity();
    entity2.setId(2);
    entity2.setName("entity2");

    entity3 = new Entity();
    entity3.setId(3);
    entity3.setName("entity3");

    allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);
    allEntities.add(entity3);


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


    // -------------------------- Set alerts to entities and viceversa --------------------------
    entity1Alerts = new ArrayList<>();
    entity1Alerts.add(alert1);
    entity1Alerts.add(alert2);
    entity1.setAlerts(entity1Alerts);
    alert1.setEntity(entity1);
    alert2.setEntity(entity1);

    entity2Alerts = new ArrayList<>();
    entity2Alerts.add(alert3);
    entity2.setAlerts(entity2Alerts);
    alert3.setEntity(entity2);

    entity3Alerts = new ArrayList<>();
    entity3Alerts.add(alert4);
    entity3.setAlerts(entity3Alerts);
    alert4.setEntity(entity3);


    // -------------------------- Set alerts to sensors and viceversa --------------------------
    sensor1Alerts = new ArrayList<>();
    sensor1Alerts.add(alert1);
    sensor1Alerts.add(alert2);
    sensor1.setAlerts(sensor1Alerts);
    alert1.setSensor(sensor1);
    alert2.setSensor(sensor1);

    sensor2Alerts = new ArrayList<>();
    sensor2Alerts.add(alert3);
    sensor2.setAlerts(sensor2Alerts);
    alert3.setSensor(sensor2);

    sensor3Alerts = new ArrayList<>();
    sensor3Alerts.add(alert4);
    sensor3.setAlerts(sensor3Alerts);
    alert4.setSensor(sensor3);



    // Core Controller needed mock
    user.setEntity(entity1);
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
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
  }

  @Test
  public void getAllAlertsByAdmin() {
    ResponseEntity<List<Alert>> response = alertController.getAlerts(adminTokenWithBearer, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allAlerts, response.getBody());
  }

  @Test
  public void getAllAlertsByEntityIdByAdmin() {
    ResponseEntity<List<Alert>> response = alertController.getAlerts(adminTokenWithBearer, entity2.getId(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(entity2Alerts, response.getBody());
  }

  @Test
  public void getAllAlertsBySensorIdByAdmin() {
    ResponseEntity<List<Alert>> response = alertController.getAlerts(adminTokenWithBearer, null, sensor3.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(sensor3Alerts, response.getBody());
  }

  @Test
  public void getAllAlertsByEntityIdAndSensorIdByAdmin() {
    ResponseEntity<List<Alert>> response = alertController.getAlerts(adminTokenWithBearer, entity3.getId(), sensor3.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(entity3Alerts, response.getBody());
  }

  @Test
  public void getAllAlertsByUser() {
    ResponseEntity<List<Alert>> response = alertController.getAlerts(userTokenWithBearer, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(entity1Alerts, response.getBody());
  }

  @Test
  public void getAllAlertsBySensorByUser() {
    ResponseEntity<List<Alert>> response = alertController.getAlerts(userTokenWithBearer, null, sensor1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().size() == 2);
  }

  @Test
  public void getAllAlertsByUserDifferentEntityEmptyResult() {
    ResponseEntity<List<Alert>> response = alertController.getAlerts(userTokenWithBearer, entity2.getId(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }
}
