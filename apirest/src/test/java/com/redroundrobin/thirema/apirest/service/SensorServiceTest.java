package com.redroundrobin.thirema.apirest.service;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.service.postgres.AlertService;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.ViewGraphService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private SensorRepository repo;

  @MockBean
  private AlertService alertService;

  @MockBean
  private DeviceService deviceService;

  @MockBean
  private EntityService entityService;

  @MockBean
  private ViewGraphService viewGraphService;


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
    sensorService = new SensorService(repo);
    sensorService.setAlertService(alertService);
    sensorService.setDeviceService(deviceService);
    sensorService.setEntityService(entityService);
    sensorService.setViewGraphService(viewGraphService);


    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity();
    entity1.setId(1);
    entity1.setName("entity1");

    entity2 = new Entity();
    entity2.setId(2);
    entity2.setName("entity2");

    entity3 = new Entity();
    entity3.setId(3);
    entity3.setName("entity3");

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);
    allEntities.add(entity3);


    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor();
    sensor1.setId(1);
    sensor1.setRealSensorId(1);

    sensor2 = new Sensor();
    sensor2.setId(2);
    sensor2.setRealSensorId(2);

    sensor3 = new Sensor();
    sensor3.setId(3);
    sensor3.setRealSensorId(1);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    // ----------------------------------------- Set Devices --------------------------------------
    device1 = new Device();
    device1.setId(1);

    device2 = new Device();
    device2.setId(2);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);


    // ----------------------------------------- Set Alerts --------------------------------------
    alert1 = new Alert();
    alert1.setAlertId(1);

    alert2 = new Alert();
    alert2.setAlertId(2);

    alert3 = new Alert();
    alert3.setAlertId(3);

    List<Alert> allAlerts = new ArrayList<>();
    allAlerts.add(alert1);
    allAlerts.add(alert2);
    allAlerts.add(alert3);


    // --------------------------------------- Set ViewGraphs -------------------------------------
    viewGraph1 = new ViewGraph();
    viewGraph1.setId(1);

    viewGraph2 = new ViewGraph();
    viewGraph2.setId(2);

    List<ViewGraph> allViewGraphs = new ArrayList<>();
    allViewGraphs.add(viewGraph1);
    allViewGraphs.add(viewGraph2);


    // --------------------------------------- Set Gateways -------------------------------------
    gateway1 = new Gateway();
    gateway1.setId(1);

    gateway2 = new Gateway();
    gateway1.setId(2);

    List<Gateway> allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);



    // -------------------------- Set sensors to alerts and viceversa --------------------------
    List<Alert> sensor1Alerts = new ArrayList<>();
    sensor1Alerts.add(alert1);
    sensor1Alerts.add(alert2);
    sensor1.setAlerts(sensor1Alerts);
    alert1.setSensor(sensor1);
    alert2.setSensor(sensor1);

    List<Alert> sensor2Alerts = new ArrayList<>();
    sensor2Alerts.add(alert3);
    sensor2.setAlerts(sensor2Alerts);
    alert3.setSensor(sensor2);

    sensor3.setAlerts(Collections.emptyList());


    // -------------------------- Set sensors to entities and viceversa --------------------------
    List<Entity> sensor1And2Entities = new ArrayList<>();
    sensor1And2Entities.add(entity1);
    sensor1And2Entities.add(entity2);
    sensor1.setEntities(sensor1And2Entities);
    sensor2.setEntities(sensor1And2Entities);

    List<Entity> sensor3Entities = new ArrayList<>();
    sensor3Entities.add(entity3);
    sensor3.setEntities(sensor3Entities);


    // -------------------------- Set sensors to devices and viceversa --------------------------
    device1.setSensors(allSensors.stream().filter(s -> s.getId() <= 2)
        .collect(Collectors.toList()));
    sensor1.setDevice(device1);
    sensor2.setDevice(device1);

    device2.setSensors(allSensors.stream().filter(s -> s.getId() > 2)
        .collect(Collectors.toList()));
    sensor3.setDevice(device2);


    // -------------------------- Set sensors to ViewGraphs and viceversa --------------------------
    List<ViewGraph> sensor1To1ViewGraphs1 = new ArrayList<>();
    sensor1To1ViewGraphs1.add(viewGraph1);
    viewGraph1.setSensor1(sensor1);

    List<ViewGraph> sensor1To2ViewGraphs2 = new ArrayList<>();
    sensor1To2ViewGraphs2.add(viewGraph2);
    viewGraph2.setSensor2(sensor1);
    sensor1.setViewGraphs1(sensor1To1ViewGraphs1);
    sensor1.setViewGraphs2(sensor1To2ViewGraphs2);

    List<ViewGraph> sensor2To2ViewGraphs1 = new ArrayList<>();
    sensor2To2ViewGraphs1.add(viewGraph1);
    viewGraph1.setSensor2(sensor2);
    sensor2.setViewGraphs1(Collections.emptyList());
    sensor2.setViewGraphs2(sensor2To2ViewGraphs1);

    List<ViewGraph> sensor3To1ViewGraphs2 = new ArrayList<>();
    sensor3To1ViewGraphs2.add(viewGraph2);
    viewGraph2.setSensor1(sensor3);
    sensor3.setViewGraphs1(sensor3To1ViewGraphs2);
    sensor3.setViewGraphs2(Collections.emptyList());


    // -------------------------- Set Devices to Gateways and viceversa --------------------------
    device1.setGateway(gateway1);
    List<Device> gateway1Devices = new ArrayList<>();
    gateway1Devices.add(device1);
    gateway1.setDevices(gateway1Devices);

    device2.setGateway(gateway2);
    List<Device> gateway2Devices = new ArrayList<>();
    gateway2Devices.add(device2);
    gateway2.setDevices(gateway2Devices);


    when(repo.findAll()).thenReturn(allSensors);
    when(repo.findAllByDevice(any(Device.class))).thenAnswer(i -> {
      Device device = allDevices.stream().filter(d -> i.getArgument(0).equals(d))
          .findFirst().orElse(null);
      return allSensors.stream().filter(s -> s.getDevice().equals(device))
          .collect(Collectors.toList());
    });
    when(repo.findAllByEntities(any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(0);
      return allSensors.stream().filter(s -> s.getEntities().contains(entity))
          .collect(Collectors.toList());
    });
    when(repo.findAllByDeviceAndEntities(any(Device.class), any(Entity.class))).thenAnswer(i -> {
      Device device = i.getArgument(0);
      Entity entity = i.getArgument(1);
      return allSensors.stream().filter(s -> s.getEntities().contains(entity) && device.equals(s.getDevice()))
          .collect(Collectors.toList());
    });
    when(repo.findAllByViewGraphs1OrViewGraphs2(any(ViewGraph.class),any(ViewGraph.class))).thenAnswer(i -> {
      return allSensors.stream().filter(s -> s.getViewGraphs1().contains(
          i.getArgument(0)) || s.getViewGraphs2().contains(i.getArgument(1)))
          .collect(Collectors.toList());
    });
    when(repo.findAllByGatewayIdAndRealDeviceId(anyInt(),anyInt())).thenAnswer(i -> {
      return allSensors.stream()
          .filter(s -> i.getArgument(0).equals(s.getDevice().getGateway().getId())
              && i.getArgument(1).equals(s.getDevice().getRealDeviceId()))
          .collect(Collectors.toList());
    });
    when(repo.findById(anyInt())).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst();
    });
    when(repo.findByIdAndEntities(anyInt(), any(Entity.class))).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()) && s.getEntities().contains(i.getArgument(1)))
          .findFirst().orElse(null);
    });
    when(repo.findByAlerts(any(Alert.class))).thenAnswer(i -> {
      Alert alert = i.getArgument(0);
      return allSensors.stream().filter(s -> s.getAlerts().contains(alert))
          .findFirst().orElse(null);
    });
    when(repo.findByDeviceAndRealSensorId(any(Device.class), anyInt())).thenAnswer(i -> {
      Device device = i.getArgument(0);
      int realSensorId = i.getArgument(1);
      return allSensors.stream()
          .filter(s -> device.equals(s.getDevice()) && realSensorId == s.getRealSensorId())
          .findFirst().orElse(null);
    });
    when(repo.findByDeviceAndRealSensorIdAndEntities(any(Device.class), anyInt(), any(Entity.class))).thenAnswer(i -> {
      Device device = i.getArgument(0);
      int realSensorId = i.getArgument(1);
      Entity entity = i.getArgument(2);
      return allSensors.stream()
          .filter(s -> device.equals(s.getDevice()) && realSensorId == s.getRealSensorId()
              && s.getEntities().contains(entity))
          .findFirst().orElse(null);
    });
    when(repo.findByGatewayIdAndRealDeviceIdAndRealSensorId(anyInt(), anyInt(), anyInt())).thenAnswer(i -> {
      return allSensors.stream()
          .filter(s -> i.getArgument(0).equals(s.getDevice().getGateway().getId())
              && i.getArgument(1).equals(s.getDevice().getRealDeviceId())
              && i.getArgument(2).equals(s.getRealSensorId()))
          .findFirst().orElse(null);
    });

    when(alertService.findById(anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getAlertId()))
          .findFirst().orElse(null);
    });

    when(deviceService.findById(anyInt())).thenAnswer(i -> {
      return allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
          .findFirst().orElse(null);
    });

    when(entityService.findById(anyInt())).thenAnswer(i -> {
      return allEntities.stream().filter(e -> i.getArgument(0).equals(e.getId()))
          .findFirst().orElse(null);
    });

    when(viewGraphService.findById(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getId()))
          .findFirst().orElse(null);
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
