package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.*;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

  MockHttpServletRequest httpRequest;

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
  private List<Entity> allEntities;

  @Before
  public void setUp() {

    httpRequest = new MockHttpServletRequest();
    httpRequest.setRemoteAddr("localhost");

    gatewayController = new GatewayController(gatewayService, deviceService, sensorService, jwtUtil, logService, userService);

    // ----------------------------------------- Set Users --------------------------------------
    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

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

    user.setEntity(new Entity(1, "name", "loc"));
    allEntities = new ArrayList<>();
    allEntities.add(user.getEntity());

    Set<Sensor> entity1Sensors = new HashSet<>();
    entity1Sensors.add(sensor1);
    allEntities.get(0).setSensors(entity1Sensors);

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
    when(gatewayService.findByDeviceIdAndEntityId(anyInt(), anyInt())).thenAnswer(i -> {
      Entity entity = allEntities.stream().filter(e -> (i.getArgument(1)).equals(e.getId())).findFirst().orElse(null);
      if(entity == null) {
        return null;
      }
      Device device = allDevices.stream().filter(d -> (i.getArgument(0)).equals(d.getId())).findFirst().orElse(null);
      if(device == null) {
        return null;
      }
      //se all'entità passata alla funzione è abilitato almeno un sensore del device fornito, mostro il suo gateway;
      //altrimenti restituisco null
          if(entity.getSensors().stream().filter(
              s -> s.getDevice().getId() == device.getId()).findFirst().orElse(null) != null) {
            return device.getGateway();
          }
      return null;
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
  public void getGatewayGivenUserAndDeviceId() {
    ResponseEntity<List<Gateway>> response = gatewayController.getGateways(userTokenWithBearer, 1);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(gateway1, (response.getBody().get(0)));
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

  @Test
  public void addGatewayByAdminSuccesful()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newGatewayFields = new HashMap<>();
    newGatewayFields.put("name", "newGateway");

    when(gatewayService.addGateway(any(Map.class))).thenAnswer(i -> {
      Gateway gateway = new Gateway((String)((HashMap<String, Object>)i.getArgument(0)).get("name"));
      allGateways.add(gateway);
      return gateway;
    });

    ResponseEntity<Gateway> response = gatewayController.addGateway(
        adminTokenWithBearer, newGatewayFields, httpRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(allGateways.get(allGateways.size()-1), response.getBody());
  }

  @Test
  public void addGatewayByAdminMissingFieldsException()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newGatewayFields = new HashMap<>();

    when(gatewayService.addGateway(any(Map.class))).thenThrow(new MissingFieldsException(""));

    ResponseEntity<Gateway> response = gatewayController.addGateway(
        adminTokenWithBearer, newGatewayFields, httpRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody() == null);
  }

  @Test
  public void addGatewayByUserError403() {
    Map<String, Object> newGatewayFields = new HashMap<>();
    newGatewayFields.put("name", "newGateway");

    ResponseEntity<Gateway> response = gatewayController.addGateway(
        userTokenWithBearer, newGatewayFields, httpRequest);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void editGateway1ByAdminSuccesful()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editGatewayFields = new HashMap<>();
    editGatewayFields.put("name", "gatewayTest2");

    gateway1.setName((String)editGatewayFields.get("name"));
    when(gatewayService.editGateway(anyInt(), any(Map.class))).thenReturn(gateway1);

    ResponseEntity<Object> response = gatewayController.editGateway(adminTokenWithBearer,
         gateway1.getId(), editGatewayFields, httpRequest);

    assertEquals(gateway1.getName(), (String)editGatewayFields.get("name"));
    assertEquals(gateway1, response.getBody());
  }

  @Test
  public void editGateway1ByAdminSuccesfulMissingFieldsException()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editGatewayFields = new HashMap<>();
    editGatewayFields.put("notExistingField", "gatewayTest2");

    when(gatewayService.editGateway(anyInt(), any(Map.class))).thenThrow(new MissingFieldsException(""));

    ResponseEntity<Object> response = gatewayController.editGateway(adminTokenWithBearer,
        gateway1.getId(), editGatewayFields, httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.BAD_REQUEST), response);
  }

  @Test
  public void editGateway1ByUser403Error()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editGatewayFields = new HashMap<>();
    editGatewayFields.put("name", "gatewayTest2");

    ResponseEntity<Object> response = gatewayController.editGateway(userTokenWithBearer,
        gateway1.getId(), editGatewayFields, httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
  }

  @Test
  public void sendConfigToGateway1ByAdminSuccesful()
      throws MissingFieldsException, InvalidFieldsValuesException,
      ElementNotFoundException, JsonProcessingException {
    Map<String, Object> sendConfig = new HashMap<>();
    sendConfig.put("reconfig", true);

    when(gatewayService.sendGatewayConfigToKafka(anyInt())).thenReturn("sentConfigToGateway1");

    ResponseEntity<Object> response = gatewayController.editGateway(adminTokenWithBearer,
        gateway1.getId(), sendConfig, httpRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("sentConfigToGateway1", (String)response.getBody());
  }

  @Test
  public void deleteGateway1ByAdmin()
      throws ElementNotFoundException {
    assertTrue(allGateways.remove(gateway1));

    when(gatewayService.deleteGateway(eq(gateway1.getId())))
        .thenReturn(true);

    ResponseEntity response = gatewayController.deleteGateway(adminTokenWithBearer,
        gateway1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.OK), response);

  }

  @Test
  public void deleteGateway1ByAdminConflictError409()
      throws ElementNotFoundException {

    assertTrue(allGateways.remove(gateway1));

    when(gatewayService.deleteGateway(eq(device1.getId())))
        .thenReturn(false); //errore a db: device selezionato dall'admin cè ma non viene cancellato!

    ResponseEntity response = gatewayController.deleteGateway(adminTokenWithBearer,
        device1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.CONFLICT), response);
  }

  @Test
  public void deleteNotExistingGatewayByAdminElementNotFoundException()
      throws  ElementNotFoundException {

    Gateway gatewayNotPresentInMock = new Gateway(gateway1.getId(), "gateway8");

    assertFalse(allGateways.remove(gatewayNotPresentInMock));

    when(gatewayService.deleteGateway(eq(gatewayNotPresentInMock.getId())))
        .thenThrow(new ElementNotFoundException(""));

    ResponseEntity response = gatewayController.deleteGateway(adminTokenWithBearer,
        gatewayNotPresentInMock.getId(), httpRequest);

    assertEquals(new ResponseEntity<>(HttpStatus.BAD_REQUEST), response);
  }

  @Test
  public void deleteGateway1ByUserError403Forbidden()
      throws  ElementNotFoundException {

    ResponseEntity response = gatewayController.deleteGateway(userTokenWithBearer,
        gateway1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
  }

}
