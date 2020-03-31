package com.redroundrobin.thirema.apirest.controller;

    import com.redroundrobin.thirema.apirest.models.postgres.Device;
    import com.redroundrobin.thirema.apirest.models.postgres.Entity;
    import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
    import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
    import com.redroundrobin.thirema.apirest.models.postgres.User;
    import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
    import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
    import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
    import com.redroundrobin.thirema.apirest.service.postgres.UserService;
    import com.redroundrobin.thirema.apirest.utils.JwtUtil;
    import org.junit.Before;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.mockito.ArgumentMatchers;
    import org.springframework.boot.test.mock.mockito.MockBean;
    import org.springframework.http.HttpStatus;
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
public class GatewayControllerTest {

  private GatewayController gatewayController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private UserService userService;

  @MockBean
  private GatewayService gatewayService;

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

  private List<Device> gateway1Devices;
  private List<Device> gateway2Devices;
  private List<Sensor> device1Sensors;
  private List<Sensor> device2Sensors;
  private List<Sensor> device3Sensors;


  @Before
  public void setUp() {
    gatewayController = new GatewayController(gatewayService, deviceService, sensorService);
    gatewayController.setJwtUtil(jwtUtil);
    gatewayController.setUserService(userService);

    // ----------------------------------------- Set Users --------------------------------------
    admin = new User();
    admin.setId(1);
    admin.setEmail("admin");
    admin.setType(User.Role.ADMIN);

    user = new User();
    user.setId(2);
    user.setEmail("user");
    user.setType(User.Role.USER);

    // ----------------------------------------- Set Devices --------------------------------------
    device1 = new Device();
    device1.setId(1);
    device2 = new Device();
    device2.setId(2);
    device3 = new Device();
    device3.setId(3);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);


    // ----------------------------------------- Set Gateways --------------------------------------
    gateway1 = new Gateway();
    gateway1.setId(1);
    gateway2 = new Gateway();
    gateway2.setId(2);

    allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);


    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor();
    sensor1.setId(1);
    sensor2 = new Sensor();
    sensor2.setId(2);
    sensor3 = new Sensor();
    sensor3.setId(3);
    sensor4 = new Sensor();
    sensor4.setId(4);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);
    allSensors.add(sensor4);


    // --------------------------- Set Devices to Gateways and viceversa ---------------------------
    gateway1Devices = new ArrayList<>();
    gateway1Devices.add(device1);
    gateway1Devices.add(device2);
    gateway1.setDevices(gateway1Devices);
    device1.setGateway(gateway1);
    device2.setGateway(gateway1);

    gateway2Devices = new ArrayList<>();
    gateway2Devices.add(device3);
    gateway2.setDevices(gateway2Devices);
    device3.setGateway(gateway2);


    // --------------------------- Set Devices to Sensors and viceversa ---------------------------
    sensor1.setDevice(device1);
    sensor2.setDevice(device1);
    device1Sensors = new ArrayList<>();
    device1Sensors.add(sensor1);
    device1Sensors.add(sensor2);
    device1.setSensors(device1Sensors);

    sensor3.setDevice(device2);
    device2Sensors = new ArrayList<>();
    device2Sensors.add(sensor3);
    device2.setSensors(device2Sensors);

    sensor4.setDevice(device3);
    device3Sensors = new ArrayList<>();
    device3Sensors.add(sensor4);
    device3.setSensors(device3Sensors);




    // Core Controller needed mock
    when(jwtUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(jwtUtil.extractUsername(adminToken)).thenReturn(admin.getEmail());
    when(jwtUtil.extractType(anyString())).thenReturn("webapp");
    when(userService.findByEmail(admin.getEmail())).thenReturn(admin);
    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    when(gatewayService.findAll()).thenReturn(allGateways);
    when(gatewayService.findById(anyInt())).thenAnswer(i -> {
      return allGateways.stream()
          .filter(g -> i.getArgument(0).equals(g.getId()))
          .findFirst().orElse(null);
    });

    when(deviceService.findAllByGatewayId(anyInt())).thenAnswer(i -> {
      Gateway gateway = gatewayService.findById(i.getArgument(0));
        return gateway != null ? gateway.getDevices() : Collections.emptyList();
    });
    when(deviceService.findByGatewayIdAndRealDeviceId(anyInt(), anyInt())).thenAnswer(i -> {
      Gateway gateway = gatewayService.findById(i.getArgument(0));
      if (gateway != null) {
        return gateway.getDevices().stream()
            .filter(d -> i.getArgument(1).equals(d.getRealDeviceId()))
            .findFirst().orElse(null);
      } else {
        return null;
      }
    });

    when(sensorService.findAllByGatewayIdAndRealDeviceId(anyInt(), anyInt())).thenAnswer(i -> {
      Device device = deviceService.findByGatewayIdAndRealDeviceId(i.getArgument(0),
          i.getArgument(1));
      return device != null ? device.getSensors() : Collections.emptyList();
    });
    when(sensorService.findByGatewayIdAndRealDeviceIdAndRealSensorId(anyInt(), anyInt(), anyInt())).thenAnswer(i -> {
      Device device = deviceService.findByGatewayIdAndRealDeviceId(i.getArgument(0),
          i.getArgument(1));
      if (device != null) {
        return device.getSensors().stream()
            .filter(s -> i.getArgument(2).equals(s.getRealSensorId()))
            .findFirst().orElse(null);
      } else {
        return null;
      }
    });

  }

  @Test
  public void getAllGatewaysByAdmin() {
    ResponseEntity<List<Gateway>> response = gatewayController.getGateways(adminTokenWithBearer);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allGateways, response.getBody());
  }

  @Test
  public void getAllGatewaysByUserError403() {
    ResponseEntity<List<Gateway>> response = gatewayController.getGateways(userTokenWithBearer);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }



  @Test
  public void getGatewayByIdByAdmin() {
    ResponseEntity<Gateway> response = gatewayController.getGateway(
        adminTokenWithBearer, gateway1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(gateway1, response.getBody());
  }

  @Test
  public void getGatewayByIdByUserError403() {
    ResponseEntity<Gateway> response = gatewayController.getGateway(
        userTokenWithBearer, gateway1.getId());

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }



  @Test
  public void getGatewayDevicesByGatewayIdByAdmin() {
    ResponseEntity<List<Device>> response = gatewayController.getGatewaysDevices(
        adminTokenWithBearer, gateway1.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(gateway1Devices, response.getBody());
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
    assertEquals(device1Sensors, response.getBody());
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
