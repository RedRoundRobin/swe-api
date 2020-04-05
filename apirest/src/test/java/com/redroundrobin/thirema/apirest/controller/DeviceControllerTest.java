package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

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
public class DeviceControllerTest {

  private DeviceController deviceController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private UserService userService;

  @MockBean
  private DeviceService deviceService;

  @MockBean
  private SensorService sensorService;

  private String userTokenWithBearer = "Bearer userToken";
  private String adminTokenWithBearer = "Bearer adminToken";
  private String userToken = "userToken";
  private String adminToken = "adminToken";

  private Entity entity1;
  private Entity entity2;

  private User admin;
  private User user;

  private Gateway gateway1;
  private Gateway gateway2;
  private Gateway gateway3;

  private Device device1;
  private Device device2;
  private Device device3;
  private Device device4;
  private Device device5;
  private Device device6;
  private Device device7;

  private Sensor sensor1;
  private Sensor sensor2;

  List<Sensor> device1Sensors;

  private List<Device> allDevices;
  private List<Device> entity1Devices;

  @Before
  public void setUp() {
    deviceController = new DeviceController(deviceService, sensorService);
    deviceController.setJwtUtil(jwtUtil);
    deviceController.setUserService(userService);

    admin = new User();
    admin.setId(1);
    admin.setEmail("admin");
    admin.setType(User.Role.ADMIN);

    user = new User();
    user.setId(2);
    user.setEmail("user");
    user.setType(User.Role.USER);


    // -------------------------------------- Set entities ----------------------------------------
    entity1 = new Entity();
    entity1.setId(1);
    user.setEntity(entity1);

    entity2 = new Entity();
    entity2.setId(2);

    // -------------------------------------- Set Devices -----------------------------------------
    device1 = new Device();
    device1.setId(1);
    device2 = new Device();
    device2.setId(2);
    device3 = new Device();
    device3.setId(3);
    device4 = new Device();
    device4.setId(4);
    device5 = new Device();
    device5.setId(5);
    device6 = new Device();
    device6.setId(6);
    device7 = new Device();
    device7.setId(7);

    allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);
    allDevices.add(device4);
    allDevices.add(device5);
    allDevices.add(device6);
    allDevices.add(device7);


    // ---------------------------------------- Set gateways ---------------------------------------
    gateway1 = new Gateway();
    gateway1.setId(1);
    gateway2 = new Gateway();
    gateway2.setId(2);
    gateway3 = new Gateway();
    gateway3.setId(3);


    // ---------------------------------------- Set sensors ---------------------------------------
    sensor1 = new Sensor();
    sensor1.setRealSensorId(1);
    sensor2 = new Sensor();
    sensor2.setRealSensorId(2);


    // --------------------------- Set devices to sensors and viceversa ---------------------------
    device1Sensors = new ArrayList<>();
    device1Sensors.add(sensor1);
    device1Sensors.add(sensor2);
    device1.setSensors(device1Sensors);


    // ---------------------------- Set devices to gateways and viceversa -------------------------
    List<Device> gateway1Devices = new ArrayList<>();
    gateway1Devices.add(device1);
    gateway1Devices.add(device3);
    gateway1Devices.add(device5);
    gateway1Devices.add(device6);
    device1.setGateway(gateway1);
    device3.setGateway(gateway1);
    device5.setGateway(gateway1);
    device6.setGateway(gateway1);
    gateway1.setDevices(gateway1Devices);

    List<Device> gateway2Devices = new ArrayList<>();
    gateway2Devices.add(device2);
    gateway2Devices.add(device4);
    device2.setGateway(gateway2);
    device4.setGateway(gateway2);
    gateway2.setDevices(gateway2Devices);

    List<Device> gateway3Devices = new ArrayList<>();
    gateway3Devices.add(device7);
    device7.setGateway(gateway3);
    gateway3.setDevices(gateway3Devices);


    // --------------------------- Set sensors to entities and viceversa --------------------------
    entity1Devices = new ArrayList<>();
    entity1Devices.add(device1);
    entity1Devices.add(device2);
    entity1Devices.add(device3);
    entity1Devices.add(device4);


    // Core Controller needed mock
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(deviceService.findAll()).thenReturn(allDevices);
    when(deviceService.findAllByEntityId(1)).thenReturn(entity1Devices);
    when(deviceService.findAllByGatewayId(anyInt())).thenAnswer(i -> {
      return allDevices.stream().filter(d -> i.getArgument(0).equals(d.getGateway().getId()))
          .collect(Collectors.toList());
    });
    when(deviceService.findAllByEntityIdAndGatewayId(anyInt(),anyInt())).thenAnswer(i -> {
      if (i.getArgument(0).equals(1)) {
        return entity1Devices.stream().filter(d -> i.getArgument(1).equals(d.getGateway().getId()))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(deviceService.findById(device1.getId())).thenReturn(device1);
    when(deviceService.findByIdAndEntityId(device6.getId(), entity1.getId())).thenReturn(null);

    when(sensorService.findAllByDeviceId(device1.getId())).thenReturn(device1Sensors);
    when(sensorService.findAllByDeviceIdAndEntityId(device1.getId(), entity1.getId()))
        .thenReturn(device1Sensors);

    when(sensorService.findByDeviceIdAndRealSensorId(device1.getId(), sensor1.getRealSensorId()))
        .thenReturn(sensor1);
    when(sensorService.findByDeviceIdAndRealSensorIdAndEntityId(device1.getId(),
        sensor1.getRealSensorId(),entity1.getId())).thenReturn(sensor1);
  }

  @Test
  public void getAllDevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, null, null);

    assertEquals(allDevices, response.getBody());
  }

  @Test
  public void getEntity1DevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, 1, null);

    assertEquals(entity1Devices, response.getBody());
  }

  @Test
  public void getEntity1AndGateway1DevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, 1, 1);

    assertTrue(!response.getBody().isEmpty());
  }

  @Test
  public void getGateway1DevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, null, 1);

    assertTrue(!response.getBody().isEmpty());
  }

  @Test
  public void getEntity2DevicesByUserEmptyList() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(userTokenWithBearer, 2, null);

    assertEquals(Collections.emptyList(), response.getBody());
  }

  @Test
  public void getGateway1DevicesByUser() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(userTokenWithBearer, null, 1);

    assertTrue(!response.getBody().isEmpty());
  }

  @Test
  public void getAllDevicesByUser() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(userTokenWithBearer, null, null);

    assertEquals(entity1Devices, response.getBody());
  }



  @Test
  public void getDeviceByAdmin() {
    ResponseEntity<Device> response = deviceController.getDevice(adminTokenWithBearer,
        device1.getId());

    assertEquals(device1, response.getBody());
  }

  @Test
  public void getDeviceByUser() {
    ResponseEntity<Device> response = deviceController.getDevice(userTokenWithBearer,
        device6.getId());

    assertEquals(null, response.getBody());
  }



  @Test
  public void getSensorsByDeviceIdByAdmin() {
    ResponseEntity<List<Sensor>> response = deviceController.getSensorsByDevice(
        adminTokenWithBearer, device1.getId());

    assertEquals(device1Sensors, response.getBody());
  }

  @Test
  public void getSensorsByDeviceIdByUser() {
    ResponseEntity<List<Sensor>> response = deviceController.getSensorsByDevice(userTokenWithBearer,
        device1.getId());

    assertEquals(device1Sensors, response.getBody());
  }



  @Test
  public void getSensorByDeviceByAdmin() {
    ResponseEntity<Sensor> response = deviceController.getSensorByDevice(adminTokenWithBearer,
        device1.getId(), sensor1.getRealSensorId());

    assertEquals(sensor1, response.getBody());
  }

  @Test
  public void getSensorByDeviceByUser() {
    ResponseEntity<Sensor> response = deviceController.getSensorByDevice(userTokenWithBearer,
        device1.getId(), sensor1.getRealSensorId());

    assertEquals(sensor1, response.getBody());
  }
}
