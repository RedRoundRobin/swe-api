package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.SensorService;
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
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DataControllerTest {

  private DataController dataController;

  @MockBean
  private JwtUtil jwtUtil;

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

  Sensor sensor1111;
  Sensor sensor1112;
  Sensor sensor1113;
  Sensor sensor1121;
  Sensor sensor1122;
  Sensor sensor1123;

  Sensor sensor1211;
  Sensor sensor1212;
  Sensor sensor1213;
  Sensor sensor1221;
  Sensor sensor1222;
  Sensor sensor1223;

  List<Sensor> allSensors;
  Map<Integer, List<Sensor>> allSensorsMap;

  List<Sensor> id1Sensors;
  List<Sensor> id2Sensors;
  List<Sensor> id3Sensors;
  List<Sensor> id4Sensors;


  @Before
  public void setUp() {
    dataController = new DataController(sensorService);
    dataController.setJwtUtil(jwtUtil);
    dataController.setUserService(userService);

    admin = new User();
    admin.setId(1);
    admin.setEmail("admin");
    admin.setType(User.Role.ADMIN);

    user = new User();
    user.setId(2);
    user.setEmail("user");
    user.setType(User.Role.USER);

    entity1 = new Entity();
    entity1.setId(1);
    entity1.setName("entity1");

    List<User> allUsers = new ArrayList<>();
    allUsers.add(user);



    // ------------------------------------ Set Timescale Sensors ---------------------------------
    sensor1111 = new Sensor();
    sensor1111.setTime(new Timestamp(100));
    sensor1111.setGatewayName("gateway1");
    sensor1111.setRealDeviceId(1);
    sensor1111.setRealSensorId(1);
    sensor1111.setValue(1);
    sensor1112 = new Sensor();
    sensor1112.setTime(new Timestamp(200));
    sensor1112.setGatewayName("gateway1");
    sensor1112.setRealDeviceId(1);
    sensor1112.setRealSensorId(1);
    sensor1112.setValue(2);
    sensor1113 = new Sensor();
    sensor1113.setTime(new Timestamp(300));
    sensor1113.setGatewayName("gateway1");
    sensor1113.setRealDeviceId(1);
    sensor1113.setRealSensorId(1);
    sensor1113.setValue(3);

    sensor1121 = new Sensor();
    sensor1121.setTime(new Timestamp(100));
    sensor1121.setGatewayName("gateway1");
    sensor1121.setRealDeviceId(1);
    sensor1121.setRealSensorId(2);
    sensor1121.setValue(1);
    sensor1122 = new Sensor();
    sensor1122.setTime(new Timestamp(200));
    sensor1122.setGatewayName("gateway1");
    sensor1122.setRealDeviceId(1);
    sensor1122.setRealSensorId(2);
    sensor1122.setValue(2);
    sensor1123 = new Sensor();
    sensor1123.setTime(new Timestamp(300));
    sensor1123.setGatewayName("gateway1");
    sensor1123.setRealDeviceId(1);
    sensor1123.setRealSensorId(2);
    sensor1123.setValue(3);

    sensor1211 = new Sensor();
    sensor1211.setTime(new Timestamp(100));
    sensor1211.setGatewayName("gateway1");
    sensor1211.setRealDeviceId(2);
    sensor1211.setRealSensorId(1);
    sensor1211.setValue(1);
    sensor1212 = new Sensor();
    sensor1212.setTime(new Timestamp(200));
    sensor1212.setGatewayName("gateway1");
    sensor1212.setRealDeviceId(2);
    sensor1212.setRealSensorId(1);
    sensor1212.setValue(2);
    sensor1213 = new Sensor();
    sensor1213.setTime(new Timestamp(300));
    sensor1213.setGatewayName("gateway1");
    sensor1213.setRealDeviceId(2);
    sensor1213.setRealSensorId(1);
    sensor1213.setValue(3);

    sensor1221 = new Sensor();
    sensor1221.setTime(new Timestamp(100));
    sensor1221.setGatewayName("gateway1");
    sensor1221.setRealDeviceId(2);
    sensor1221.setRealSensorId(2);
    sensor1221.setValue(1);
    sensor1222 = new Sensor();
    sensor1222.setTime(new Timestamp(200));
    sensor1222.setGatewayName("gateway1");
    sensor1222.setRealDeviceId(2);
    sensor1222.setRealSensorId(2);
    sensor1222.setValue(2);
    sensor1223 = new Sensor();
    sensor1223.setTime(new Timestamp(300));
    sensor1223.setGatewayName("gateway1");
    sensor1223.setRealDeviceId(2);
    sensor1223.setRealSensorId(2);
    sensor1223.setValue(3);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1111);
    allSensors.add(sensor1112);
    allSensors.add(sensor1113);
    allSensors.add(sensor1121);
    allSensors.add(sensor1122);
    allSensors.add(sensor1123);
    allSensors.add(sensor1211);
    allSensors.add(sensor1212);
    allSensors.add(sensor1213);
    allSensors.add(sensor1221);
    allSensors.add(sensor1222);
    allSensors.add(sensor1223);

    id1Sensors = new ArrayList<>();
    id1Sensors.add(sensor1111);
    id1Sensors.add(sensor1112);
    id1Sensors.add(sensor1113);
    id2Sensors = new ArrayList<>();
    id2Sensors.add(sensor1121);
    id2Sensors.add(sensor1122);
    id2Sensors.add(sensor1123);
    id3Sensors = new ArrayList<>();
    id3Sensors.add(sensor1211);
    id3Sensors.add(sensor1212);
    id3Sensors.add(sensor1213);
    id4Sensors = new ArrayList<>();
    id4Sensors.add(sensor1221);
    id4Sensors.add(sensor1222);
    id4Sensors.add(sensor1223);


    allSensorsMap = new HashMap<>();
    allSensorsMap.put(1, id1Sensors);
    allSensorsMap.put(2, id2Sensors);
    allSensorsMap.put(3, id3Sensors);
    allSensorsMap.put(4, id4Sensors);


    // Core Controller needed mock
    user.setEntity(entity1);
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(sensorService.findAllForEachSensor()).thenReturn(allSensorsMap);
    when(sensorService.findAllForEachSensorByEntityId(anyInt())).thenAnswer(i -> {
      int entityId = i.getArgument(0);
      Map<Integer, List<Sensor>> responseMap = allSensorsMap;
      if (entityId == 1) {
        responseMap.remove(2);
        responseMap.remove(4);
      } else if (entityId == 2) {
        responseMap.remove(1);
        responseMap.remove(3);
      } else {
        responseMap.clear();
      }
      return responseMap;
    });
    when(sensorService.findTopNForEachSensor(anyInt())).thenAnswer(i -> {
      Long limit = i.getArgument(0, Integer.class).longValue();
      Map<Integer, List<Sensor>> responseMap = new HashMap<>();
      for (Map.Entry<Integer, List<Sensor>> entry : allSensorsMap.entrySet()) {
        responseMap.put(entry.getKey(), entry.getValue().stream().limit(limit)
            .collect(Collectors.toList()));
      }
      return responseMap;
    });
    when(sensorService.findTopNForEachSensorByEntityId(anyInt(), anyInt())).thenAnswer(i -> {
      Long limit = i.getArgument(0, Integer.class).longValue();
      int entityId = i.getArgument(1);
      Map<Integer, List<Sensor>> responseMap = new HashMap<>();
      for (Map.Entry<Integer, List<Sensor>> entry : allSensorsMap.entrySet()) {
        if ((entityId == 1 && (entry.getKey() == 1 || entry.getKey() == 3))
            || (entityId == 2 && (entry.getKey() == 2 || entry.getKey() == 4)))
        responseMap.put(entry.getKey(), entry.getValue().stream().limit(limit)
            .collect(Collectors.toList()));
      }
      return responseMap;
    });
    when(sensorService.findAllBySensorIdList(any(List.class))).thenAnswer(i -> {
      List<Integer> sensorIdsList = i.getArgument(0);
      Map<Integer, List<Sensor>> responseMap = new HashMap<>();
      for (int id : sensorIdsList) {
        responseMap.put(id, allSensorsMap.get(id));
      }
      return responseMap;
    });
    when(sensorService.findAllBySensorIdListAndEntityId(any(List.class), anyInt()))
        .thenAnswer(i -> {
          List<Integer> sensorIdsList = i.getArgument(0);
          int entityId = i.getArgument(1);
          Map<Integer, List<Sensor>> responseMap = new HashMap<>();
          for (int id : sensorIdsList) {
            if ((entityId == 1 && (id == 1 || id == 3))
                || (entityId == 2 && (id == 2 || id == 4))) {
              responseMap.put(id, allSensorsMap.get(id));
            } else {
              responseMap.put(id, Collections.emptyList());
            }
          }
          return responseMap;
        });
    when(sensorService.findTopNBySensorIdList(anyInt(), any(List.class))).thenAnswer(i -> {
      Long limit = i.getArgument(0, Integer.class).longValue();
      List<Integer> sensorIdsList = i.getArgument(1);
      Map<Integer, List<Sensor>> responseMap = new HashMap<>();
      for (int id : sensorIdsList) {
        responseMap.put(id, allSensorsMap.get(id).stream().limit(limit).collect(Collectors.toList()));
      }
      return responseMap;
    });
    when(sensorService.findTopNBySensorIdListAndEntityId(anyInt(), any(List.class), anyInt()))
        .thenAnswer(i -> {
          Long limit = i.getArgument(0, Integer.class).longValue();
          List<Integer> sensorIdsList = i.getArgument(1);
          int entityId = i.getArgument(2);
          Map<Integer, List<Sensor>> responseMap = new HashMap<>();
          for (int id : sensorIdsList) {
            if ((entityId == 1 && (id == 1 || id == 3))
                || (entityId == 2 && (id == 2 || id == 4))) {
              responseMap.put(id, allSensorsMap.get(id).stream().limit(limit).collect(Collectors.toList()));
            } else {
              responseMap.put(id, Collections.emptyList());
            }
          }
          return responseMap;
        });
    when(sensorService.findLastValueBySensorId(anyInt())).thenAnswer(i -> {
      return allSensorsMap.get(i.getArgument(0)).stream()
          .sorted((v1, v2) -> Long.compare(v2.getTime().getTime(), v1.getTime().getTime()))
          .findFirst().orElse(null);
    });
    when(sensorService.findLastValueBySensorIdAndEntityId(anyInt(), anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      int entityId = i.getArgument(1);
      if ((entityId == 1 && (id == 1 || id == 3))
          || (entityId == 2 && (id == 2 || id == 4))) {
        return allSensorsMap.get(id).stream()
            .sorted((v1, v2) -> Long.compare(v2.getTime().getTime(), v1.getTime().getTime()))
            .findFirst().orElse(null);
      } else {
        return null;
      }
    });
  }

  @Test
  public void getAllTimescaleAlertsByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, null, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 4);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }

  @Test
  public void getAllTimescaleAlertsWithLimitByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, null, 1, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 4);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 1));
  }

  @Test
  public void getAllTimescaleAlertsWithEntityIdByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, null, null, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 2);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }

  @Test
  public void getAllTimescaleAlertsWithSensorIdListByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, new Integer[]{1,3}, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 2);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }

  @Test
  public void getAllTimescaleAlertsWithLimitAndEntityIdByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, null, 2, 2);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 2);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 2));
  }

  @Test
  public void getAllTimescaleAlertsWithLimitAndSensorIdListByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, new Integer[]{1,2}, 5, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 2);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }

  @Test
  public void getAllTimescaleAlertsWithSensorIdListAndEntityIdByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, new Integer[]{1,2}, null, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 2);
    assertTrue(response.getBody().entrySet().stream().anyMatch(e -> e.getValue().size() == 3));
    assertTrue(response.getBody().entrySet().stream().anyMatch(e -> e.getValue().size() == 0));
  }

  @Test
  public void getAllTimescaleAlertsWithLimitAndSensorIdListAndEntityIdByAdmin() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        adminTokenWithBearer, new Integer[]{1}, 1, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 1);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 1));
  }

  @Test
  public void getAllTimescaleAlertsWithEntityIdByUser() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        userTokenWithBearer, null, null, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 2);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }

  @Test
  public void getAllTimescaleAlertsByUserAnotherEntity() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        userTokenWithBearer, null, null, 2);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 0);
  }

  @Test
  public void getAllTimescaleAlertsWithLimitAndEntityIdByUser() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        userTokenWithBearer, null, 1, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 2);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 1));
  }

  @Test
  public void getAllTimescaleAlertsWithSensorIdListAndEntityIdByUser() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        userTokenWithBearer, new Integer[]{1,2,3}, null, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 3);
    assertTrue(response.getBody().entrySet().stream().anyMatch(e -> e.getValue().size() == 3));
    assertTrue(response.getBody().entrySet().stream().anyMatch(e -> e.getValue().size() == 0));
  }

  @Test
  public void getAllTimescaleAlertsWithLimitAndSensorIdListAndEntityIdByUser() {
    ResponseEntity<Map<Integer, List<Sensor>>> response = dataController.getSensorsValues(
        userTokenWithBearer, new Integer[]{3}, 2, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().keySet().size() == 1);
    assertTrue(response.getBody().entrySet().stream().allMatch(e -> e.getValue().size() == 2));
  }



  @Test
  public void getLastSensorValueByAdminSuccesfull() {
    ResponseEntity<Sensor> response = dataController.getLastSensorValue(
        adminTokenWithBearer, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void getLastSensorValueByUserSuccesfull() {
    ResponseEntity<Sensor> response = dataController.getLastSensorValue(
        userTokenWithBearer, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }
}