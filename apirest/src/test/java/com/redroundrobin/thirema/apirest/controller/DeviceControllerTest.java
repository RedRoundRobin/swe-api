package com.redroundrobin.thirema.apirest.controller;



import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DeviceControllerTest {

  private DeviceController deviceController;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

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
  private Sensor sensor3;

  List<Sensor> device1Sensors;
  List<Sensor> device1EnabledSensorsCmd;
  List<Sensor> device1NotEnabledSensorsCmd;

  private List<Device> allDevices;
  private List<Device> entity1Devices;
  private List<Device> devicesEnabledForCommands;
  private List<Device> devicesNotEnabledForCommands;

  @Before
  public void setUp() {
    httpRequest = new MockHttpServletRequest();
    httpRequest.setRemoteAddr("localhost");

    deviceController = new DeviceController(deviceService, sensorService, jwtUtil, logService, userService);

    admin = new User(1, "admin", "admin", "admin", "pass", User.Role.ADMIN);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

    // -------------------------------------- Set entities ----------------------------------------
    entity1 = new Entity(1, "entity1", "loc1");
    entity2 = new Entity(2, "entity2", "loc2");

    // -------------------------------------- Set Devices -----------------------------------------
    device1 = new Device(1, "dev1", 1, 1);
    device2 = new Device(2, "dev2", 1, 1);
    device3 = new Device(3, "dev3", 1, 2);
    device4 = new Device(4, "dev4", 1, 2);
    device5 = new Device(5, "dev5", 1, 3);
    device6 = new Device(6, "dev6", 1, 4);
    device7 = new Device(7, "dev7", 1, 1);

    allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);
    allDevices.add(device4);
    allDevices.add(device5);
    allDevices.add(device6);
    allDevices.add(device7);

    // ----------------------------------- Set entities to users ----------------------------------
    user.setEntity(entity1);

    // ---------------------------------------- Set gateways ---------------------------------------
    gateway1 = new Gateway(1, "gw1");
    gateway2 = new Gateway(2, "gw2");
    gateway3 = new Gateway(3, "gw3");

    // ---------------------------------------- Set sensors ---------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 3);
    // --------------------------------- Set devices to sensors ----------------------------------
    sensor1.setDevice(device1);
    sensor2.setDevice(device1);
    sensor3.setDevice(device2);

    device1Sensors = new ArrayList<>();
    device1Sensors.add(sensor1);
    device1Sensors.add(sensor2);


    // ---------------------------------- Set lists devices (not)enabled for commands -------------
    sensor1.setCmdEnabled(true);
    sensor2.setCmdEnabled(false);
    sensor3.setCmdEnabled(false);

    device1EnabledSensorsCmd = new ArrayList();
    device1EnabledSensorsCmd.add(sensor1);

    device1NotEnabledSensorsCmd = new ArrayList();
    device1NotEnabledSensorsCmd.add(sensor2);

    devicesEnabledForCommands = new ArrayList<>();
    devicesEnabledForCommands.add(device1);
    devicesNotEnabledForCommands = new ArrayList<>();
    devicesNotEnabledForCommands.add(device2);

    // ---------------------------------- Set gateways to devices -------------------------------
    device1.setGateway(gateway1);
    device2.setGateway(gateway2);
    device3.setGateway(gateway1);
    device4.setGateway(gateway2);
    device5.setGateway(gateway1);
    device6.setGateway(gateway1);
    device7.setGateway(gateway3);

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
    when(deviceService.findAllByGatewayId(anyInt())).thenAnswer(i -> allDevices.stream().filter(d -> i.getArgument(0).equals(d.getGateway().getId()))
        .collect(Collectors.toList()));
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
    when(deviceService.getEnabled(true)).thenReturn(devicesEnabledForCommands);
    when(deviceService.getEnabled(false)).thenReturn(devicesNotEnabledForCommands);
    when(deviceService.getEnabledSensorsDevice(true, device1.getId())).thenReturn(device1EnabledSensorsCmd);
    when(deviceService.getEnabledSensorsDevice(false, device1.getId())).thenReturn(device1NotEnabledSensorsCmd);

    when(sensorService.findAllByDeviceId(device1.getId())).thenReturn(device1Sensors);
    when(sensorService.findAllByDeviceIdAndEntityId(device1.getId(), entity1.getId()))
        .thenReturn(device1Sensors);

    when(sensorService.findByDeviceIdAndRealSensorId(device1.getId(), sensor1.getRealSensorId()))
        .thenReturn(sensor1);
    when(sensorService.findByDeviceIdAndRealSensorIdAndEntityId(device1.getId(),
        sensor1.getRealSensorId(),entity1.getId())).thenReturn(sensor1);
  }

  private Device cloneDevice(Device device) {
    Device clone = new Device(device.getName(), device.getFrequency(), device.getRealDeviceId());
    clone.setGateway(device.getGateway());

    return clone;
  }

  @Test
  public void getAllDevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, null, null, null);

    assertEquals(allDevices, response.getBody());
  }

  @Test
  public void getAllDevicesByAdminNotFound1() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, null, 1, true);

    assertEquals(new ResponseEntity(HttpStatus.NOT_FOUND), response);
  }

  @Test
  public void getAllDevicesByAdminNotFound2() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, 1, 1, true);

    assertEquals(new ResponseEntity(HttpStatus.NOT_FOUND), response);
  }

  @Test
  public void getAllDevicesEnabledForCommandsByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, null, null, true);

    assertEquals(ResponseEntity.ok(devicesEnabledForCommands), response);
  }

  @Test
  public void getAllDevicesNotEnabledForCommandsByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, null, null, false);

    assertEquals(ResponseEntity.ok(devicesNotEnabledForCommands), response);
  }


  @Test
  public void getEntity1DevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, 1, null, null);

    assertEquals(entity1Devices, response.getBody());
  }

  @Test
  public void getEntity1AndGateway1DevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, 1, 1, null);

    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getGateway1DevicesByAdmin() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(adminTokenWithBearer, null, 1, null);

    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getEntity2DevicesByUserEmptyList() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(userTokenWithBearer, 2, null, null);

    assertEquals(Collections.emptyList(), response.getBody());
  }

  @Test
  public void getAllDevicesEnabledForCommandsByAUser() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(userTokenWithBearer, null, null, false);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
  }

  @Test
  public void getGateway1DevicesByUser() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(userTokenWithBearer, null, 1, null);

    assertFalse(response.getBody().isEmpty());
  }

  @Test
  public void getAllDevicesByUser() {
    ResponseEntity<List<Device>> response = deviceController.getDevices(userTokenWithBearer, null, null, null);

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

    assertNull(response.getBody());
  }

  @Test
  public void getSensorsByDeviceIdByAdmin() {
    ResponseEntity<List<Sensor>> response = deviceController.getSensorsByDevice(
        adminTokenWithBearer, device1.getId(), null);

    assertEquals(device1Sensors, response.getBody());
  }

  @Test
  public void getAllEnabledForCommandsSensorsByDeviceIdByAdmin() {
    ResponseEntity<List<Sensor>> response = deviceController.getSensorsByDevice(
        adminTokenWithBearer, device1.getId(), true);

    assertEquals(device1EnabledSensorsCmd, response.getBody());
  }

  @Test
  public void getAllNotEnabledForCommandsSensorsByDeviceIdByAdmin() {
    ResponseEntity<List<Sensor>> response = deviceController.getSensorsByDevice(
        adminTokenWithBearer, device1.getId(), false);

    assertEquals(device1NotEnabledSensorsCmd, response.getBody());
  }

  @Test
  public void getSensorsByDeviceIdByUser() {
    ResponseEntity<List<Sensor>> response = deviceController.getSensorsByDevice(userTokenWithBearer,
        device1.getId(), null);

    assertEquals(device1Sensors, response.getBody());
  }

  @Test
  public void getAllSensorsByDeviceIdEnabledForCommandsByUser() {
    ResponseEntity<List<Sensor>> response = deviceController.getSensorsByDevice(
        userTokenWithBearer,  device1.getId(), false);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
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

  @Test
  public void createDeviceByAdminSuccessful()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("name", "devTest");
    newDeviceFields.put("realDeviceId", 2);
    newDeviceFields.put("frequency", 1);
    newDeviceFields.put("gatewayId", 3);

    Device expectedDevice = new Device((String)newDeviceFields.get("name"),
        (int)newDeviceFields.get("frequency"), (int)newDeviceFields.get("realDeviceId"));
    expectedDevice.setGateway(gateway3);

    when(deviceService.addDevice(any(Map.class))).thenReturn(expectedDevice);

    ResponseEntity<Device> response = deviceController.createDevice(adminTokenWithBearer,
        newDeviceFields  , httpRequest);

    assertEquals(expectedDevice, response.getBody());
  }

  @Test
  public void createDeviceByAdminMissingFieldsException()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("name", "devTest");
    newDeviceFields.put("realDeviceId", 2);
    newDeviceFields.put("gatewayId", 3);
    //manca la frequency (basta manci un solo field per lanciare l'eccezione MissingFieldsException)

    when(deviceService.addDevice(any(Map.class))).thenThrow(new MissingFieldsException(""));

    ResponseEntity<Device> response = deviceController.createDevice(adminTokenWithBearer,
        newDeviceFields  , httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.BAD_REQUEST) , response);
  }

  @Test
  public void createDeviceByAdminInvalidFieldsValuesException()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("name", "devTest");
    newDeviceFields.put("realDeviceId", 2);
    newDeviceFields.put("gatewayId", 4);
    //questo gatewayId non esiste

    when(deviceService.addDevice(any(Map.class))).thenThrow(new InvalidFieldsValuesException(""));

    ResponseEntity<Device> response = deviceController.createDevice(adminTokenWithBearer,
        newDeviceFields  , httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.BAD_REQUEST) , response);
  }

  @Test
  public void createDeviceByUserErro403()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("name", "devTest");
    newDeviceFields.put("realDeviceId", 2);
    newDeviceFields.put("frequency", 1);
    newDeviceFields.put("gatewayId", 3);

    ResponseEntity<Device> response = deviceController.createDevice(userTokenWithBearer,
        newDeviceFields  , httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN) , response);
  }

  @Test
  public void createSensorByAdminSuccessful()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newSensorFields = new HashMap<>();
    newSensorFields.put("type", "description");
    newSensorFields.put("realSensorId", 3);
    newSensorFields.put("deviceId", device1.getId());
    newSensorFields.put("cmdEnabled", false);

    Sensor expectedSensor = new Sensor((int)newSensorFields.get("deviceId"),
        (String)newSensorFields.get("type"), (int)newSensorFields.get("realSensorId"));

    expectedSensor.setCmdEnabled((boolean)newSensorFields.get("cmdEnabled"));
    expectedSensor.setDevice(deviceService.findById((int)newSensorFields.get("deviceId")));

    when(sensorService.addSensor(any(Map.class))).thenReturn(expectedSensor);

    ResponseEntity<Sensor> response = deviceController.createSensor(adminTokenWithBearer,
        newSensorFields  , device1.getId(), httpRequest);

    assertEquals(expectedSensor, response.getBody());
  }

  @Test
  public void createSensorByAdminSuccessfulWithoutSpecifiyingCmdEnabled()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newSensorFields = new HashMap<>();
    newSensorFields.put("type", "description");
    newSensorFields.put("realSensorId", 3);
    newSensorFields.put("deviceId", device1.getId());

    Sensor expectedSensor = new Sensor((int)newSensorFields.get("deviceId"),
        (String)newSensorFields.get("type"), (int)newSensorFields.get("realSensorId"));

    expectedSensor.setDevice(deviceService.findById((int)newSensorFields.get("deviceId")));

    when(sensorService.addSensor(any(Map.class))).thenReturn(expectedSensor);

    ResponseEntity<Sensor> response = deviceController.createSensor(adminTokenWithBearer,
        newSensorFields, device1.getId(), httpRequest);

    assertEquals(expectedSensor, response.getBody());
  }

  @Test
  public void createSensorByAdminMissingFieldsException()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newSensorFields = new HashMap<>();
    newSensorFields.put("realSensorId", 3);
    newSensorFields.put("deviceId", device1.getId());
    newSensorFields.put("cmdEnabled", false);
    //manca il type (basta manchi un solo field che non sia cmdEnabled per lanciare l'eccezione MissingFieldsException)

    when(sensorService.addSensor(any(Map.class))).thenThrow(new MissingFieldsException(""));

    ResponseEntity<Sensor> response = deviceController.createSensor(adminTokenWithBearer,
        newSensorFields , device1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.BAD_REQUEST) , response);
  }


  @Test
  public void createSensorByAdminInvalidFieldsValuesException()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newSensorFields = new HashMap<>();
    newSensorFields.put("realSensorId", 3);
    newSensorFields.put("type", "description");
    newSensorFields.put("deviceId", 8); //id non esistente
    newSensorFields.put("cmdEnabled", false);

    when(sensorService.addSensor(any(Map.class))).thenThrow(new InvalidFieldsValuesException(""));

    ResponseEntity<Sensor> response = deviceController.createSensor(adminTokenWithBearer,
        newSensorFields , 8, httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.BAD_REQUEST) , response);
  }


  @Test
  public void createSensorByUserError403()
      throws MissingFieldsException, InvalidFieldsValuesException {
    Map<String, Object> newSensorFields = new HashMap<>();
    newSensorFields.put("realSensorId", 3);
    newSensorFields.put("type", "description");
    newSensorFields.put("deviceId", 1); //id non esistente
    newSensorFields.put("cmdEnabled", false);

    ResponseEntity<Sensor> response = deviceController.createSensor(userTokenWithBearer,
        newSensorFields, 1 , httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN) , response);
  }

  @Test
  public void editDevice1ByAdminSuccesful()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editDeviceFields = new HashMap<>();
    editDeviceFields.put("name", "devTest2");

    device1.setName((String)editDeviceFields.get("name"));
    when(deviceService.editDevice(anyInt(), any(Map.class))).thenReturn(device1);

    ResponseEntity<Device> response = deviceController.editDevice(adminTokenWithBearer,
        editDeviceFields, device1.getId(), httpRequest);

    assertEquals(device1.getName(), (String)editDeviceFields.get("name"));
    assertEquals(device1, response.getBody());
  }

  /*in th next test, none of the map filds given exist in a Device object!*/
  @Test
  public void editDevice1ByAdminSuccesfulMissingFieldsException()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editDeviceFields = new HashMap<>();
    editDeviceFields.put("notExistingField", "devTest2");

    when(deviceService.editDevice(anyInt(), any(Map.class))).thenThrow(new MissingFieldsException(""));

    ResponseEntity<Device> response = deviceController.editDevice(adminTokenWithBearer,
        editDeviceFields, device1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.BAD_REQUEST), response);
  }


  @Test
  public void editDevice1ByUser403Error()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editDeviceFields = new HashMap<>();
    editDeviceFields.put("name", "devTest2");

    ResponseEntity<Device> response = deviceController.editDevice(userTokenWithBearer,
        editDeviceFields, device1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
  }


  @Test
  public void editSensor1Device1ByAdminSuccesful()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editSensorFields = new HashMap<>();
    editSensorFields.put("type", "devTest2");

    sensor1.setType((String)editSensorFields.get("type"));
    when(sensorService.editSensor(anyInt(), anyInt(), any(Map.class))).thenReturn(sensor1);

    ResponseEntity<Sensor> response = deviceController.editSensor(adminTokenWithBearer,
        editSensorFields, device1.getId(), sensor1.getRealSensorId(), httpRequest);

    ResponseEntity<Sensor> expected = ResponseEntity.ok(sensor1);

    assertEquals(sensor1.getType(), (String)editSensorFields.get("type"));
    assertEquals(expected, response);
  }

  /*in th next test, none of the map filds given exist in a Sensor object!*/
  @Test
  public void editSensor1ByAdminMissingFieldsException()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editSensorFields = new HashMap<>();
    editSensorFields.put("notExistingField", "devTest2");

    when(sensorService.editSensor(eq(device1.getId()), eq(sensor1.getRealSensorId()), any(Map.class)))
        .thenThrow(new MissingFieldsException(""));

    ResponseEntity<Sensor> response = deviceController.editSensor(adminTokenWithBearer,
        editSensorFields, device1.getId(), sensor1.getRealSensorId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.BAD_REQUEST), response);
  }

  @Test
  public void editSensor1Device1ByUser403Error()
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException {
    Map<String, Object> editSensorFields = new HashMap<>();
    editSensorFields.put("type", "senTest2");

    ResponseEntity<Sensor> response = deviceController.editSensor(userTokenWithBearer,
        editSensorFields, device1.getId(), sensor1.getRealSensorId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
  }

  @Test
  public void deleteDevice1ByAdminSuccesful()
      throws ElementNotFoundException {

    assertTrue(allDevices.remove(device1));

    when(deviceService.deleteDevice(eq(device1.getId())))
        .thenReturn(true);

    ResponseEntity response = deviceController.deleteDevice(adminTokenWithBearer,
        device1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.OK), response);
  }

  @Test
  public void deleteDevice1ByAdminConflictError409()
      throws ElementNotFoundException {

    assertTrue(allDevices.remove(device1));

    when(deviceService.deleteDevice(eq(device1.getId())))
        .thenReturn(false); //errore a db: device selezionato dall'admin cè ma non viene cancellato!

    ResponseEntity response = deviceController.deleteDevice(adminTokenWithBearer,
        device1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.CONFLICT), response);
  }

  @Test
  public void deleteNotExistingDeviceByAdminElementNotFoundException()
      throws  ElementNotFoundException {

    Device deviceNotPresentInMock = new Device(8, "dev8", 1, 1);

    assertFalse(allDevices.remove(deviceNotPresentInMock));

    when(deviceService.deleteDevice(eq(deviceNotPresentInMock.getId())))
        .thenThrow(new ElementNotFoundException(""));

    ResponseEntity response = deviceController.deleteDevice(adminTokenWithBearer,
        deviceNotPresentInMock.getId(), httpRequest);

    assertEquals(new ResponseEntity<>(HttpStatus.BAD_REQUEST), response);
  }

  @Test
  public void deleteDevice1ByUserError403Forbidden()
      throws  ElementNotFoundException {

    ResponseEntity response = deviceController.deleteDevice(userTokenWithBearer,
        device1.getId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
  }

  @Test
  public void deleteSensor1Device1ByAdminSuccesful()
      throws ElementNotFoundException {

    assertTrue(device1Sensors.remove(sensor1));

    when(sensorService.deleteSensor(eq(device1.getId()), eq(sensor1.getRealSensorId())))
        .thenReturn(true);

    ResponseEntity response = deviceController.deleteSensor(adminTokenWithBearer,
        device1.getId(), sensor1.getRealSensorId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.OK), response);
  }

  @Test
  public void deleteSensor1Device1ByAdminConflictError409()
      throws ElementNotFoundException {

    assertTrue(device1Sensors.remove(sensor1));

    when(sensorService.deleteSensor(eq(device1.getId()), eq(sensor1.getRealSensorId())))
        .thenReturn(false);

    ResponseEntity response = deviceController.deleteSensor(adminTokenWithBearer,
        device1.getId(), sensor1.getRealSensorId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.CONFLICT), response);
  }

  @Test
  public void deleteNotExistingSensorInDevice1ByAdminElementNotFoundException()
      throws  ElementNotFoundException {

    Sensor sensorNotPresentInMock = new Sensor(10, "type1", 10);

    assertFalse(device1Sensors.remove(sensorNotPresentInMock));

    when(sensorService.deleteSensor(eq(device1.getId()),
        eq(sensorNotPresentInMock.getRealSensorId())))
        .thenThrow(new ElementNotFoundException(""));

    ResponseEntity response = deviceController.deleteSensor(adminTokenWithBearer,
        device1.getId(), sensorNotPresentInMock.getRealSensorId(), httpRequest);

    assertEquals(new ResponseEntity<>(HttpStatus.BAD_REQUEST), response);
  }

  @Test
  public void deleteSensor1Device1ByUserError403Forbidden()
      throws  ElementNotFoundException {

    ResponseEntity response = deviceController.deleteSensor(userTokenWithBearer,
        device1.getId(),  sensor1.getRealSensorId(), httpRequest);

    assertEquals(new ResponseEntity(HttpStatus.FORBIDDEN), response);
  }

}
