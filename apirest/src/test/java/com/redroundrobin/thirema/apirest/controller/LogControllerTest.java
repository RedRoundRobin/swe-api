package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Log;
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
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class LogControllerTest {

  private LogController logController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;


  private final String userTokenWithBearer = "Bearer userToken";
  private final String modTokenWithBearer = "Bearer modToken";
  private final String adminTokenWithBearer = "Bearer adminToken";
  private final String userToken = "userToken";
  private final String modToken = "modToken";
  private final String adminToken = "adminToken";

  private User admin;
  private User mod;
  private User user;

  private Entity entity1;

  private Log log1;
  private Log log2;
  private Log log3;
  private Log log4;

  List<Log> allLogs;

  @Before
  public void setUp() {
    logController = new LogController(jwtUtil, logService, userService);

    // ----------------------------------------- Set Users --------------------------------------
    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    mod = new User(3, "mod", "mod", "mod", "pass", User.Role.MOD);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

    // ------------------------------------------ Set Logs ----------------------------------------
    log1 = new Log(user.getId(), "localhost", "user.edit_settings", "");
    log2 = new Log(user.getId(), "localhost", "auth.login", "webapp");
    log3 = new Log(mod.getId(), "localhost", "auth.login", "telegram");
    log4 = new Log(admin.getId(), "localhost", "user.reset_password",
        Integer.toString(user.getId()));

    allLogs = new ArrayList<>();
    allLogs.add(log1);
    allLogs.add(log2);
    allLogs.add(log3);
    allLogs.add(log4);


    // --------------------------------------- Set Entity ----------------------------------------
    entity1 = new Entity(1, "name", "location");



    // ---------------------------------- Set Entity to Users -------------------------------------
    user.setEntity(entity1);
    mod.setEntity(entity1);

    List<Integer> entity1Users = new ArrayList<>();
    entity1Users.add(user.getId());
    entity1Users.add(mod.getId());


    // Core Controller needed mock
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(modToken)).thenReturn(mod.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(mod.getEmail())).thenReturn(mod);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(logService.findAll()).thenReturn(allLogs);
    when(logService.findAllByEntityId(entity1.getId())).thenAnswer(i -> {
      return allLogs.stream()
          .filter(l -> entity1Users.contains(l.getUserId()))
          .collect(Collectors.toList());
    });
    when(logService.findTopN(anyInt())).thenAnswer(i -> {
      return allLogs.stream()
          .limit(Integer.toUnsignedLong(i.getArgument(0)))
          .collect(Collectors.toList());
    });
    when(logService.findTopNByEntityId(anyInt(),eq(entity1.getId()))).thenAnswer(i -> {
      return allLogs.stream()
          .filter(l -> entity1Users.contains(l.getUserId()))
          .limit(Integer.toUnsignedLong(i.getArgument(0)))
          .collect(Collectors.toList());
    });
  }


  @Test
  public void getAllLogsByAdminSuccessfull() {
    ResponseEntity<List<Log>> response = logController.getLogs(adminTokenWithBearer, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allLogs, response.getBody());
  }

  @Test
  public void getAllLogsByUserError403() {
    ResponseEntity<List<Log>> response = logController.getLogs(userTokenWithBearer, null, null);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void getEntity1LogsByAdminSuccessfull() {
    ResponseEntity<List<Log>> response = logController.getLogs(adminTokenWithBearer,
        entity1.getId(), null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(3, response.getBody().size());
  }

  @Test
  public void getTop2LogsByAdminSuccessfull() {
    ResponseEntity<List<Log>> response = logController.getLogs(adminTokenWithBearer, null, 2);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
  }

  @Test
  public void getTop1Entity1LogsByAdminSuccessfull() {
    ResponseEntity<List<Log>> response = logController.getLogs(adminTokenWithBearer,
        entity1.getId(), 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void getTop1Entity1LogsByModSuccessfull() {
    ResponseEntity<List<Log>> response = logController.getLogs(modTokenWithBearer,
        entity1.getId(), 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void getTop1LogsByModSuccessfull() {
    ResponseEntity<List<Log>> response = logController.getLogs(modTokenWithBearer,
        null, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void getAllLogsByModSuccessfull() {
    ResponseEntity<List<Log>> response = logController.getLogs(modTokenWithBearer,
        null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(3, response.getBody().size());
  }

  @Test
  public void getEntity2LogsByModEmptyList() {
    ResponseEntity<List<Log>> response = logController.getLogs(modTokenWithBearer,
        2, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }
}
