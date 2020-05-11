package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GatewayServiceTest {

  private GatewayService gatewayService;

  @MockBean
  private GatewayRepository gatewayRepo;

  @MockBean
  private DeviceRepository deviceRepo;

  @MockBean
  private KafkaTemplate<String, String> kafkaTemplate;

  private Device device1;
  private Device device2;
  private Device device3;

  private Entity entity1;
  private Entity entity2;

  private Gateway gateway1;
  private Gateway gateway2;

  @Before
  public void setUp() {
    gatewayService = new GatewayService(gatewayRepo, deviceRepo, kafkaTemplate);

    // -------------------------------------- Set Devices ----------------------------------------
    device1 = new Device(1, "name1", 1, 1);
    device2 = new Device(2, "name2", 2, 2);
    device3 = new Device(3, "name3", 3, 3);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);

    Sensor sensor1 = new Sensor(1, "type1", 1);
    Sensor sensor2 = new Sensor(2, "type2", 2);
    Sensor sensor3 = new Sensor(3, "type3", 3);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);

    sensor1.setDevice(device1);
    sensor2.setDevice(device1);

    sensor3.setDevice(device2);

    entity1 = new Entity();
    entity2 = new Entity();

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);

    Set<Sensor> entity1Sensors = new HashSet<>();
    entity1Sensors.add(sensor1);
    entity1Sensors.add(sensor2);


    Set<Sensor> entity2Sensors = new HashSet<>();
    entity2Sensors.add(sensor3);


    //-------------------------------------- Set sensors to entities -----------------------------
    entity1.setSensors(entity1Sensors);
    entity2.setSensors(entity2Sensors);

    // -------------------------------------- Set Gateways ----------------------------------------
    gateway1 = new Gateway(1, "name1");
    gateway2 = new Gateway(2, "name2");

    List<Gateway> allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);

    // -------------------------------- Set Gateways to Devices --------------------------------
    device1.setGateway(gateway1);
    device2.setGateway(gateway1);

    device3.setGateway(gateway2);

    when(gatewayRepo.findAll()).thenReturn(allGateways);
    when(gatewayRepo.findByDevice(anyInt())).thenAnswer(i -> {
      Device device = deviceRepo.findById(i.getArgument(0)).orElse(null);
      if (device != null) {
        return allGateways.stream().filter(g -> device.getGateway().equals(g))
            .findFirst().orElse(null);
      } else {
        return null;
      }
    });
    when(gatewayRepo.findById(anyInt())).thenAnswer(i -> allGateways.stream().filter(g -> i.getArgument(0).equals(g.getId()))
        .findFirst());
    when(gatewayRepo.findAllByEntityId(anyInt())).thenAnswer(i -> {
      Entity entity;
      if((entity = allEntities.stream().filter(
          e -> (i.getArgument(0)).equals(e.getId())).findFirst().orElse(null)) == null) {
        return null;
      }

      List<Gateway> gateways = new ArrayList<>();
      for(Sensor s : entity.getSensors()) {
        if(gateways.contains(s.getDevice().getGateway())) {
          gateways.add(s.getDevice().getGateway());
        }
      }
      return gateways;
    });
    when(gatewayRepo.findByIdAndEntityId(anyInt(), anyInt())).thenAnswer(i -> {
      Entity entity;Gateway gateway;
      if((entity = allEntities.stream().filter(
          e -> (i.getArgument(1)).equals(e.getId())).findFirst().orElse(null)) == null
          || (gateway = allGateways.stream().filter(
          g -> (i.getArgument(0)).equals(g.getId())).findFirst().orElse(null)) == null
          || allSensors.stream().filter(
              s -> (entity.getSensors().contains(s)
                  && s.getDevice().getGateway() == gateway)).
                  collect(Collectors.toList()).isEmpty()) {
        return null;
      }
      return gateway;
    });

    when(deviceRepo.findById(anyInt())).thenAnswer(i -> allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
        .findFirst());

  }

  @Test
  public void findAllGateways() {
    List<Gateway> gateways = gatewayService.findAll();

    assertFalse(gateways.isEmpty());
    assertEquals(2, (long) gateways.size());
  }

  @Test
  public void findGatewayById() {
    Gateway gateway = gatewayService.findById(gateway1.getId());

    assertNotNull(gateway);
  }

  @Test
  public void findGatewayByDeviceId() {
    Gateway gateway = gatewayService.findByDeviceId(device2.getId());

    assertNotNull(gateway);
    assertEquals(gateway1, gateway);
  }

  @Test
  public void findGatewayByNotExistentDeviceId() {
    Gateway gateway = gatewayService.findByDeviceId(8);

    assertNull(gateway);
  }

  @Test
  public void findByIdAndEntityId() {
    Gateway gateway =  gatewayService.findByIdAndEntityId(1, entity1.getId());

    assertEquals(gateway1, gateway);
  }
}
