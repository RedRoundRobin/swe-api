package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SensorServiceTest {

  @MockBean
  private SensorRepository sensorRepo;

  @MockBean
  private AlertRepository alertRepo;

  @MockBean
  private DeviceRepository deviceRepo;

  @MockBean
  private EntityRepository entityRepo;

  @MockBean
  private ViewGraphRepository viewGraphRepo;


  private SensorService sensorService;


  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;

  private Device device1;
  private Device device2;

  private Alert alert1;
  private Alert alert2;
  private Alert alert3;

  private ViewGraph viewGraph1;
  private ViewGraph viewGraph2;

  private Gateway gateway1;
  private Gateway gateway2;


  @Before
  public void setUp() {
    sensorService = new SensorService(sensorRepo, alertRepo, deviceRepo, entityRepo, viewGraphRepo);


    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity(1, "entity1", "location1");
    entity2 = new Entity(2, "entity2", "location2");
    entity3 = new Entity(3, "entity3", "location3");

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);
    allEntities.add(entity3);


    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 3);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    // ----------------------------------------- Set Devices --------------------------------------
    device1 = new Device(1, "name1", 1, 1);
    device2 = new Device(2, "name2", 2, 2);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);


    // ----------------------------------------- Set Alerts --------------------------------------
    alert1 = new Alert(1, 10.0, Alert.Type.GREATER, entity1, sensor1);
    alert2 = new Alert(2, 10.0, Alert.Type.GREATER, entity1, sensor1);
    alert3 = new Alert(3, 10.0, Alert.Type.GREATER, entity2, sensor2);

    List<Alert> allAlerts = new ArrayList<>();
    allAlerts.add(alert1);
    allAlerts.add(alert2);
    allAlerts.add(alert3);


    // --------------------------------------- Set ViewGraphs -------------------------------------
    viewGraph1 = new ViewGraph(1, ViewGraph.Correlation.NULL);
    viewGraph2 = new ViewGraph(2, ViewGraph.Correlation.NULL);

    List<ViewGraph> allViewGraphs = new ArrayList<>();
    allViewGraphs.add(viewGraph1);
    allViewGraphs.add(viewGraph2);


    // --------------------------------------- Set Gateways -------------------------------------
    gateway1 = new Gateway(1, "gw1");
    gateway2 = new Gateway(2, "gw2");

    List<Gateway> allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);



    // -------------------------------- Set sensors to entities ---------------------------------
    Set<Sensor> entity1Sensors = new HashSet<>();
    entity1Sensors.add(sensor1);
    entity1Sensors.add(sensor2);
    entity1.setSensors(entity1Sensors);

    Set<Sensor> entity2Sensors = new HashSet<>();
    entity2Sensors.add(sensor1);
    entity2Sensors.add(sensor2);
    entity2.setSensors(entity2Sensors);

    Set<Sensor> entity3Sensors = new HashSet<>();
    entity3Sensors.add(sensor3);


    // ---------------------------------- Set devices to sensors --------------------------------
    sensor1.setDevice(device1);

    sensor2.setDevice(device1);

    sensor3.setDevice(device2);


    // ---------------------------------- Set sensors to ViewGraphs -----------------------------
    viewGraph1.setSensor1(sensor1);
    viewGraph1.setSensor2(sensor2);

    viewGraph2.setSensor1(sensor3);
    viewGraph2.setSensor2(sensor1);


    // ---------------------------------- Set Gateways to Devices -------------------------------
    device1.setGateway(gateway1);

    device2.setGateway(gateway2);



    when(sensorRepo.findAll()).thenReturn(allSensors);
    when(sensorRepo.findAllByDevice(any(Device.class))).thenAnswer(i -> {
      Device device = allDevices.stream().filter(d -> i.getArgument(0).equals(d))
          .findFirst().orElse(null);
      return allSensors.stream().filter(s -> s.getDevice().equals(device))
          .collect(Collectors.toList());
    });
    when(sensorRepo.findAllByEntities(any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(0);
      return allSensors.stream().filter(s -> entity.getSensors().contains(s))
          .collect(Collectors.toList());
    });
    when(sensorRepo.findAllByDeviceAndEntities(any(Device.class), any(Entity.class))).thenAnswer(i -> {
      Device device = i.getArgument(0);
      Entity entity = i.getArgument(1);
      return allSensors.stream().filter(s -> entity.getSensors().contains(s) && device.equals(s.getDevice()))
          .collect(Collectors.toList());
    });
    when(sensorRepo.findAllByViewGraphs1OrViewGraphs2(any(ViewGraph.class),any(ViewGraph.class))).thenAnswer(i -> {
      ViewGraph viewGraph1 = i.getArgument(0);
      ViewGraph viewGraph2 = i.getArgument(1);
      return allSensors.stream()
          .filter(s -> viewGraph1.getSensor1().equals(s) || viewGraph1.getSensor2().equals(s))
          .collect(Collectors.toList());
    });
    when(sensorRepo.findAllByGatewayIdAndRealDeviceId(anyInt(),anyInt())).thenAnswer(i -> {
      return allSensors.stream()
          .filter(s -> i.getArgument(0).equals(s.getDevice().getGateway().getId())
              && i.getArgument(1).equals(s.getDevice().getRealDeviceId()))
          .collect(Collectors.toList());
    });
    when(sensorRepo.findById(anyInt())).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst();
    });
    when(sensorRepo.findBySensorIdAndEntities(anyInt(), any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(1);
      return allSensors.stream()
          .filter(s -> i.getArgument(0).equals(s.getId()) && entity.getSensors().contains(s))
          .findFirst().orElse(null);
    });
    when(sensorRepo.findByAlerts(any(Alert.class))).thenAnswer(i -> {
      Alert alert = i.getArgument(0);
      return allSensors.stream().filter(s -> alert.getSensor().equals(s))
          .findFirst().orElse(null);
    });
    when(sensorRepo.findByDeviceAndRealSensorId(any(Device.class), anyInt())).thenAnswer(i -> {
      Device device = i.getArgument(0);
      int realSensorId = i.getArgument(1);
      return allSensors.stream()
          .filter(s -> device.equals(s.getDevice()) && realSensorId == s.getRealSensorId())
          .findFirst().orElse(null);
    });
    when(sensorRepo.findByDeviceAndRealSensorIdAndEntities(any(Device.class), anyInt(), any(Entity.class))).thenAnswer(i -> {
      Device device = i.getArgument(0);
      int realSensorId = i.getArgument(1);
      Entity entity = i.getArgument(2);
      return allSensors.stream()
          .filter(s -> device.equals(s.getDevice()) && realSensorId == s.getRealSensorId()
              && entity.getSensors().contains(s))
          .findFirst().orElse(null);
    });
    when(sensorRepo.findByGatewayIdAndRealDeviceIdAndRealSensorId(anyInt(), anyInt(), anyInt())).thenAnswer(i -> {
      return allSensors.stream()
          .filter(s -> i.getArgument(0).equals(s.getDevice().getGateway().getId())
              && i.getArgument(1).equals(s.getDevice().getRealDeviceId())
              && i.getArgument(2).equals(s.getRealSensorId()))
          .findFirst().orElse(null);
    });

    when(alertRepo.findById(anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getAlertId()))
          .findFirst();
    });

    when(deviceRepo.findById(anyInt())).thenAnswer(i -> {
      return allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
          .findFirst();
    });

    when(entityRepo.findById(anyInt())).thenAnswer(i -> {
      return allEntities.stream().filter(e -> i.getArgument(0).equals(e.getId()))
          .findFirst();
    });

    when(viewGraphRepo.findById(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getId()))
          .findFirst();
    });
  }

  @Test
  public void findAllSensors() {
    List<Sensor> sensors = sensorService.findAll();

    assertTrue(!sensors.isEmpty());
  }



  @Test
  public void findAllSensorsByDeviceId() {
    List<Sensor> sensors = sensorService.findAllByDeviceId(device1.getId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByDeviceEmptyResult() {
    List<Sensor> sensors = sensorService.findAllByDeviceId(4);

    assertTrue(sensors.stream().count() == 0);
  }



  @Test
  public void findAllSensorsByDeviceIdAndEntityId() {
    List<Sensor> sensors = sensorService.findAllByDeviceIdAndEntityId(device1.getId(), entity1.getId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByDeviceIdAndNotExistentEntityId() {
    List<Sensor> sensors = sensorService.findAllByDeviceIdAndEntityId(device1.getId(), 5);

    assertTrue(sensors.isEmpty());
  }



  @Test
  public void findAllSensorsByEntityId() {
    List<Sensor> sensors = sensorService.findAllByEntityId(entity1.getId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByEntitesEmptyResult() {
    List<Sensor> sensors = sensorService.findAllByEntityId(4);

    assertTrue(sensors.stream().count() == 0);
  }



  @Test
  public void findAllSensorsByViewGraphId() {
    List<Sensor> sensors = sensorService.findAllByViewGraphId(viewGraph1.getId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByViewGraphEmptyResult() {
    List<Sensor> sensors = sensorService.findAllByViewGraphId(4);

    assertTrue(sensors.stream().count() == 0);
  }



  @Test
  public void findAllSensorsByGatewayIdAndRealDeviceId() {
    List<Sensor> sensors = sensorService.findAllByGatewayIdAndRealDeviceId(gateway1.getId(),
        device1.getRealDeviceId());

    System.out.println(sensors);
    assertTrue(sensors.stream().count() == 2);
  }



  @Test
  public void findSensorById() {
    Sensor sensor = sensorService.findById(sensor1.getId());

    assertNotNull(sensor);
  }



  @Test
  public void findSensorByIdAndEntityId() {
    Sensor sensor = sensorService.findByIdAndEntityId(sensor1.getId(),entity1.getId());

    assertNotNull(sensor);
  }

  @Test
  public void findSensorByIdAndNotExistentEntityId() {
    Sensor sensor = sensorService.findByIdAndEntityId(sensor1.getId(),9);

    assertNull(sensor);
  }



  @Test
  public void findSensorByAlertId() {
    Sensor sensor = sensorService.findByAlertId(alert1.getAlertId());

    assertNotNull(sensor);
  }

  @Test
  public void findSensorByNotExistentAlertId() {
    Sensor sensor = sensorService.findByAlertId(4);

    assertNull(sensor);
  }



  @Test
  public void findSensorByDeviceIdAndRealSensorId() {
    Sensor sensor = sensorService.findByDeviceIdAndRealSensorId(device1.getId(), sensor1.getRealSensorId());

    assertEquals(sensor1,sensor);
  }

  @Test
  public void findSensorByNotExistentDeviceIdAndRealSensorId() {
    Sensor sensor = sensorService.findByDeviceIdAndRealSensorId(4, 1);

    assertNull(sensor);
  }



  @Test
  public void findSensorByDeviceIdAndRealSensorIdAndEntityId() {
    Sensor sensor = sensorService.findByDeviceIdAndRealSensorIdAndEntityId(device1.getId(), sensor1.getRealSensorId(),entity1.getId());

    assertEquals(sensor1,sensor);
  }

  @Test
  public void findSensorByNotExistentDeviceIdAndRealSensorIdAndNotExistentEntityId() {
    Sensor sensor = sensorService.findByDeviceIdAndRealSensorIdAndEntityId(device1.getId(), sensor1.getRealSensorId(),5);

    assertNull(sensor);
  }



  @Test
  public void findSensorByGatewayIdAndRealDeviceIdAndRealSensorId() {
    Sensor sensor = sensorService.findByGatewayIdAndRealDeviceIdAndRealSensorId(gateway1.getId(),
        device1.getRealDeviceId(), sensor1.getRealSensorId());

    assertEquals(sensor1,sensor);
  }
}
