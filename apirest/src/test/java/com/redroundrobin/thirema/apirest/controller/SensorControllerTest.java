package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SensorControllerTest {

  private SensorController sensorController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

  @MockBean
  private SensorService sensorService;

  private String userTokenWithBearer = "Bearer userToken";
  private String adminTokenWithBearer = "Bearer adminToken";
  private String userToken = "userToken";
  private String adminToken = "adminToken";

  private User admin;
  private User user;

  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;

  List<Sensor> allSensors;

  List<Sensor> entity1And2Sensors;
  List<Sensor> entity3Sensors;


  @Before
  public void setUp() {
    sensorController = new SensorController(sensorService, jwtUtil, logService, userService);


    admin = new User();
    admin.setId(1);
    admin.setEmail("admin");
    admin.setType(User.Role.ADMIN);

    user = new User();
    user.setId(2);
    user.setEmail("user");
    user.setType(User.Role.USER);


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

    List<Entity> allEntities = new ArrayList<>();
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


    // -------------------------- Set sensors to entities and viceversa --------------------------
    List<Entity> sensor1And2Entities = new ArrayList<>();
    sensor1And2Entities.add(entity1);
    sensor1And2Entities.add(entity2);
    sensor1.setEntities(sensor1And2Entities);
    sensor2.setEntities(sensor1And2Entities);
    entity1And2Sensors = new ArrayList<>();
    entity1And2Sensors.add(sensor1);
    entity1And2Sensors.add(sensor2);
    entity1.setSensors(entity1And2Sensors);

    List<Entity> sensor3Entities = new ArrayList<>();
    sensor3Entities.add(entity3);
    sensor3.setEntities(sensor3Entities);
    entity3Sensors = new ArrayList<>();
    entity3Sensors.add(sensor3);
    entity3.setSensors(entity3Sensors);



    // Core Controller needed mock
    user.setEntity(entity1);
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(sensorService.findAll()).thenReturn(allSensors);
    when(sensorService.findAllByEntityId(anyInt())).thenAnswer(i -> {
      Entity entity = allEntities.stream()
          .filter(e -> i.getArgument(0).equals(e.getId()))
          .findFirst().orElse(null);
      if (entity != null) {
        return allSensors.stream()
          .filter(s -> s.getEntities().contains(entity)).collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
  }

  @Test
  public void getAllSensorsByAdmin() {
    ResponseEntity<List<Sensor>> response = sensorController.getSensors(adminTokenWithBearer, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allSensors, response.getBody());
  }

  @Test
  public void getAllSensorsByEntityIdByAdmin() {
    ResponseEntity<List<Sensor>> response = sensorController.getSensors(adminTokenWithBearer,
        entity1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(entity1And2Sensors, response.getBody());
  }

  @Test
  public void getAllSensorsByUser() {
    ResponseEntity<List<Sensor>> response = sensorController.getSensors(userTokenWithBearer, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(entity1And2Sensors, response.getBody());
  }

  @Test
  public void getAllSensorsByEntityIdByUserEmptyList() {
    ResponseEntity<List<Sensor>> response = sensorController.getSensors(userTokenWithBearer,
        entity2.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Collections.emptyList(), response.getBody());
  }
}
