package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.*;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GatewayControllerTest {

  private GatewayController gatewayController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

  @MockBean
  private GatewayService gatewayService;

  @MockBean
  private DeviceService deviceService;

  @MockBean
  private SensorService sensorService;

  private final String userTokenWithBearer = "Bearer userToken";
  private final String adminTokenWithBearer = "Bearer adminToken";
  private final String userToken = "userToken";
  private final String adminToken = "adminToken";

  private Entity entity1;

  private User admin;
  private User user;

  private Gateway gateway1;
  private Gateway gateway2;

  private Device device1;
  private Device device2;
  private Device device3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;
  private Sensor sensor4;

  private List<Gateway> allGateways;

  @Before
  public void setUp() {
    gatewayController = new GatewayController(gatewayService, deviceService, sensorService, jwtUtil, logService, userService);

    // ----------------------------------------- Set Users --------------------------------------
    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

    user.setEntity(new Entity(1, "name", "loc"));

    // ----------------------------------------- Set Devices --------------------------------------
    device1 = new Device(1, "dev1", 1, 1);
    device2 = new Device(2, "dev2", 1, 2);
    device3 = new Device(3, "dev3", 1, 3);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);

    // ----------------------------------------- Set Gateways --------------------------------------
    gateway1 = new Gateway(1, "gw1");
    gateway2 = new Gateway(2, "gw2");

    allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);

    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 3);
    sensor4 = new Sensor(4, "type4", 4);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);
    allSensors.add(sensor4);

    // ---------------------------------- Set Gateways to Devices -------------------------------
    device1.setGateway(gateway1);
    device2.setGateway(gateway1);

    device3.setGateway(gateway2);

    // ---------------------------------- Set Devices to Sensors --------------------------------
    sensor1.setDevice(device1);
    sensor2.setDevice(device1);

    sensor3.setDevice(device2);

    sensor4.setDevice(device3);

    // Core Controller needed mock
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(gatewayService.findAll()).thenReturn(allGateways);
    when(gatewayService.findAllByEntityId(anyInt())).thenAnswer(i -> Collections.emptyList());
    when(gatewayService.findById(anyInt())).thenAnswer(i -> allGateways.stream()
        .filter(g -> i.getArgument(0).equals(g.getId()))
        .findFirst().orElse(null));
    when(gatewayService.findByDeviceId(anyInt())).thenAnswer(i -> {
      Device device = allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId())).findFirst().orElse(null);
      if (device != null) {
        return device.getGateway();
      } else {
        return null;
      }
    });

    when(deviceService.findAllByGatewayId(anyInt())).thenAnswer(i -> {
      Gateway gateway = gatewayService.findById(i.getArgument(0));
      if (gateway != null) {
        return allDevices.stream()
            .filter(d -> d.getGateway().equals(gateway))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(deviceService.findByGatewayIdAndRealDeviceId(anyInt(), anyInt())).thenAnswer(i -> {
      Gateway gateway = gatewayService.findById(i.getArgument(0));
      if (gateway != null) {
        return allDevices.stream()
            .filter(d -> d.getGateway().equals(gateway) && i.getArgument(0).equals(d.getId()))
            .findFirst().orElse(null);
      } else {
        return null;
      }
    });

    when(sensorService.findAllByGatewayIdAndRealDeviceId(anyInt(), anyInt())).thenAnswer(i -> {
      Device device = deviceService.findByGatewayIdAndRealDeviceId(i.getArgument(0),
          i.getArgument(1));
      if (device != null) {
        return allSensors.stream()
            .filter(s -> s.getDevice().equals(device))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(sensorService.findByGatewayIdAndRealDeviceIdAndRealSensorId(anyInt(), anyInt(), anyInt())).thenAnswer(i -> {
      Device device = deviceService.findByGatewayIdAndRealDeviceId(i.getArgument(0),
          i.getArgument(1));
      if (device != null) {
        return allSensors.stream()
            .filter(s -> s.getDevice().equals(device))
            .findFirst().orElse(null);
      } else {
        return null;
      }
    });

  }

  @Test
  public void getAllGatewaysByAdmin() {
    ResponseEntity<List<Gateway>> response = gatewayController.getGateways(adminTokenWithBearer, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allGateways, response.getBody());
  }

  @Test
  public void getAllGatewaysByDeviceIdByAdmin() {
    ResponseEntity<List<Gateway>> response = gatewayController.getGateways(adminTokenWithBearer, device1.getId());

    System.out.println(response.getBody().size());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void getAllGatewaysByNotExistentDeviceIdByAdmin() {
    ResponseEntity<List<Gateway>> response = gatewayController.getGateways(adminTokenWithBearer, 16);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  public void getAllGatewaysByUserEmptyList() {
    ResponseEntity<List<Gateway>> response = gatewayController.getGateways(userTokenWithBearer, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  public void getGatewayByIdByAdmin() {
    ResponseEntity<Gateway> response = gatewayController.getGateway(
        adminTokenWithBearer, gateway1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(gateway1, response.getBody());
  }

  @Test
  public void getGatewayByIdByUserNull() {
    ResponseEntity<Gateway> response = gatewayController.getGateway(
        userTokenWithBearer, gateway1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void getGatewayDevicesByGatewayIdByAdmin() {
    ResponseEntity<List<Device>> response = gatewayController.getGatewaysDevices(
        adminTokenWithBearer, gateway1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getGatewayDevicesByGatewayIdByUserError403() {
    ResponseEntity<List<Device>> response = gatewayController.getGatewaysDevices(
        userTokenWithBearer, gateway1.getId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void getGatewayDeviceByGatewayIdAndRealDeviceIdByAdmin() {
    ResponseEntity<Device> response = gatewayController.getGatewaysDevice(
        adminTokenWithBearer, gateway1.getId(), device1.getRealDeviceId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(device1, response.getBody());
  }

  @Test
  public void getGatewayDeviceByGatewayIdAndRealDeviceIdByUserError403() {
    ResponseEntity<Device> response = gatewayController.getGatewaysDevice(
        userTokenWithBearer, gateway1.getId(), device1.getRealDeviceId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void getGatewayDeviceSensorsByGatewayIdAndRealDeviceIdByAdmin() {
    ResponseEntity<List<Sensor>> response = gatewayController.getGatewaysDevicesSensors(
        adminTokenWithBearer, gateway1.getId(), device1.getRealDeviceId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getGatewayDeviceSensorsByGatewayIdAndRealDeviceIdByUserError403() {
    ResponseEntity<List<Sensor>> response = gatewayController.getGatewaysDevicesSensors(
        userTokenWithBearer, gateway1.getId(), device1.getRealDeviceId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void getGatewayDeviceSensorByGatewayIdAndRealDeviceIdAndRealSensorIdByAdmin() {
    ResponseEntity<Sensor> response = gatewayController.getGatewaysDevicesSensor(
        adminTokenWithBearer, gateway1.getId(), device1.getRealDeviceId(),
        sensor1.getRealSensorId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(sensor1, response.getBody());
  }

  @Test
  public void getGatewayDeviceSensorByGatewayIdAndRealDeviceIdAndRealSensorIdByUserError403() {
    ResponseEntity<Sensor> response = gatewayController.getGatewaysDevicesSensor(
        userTokenWithBearer, gateway1.getId(), device1.getRealDeviceId(),
        sensor1.getRealSensorId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
