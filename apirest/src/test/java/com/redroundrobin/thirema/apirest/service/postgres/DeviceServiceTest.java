package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ConflictException;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
//import        org.mockito.invocation.InvocationOnMock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import        org.mockito.stubbing.Answer;


@RunWith(SpringRunner.class)
public class DeviceServiceTest {

  @MockBean
  private DeviceRepository deviceRepo;

  @MockBean
  private EntityRepository entityRepo;

  @MockBean
  private GatewayRepository gatewayRepo;

  @MockBean
  private SensorRepository sensorRepo;

  private DeviceService deviceService;

  private Device device1;
  private Device device2;
  private Device device3;

  private Entity entity1;

  private Gateway gateway1;
  private Gateway gateway2;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;
  private Sensor sensor4;
  private Sensor sensor5;

  private List<Device> allDevices;
  private List<Sensor> allSensors;

  @Before
  public void setUp() throws Exception {
    deviceService = new DeviceService(deviceRepo, entityRepo, gatewayRepo, sensorRepo);

    // ----------------------------------------- Set Devices --------------------------------------
    device1 = new Device(1, "dev1", 1, 1);
    device2 = new Device(2, "dev2", 2, 2);
    device3 = new Device(3, "dev3", 3, 3);

    allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);

    // ----------------------------------------- Set Entities -------------------------------------
    entity1 = new Entity(1, "name", "location");

    // ----------------------------------------- Set Gateways --------------------------------------
    gateway1 = new Gateway(1, "name1");
    gateway2 = new Gateway(2, "name2");

    List<Gateway> allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);

    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor1.setCmdEnabled(true);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 3);
    sensor4 = new Sensor(4, "type4", 4);
    sensor4.setCmdEnabled(true);
    sensor5 = new Sensor(5, "type5", 5);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);
    allSensors.add(sensor4);
    allSensors.add(sensor5);

    // --------------------------------- Set Gateways to Devices ---------------------------------
    device1.setGateway(gateway1);
    device2.setGateway(gateway1);
    device3.setGateway(gateway2);

    // --------------------------------- Set Devices to Sensors ---------------------------------
    sensor1.setDevice(device1);
    sensor2.setDevice(device1);

    sensor3.setDevice(device2);
    sensor4.setDevice(device2);

    sensor5.setDevice(device3);

    // --------------------------------- Set Sensors to Entities --------------------------------
    Set<Sensor> entity1Sensor = new HashSet<>();
    entity1Sensor.add(sensor1);
    entity1Sensor.add(sensor3);
    entity1.setSensors(entity1Sensor);
    List<Device> entity1Devices = new ArrayList<>();
    entity1Devices.add(device1);
    entity1Devices.add(device2);

    when(deviceRepo.findById(anyInt())).thenAnswer(i -> allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
        .findFirst());
  /*  when(deviceRepo.existsById(anyInt())).thenAnswer(i -> {
      Device dev =
          allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId())).findFirst().orElse(null);
      return (dev != null);
    });*/
    doNothing().when(deviceRepo).delete(any(Device.class));
    /* PROBLEMA delete  RESTITUISCE VOID...
    when(deviceRepo.delete(any(Device.class))).thenAnswer(i -> {
      Device dev = i.getArgument(0);
      if (dev != null) {
        for(Sensor s : allSensors) {
          if(s.getDevice().getId() == dev.getId()) {
            s.setDevice(null);
          }
        }
        allDevices.remove(dev);
      }
    });*/
    /*perche la soluzione sotto non è okay??*/
  /*  when(deviceService.deleteDevice(anyInt())).thenAnswer(i -> {
      Device dev =
          allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
              .findFirst().orElse(null);
      if (dev != null) {
        //deviceRepo.delete(device); done because couldn't find a way to mock a nested void method (delete)
        for(Sensor s : allSensors) {
          if(s.getDevice().getId() == dev.getId()) {
            s.setDevice(null);
          }
        }
        allDevices.remove(dev);
        if (!deviceRepo.existsById((int)i.getArgument(0))) {
          return true;
        } else {
          return false;
        }
      } else {
        throw new ElementNotFoundException("");
      }
    });*/
    when(deviceRepo.findAll()).thenReturn(allDevices);
    when(deviceRepo.findAllByGateway(any(Gateway.class))).thenAnswer(i -> {
      Gateway gateway = i.getArgument(0);
      return allDevices.stream().filter(d -> gateway.equals(d.getGateway()))
          .collect(Collectors.toList());
    });
    when(deviceRepo.findAllByEntityId(anyInt())).thenAnswer(i -> {
      if (i.getArgument(0).equals(entity1.getId())) {
        return entity1Devices;
      } else {
        return Collections.emptyList();
      }
    });
    when(deviceRepo.findAllByEntityIdAndGateway(anyInt(),any(Gateway.class))).thenAnswer(i -> {
      if (i.getArgument(0).equals(entity1.getId())) {
        return entity1Devices.stream().filter(d -> i.getArgument(1).equals(d.getGateway()))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });

    when(deviceRepo.findByIdAndEntityId(anyInt(), eq(entity1.getId()))).thenAnswer(i -> entity1Devices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
        .findFirst().orElse(null));
    when(deviceRepo.findBySensors(any(Sensor.class))).thenAnswer(i -> {
      Sensor sensor = i.getArgument(0);
      return allDevices.stream().filter(d -> sensor.getDevice().equals(d))
          .findFirst().orElse(null);
    });
    when(deviceRepo.findByGatewayAndRealDeviceId(any(Gateway.class), anyInt())).thenAnswer(i -> allDevices.stream().filter(d -> d.getGateway().equals(i.getArgument(0))
        && i.getArgument(1).equals(d.getRealDeviceId()))
        .findFirst().orElse(null));
    when(deviceRepo.findByCmdEnabledAndDeviceId(anyBoolean(), anyInt())).thenAnswer(
        i -> allSensors.stream().filter(s -> i.getArgument(1).equals(s.getDevice().getId())
        && i.getArgument(0).equals(s.getCmdEnabled())).collect(Collectors.toList()));
    when(deviceRepo.findAllByDeviceId(anyInt())).thenAnswer(
        i -> allSensors.stream().filter(s -> i.getArgument(0).equals(s.getDevice().getId())));
    when(deviceRepo.findBySensorsCmdEnabledField(anyBoolean())).thenAnswer(i -> {
      List<Device> d = new ArrayList<>();
      for(int j=0; j<allSensors.size(); j++) {
        if(i.getArgument(0).equals(allSensors.get(j).getCmdEnabled())) {
          d.add(allSensors.get(j).getDevice());
        }
      }
      return d;
    });
    when(deviceRepo.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));
    when(entityRepo.findById(anyInt())).thenAnswer(i -> {
      if (i.getArgument(0).equals(entity1.getId())) {
        return Optional.of(entity1);
      } else {
        return Optional.empty();
      }
    });

    when(gatewayRepo.findById(anyInt())).thenAnswer(i -> allGateways.stream().filter(g -> i.getArgument(0).equals(g.getId()))
        .findFirst());

    when(sensorRepo.findById(anyInt())).thenAnswer(i -> allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
        .findFirst());

  }

  @Test
  public void findAllDevices() {
    List<Device> devices = deviceService.findAll();

    assertFalse(devices.isEmpty());
    assertEquals(3, (long) devices.size());
  }

  @Test
  public void findAllDevicesByEntityIdAndGatewayId() {
    List<Device> devices = deviceService.findAllByEntityIdAndGatewayId(entity1.getId(), gateway1.getId());

    assertFalse(devices.isEmpty());
    assertEquals(2, (long) devices.size());
  }

  @Test
  public void findAllDevicesByEntityIdAndNotExistentGatewayId() {
    List<Device> devices = deviceService.findAllByEntityIdAndGatewayId(entity1.getId(), 6);

    assertTrue(devices.isEmpty());
  }

  @Test
  public void findAllDevicesByGatewayId() {
    List<Device> devices = deviceService.findAllByGatewayId(gateway1.getId());

    assertFalse(devices.isEmpty());
    assertEquals(2, (long) devices.size());
  }

  @Test
  public void findAllDevicesByNotExistentGatewayId() {
    List<Device> devices = deviceService.findAllByGatewayId(5);

    assertTrue(devices.isEmpty());
  }

  @Test
  public void findAllDevicesByEntityId() {
    List<Device> devices = deviceService.findAllByEntityId(entity1.getId());

    assertFalse(devices.isEmpty());
    assertEquals(2, (long) devices.size());
  }

  @Test
  public void findAllDevicesByNotExistentEntityId() {
    List<Device> devices = deviceService.findAllByEntityId(5);

    assertTrue(devices.isEmpty());
  }

  @Test
  public void findDeviceById() {
    Device device = deviceService.findById(device1.getId());

    assertNotNull(device);
  }

  @Test
  public void findDeviceBySensorId() {
    Device device = deviceService.findBySensorId(sensor5.getId());

    assertNotNull(device);
    assertEquals(device3 ,device);
  }

  @Test
  public void findDeviceByNotExistentSensorId() {
    Device device = deviceService.findBySensorId(8);

    assertNull(device);
  }

  @Test
  public void findDeviceByIdAndEntityId() {
    Device device = deviceService.findByIdAndEntityId(device1.getId(), entity1.getId());

    assertNotNull(device);
  }

  @Test
  public void findDeviceByGatewayIdAndRealSensorId() {
    Device device = deviceService.findByGatewayIdAndRealDeviceId(gateway1.getId(),
        device1.getRealDeviceId());

    assertNotNull(device);
    assertEquals(device1 ,device);
  }

  @Test
  public void findDeviceByNotExistentGatewayIdAndRealSensorId() {
    Device device = deviceService.findByGatewayIdAndRealDeviceId(8, 1);

    assertNull(device);
  }

  @Test
  public void getEnabled() {
    List<Device> enabledDevices =
        deviceService.getEnabled(true);
    assertTrue(enabledDevices.size() == 2
        && enabledDevices.contains(device1)
        && enabledDevices.contains(device2));
  }

  @Test
  public void getDevicesWithAtLeastOneSensorNotEnabledNotEnabled() {
    List<Device> enabledDevices =
        deviceService.getEnabled(false);
    assertTrue(enabledDevices.size() == 3
        && enabledDevices.contains(device3)
        && enabledDevices.contains(device2)
        && enabledDevices.contains(device1));
  }

  @Test
  public void getEnabledSensorsDevice1() {
    List<Sensor> enabledSensors =
        deviceService.getEnabledSensorsDevice(true, device1.getId());
    assertTrue(enabledSensors.size() == 1
        && enabledSensors.contains(sensor1));
  }

  @Test
  public void getNotEnabledSensorsDevice1() {
    List<Sensor> notEnabledSensors =
        deviceService.getEnabledSensorsDevice(false, device1.getId());
    assertTrue(notEnabledSensors.size() == 1
        && notEnabledSensors.contains(sensor2));
  }

  @Test
  public void addDeviceSuccesful() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("gatewayId", 1);
    newDeviceFields.put("frequency", 1);
    newDeviceFields.put("name", "newDevice");
    newDeviceFields.put("realDeviceId", 4);
    try {
      Device addedDevice = deviceService.addDevice(newDeviceFields);
      assertTrue(addedDevice.getGateway().getId()
          == (int)newDeviceFields.get("gatewayId")
          && addedDevice.getFrequency()
          == (int)newDeviceFields.get("frequency")
          && (String)newDeviceFields.get("name")
          == addedDevice.getName()
          && (int)newDeviceFields.get("realDeviceId")
          == addedDevice.getRealDeviceId());
    } catch(MissingFieldsException | InvalidFieldsValuesException | ConflictException e) {
      fail();
    }
  }

  @Test
  public void addDeviceFrequencyInvalidFieldsValuesException() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("gatewayId", 1);
    newDeviceFields.put("frequency", "1 ");
    newDeviceFields.put("name", "newDevice");
    newDeviceFields.put("realDeviceId", 4);
    try {
      Device addedDevice = deviceService.addDevice(newDeviceFields);
      fail();
    } catch(InvalidFieldsValuesException e) {
      assertTrue(true);
    } catch (ConflictException | MissingFieldsException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void addDeviceGatewayNotExistingInvalidFieldsValuesException() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("gatewayId", 4);
    newDeviceFields.put("frequency", 1);
    newDeviceFields.put("name", "newDevice");
    newDeviceFields.put("realDeviceId", 4);
    try {
      Device addedDevice = deviceService.addDevice(newDeviceFields);
      fail();
    } catch(InvalidFieldsValuesException e) {
      assertTrue(true);
    } catch (ConflictException | MissingFieldsException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void addDifferentDeviceWithALreadyExistingRealDeviceIdAndGatewayIdException() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("gatewayId", 1);
    newDeviceFields.put("frequency", 1);
    newDeviceFields.put("name", "newDevice");
    newDeviceFields.put("realDeviceId", 1);
    try {
      Device addedDevice = deviceService.addDevice(newDeviceFields);
      fail();
    } catch(InvalidFieldsValuesException | MissingFieldsException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (ConflictException ce) {
      assertTrue(true);
    }
  }

  @Test
  public void addDeviceMissingFieldsException() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("gatewayId", 1);
    newDeviceFields.put("frequency", 1);
    newDeviceFields.put("name", "newDevice");
    try {
      Device addedDevice = deviceService.addDevice(newDeviceFields);
      fail();
    } catch(MissingFieldsException e) {
      assertTrue(true);
    } catch (ConflictException | InvalidFieldsValuesException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void editDevice1Succesful() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    newDeviceFields.put("gatewayId", 2);
    newDeviceFields.put("frequency", 2);
    newDeviceFields.put("name", "Device1Edited");
    newDeviceFields.put("realDeviceId", 5);
    try {
      Device editedDevice = deviceService.editDevice(device1.getId(), newDeviceFields);
      assertTrue(editedDevice.getGateway().getId()
          == (int)newDeviceFields.get("gatewayId")
          && editedDevice.getFrequency()
          == (int)newDeviceFields.get("frequency")
          && (String)newDeviceFields.get("name")
          == editedDevice.getName()
          && (int)newDeviceFields.get("realDeviceId")
          == editedDevice.getRealDeviceId());
    } catch(ElementNotFoundException | MissingFieldsException | InvalidFieldsValuesException | ConflictException e) {
      fail();
    }
  }

  @Test
  public void editDevice1MissingFieldsException() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    try {
      Device editedDevice = deviceService.editDevice(device1.getId(), newDeviceFields);
      fail();
    } catch(MissingFieldsException e) {
      assertTrue(true);
    } catch (ConflictException | ElementNotFoundException | InvalidFieldsValuesException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void editNotExistingDeviceElementNotFoundException() {
    Map<String, Object> newDeviceFields = new HashMap<>();
    try {
      Device editedDevice = deviceService.editDevice(10, newDeviceFields);
      fail();
    } catch(ElementNotFoundException e) {
      assertTrue(true);
    } catch (ConflictException | MissingFieldsException | InvalidFieldsValuesException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void deleteDevice1Succesful() //don't like the way I've done it
      throws ElementNotFoundException {
    try {
      when(deviceRepo.existsById(anyInt())).thenReturn(false); //why twice?
      boolean deleted = deviceService.deleteDevice(device1.getId());
     // assertFalse(allDevices.contains(device1)); couldn't do it like this...
      assertTrue(deleted);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void deleteNotExistingDeviceNotSuccesful() //don't like the way I've done it
      throws ElementNotFoundException {
    try {
      int notExistingId=10;
      when(deviceRepo.existsById(anyInt())).thenReturn(false); //why twice?
      boolean deleted = deviceService.deleteDevice(notExistingId);
      // assertFalse(allDevices.contains(device1)); couldn't do it like this...
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

}
