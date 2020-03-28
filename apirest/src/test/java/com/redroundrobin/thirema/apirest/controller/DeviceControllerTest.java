package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  private User admin;
  private User user;

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

    entity1 = new Entity();
    entity1.setId(1);

    user = new User();
    user.setId(2);
    user.setEmail("user");
    user.setType(User.Role.USER);
    user.setEntity(entity1);

    sensor1 = new Sensor();
    sensor1.setRealSensorId(1);
    sensor2 = new Sensor();
    sensor2.setRealSensorId(2);
    device1Sensors = new ArrayList<>();
    device1Sensors.add(sensor1);
    device1Sensors.add(sensor2);

    device1 = new Device();
    device1.setId(1);
    device1.setSensors(device1Sensors);
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

    entity1Devices = new ArrayList<>();
    entity1Devices.add(device1);
    entity1Devices.add(device2);
    entity1Devices.add(device3);
    entity1Devices.add(device4);

    allDevices = new ArrayList<>();
    allDevices.addAll(entity1Devices);
    entity1Devices.add(device5);
    entity1Devices.add(device6);
    entity1Devices.add(device7);

    // Core Controller needed mock
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(deviceService.findAll()).thenReturn(allDevices);
    when(deviceService.findAllByEntityId(1)).thenReturn(entity1Devices);

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
    List<Device> devices = deviceController.getDevices(adminTokenWithBearer, null);

    assertEquals(allDevices, devices);
  }

  @Test
  public void getEntity1DevicesByAdmin() {
    List<Device> devices = deviceController.getDevices(adminTokenWithBearer, 1);

    assertEquals(entity1Devices, devices);
  }

  @Test
  public void getEntity2DevicesByUser() {
    List<Device> devices = deviceController.getDevices(userTokenWithBearer, 2);

    assertEquals(Collections.emptyList(), devices);
  }

  @Test
  public void getAllDevicesByUser() {
    List<Device> devices = deviceController.getDevices(userTokenWithBearer, null);

    assertEquals(entity1Devices, devices);
  }



  @Test
  public void getDeviceByAdmin() {
    Device device = deviceController.getDevice(adminTokenWithBearer, device1.getId());

    assertEquals(device1, device);
  }

  @Test
  public void getDeviceByUser() {
    Device device = deviceController.getDevice(userTokenWithBearer, device6.getId());

    assertEquals(null, device);
  }



  @Test
  public void getSensorsByDeviceIdByAdmin() {
    List<Sensor> sensors = deviceController.getSensorsByDevice(adminTokenWithBearer, device1.getId());

    assertEquals(device1Sensors, sensors);
  }

  @Test
  public void getSensorsByDeviceIdByUser() {
    List<Sensor> sensors = deviceController.getSensorsByDevice(userTokenWithBearer, device1.getId());

    assertEquals(device1Sensors, sensors);
  }



  @Test
  public void getSensorByDeviceByAdmin() {
    Sensor sensor = deviceController.getSensorByDevice(adminTokenWithBearer, device1.getId(),
        sensor1.getRealSensorId());

    assertEquals(sensor1, sensor);
  }

  @Test
  public void getSensorByDeviceByUser() {
    Sensor sensor = deviceController.getSensorByDevice(userTokenWithBearer, device1.getId(),
        sensor1.getRealSensorId());

    assertEquals(sensor1, sensor);
  }
}
