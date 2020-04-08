package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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


  @Before
  public void setUp() {
    deviceService = new DeviceService(deviceRepo, entityRepo, gatewayRepo, sensorRepo);

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

    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity();
    entity1.setId(1);


    // ----------------------------------------- Set Gateways --------------------------------------
    gateway1 = new Gateway();
    gateway1.setId(1);
    gateway2 = new Gateway();
    gateway2.setId(2);

    List<Gateway> allGateways = new ArrayList<>();
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
    sensor5 = new Sensor();
    sensor5.setId(5);

    List<Sensor> allSensors = new ArrayList<>();
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
    when(deviceRepo.findById(anyInt())).thenAnswer(i -> {
      return allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
          .findFirst();
    });
    when(deviceRepo.findByIdAndEntityId(anyInt(), eq(entity1.getId()))).thenAnswer(i -> {
      return entity1Devices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
          .findFirst().orElse(null);
    });
    when(deviceRepo.findBySensors(any(Sensor.class))).thenAnswer(i -> {
      Sensor sensor = i.getArgument(0);
      return allDevices.stream().filter(d -> sensor.getDevice().equals(d))
          .findFirst().orElse(null);
    });
    when(deviceRepo.findByGatewayAndRealDeviceId(any(Gateway.class), anyInt())).thenAnswer(i -> {
      return allDevices.stream().filter(d -> d.getGateway().equals(i.getArgument(0))
          && i.getArgument(1).equals(d.getRealDeviceId()))
          .findFirst().orElse(null);
    });

    when(entityRepo.findById(anyInt())).thenAnswer(i -> {
      if (i.getArgument(0).equals(entity1.getId())) {
        return Optional.of(entity1);
      } else {
        return Optional.empty();
      }
    });

    when(gatewayRepo.findById(anyInt())).thenAnswer(i -> {
      return allGateways.stream().filter(g -> i.getArgument(0).equals(g.getId()))
          .findFirst();
    });

    when(sensorRepo.findById(anyInt())).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst();
    });

  }

  @Test
  public void findAllDevices() {
    List<Device> devices = deviceService.findAll();

    assertTrue(!devices.isEmpty());
    assertTrue(devices.stream().count() == 3);
  }



  @Test
  public void findAllDevicesByEntityIdAndGatewayId() {
    List<Device> devices = deviceService.findAllByEntityIdAndGatewayId(entity1.getId(), gateway1.getId());

    assertTrue(!devices.isEmpty());
    assertTrue(devices.stream().count() == 2);
  }

  @Test
  public void findAllDevicesByEntityIdAndNotExistentGatewayId() {
    List<Device> devices = deviceService.findAllByEntityIdAndGatewayId(entity1.getId(), 6);

    assertTrue(devices.isEmpty());
  }



  @Test
  public void findAllDevicesByGatewayId() {
    List<Device> devices = deviceService.findAllByGatewayId(gateway1.getId());

    assertTrue(!devices.isEmpty());
    assertTrue(devices.stream().count() == 2);
  }

  @Test
  public void findAllDevicesByNotExistentGatewayId() {
    List<Device> devices = deviceService.findAllByGatewayId(5);

    assertTrue(devices.isEmpty());
  }

  @Test
  public void findAllDevicesByEntityId() {
    List<Device> devices = deviceService.findAllByEntityId(entity1.getId());

    assertTrue(!devices.isEmpty());
    assertTrue(devices.stream().count() == 2);
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
}
