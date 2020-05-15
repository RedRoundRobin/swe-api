package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Log;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class StatsControllerTest {

  private StatsController statsController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

  @MockBean
  private DeviceService deviceService;

  @MockBean
  private EntityService entityService;

  private final String userTokenWithBearer = "Bearer userToken";
  private final String adminTokenWithBearer = "Bearer adminToken";
  private final String userToken = "userToken";
  private final String adminToken = "adminToken";

  private User admin;
  private User user;

  List<User> allUsers;

  private Entity entity1;

  private Log log1;
  private Log log2;

  List<Log> allLogs;

  @Before
  public void setUp() {
    statsController = new StatsController(jwtUtil, logService, userService, deviceService,
        entityService);

    // ----------------------------------------- Set Users --------------------------------------
    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

    allUsers = new ArrayList<>();
    allUsers.add(admin);
    allUsers.add(user);

    // ----------------------------------------- Set Logs --------------------------------------
    log1 = new Log(admin.getId(), "localhost", "user.edit_settings", "");
    log2 = new Log(user.getId(), "localhost", "auth.login", "webapp");

    allLogs = new ArrayList<>();
    allLogs.add(log1);
    allLogs.add(log2);



    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity(1, "entity1", "loc1");

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);

    // ------------------------------------ Set Entities to Users --------------------------------
    user.setEntity(entity1);


    // Core Controller needed mock
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(logService.findAllLoginsByTimeAfter(any(Timestamp.class))).thenReturn(allLogs);

    when(userService.findAll()).thenReturn(allUsers);
    when(userService.findAllByEntityId(anyInt())).thenReturn(allUsers.stream()
        .filter(u -> u.getEntity() != null).collect(Collectors.toList()));

    when(deviceService.findAll()).thenReturn(Collections.emptyList());
    when(deviceService.findAllByEntityId(anyInt())).thenReturn(Collections.emptyList());

    when(entityService.findAll()).thenReturn(Collections.emptyList());
  }

  @Test
  public void getStatsByAdmin() {
    ResponseEntity<Map<String, Integer>> response = statsController.getStats(adminTokenWithBearer);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("activeMembers"));
    assertTrue(response.getBody().containsKey("registeredUsers"));
    assertTrue(response.getBody().containsKey("registeredDevices"));
    assertTrue(response.getBody().containsKey("entitiesNumber"));
  }

  @Test
  public void getStatsByUser() {
    ResponseEntity<Map<String, Integer>> response = statsController.getStats(userTokenWithBearer);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("activeMembers"));
    assertTrue(response.getBody().containsKey("registeredUsers"));
    assertTrue(response.getBody().containsKey("registeredDevices"));
    assertTrue(response.getBody().containsKey("entitiesNumber"));
    assertTrue(response.getBody().containsKey("entityActiveMembers"));
    assertTrue(response.getBody().containsKey("entityRegisteredUsers"));
    assertTrue(response.getBody().containsKey("entityRegisteredDevices"));
  }
}
