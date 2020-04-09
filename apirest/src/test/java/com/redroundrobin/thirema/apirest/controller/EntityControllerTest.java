package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class EntityControllerTest {

  private EntityController entityController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

  @MockBean
  private EntityService entityService;

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
  List<Entity> allEntities;

  Set<Sensor> entity1And2Sensors;
  Set<Sensor> entity3Sensors;


  @Before
  public void setUp() {
    entityController = new EntityController(entityService, jwtUtil, logService, userService);


    // ----------------------------------------- Set Users --------------------------------------
    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

    List<User> allUsers = new ArrayList<>();
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
    sensor3 = new Sensor(3, "type3", 3);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    // -------------------------------- Set sensors to entities ----------------------------------
    entity1And2Sensors = new HashSet<>();
    entity1And2Sensors.add(sensor1);
    entity1And2Sensors.add(sensor2);
    entity1.setSensors(entity1And2Sensors);
    entity2.setSensors(entity1And2Sensors);

    entity3Sensors = new HashSet<>();
    entity3Sensors.add(sensor3);
    entity3.setSensors(entity3Sensors);


    // ------------------------------- Set entities to users -----------------------------------
    user.setEntity(entity1);



    // Core Controller needed mock
    user.setEntity(entity1);
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(entityService.findAll()).thenReturn(allEntities);
    when(entityService.findAllBySensorId(anyInt())).thenAnswer(i -> {
      Sensor sensor = allSensors.stream()
          .filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst().orElse(null);
      if (sensor != null) {
        return allEntities.stream()
            .filter(e -> e.getSensors().contains(sensor))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(entityService.findAllBySensorIdAndUserId(anyInt(), anyInt())).thenAnswer(i -> {
      Sensor sensor = allSensors.stream()
          .filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst().orElse(null);
      User user = allUsers.stream().filter(u -> i.getArgument(1).equals(u.getId())).findFirst().orElse(null);
      if (sensor != null && user != null) {
        return allEntities.stream()
            .filter(e -> e.getSensors().contains(sensor) && user.getEntity().equals(e))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(entityService.findAllByUserId(anyInt())).thenAnswer(i -> {
      User user = allUsers.stream()
          .filter(u -> i.getArgument(0).equals(u.getId()))
          .findFirst().orElse(null);
      if (user != null) {
        return allEntities.stream()
            .filter(e -> user.getEntity().equals(e))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(entityService.findById(anyInt())).thenAnswer(i -> {
      return allEntities.stream().filter(e -> i.getArgument(0).equals(e.getId())).findFirst().orElse(null);
    });
  }

  @Test
  public void getAllEntitiesBySensorIdAndUserIdByAdmin() {
    ResponseEntity<List<Entity>> response = entityController.getEntities(
        adminTokenWithBearer, sensor1.getId(), user.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(user.getEntity(), response.getBody().get(0));
  }

  @Test
  public void getAllEntitiesByAdmin() {
    ResponseEntity<List<Entity>> response = entityController.getEntities(adminTokenWithBearer, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allEntities, response.getBody());
  }

  @Test
  public void getAllEntitiesBySensorByAdmin() {
    ResponseEntity<List<Entity>> response = entityController.getEntities(
        adminTokenWithBearer, sensor1.getId(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(!response.getBody().isEmpty());
  }

  @Test
  public void getAllEntitiesByUserIdByAdmin() {
    ResponseEntity<List<Entity>> response = entityController.getEntities(adminTokenWithBearer, null, user.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(user.getEntity(), response.getBody().get(0));
  }

  @Test
  public void getAllEntitiesByUser() {
    ResponseEntity<List<Entity>> response = entityController.getEntities(userTokenWithBearer, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(user.getEntity(), response.getBody().get(0));
  }

  @Test
  public void getAllEntitiesByUserWithDifferentUserId() {
    ResponseEntity<List<Entity>> response = entityController.getEntities(userTokenWithBearer, null, admin.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(Collections.emptyList(), response.getBody());
  }

  @Test
  public void getAllEntitiesBySensorId() {
    ResponseEntity<List<Entity>> response = entityController.getEntities(userTokenWithBearer, sensor1.getId(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(user.getEntity(), response.getBody().stream().findFirst().orElse(null));
  }



  @Test
  public void getEntityByIdByAdminSuccessfull() {
    ResponseEntity<Entity> response = entityController.getEntity(adminTokenWithBearer, entity3.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(entity3, response.getBody());
  }

  @Test
  public void getEntityByIdByUserNotAllowerError403() {
    ResponseEntity<Entity> response = entityController.getEntity(userTokenWithBearer, entity3.getId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
